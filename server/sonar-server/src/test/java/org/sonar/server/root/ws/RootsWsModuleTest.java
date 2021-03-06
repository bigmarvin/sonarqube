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
package org.sonar.server.root.ws;

import org.junit.Test;
import org.sonar.core.platform.ComponentContainer;

import static org.assertj.core.api.Assertions.assertThat;

public class RootsWsModuleTest {
  private static final int ADAPTERS_IN_EMPTY_CONTAINER = 2;

  private RootWsModule underTest = new RootWsModule();

  @Test
  public void verify_number_of_components_added_by_module() {
    ComponentContainer container = new ComponentContainer();

    underTest.configure(container);

    assertThat(container.getPicoContainer().getComponentAdapters()).hasSize(ADAPTERS_IN_EMPTY_CONTAINER + 4);
  }
}
