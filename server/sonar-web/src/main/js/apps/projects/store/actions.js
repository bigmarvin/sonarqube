/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import groupBy from 'lodash/groupBy';
import { searchProjects } from '../../../api/components';
import { addGlobalErrorMessage } from '../../../components/store/globalMessages';
import { parseError } from '../../code/utils';
import { receiveComponents } from '../../../app/store/components/actions';
import { receiveProjects, receiveMoreProjects } from './projectsDuck';
import { updateState } from './stateDuck';
import { getProjectsAppState } from '../../../app/store/rootReducer';
import { getMeasuresForProjects } from '../../../api/measures';
import { receiveComponentsMeasures } from '../../../app/store/measures/actions';
import { convertToFilter } from './utils';
import { receiveFavorites } from '../../../app/store/favorites/duck';

const PAGE_SIZE = 50;

const METRICS = [
  'alert_status',
  'reliability_rating',
  'security_rating',
  'sqale_rating',
  'duplicated_lines_density',
  'coverage',
  'ncloc',
  'ncloc_language_distribution'
];

const FACETS = [
  'reliability_rating',
  'security_rating',
  'sqale_rating',
  'coverage',
  'duplicated_lines_density',
  'ncloc',
  'alert_status'
];

const onFail = dispatch => error => {
  parseError(error).then(message => dispatch(addGlobalErrorMessage(message)));
  dispatch(updateState({ loading: false }));
};

const onReceiveMeasures = (dispatch, expectedProjectKeys) => response => {
  const byComponentKey = groupBy(response.measures, 'component');

  const toStore = {};

  // fill store with empty objects for expected projects
  // this is required to not have "null"s for provisioned projects
  expectedProjectKeys.forEach(projectKey => toStore[projectKey] = {});

  Object.keys(byComponentKey).forEach(componentKey => {
    const measures = {};
    byComponentKey[componentKey].forEach(measure => {
      measures[measure.metric] = measure.value;
    });
    toStore[componentKey] = measures;
  });

  dispatch(receiveComponentsMeasures(toStore));
};

const fetchProjectMeasures = projects => dispatch => {
  if (!projects.length) {
    return Promise.resolve();
  }

  const projectKeys = projects.map(project => project.key);
  return getMeasuresForProjects(projectKeys, METRICS).then(onReceiveMeasures(dispatch, projectKeys), onFail(dispatch));
};

const handleFavorites = (dispatch, projects) => {
  const favorites = projects.filter(project => project.isFavorite);
  if (favorites.length) {
    dispatch(receiveFavorites(favorites));
  }
};

const onReceiveProjects = dispatch => response => {
  dispatch(receiveComponents(response.components));
  dispatch(receiveProjects(response.components, response.facets));
  handleFavorites(dispatch, response.components);
  dispatch(fetchProjectMeasures(response.components)).then(() => {
    dispatch(updateState({ loading: false }));
  });
  dispatch(updateState({
    total: response.paging.total,
    pageIndex: response.paging.pageIndex
  }));
};

const onReceiveMoreProjects = dispatch => response => {
  dispatch(receiveComponents(response.components));
  dispatch(receiveMoreProjects(response.components));
  handleFavorites(dispatch, response.components);
  dispatch(fetchProjectMeasures(response.components)).then(() => {
    dispatch(updateState({ loading: false }));
  });
  dispatch(updateState({ pageIndex: response.paging.pageIndex }));
};

export const fetchProjects = (query, isFavorite) => dispatch => {
  dispatch(updateState({ loading: true }));
  const data = { ps: PAGE_SIZE, facets: FACETS.join() };
  const filter = convertToFilter(query, isFavorite);
  if (filter) {
    data.filter = filter;
  }
  return searchProjects(data).then(onReceiveProjects(dispatch), onFail(dispatch));
};

export const fetchMoreProjects = (query, isFavorite) => (dispatch, getState) => {
  dispatch(updateState({ loading: true }));
  const state = getState();
  const { pageIndex } = getProjectsAppState(state);
  const data = { ps: PAGE_SIZE, p: pageIndex + 1 };
  const filter = convertToFilter(query, isFavorite);
  if (filter) {
    data.filter = filter;
  }
  return searchProjects(data).then(onReceiveMoreProjects(dispatch), onFail(dispatch));
};
