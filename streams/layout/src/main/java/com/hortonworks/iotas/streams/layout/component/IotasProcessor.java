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
package com.hortonworks.iotas.streams.layout.component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The base implementation of a {@link Processor} that all Iotas processors should extend.
 */
public class IotasProcessor extends IotasComponent implements Processor {
    private final Set<Stream> outputStreams = new HashSet<>();

    public IotasProcessor() {
        this(Collections.EMPTY_SET);
    }

    public IotasProcessor(Set<Stream> outputStreams) {
        addOutputStreams(outputStreams);
    }

    public IotasProcessor(IotasProcessor other) {
        super(other);
        addOutputStreams(other.getOutputStreams());
    }

    @Override
    public Set<Stream> getOutputStreams() {
        return outputStreams;
    }

    public void addOutputStream(Stream stream) {
        outputStreams.add(stream);
    }

    public void addOutputStreams(Set<Stream> streams) {
        outputStreams.addAll(streams);
    }

    @Override
    public Stream getOutputStream(String streamId) {
        for (Stream stream : this.getOutputStreams()) {
            if (stream.getId().equals(streamId)) {
                return stream;
            }
        }
        throw new IllegalArgumentException("Invalid streamId " + streamId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        IotasProcessor processor = (IotasProcessor) o;

        return outputStreams != null ? outputStreams.equals(processor.outputStreams) : processor.outputStreams == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (outputStreams != null ? outputStreams.hashCode() : 0);
        return result;
    }

    @Override
    public void accept(TopologyDagVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "IotasProcessor{" +
                "outputStreams=" + outputStreams +
                '}'+super.toString();
    }
}
