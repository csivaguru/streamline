package com.hortonworks.iotas.streams.catalog.topology.component;


import com.hortonworks.iotas.common.QueryParam;
import com.hortonworks.iotas.streams.catalog.Topology;
import com.hortonworks.iotas.streams.catalog.TopologyEdge;
import com.hortonworks.iotas.streams.catalog.TopologyProcessor;
import com.hortonworks.iotas.streams.catalog.TopologySink;
import com.hortonworks.iotas.streams.catalog.TopologySource;
import com.hortonworks.iotas.streams.catalog.service.StreamCatalogService;
import com.hortonworks.iotas.streams.layout.component.TopologyDag;

import java.util.ArrayList;
import java.util.List;

public class TopologyDagBuilder {
    private final StreamCatalogService catalogService;
    private final TopologyComponentFactory factory;

    public TopologyDagBuilder(StreamCatalogService catalogService) {
        this.catalogService = catalogService;
        factory = new TopologyComponentFactory(catalogService);
    }

    public TopologyDag getDag(Topology topology) {
        try {
            TopologyDag dag = new TopologyDag();
            addSources(dag, topology);
            addProcessors(dag, topology);
            addSinks(dag, topology);
            addEdges(dag, topology);
            return dag;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void addSources(TopologyDag dag, Topology topology) throws Exception {
        for (TopologySource topologySource : catalogService.listTopologySources(queryParam(topology))) {
            dag.add(factory.getIotasSource(topologySource));
        }
    }

    private void addProcessors(TopologyDag dag, Topology topology) throws Exception {
        for (TopologyProcessor topologyProcessor : catalogService.listTopologyProcessors(queryParam(topology))) {
            dag.add(factory.getIotasProcessor(topologyProcessor));
        }
    }

    private void addSinks(TopologyDag dag, Topology topology) throws Exception {
        for (TopologySink topologySink : catalogService.listTopologySinks(queryParam(topology))) {
            dag.add(factory.getIotasSink(topologySink));
        }
    }

    private void addEdges(TopologyDag dag, Topology topology) throws Exception {
        for (TopologyEdge topologyEdge: catalogService.listTopologyEdges(queryParam(topology))) {
            dag.addEdge(factory.getIotasEdge(topologyEdge));
        }
    }

    private List<QueryParam> queryParam(Topology topology) {
        List<QueryParam> queryParams = new ArrayList<>();
        queryParams.add(new QueryParam("topologyId", topology.getId().toString()));
        return queryParams;
    }
}
