package com.hortonworks.iotas.streams.schema;

import com.google.common.collect.Sets;
import com.hortonworks.iotas.common.Schema;
import com.hortonworks.iotas.streams.layout.component.Stream;
import com.hortonworks.iotas.streams.schema.exception.BadComponentConfigException;

import java.util.Set;

public class MockEvolvingSchemaImpl implements EvolvingSchema {
    private Set<Stream> streams;

    public MockEvolvingSchemaImpl() {
        streams = Sets.newHashSet();
        initializeAppliedResult();
    }

    @Override
    public Set<Stream> apply(String config, Stream inputStream) throws BadComponentConfigException {
        return streams;
    }

    public Set<Stream> getStreams() {
        return streams;
    }

    private void initializeAppliedResult() {
        Schema schema = new Schema.SchemaBuilder().field(new Schema.Field("field1", Schema.Type.STRING)).build();
        Schema schema2 = new Schema.SchemaBuilder().field(new Schema.Field("field2", Schema.Type.LONG)).build();
        streams.add(new Stream("stream1", schema));
        streams.add(new Stream("stream2", schema2));
    }
}
