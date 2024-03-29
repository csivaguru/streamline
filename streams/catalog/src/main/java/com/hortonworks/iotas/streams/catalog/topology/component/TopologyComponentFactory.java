package com.hortonworks.iotas.streams.catalog.topology.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.hortonworks.iotas.common.Config;
import com.hortonworks.iotas.streams.catalog.RuleInfo;
import com.hortonworks.iotas.streams.catalog.StreamInfo;
import com.hortonworks.iotas.streams.catalog.TopologyComponent;
import com.hortonworks.iotas.streams.catalog.TopologyEdge;
import com.hortonworks.iotas.streams.catalog.TopologyOutputComponent;
import com.hortonworks.iotas.streams.catalog.TopologyProcessor;
import com.hortonworks.iotas.streams.catalog.TopologySink;
import com.hortonworks.iotas.streams.catalog.TopologySource;
import com.hortonworks.iotas.streams.catalog.WindowInfo;
import com.hortonworks.iotas.streams.catalog.service.StreamCatalogService;
import com.hortonworks.iotas.streams.layout.component.Edge;
import com.hortonworks.iotas.streams.layout.component.InputComponent;
import com.hortonworks.iotas.streams.layout.component.IotasComponent;
import com.hortonworks.iotas.streams.layout.component.IotasProcessor;
import com.hortonworks.iotas.streams.layout.component.IotasSink;
import com.hortonworks.iotas.streams.layout.component.IotasSource;
import com.hortonworks.iotas.streams.layout.component.OutputComponent;
import com.hortonworks.iotas.streams.layout.component.Stream;
import com.hortonworks.iotas.streams.layout.component.StreamGrouping;
import com.hortonworks.iotas.streams.layout.component.impl.CustomProcessor;
import com.hortonworks.iotas.streams.layout.component.impl.KafkaSource;
import com.hortonworks.iotas.streams.layout.component.impl.NotificationSink;
import com.hortonworks.iotas.streams.layout.component.impl.RulesProcessor;
import com.hortonworks.iotas.streams.layout.component.impl.normalization.NormalizationConfig;
import com.hortonworks.iotas.streams.layout.component.impl.normalization.NormalizationProcessor;
import com.hortonworks.iotas.streams.layout.component.impl.splitjoin.JoinAction;
import com.hortonworks.iotas.streams.layout.component.impl.splitjoin.JoinProcessor;
import com.hortonworks.iotas.streams.layout.component.impl.splitjoin.SplitAction;
import com.hortonworks.iotas.streams.layout.component.impl.splitjoin.SplitProcessor;
import com.hortonworks.iotas.streams.layout.component.impl.splitjoin.StageAction;
import com.hortonworks.iotas.streams.layout.component.impl.splitjoin.StageProcessor;
import com.hortonworks.iotas.streams.layout.component.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hortonworks.iotas.common.ComponentTypes.CUSTOM;
import static com.hortonworks.iotas.common.ComponentTypes.JOIN;
import static com.hortonworks.iotas.common.ComponentTypes.KAFKA;
import static com.hortonworks.iotas.common.ComponentTypes.NORMALIZATION;
import static com.hortonworks.iotas.common.ComponentTypes.NOTIFICATION;
import static com.hortonworks.iotas.common.ComponentTypes.RULE;
import static com.hortonworks.iotas.common.ComponentTypes.SPLIT;
import static com.hortonworks.iotas.common.ComponentTypes.STAGE;
import static com.hortonworks.iotas.common.ComponentTypes.WINDOW;
import static java.util.AbstractMap.SimpleImmutableEntry;

/**
 * Constructs various topology components based on the
 * TopologyComponent catalog entities
 */
public class TopologyComponentFactory {
    private static final Logger LOG = LoggerFactory.getLogger(TopologyComponentFactory.class);

    private final Map<Class<?>, Map<String, ?>> providerMap;
    private final StreamCatalogService catalogService;

    public TopologyComponentFactory(StreamCatalogService catalogService) {
        this.catalogService = catalogService;
        ImmutableMap.Builder<Class<?>, Map<String, ?>> builder = ImmutableMap.builder();
        builder.put(IotasSource.class, createSourceProviders());
        builder.put(IotasProcessor.class, createProcessorProviders());
        builder.put(IotasSink.class, createSinkProviders());
        providerMap = builder.build();
    }

