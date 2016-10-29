package org.apache.streamline.streams.catalog.topology.component;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.streamline.streams.layout.component.Stream;
import org.apache.streamline.streams.layout.component.StreamlineProcessor;
import org.apache.streamline.streams.catalog.service.StreamCatalogService;
import org.apache.streamline.streams.catalog.TopologyProcessor;
import java.util.ArrayList;

/**
 * Created by schendamaraikannan on 10/27/16.
 */
public final class StreamlineProcessorCloner implements Cloner<StreamlineProcessor> {
    @Override
    public StreamlineProcessor clone(StreamlineProcessor StreamlineProcessor, StreamCatalogService streamCatalogService, Long topologyId) {
        TopologyProcessor newTopologyProcessor = getTopologyProcessor(StreamlineProcessor);
        newTopologyProcessor = streamCatalogService.addTopologyProcessor(topologyId, newTopologyProcessor);
        return streamCatalogService.getFactory().getStreamlineProcessor(newTopologyProcessor);
    }

    private TopologyProcessor getTopologyProcessor(StreamlineProcessor processor) {
        TopologyProcessor topologyProcessor = new TopologyProcessor();
        topologyProcessor.setType(processor.getType());
        topologyProcessor.setConfig(processor.getConfig());
        topologyProcessor.setName(processor.getName());
        topologyProcessor.setOutputStreamIds(
                new ArrayList<>(
                        Collections2.transform(
                                processor.getOutputStreams(),
                                new Function<Stream, Long>() {
                                    @Override
                                    public Long apply(final Stream stream) {
                                        return Long.parseLong(stream.getId());}
                                })));

        return topologyProcessor;
    }

}
