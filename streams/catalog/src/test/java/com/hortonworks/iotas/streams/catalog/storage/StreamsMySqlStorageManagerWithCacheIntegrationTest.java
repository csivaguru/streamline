/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hortonworks.iotas.streams.catalog.storage;

import com.hortonworks.iotas.common.test.IntegrationTest;
import com.hortonworks.iotas.storage.impl.jdbc.JdbcStorageManager;
import com.hortonworks.iotas.storage.impl.jdbc.mysql.MySqlStorageManagerWithCacheIntegrationTest;
import com.hortonworks.iotas.storage.impl.jdbc.provider.sql.factory.QueryExecutor;
import com.hortonworks.iotas.streams.catalog.service.StreamCatalogService;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class StreamsMySqlStorageManagerWithCacheIntegrationTest extends MySqlStorageManagerWithCacheIntegrationTest {

    @Override
    protected void setStorableTests() {
        storableTests = new StreamCatagoryTests().getAllTests();
    }

    public JdbcStorageManager createJdbcStorageManager(QueryExecutor queryExecutor) {
        JdbcStorageManager jdbcStorageManager = new JdbcStorageManager(queryExecutor);
        jdbcStorageManager.registerStorables(StreamCatalogService.getStorableClasses());
        return jdbcStorageManager;
    }
}