    public IotasSource getIotasSource(TopologySource topologySource) {
        IotasSource source = getProvider(IotasSource.class, topologySource.getType()).create(topologySource);
        source.setId(topologySource.getId().toString());
        source.setName(topologySource.getName());
        source.setConfig(topologySource.getConfig());
        source.setType(topologySource.getType());
        source.addOutputStreams(createOutputStreams(topologySource));
        return source;
    }

    public IotasProcessor getIotasProcessor(TopologyProcessor topologyProcessor) {
        IotasProcessor processor = getProvider(IotasProcessor.class, topologyProcessor.getType()).create(topologyProcessor);
        processor.setId(topologyProcessor.getId().toString());
        processor.setName(topologyProcessor.getName());
        processor.setConfig(topologyProcessor.getConfig());
        processor.setType(topologyProcessor.getType());
        if (processor.getOutputStreams() == null || processor.getOutputStreams().isEmpty()) {
            processor.addOutputStreams(createOutputStreams(topologyProcessor));
        }
        return processor;
    }

    public IotasSink getIotasSink(TopologySink topologySink) {
        IotasSink sink = getProvider(IotasSink.class, topologySink.getType()).create(topologySink);
        sink.setId(topologySink.getId().toString());
        sink.setName(topologySink.getName());
        sink.setConfig(topologySink.getConfig());
        sink.setType(topologySink.getType());
        return sink;
    }

    public Edge getIotasEdge(TopologyEdge topologyEdge) {
        Edge edge = new Edge();
        edge.setFrom(getOutputComponent(topologyEdge));
        edge.setTo(getInputComponent(topologyEdge));
        Set<StreamGrouping> streamGroupings = new HashSet<>();
        for (TopologyEdge.StreamGrouping streamGrouping : topologyEdge.getStreamGroupings()) {
            Stream stream = getStream(catalogService.getStreamInfo(streamGrouping.getStreamId()));
            Stream.Grouping grouping = Stream.Grouping.valueOf(streamGrouping.getGrouping().name());
            streamGroupings.add(new StreamGrouping(stream, grouping, streamGrouping.getFields()));
        }
        edge.addStreamGroupings(streamGroupings);
        return edge;
    }

    private OutputComponent getOutputComponent(TopologyEdge topologyEdge) {
        TopologySource topologySource;
        TopologyProcessor topologyProcessor;
        if ((topologySource = catalogService.getTopologySource(topologyEdge.getFromId())) != null) {
            return getIotasSource(topologySource);
        } else if ((topologyProcessor = catalogService.getTopologyProcessor(topologyEdge.getFromId())) != null) {
            return getIotasProcessor(topologyProcessor);
        } else {
            throw new IllegalArgumentException("Invalid from id for edge " + topologyEdge);
        }
    }

    private InputComponent getInputComponent(TopologyEdge topologyEdge) {
        TopologySink topologySink;
        TopologyProcessor topologyProcessor;
        if ((topologySink = catalogService.getTopologySink(topologyEdge.getToId())) != null) {
            return getIotasSink(topologySink);
        } else if ((topologyProcessor = catalogService.getTopologyProcessor(topologyEdge.getToId())) != null) {
            return getIotasProcessor(topologyProcessor);
        } else {
            throw new IllegalArgumentException("Invalid to id for edge " + topologyEdge);
        }
    }

    public Stream getStream(StreamInfo streamInfo) {
        return new Stream(streamInfo.getStreamId(), streamInfo.getFields());
    }

    /*
     * Its optional to register the specific child providers. Its needed only if
     * the specific child component exists and needs to be passed in the runtime.
     * In that case the specific child component would be added to the topology dag vertex.
     * Otherwise an instance of IotasSource is used.
     */
    private Map<String, Provider<IotasSource>> createSourceProviders() {
        ImmutableMap.Builder<String, Provider<IotasSource>> builder = ImmutableMap.builder();
        builder.put(kafkaSourceProvider());
        return builder.build();
    }

    /*
     * Its optional to register the specific child providers. Its needed only if
     * the specific child component exists and needs to be passed in the runtime.
     * In that case the specific child component would be added to the topology dag vertex.
     * Otherwise an instance of IotasProcessor is used.
     */
    private Map<String, Provider<IotasProcessor>> createProcessorProviders() {
        ImmutableMap.Builder<String, Provider<IotasProcessor>> builder = ImmutableMap.builder();
        builder.put(rulesProcessorProvider());
        builder.put(windowProcessorProvider());
        builder.put(normalizationProcessorProvider());
        builder.put(splitProcessorProvider());
        builder.put(joinProcessorProvider());
        builder.put(stageProcessorProvider());
        builder.put(customProcessorProvider());
        return builder.build();
    }

