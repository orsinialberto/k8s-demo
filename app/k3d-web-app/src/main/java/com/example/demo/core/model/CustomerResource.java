package com.example.demo.core.model;

import java.util.Date;
import java.util.Objects;

public class CustomerResource {

    private String id;

    // TODO convert to jsonNode
    private String base;

    private Date registeredAt;

    private Date updatedAt;

    private Long version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public Date getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Date registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerResource that = (CustomerResource) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(base, that.base) &&
                Objects.equals(registeredAt, that.registeredAt) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, base, registeredAt, updatedAt, version);
    }
}
