package org.apache.streamline.streams.catalog.topology.component;

import org.apache.streamline.streams.catalog.TopologySink;
import org.apache.streamline.streams.catalog.service.StreamCatalogService;
import org.apache.streamline.streams.layout.component.InputComponent;
import org.apache.streamline.streams.layout.component.StreamlineComponent;
import org.apache.streamline.streams.layout.component.StreamlineSink;
import org.apache.streamline.streams.catalog.topology.component.Cloner;

/**
 * Created by schendamaraikannan on 10/27/16.
 */
public final class StreamlineSinkCloner implements Cloner<StreamlineSink> {
    @Override
    public StreamlineSink clone(StreamlineSink originalComponent, StreamCatalogService streamCatalogService, Long topologyId) {
        TopologySink newTopologySink = getTopologySink(originalComponent);
        streamCatalogService.addTopologySink(topologyId, newTopologySink);
        return streamCatalogService.getFactory().getStreamlineSink(newTopologySink);
    }

    private TopologySink getTopologySink(StreamlineSink sink) {
        TopologySink topologySink = new TopologySink();
        topologySink.setType(sink.getType());
        topologySink.setConfig(sink.getConfig());
        topologySink.setName(sink.getName());
        return topologySink;
    }
}