    /*
     * Its optional to register the specific child providers. Its needed only if
     * the specific child component exists and needs to be passed in the runtime.
     * In that case the specific child component would be added to the topology dag vertex.
     * Otherwise an instance of IotasSink is used.
     */
    private Map<String, Provider<IotasSink>> createSinkProviders() {
        ImmutableMap.Builder<String, Provider<IotasSink>> builder = ImmutableMap.builder();
        builder.put(notificationSinkProvider());
        return builder.build();
    }

    private Set<Stream> createOutputStreams(TopologyOutputComponent outputComponent) {
        Set<Stream> outputStreams = new HashSet<>();
        for (Long id : outputComponent.getOutputStreamIds()) {
            outputStreams.add(getStream(catalogService.getStreamInfo(id)));
        }
        return outputStreams;
    }

    private <T extends IotasComponent> Provider<T> getProvider(final Class<T> clazz, String type) {
        if (providerMap.get(clazz).containsKey(type)) {
            return (Provider<T>) providerMap.get(clazz).get(type);
        } else {
            LOG.warn("Type {} not found in provider map, returning an instance of {}", type, clazz.getName());
            return new Provider<T>() {
                @Override
                public T create(TopologyComponent component) {
                    try {
                        return clazz.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        LOG.error("Got exception ", e);
                        throw new RuntimeException(e);
                    }
                }
            };
        }
    }

    private interface Provider<T extends IotasComponent> {
        T create(TopologyComponent component);
    }

    private Map.Entry<String, Provider<IotasSource>> kafkaSourceProvider() {
        Provider<IotasSource> provider = new Provider<IotasSource>() {
            @Override
            public IotasSource create(TopologyComponent component) {
                return new KafkaSource();
            }
        };
        return new SimpleImmutableEntry<>(KAFKA, provider);
    }

    private Map.Entry<String, Provider<IotasProcessor>> normalizationProcessorProvider() {
        Provider<IotasProcessor> provider = new Provider<IotasProcessor>() {
            @Override
            public IotasProcessor create(TopologyComponent component) {
                Config config = component.getConfig();
                Object typeObj = config.getAny(NormalizationProcessor.CONFIG_KEY_TYPE);
                Object normConfObj = config.getAny(NormalizationProcessor.CONFIG_KEY_NORMALIZATION);
                ObjectMapper objectMapper = new ObjectMapper();
                NormalizationProcessor.Type type = objectMapper.convertValue(typeObj, NormalizationProcessor.Type.class);
                Map<String, NormalizationConfig> normConfig = objectMapper.convertValue(normConfObj, new TypeReference<Map<String, NormalizationConfig>>() {
                });
                updateWithSchemas(normConfig);

                Set<Stream> outputStreams = createOutputStreams((TopologyOutputComponent) component);
                if (outputStreams.size() != 1) {
                    throw new IllegalArgumentException("Normalization component [" + component + "] must have only one output stream");
                }

                return new NormalizationProcessor(normConfig, outputStreams.iterator().next(), type);
            }
        };
        return new SimpleImmutableEntry<>(NORMALIZATION, provider);
    }

    private void updateWithSchemas(Map<String, NormalizationConfig> normalizationConfigRead) {
        for (Map.Entry<String, NormalizationConfig> entry : normalizationConfigRead.entrySet()) {
            NormalizationConfig normalizationConfig = entry.getValue();
            normalizationConfig.setInputSchema(catalogService.getStreamInfo(Long.parseLong(entry.getKey())).getSchema());
        }
    }

    private Map.Entry<String, Provider<IotasProcessor>> splitProcessorProvider() {
        Provider<IotasProcessor> provider = new Provider<IotasProcessor>() {
            @Override
            public IotasProcessor create(TopologyComponent component) {
                Object splitConfig = component.getConfig().getAny(SplitProcessor.CONFIG_KEY_SPLIT);
                ObjectMapper objectMapper = new ObjectMapper();
                SplitAction splitAction = objectMapper.convertValue(splitConfig, SplitAction.class);
                SplitProcessor splitProcessor = new SplitProcessor();
                splitProcessor.addOutputStreams(createOutputStreams((TopologyOutputComponent) component));
                splitProcessor.setSplitAction(splitAction);
                return splitProcessor;
            }
        };
        return new SimpleImmutableEntry<>(SPLIT, provider);
    }

