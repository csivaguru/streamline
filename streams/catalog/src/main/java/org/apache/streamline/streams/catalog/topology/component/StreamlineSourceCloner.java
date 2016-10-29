package org.apache.streamline.streams.catalog.topology.component;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.streamline.streams.catalog.TopologySource;
import org.apache.streamline.streams.catalog.service.StreamCatalogService;
import org.apache.streamline.streams.layout.component.StreamlineComponent;
import org.apache.streamline.streams.layout.component.StreamlineSource;
import org.apache.streamline.streams.layout.component.OutputComponent;
import org.apache.streamline.streams.layout.component.Stream;
import org.apache.streamline.streams.catalog.topology.component.Cloner;

import java.util.ArrayList;

/**
 * Created by schendamaraikannan on 10/27/16.
 */
public final class StreamlineSourceCloner implements Cloner<StreamlineSource> {
    @Override
    public StreamlineSource clone(StreamlineSource originalComponent, StreamCatalogService streamCatalogService, Long topologyId) {
        TopologySource newTopologySource = getTopologySource(originalComponent);
        streamCatalogService.addTopologySource(topologyId, newTopologySource);
        return streamCatalogService.getFactory().getStreamlineSource(newTopologySource);
    }

    private TopologySource getTopologySource(StreamlineSource iotasSource) {
        TopologySource topologySource = new TopologySource();
        topologySource.setType(iotasSource.getType());
        topologySource.setConfig(iotasSource.getConfig());
        topologySource.setName(iotasSource.getName());
        topologySource.setOutputStreamIds(
                new ArrayList<>(
                        Collections2.transform(
                                iotasSource.getOutputStreams(),
                                new Function<Stream, Long>() {
                                    @Override
                                    public Long apply(final Stream stream) {
                                        return Long.parseLong(stream.getId());}
                                })));

        return topologySource;
    }
}
