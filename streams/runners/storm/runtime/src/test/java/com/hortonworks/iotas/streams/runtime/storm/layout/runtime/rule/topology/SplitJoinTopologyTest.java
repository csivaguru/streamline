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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.hortonworks.iotas.streams.runtime.storm.layout.runtime.rule.topology;

import com.hortonworks.iotas.common.Schema;
import com.hortonworks.iotas.streams.layout.Transform;
import com.hortonworks.iotas.streams.layout.component.ComponentBuilder;
import com.hortonworks.iotas.streams.layout.component.Stream;
import com.hortonworks.iotas.streams.layout.component.impl.RulesProcessor;
import com.hortonworks.iotas.streams.layout.component.impl.splitjoin.JoinAction;
import com.hortonworks.iotas.streams.layout.component.impl.splitjoin.JoinProcessor;
import com.hortonworks.iotas.streams.layout.component.impl.splitjoin.SplitAction;
import com.hortonworks.iotas.streams.layout.component.impl.splitjoin.SplitProcessor;
import com.hortonworks.iotas.streams.layout.component.impl.splitjoin.StageAction;
import com.hortonworks.iotas.streams.layout.component.impl.splitjoin.StageProcessor;
import com.hortonworks.iotas.streams.layout.component.rule.action.transform.EnrichmentTransform;
import com.hortonworks.iotas.streams.layout.component.rule.action.transform.InmemoryTransformDataProvider;
import com.hortonworks.iotas.streams.runtime.rule.RulesDependenciesFactory;
import com.hortonworks.iotas.streams.runtime.storm.bolt.rules.RulesBolt;
import org.apache.storm.Config;
import org.apache.storm.ILocalCluster;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Test to create a split/join based storm topology and run it.
 */
public class SplitJoinTopologyTest {
    private static final Logger log = LoggerFactory.getLogger(SplitJoinTopologyTest.class);

    private static final String RULES_TEST_SPOUT = "rules-test-spout";
    private static final String SPLIT_BOLT = "split-bolt";
    private static final String STAGE_BOLT = "stage-bolt";
    private static final String JOIN_BOLT = "join-bolt";
    private static final String SINK_BOLT = "sink-bolt";
    private static final Schema SJ_SCHEMA = Schema.of(Schema.Field.of("event", Schema.Type.STRING));

    private static final Stream JOIN_OUTPUT_STREAM = new Stream("join-output-stream", SJ_SCHEMA);
    private static final Stream SPLIT_STREAM_ID = new Stream("split-stream", SJ_SCHEMA);
    private static final Stream STAGE_OUTPUT_STREAM = new Stream("stage-output-stream", SJ_SCHEMA);

    @Test
    public void testSplitJoinTopology() throws Exception {
        submitTopology();
    }

    protected void submitTopology() throws Exception {
        final Config config = getConfig();
        final String topologyName = "SplitJoinTopologyTest";
        final StormTopology topology = createTopology();
        log.info("Created topology with name: [{}] and topology: [{}]", topologyName, topology);

        ILocalCluster localCluster = new LocalCluster();
        log.info("Submitting topology: [{}]", topologyName);
        localCluster.submitTopology(topologyName, config, topology);

    }

    protected Config getConfig() {
        final Config config = new Config();
        config.setDebug(true);
        return config;
    }

    protected StormTopology createTopology() {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(RULES_TEST_SPOUT, new RulesTestSpout(2000));
        builder.setBolt(SPLIT_BOLT, createSplitBolt()).shuffleGrouping(RULES_TEST_SPOUT);
        builder.setBolt(STAGE_BOLT, createStageBolt()).shuffleGrouping(SPLIT_BOLT, SPLIT_STREAM_ID.getId());
        builder.setBolt(JOIN_BOLT, createJoinBolt()).shuffleGrouping(STAGE_BOLT, STAGE_OUTPUT_STREAM.getId());
        builder.setBolt(SINK_BOLT, new RulesTestSinkBolt()).shuffleGrouping(JOIN_BOLT, JOIN_OUTPUT_STREAM.getId());
        return builder.createTopology();
    }

    private RulesBolt createSplitBolt() {
        SplitAction splitAction = new SplitAction();
        splitAction.setOutputStreams(Collections.singleton(SPLIT_STREAM_ID.getId()));
        SplitProcessorBuilder splitProcessorBuilder = new SplitProcessorBuilder(SPLIT_STREAM_ID, splitAction);

        return new RulesBolt(new RulesDependenciesFactory(splitProcessorBuilder, getScriptType()));
    }

    private RulesBolt createStageBolt() {
        return new RulesBolt(new RulesDependenciesFactory(new StageProcessorBuilder(), getScriptType()));
    }

    private RulesBolt createJoinBolt() {
        return new RulesBolt(new RulesDependenciesFactory(new JoinProcessorBuilder(), getScriptType()));
    }

    public RulesDependenciesFactory.ScriptType getScriptType() {
        return RulesDependenciesFactory.ScriptType.SQL;
    }

    static class SplitProcessorBuilder implements ComponentBuilder<RulesProcessor> {

        private Stream splitStream;
        private SplitAction splitAction;

        public SplitProcessorBuilder(Stream splitStream, SplitAction splitAction) {
            this.splitStream = splitStream;
            this.splitAction = splitAction;
        }

        @Override
        public RulesProcessor build() {
            final SplitProcessor splitProcessor = new SplitProcessor();
            splitProcessor.addOutputStreams(Collections.singleton(splitStream));
            splitProcessor.setSplitAction(splitAction);
            splitProcessor.setName("split-processor-"+System.currentTimeMillis());
            splitProcessor.setId(UUID.randomUUID().toString());
            return splitProcessor;
        }
    }

    static class JoinProcessorBuilder implements ComponentBuilder<RulesProcessor> {

        @Override
        public RulesProcessor build() {
            JoinAction joinAction = new JoinAction();
            joinAction.setOutputStreams(Collections.singleton(JOIN_OUTPUT_STREAM.getId()));
            JoinProcessor joinProcessor = new JoinProcessor();
            joinProcessor.addOutputStreams(Collections.singleton(JOIN_OUTPUT_STREAM));
            joinProcessor.setJoinAction(joinAction);
            joinProcessor.setName("join-processor-"+System.currentTimeMillis());
            joinProcessor.setId(UUID.randomUUID().toString());
            return joinProcessor;
        }
    }

    static class StageProcessorBuilder implements ComponentBuilder<RulesProcessor> {

        public StageProcessorBuilder() {
        }

        @Override
        public RulesProcessor build() {
            final String enrichFieldName = "temperature";

            Map<Object, Object> enrichmentsData = new HashMap<>();
            for(int i=0; i< 150; i++) {
                enrichmentsData.put(i, (i - 32) / 1.8f);
            }
            InmemoryTransformDataProvider transformDataProvider = new InmemoryTransformDataProvider(enrichmentsData);
            EnrichmentTransform enrichmentTransform = new EnrichmentTransform("enricher", Collections.singletonList(enrichFieldName), transformDataProvider);
            StageAction stageAction = new StageAction(Collections.<Transform>singletonList(enrichmentTransform));
            stageAction.setOutputStreams(Collections.singleton(STAGE_OUTPUT_STREAM.getId()));

            final StageProcessor stageProcessor = new StageProcessor();
            stageProcessor.addOutputStreams(Collections.singleton(STAGE_OUTPUT_STREAM));
            stageProcessor.setStageAction(stageAction);
            stageProcessor.setName("stage-processor-" + System.currentTimeMillis());
            stageProcessor.setId(UUID.randomUUID().toString());
            return stageProcessor;
        }
    }

}