    private Map.Entry<String, Provider<IotasProcessor>> joinProcessorProvider() {
        Provider<IotasProcessor> provider = new Provider<IotasProcessor>() {
            @Override
            public IotasProcessor create(TopologyComponent component) {
                Object joinConfig = component.getConfig().getAny(JoinProcessor.CONFIG_KEY_JOIN);
                ObjectMapper objectMapper = new ObjectMapper();
                JoinAction joinAction = objectMapper.convertValue(joinConfig, JoinAction.class);
                JoinProcessor joinProcessor = new JoinProcessor();
                joinProcessor.addOutputStreams(createOutputStreams((TopologyOutputComponent) component));
                joinProcessor.setJoinAction(joinAction);
                return joinProcessor;
            }
        };
        return new SimpleImmutableEntry<>(JOIN, provider);
    }

    private Map.Entry<String, Provider<IotasProcessor>> stageProcessorProvider() {
        Provider<IotasProcessor> provider = new Provider<IotasProcessor>() {
            @Override
            public IotasProcessor create(TopologyComponent component) {
                Object stageConfig = component.getConfig().getAny(StageProcessor.CONFIG_KEY_STAGE);
                ObjectMapper objectMapper = new ObjectMapper();
                StageAction stageAction = objectMapper.convertValue(stageConfig, StageAction.class);
                StageProcessor stageProcessor = new StageProcessor();
                stageProcessor.addOutputStreams(createOutputStreams((TopologyOutputComponent) component));
                stageProcessor.setStageAction(stageAction);
                return stageProcessor;
            }
        };
        return new SimpleImmutableEntry<>(STAGE, provider);
    }

    private interface RuleExtractor {
        Rule getRule(Long ruleId) throws Exception;
    }

    private Map.Entry<String, Provider<IotasProcessor>> rulesProcessorProvider() {
        return new SimpleImmutableEntry<>(RULE, createRulesProcessorProvider(new RuleExtractor() {
            @Override
            public Rule getRule(Long ruleId) throws Exception {
                RuleInfo ruleInfo = catalogService.getRule(ruleId);
                if (ruleInfo == null) {
                    throw new IllegalArgumentException("Cannot find rule with id " + ruleId);
                }
                return ruleInfo.getRule();
            }
        }));
    }

    private Map.Entry<String, Provider<IotasProcessor>> windowProcessorProvider() {
        return new SimpleImmutableEntry<>(WINDOW, createRulesProcessorProvider(new RuleExtractor() {
            @Override
            public Rule getRule(Long ruleId) throws Exception {
                WindowInfo windowInfo = catalogService.getWindow(ruleId);
                if (windowInfo == null) {
                    throw new IllegalArgumentException("Cannot find rule with id " + ruleId);
                }
                return windowInfo.getRule();
            }
        }));
    }

    private Provider<IotasProcessor> createRulesProcessorProvider(final RuleExtractor ruleExtractor) {
        return new Provider<IotasProcessor>() {
            @Override
            public IotasProcessor create(TopologyComponent component) {
                RulesProcessor processor = new RulesProcessor();
                ObjectMapper objectMapper = new ObjectMapper();
                Set<Stream> outputStreams = createOutputStreams((TopologyOutputComponent) component);
                processor.addOutputStreams(outputStreams);

                Object ruleList = component.getConfig().getAny(RulesProcessor.CONFIG_KEY_RULES);
                List<Long> ruleIds = objectMapper.convertValue(ruleList, new TypeReference<List<Long>>() {
                });
                try {
                    List<Rule> rules = new ArrayList<>();
                    for (Long ruleId : ruleIds) {
                        rules.add(ruleExtractor.getRule(ruleId));
                    }
                    processor.setRules(rules);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                return processor;
            }
        };
    }

    private Map.Entry<String, Provider<IotasProcessor>> customProcessorProvider() {
        Provider<IotasProcessor> provider = new Provider<IotasProcessor>() {
            @Override
            public IotasProcessor create(TopologyComponent component) {
                return new CustomProcessor();
            }
        };
        return new SimpleImmutableEntry<>(CUSTOM, provider);
    }

    private Map.Entry<String, Provider<IotasSink>> notificationSinkProvider() {
        Provider<IotasSink> provider = new Provider<IotasSink>() {
            @Override
            public IotasSink create(TopologyComponent component) {
                return new NotificationSink();
            }
        };
        return new SimpleImmutableEntry<>(NOTIFICATION, provider);
    }
}
