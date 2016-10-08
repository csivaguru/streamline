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

package com.hortonworks.iotas.streams.runtime.rule.sql;


import com.hortonworks.iotas.streams.runtime.rule.condition.expression.StormSqlExpression;
import com.hortonworks.iotas.streams.runtime.script.engine.ScriptEngine;
import org.apache.storm.sql.StormSql;
import org.apache.storm.sql.runtime.ChannelContext;
import org.apache.storm.sql.runtime.ChannelHandler;
import org.apache.storm.sql.runtime.DataSource;
import org.apache.storm.sql.runtime.DataSourcesProvider;
import org.apache.storm.sql.runtime.FieldInfo;
import org.apache.storm.sql.runtime.ISqlTridentDataSource;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/** Implementation of Storm SQL engine that evaluates pre-compiled queries for each input */
public class SqlEngine implements ScriptEngine<SqlEngine> {
    protected static final Logger LOG = LoggerFactory.getLogger(SqlEngine.class);

    @Override
    public SqlEngine getEngine() {
        return this;
    }

    private DataSource dataSource;                      // step 1
    private volatile ChannelContext channelContext;              // step 2 - Data Source sets context
    private ChannelHandler channelHandler;              // step 3
    private DataSourcesProvider dataSourceProvider;     // step 4
    private List<Values> result = new ArrayList<>();

    /*
    Doing work in the constructor is not ideal but all of these inner classes make the code much simpler
    and avoid lots of callbacks. Nevertheless, this should not be an issue for testing as this is a very focused
    class that has a very specific purpose and therefore is very unlikely to change.
    Furthermore, the SQL streaming framework is still under development and it's API is subject to changing,
    so for now this is a reasonable solution
    */
    public SqlEngine() {
        // This sequence of steps cannot be changed
        this.dataSource = this.new RulesDataSource();                   // Step 1 && Step 2 - RulesDataSource Sets Channel Context
        this.channelHandler = this.new RulesChannelHandler();           // Step 3
        this.dataSourceProvider = this.new RulesDataSourcesProvider();  // Step 4
    }

    public void compileQuery(List<String> statements) {
        try {
            LOG.info("Compiling query statements {}", statements);
            StormSql stormSql = StormSql.construct();
            stormSql.execute(statements, channelHandler);
            LOG.info("Query statements successfully compiled");
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error compiling query. Statements [%s]", statements), e);
        }
    }

    public List<Values> eval(Values input) {
        channelContext.emit(input);
        List<Values> res = new ArrayList<>(result);
        result.clear();
        return res;
    }

    /*
     * force evaluation of pending results, for e.g. evaluate last group in case of group-by
     */
    public List<Values> flush() {
        channelContext.flush();
        List<Values> res = new ArrayList<>(result);
        result.clear();
        return res;
    }

    public void execute(Tuple tuple, OutputCollector outputCollector) {
        outputCollector.emit(tuple, createValues(tuple));
    }

    private Values createValues(Tuple input) {
        return (Values) input.getValues();
    }

    private class RulesDataSource implements DataSource {
        @Override
        public void open(ChannelContext ctx) {
            LOG.info("open invoked with ChannelContext {}", ctx);
            SqlEngine.this.channelContext = ctx;
        }
    }

    private class RulesChannelHandler implements ChannelHandler {
        /*
        This method only gets called when the query produces a non-empty result set.
        The hypothetical scenario of an empty result set would result in this method not being called, i.e. no data
        */
        @Override
        public void dataReceived(ChannelContext ctx, Values data) {
            LOG.debug("SQL query result set {}", data);
            SqlEngine.this.result.add(data);
        }

        @Override
        public void channelInactive(ChannelContext ctx) { }

        @Override
        public void exceptionCaught(Throwable cause) { }

        @Override
        public void flush(ChannelContext channelContext) { }

        @Override
        public void setSource(ChannelContext channelContext, Object o) { }
    }

    private class RulesDataSourcesProvider implements DataSourcesProvider {
        @Override
        public String scheme() {
            return StormSqlExpression.RULE_SCHEMA;
        }

        @Override
        public DataSource construct(URI uri, String s, String s1, List<FieldInfo> list) {
            return SqlEngine.this.dataSource;
        }

        @Override
        public ISqlTridentDataSource constructTrident(URI uri, String s, String s1, String s2, List<FieldInfo> list) {
            return null;
        }
    }

    public DataSourcesProvider getDataSourceProvider() {
        return dataSourceProvider;
    }

    @Override
    public String toString() {
        return "SqlEngine{" +
                "dataSource=" + dataSource +
                ", channelContext=" + channelContext +
                ", channelHandler=" + channelHandler +
                ", dataSourceProvider=" + dataSourceProvider +
                ", result=" + result +
                '}';
    }
}
