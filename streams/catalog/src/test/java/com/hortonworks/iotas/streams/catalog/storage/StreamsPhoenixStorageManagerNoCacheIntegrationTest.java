package com.hortonworks.iotas.streams.catalog.storage;

import com.hortonworks.iotas.common.test.HBaseIntegrationTest;
import com.hortonworks.iotas.storage.impl.jdbc.JdbcStorageManager;
import com.hortonworks.iotas.storage.impl.jdbc.config.ExecutionConfig;
import com.hortonworks.iotas.storage.impl.jdbc.phoenix.PhoenixStorageManagerNoCacheIntegrationTest;
import com.hortonworks.iotas.storage.impl.jdbc.provider.phoenix.factory.PhoenixExecutor;
import com.hortonworks.iotas.storage.impl.jdbc.provider.sql.factory.QueryExecutor;
import com.hortonworks.iotas.streams.catalog.Topology;
import com.hortonworks.iotas.streams.catalog.service.StreamCatalogService;
import com.hortonworks.iotas.streams.catalog.topology.TopologyComponentDefinition;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Phoenix storage integration tests without using cache.
 *
 */
@Category(HBaseIntegrationTest.class)
public  class StreamsPhoenixStorageManagerNoCacheIntegrationTest extends PhoenixStorageManagerNoCacheIntegrationTest {

    @Override
    protected void setStorableTests() {
        storableTests = new StreamCatagoryTests().getAllTests();
    }

    @Test
    public void testNextId_AutoincrementColumn_IdPlusOne() throws Exception {
        final PhoenixExecutor phoenixExecutor = new PhoenixExecutor(new ExecutionConfig(-1), connectionBuilder);
        String[] nameSpaces = {Topology.NAME_SPACE, TopologyComponentDefinition.NAME_SPACE};
        for (String nameSpace : nameSpaces) {
            log.info("Generating sequence-ids for namespace: [{}]", nameSpace);
            for (int x = 0; x < 100; x++) {
                final Long nextId = phoenixExecutor.nextId(nameSpace);
                log.info("\t\t[{}]", nextId);
                Assert.assertTrue(nextId > 0);
            }
        }
    }

    public JdbcStorageManager createJdbcStorageManager(QueryExecutor queryExecutor) {
        JdbcStorageManager jdbcStorageManager = new JdbcStorageManager(queryExecutor);
        jdbcStorageManager.registerStorables(StreamCatalogService.getStorableClasses());
        return jdbcStorageManager;
    }

}