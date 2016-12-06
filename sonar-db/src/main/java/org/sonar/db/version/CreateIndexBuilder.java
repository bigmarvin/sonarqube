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
package org.sonar.db.version;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.sonar.db.version.Validations.validateIndexName;
import static org.sonar.db.version.Validations.validateTableName;

public class CreateIndexBuilder {

  private String tableName;
  private String indexName;
  private List<String> columns;
  private boolean unique = false;

  public CreateIndexBuilder setTable(String s) {
    this.tableName = s;
    return this;
  }

  public CreateIndexBuilder setName(String s) {
    this.indexName = s;
    return this;
  }

  public CreateIndexBuilder setUnique(boolean b) {
    this.unique = b;
    return this;
  }

  public CreateIndexBuilder setColumns(String firstColumn, String... additionalColumns) {
    columns = Lists.asList(requireNonNull(firstColumn), additionalColumns);
    return this;
  }

  public List<String> build() {
    validateTableName(tableName);
    validateIndexName(indexName);
    checkArgument(columns != null && !columns.isEmpty(), "at least one column must be specified");
    columns.forEach(Validations::validateColumnName);
    return singletonList(createSqlStatement());
  }

  private String createSqlStatement() {
    StringBuilder sql = new StringBuilder("CREATE ");
    if (unique) {
      sql.append("UNIQUE ");
    }
    sql.append("INDEX ");
    sql.append(indexName);
    sql.append(" ON ");
    sql.append(tableName);
    sql.append(" (");
    sql.append(columns.stream().collect(Collectors.joining(", ")));
    sql.append(")");
    return sql.toString();
  }

}
