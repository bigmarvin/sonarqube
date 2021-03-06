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
package org.sonar.db.event;

import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.utils.System2;
import org.sonar.db.DbTester;

import static org.assertj.core.api.Assertions.assertThat;

public class EventDaoTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public DbTester dbTester = DbTester.create(System2.INSTANCE);

  EventDao underTest = dbTester.getDbClient().eventDao();

  @Test
  public void select_by_component_uuid() {
    dbTester.prepareDbUnit(getClass(), "shared.xml");

    List<EventDto> dtos = underTest.selectByComponentUuid(dbTester.getSession(), "ABCD");
    assertThat(dtos).hasSize(3);

    dtos = underTest.selectByComponentUuid(dbTester.getSession(), "BCDE");
    assertThat(dtos).hasSize(1);

    EventDto dto = dtos.get(0);
    assertThat(dto.getId()).isEqualTo(4L);
    assertThat(dto.getUuid()).isEqualTo("E4");
    assertThat(dto.getAnalysisUuid()).isEqualTo("uuid_1");
    assertThat(dto.getComponentUuid()).isEqualTo("BCDE");
    assertThat(dto.getName()).isEqualTo("1.0");
    assertThat(dto.getCategory()).isEqualTo("Version");
    assertThat(dto.getDescription()).isEqualTo("Version 1.0");
    assertThat(dto.getData()).isEqualTo("some data");
    assertThat(dto.getDate()).isEqualTo(1413407091086L);
    assertThat(dto.getCreatedAt()).isEqualTo(1225630680000L);
  }

  @Test
  public void return_different_categories() {
    dbTester.prepareDbUnit(getClass(), "shared.xml");

    List<EventDto> dtos = underTest.selectByComponentUuid(dbTester.getSession(), "ABCD");
    assertThat(dtos).extracting("category").containsOnly(EventDto.CATEGORY_ALERT, EventDto.CATEGORY_PROFILE, EventDto.CATEGORY_VERSION);
  }

  @Test
  public void insert() {
    dbTester.prepareDbUnit(getClass(), "empty.xml");

    underTest.insert(dbTester.getSession(), new EventDto()
      .setUuid("E1")
      .setAnalysisUuid("uuid_1")
      .setComponentUuid("ABCD")
      .setName("1.0")
      .setCategory(EventDto.CATEGORY_VERSION)
      .setDescription("Version 1.0")
      .setData("some data")
      .setDate(1413407091086L)
      .setCreatedAt(1225630680000L));
    dbTester.getSession().commit();

    dbTester.assertDbUnit(getClass(), "insert-result.xml", new String[] {"id"}, "events");
  }

  @Test
  public void delete() {
    dbTester.prepareDbUnit(getClass(), "delete.xml");

    underTest.delete(dbTester.getSession(), 1L);
    dbTester.getSession().commit();

    assertThat(dbTester.countRowsOfTable("events")).isEqualTo(0);
  }

}
