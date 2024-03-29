package com.hortonworks.iotas.streams.schema;

import com.google.common.collect.Sets;
import com.hortonworks.iotas.common.Schema;
import com.hortonworks.iotas.streams.catalog.service.CatalogService;
import com.hortonworks.iotas.streams.catalog.service.StreamCatalogService;
import com.hortonworks.iotas.streams.layout.component.Stream;
import com.hortonworks.iotas.streams.schema.exception.BadComponentConfigException;

import java.util.Set;

public class MockEvolvingSchemaCatalogServiceAwareImpl implements EvolvingSchema, CatalogServiceAware {
    private Set<Stream> streams;
    private CatalogService catalogService;
    private StreamCatalogService streamcatalogService;

    public MockEvolvingSchemaCatalogServiceAwareImpl() {
        streams = Sets.newHashSet();
        initializeAppliedResult();
    }

    @Override
    public Set<Stream> apply(String config, Stream inputStream) throws BadComponentConfigException {
        if (catalogService == null) {
            throw new RuntimeException("CatalogServiceAware is not respected!");
        }
        return streams;
    }

    public Set<Stream> getStreams() {
        return streams;
    }

    @Override
    public void setCatalogService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public void setStreamCatalogService(StreamCatalogService catalogService) {
        this.streamcatalogService = catalogService;
    }


    private void initializeAppliedResult() {
        Schema schema = new Schema.SchemaBuilder().field(new Schema.Field("field1", Schema.Type.STRING)).build();
        streams.add(new Stream("stream1", schema));
    }
}
