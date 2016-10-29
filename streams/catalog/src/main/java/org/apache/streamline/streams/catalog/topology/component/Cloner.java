package org.apache.streamline.streams.catalog.topology.component;

import org.apache.streamline.streams.layout.component.Component;
import org.apache.streamline.streams.catalog.service.StreamCatalogService;
/**
 * Created by schendamaraikannan on 10/27/16.
 */
public interface Cloner <T extends Component>{
    T clone(T originalComponent, StreamCatalogService streamCatalogService, Long topologyId);
}
