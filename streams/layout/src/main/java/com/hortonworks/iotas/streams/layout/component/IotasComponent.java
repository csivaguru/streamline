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

import com.hortonworks.iotas.common.Config;

/**
 * Any Iotas design time topology component (source, sink, processor etc)
 * inherits from {@link IotasComponent} to provide
 * the default implementation for the {@link Component} methods.
 */
public abstract class IotasComponent implements Component {
    private String id;
    private String name;
    private Config config;
    private String type;

    public IotasComponent() {
        config = new Config();
    }

    public IotasComponent(IotasComponent other) {
        this.id = other.id;
        this.name = other.name;
        this.config = new Config(other.getConfig());
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IotasComponent that = (IotasComponent) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "IotasComponent{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", config='" + config + '\'' +
                '}';
    }

}
