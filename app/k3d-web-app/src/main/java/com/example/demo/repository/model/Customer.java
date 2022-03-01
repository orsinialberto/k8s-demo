package com.example.demo.repository.model;

import com.example.demo.core.model.CustomerResource;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    private String id;

    @Column(name = "base")
    private String base;

    @Column(name = "registered_at")
    private Date registeredAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "version")
    private Long version;

    public Customer() {
    }

    public Customer(final String base) {
        this.id = UUID.randomUUID().toString();
        this.base = base;
    }

    @PreUpdate
    public void preUpdate() {
      updatedAt = new Date();
    }

    @PrePersist
    public void prePersist() {
        final Date now = new Date();
        this.registeredAt = now;
        this.updatedAt = now;
    }

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

    public CustomerResource toResource() {

        final CustomerResource customerResource = new CustomerResource();
        customerResource.setId(this.id);
        customerResource.setBase(this.base);
        customerResource.setRegisteredAt(this.registeredAt);
        customerResource.setUpdatedAt(this.updatedAt);
        customerResource.setVersion(null);

        return customerResource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) &&
                Objects.equals(base, customer.base) &&
                Objects.equals(registeredAt, customer.registeredAt) &&
                Objects.equals(updatedAt, customer.updatedAt) &&
                Objects.equals(version, customer.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, base, registeredAt, updatedAt, version);
    }
}
