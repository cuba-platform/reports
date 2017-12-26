package com.haulmont.reports.testsupport.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.BaseUuidEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "test$User")
@Table(name = "test_user")
@NamePattern("%s|name")
public class User extends BaseUuidEntity {
    @Column
    private String login;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private Set<TimeEntry> entries = new HashSet<>();

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Set<TimeEntry> getEntries() {
        return entries;
    }

    public void setEntries(Set<TimeEntry> entries) {
        this.entries = entries;
    }
}
