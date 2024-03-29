package com.hortonworks.iotas.streams.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hortonworks.iotas.common.Schema;
import com.hortonworks.iotas.storage.PrimaryKey;
import com.hortonworks.iotas.storage.catalog.AbstractStorable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component represents an indivial component of Service. For example, NIMBUS, BROKER, etc.
 */
public class Component extends AbstractStorable {
    private static final String NAMESPACE = "components";

    private Long id;
    private Long serviceId;
    private String name;
    private List<String> hosts;

    // The protocol for communicating with this port.
    // Its representation is up to component.
    // For example. protocols for KAFKA are PLAINTEXT, SSL, etc.
    private String protocol;
    private Integer port;
    private Long timestamp;

    /**
     * The primary key.
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * The foreign key reference to the service id.
     */
    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * The component name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The set of hosts where the component runs.
     */
    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    /**
     * The protocol where the component communicates. (optional)
     */
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * The port where the component listens. (optional)
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @JsonIgnore
    public String getNameSpace() {
        return NAMESPACE;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @JsonIgnore
    public PrimaryKey getPrimaryKey() {
        Map<Schema.Field, Object> fieldToObjectMap = new HashMap<>();
        fieldToObjectMap.put(new Schema.Field("id", Schema.Type.LONG), this.id);
        return new PrimaryKey(fieldToObjectMap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Component)) return false;

        Component component = (Component) o;

        if (getId() != null ? !getId().equals(component.getId()) : component.getId() != null)
            return false;
        if (getServiceId() != null ?
            !getServiceId().equals(component.getServiceId()) :
            component.getServiceId() != null) return false;
        if (getName() != null ?
            !getName().equals(component.getName()) :
            component.getName() != null) return false;
        if (getHosts() != null ?
            !getHosts().equals(component.getHosts()) :
            component.getHosts() != null) return false;
        if (getPort() != null ?
            !getPort().equals(component.getPort()) :
            component.getPort() != null) return false;
        return getTimestamp() != null ?
            getTimestamp().equals(component.getTimestamp()) :
            component.getTimestamp() == null;

    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getServiceId() != null ? getServiceId().hashCode() : 0);
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getHosts() != null ? getHosts().hashCode() : 0);
        result = 31 * result + (getPort() != null ? getPort().hashCode() : 0);
        result = 31 * result + (getTimestamp() != null ? getTimestamp().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Component{" +
            "id=" + id +
            ", serviceId=" + serviceId +
            ", name='" + name + '\'' +
            ", hosts=" + hosts +
            ", port=" + port +
            ", timestamp=" + timestamp +
            '}';
    }
}
