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
import React from 'react';
import { DonutChart } from '../charts/donut-chart';

export default class CoverageRating extends React.Component {
  static propTypes = {
    value: React.PropTypes.oneOfType([React.PropTypes.number, React.PropTypes.string]),
    small: React.PropTypes.bool,
    muted: React.PropTypes.bool
  };

  static defaultProps = {
    small: false,
    muted: false
  };

  render () {
    let data = [{ value: 100, fill: '#ccc ' }];

    if (this.props.value != null) {
      const value = Number(this.props.value);
      data = [
        { value, fill: this.props.muted ? '#bdbdbd' : '#85bb43' },
        { value: 100 - value, fill: this.props.muted ? '#f3f3f3' : '#d4333f' }
      ];
    }

    const size = this.props.small ? 16 : 24;
    const thickness = this.props.small ? 2 : 3;

    return (
        <DonutChart
            data={data}
            width={size}
            height={size}
            thickness={thickness}/>
    );
  }
}
