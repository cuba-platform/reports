/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
