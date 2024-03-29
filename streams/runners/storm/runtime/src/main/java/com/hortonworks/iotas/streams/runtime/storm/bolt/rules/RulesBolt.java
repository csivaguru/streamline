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

package com.hortonworks.iotas.streams.runtime.storm.bolt.rules;

import com.hortonworks.iotas.common.Constants;
import com.hortonworks.iotas.streams.IotasEvent;
import com.hortonworks.iotas.streams.Result;
import com.hortonworks.iotas.streams.common.IotasEventImpl;
import com.hortonworks.iotas.streams.runtime.processor.RuleProcessorRuntime;
import com.hortonworks.iotas.streams.runtime.rule.RulesDependenciesFactory;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class RulesBolt extends BaseRichBolt {
    private static final Logger LOG = LoggerFactory.getLogger(RulesBolt.class);

    private RuleProcessorRuntime ruleProcessorRuntime;

    private final RulesDependenciesFactory boltDependenciesFactory;

    private OutputCollector collector;

    public RulesBolt(RulesDependenciesFactory boltDependenciesFactory) {
        this.boltDependenciesFactory = boltDependenciesFactory;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        ruleProcessorRuntime = boltDependenciesFactory.createRuleProcessorRuntime();

        Map<String, Object> config = Collections.emptyMap();
        if (stormConf != null) {
            config = new HashMap<>();
            config.put(Constants.CATALOG_ROOT_URL, stormConf.get(Constants.CATALOG_ROOT_URL));
            config.put(Constants.LOCAL_FILES_PATH, stormConf.get(Constants.LOCAL_FILES_PATH));
        }
        ruleProcessorRuntime.initialize(config);
    }

    @Override
    public void execute(Tuple input) {  // Input tuple is expected to be an IotasEvent
        try {
            final Object iotasEvent = input.getValueByField(IotasEvent.IOTAS_EVENT);
            if (iotasEvent instanceof IotasEvent) {
                IotasEvent iotasEventWithStream = getIotasEventWithStream((IotasEvent) iotasEvent, input);
                LOG.debug("++++++++ Executing tuple [{}], IotasEvent [{}]", input, iotasEventWithStream);
                for (Result result : ruleProcessorRuntime.process(iotasEventWithStream)) {
                    for (IotasEvent event : result.events) {
                        collector.emit(result.stream, input, new Values(event));
                    }
                }
            } else {
                LOG.debug("Invalid tuple received. Tuple disregarded and rules not evaluated.\n\tTuple [{}]." +
                        "\n\tIotasEvent [{}].", input, iotasEvent);
            }
            collector.ack(input);
        } catch (Exception e) {
            collector.fail(input);
            collector.reportError(e);
            LOG.debug("", e);                        // useful to debug unit tests
        }
    }

    private IotasEvent getIotasEventWithStream(IotasEvent event, Tuple tuple) {
        return new IotasEventImpl(event.getFieldsAndValues(),
                event.getDataSourceId(), event.getId(),
                event.getHeader(), tuple.getSourceStreamId(), event.getAuxiliaryFieldsAndValues());
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        for (String stream : boltDependenciesFactory.createRuleProcessorRuntime().getStreams()) {
            declarer.declareStream(stream, new Fields(IotasEvent.IOTAS_EVENT));
        }
    }
}
