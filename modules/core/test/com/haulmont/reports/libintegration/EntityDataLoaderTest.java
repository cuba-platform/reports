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

package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.testsupport.ReportsTestContainer;
import com.haulmont.yarg.structure.BandData;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class EntityDataLoaderTest {

    @ClassRule
    public static ReportsTestContainer cont = ReportsTestContainer.Common.INSTANCE;

    private User user;
    private Group group;

    @Before
    public void setUp() throws Exception {
        Transaction tx = cont.persistence().createTransaction();
        try {
            user = new User();
            user.setLogin("test");
            user.setLoginLowerCase("test");
            user.setPassword("test");

            group = new Group();
            group.setName("test");
            user.setGroup(group);

            cont.entityManager().persist(group);
            cont.entityManager().persist(user);

            tx.commit();
        } finally {
            tx.end();
        }

        user.setGroup(null);
    }

    @After
    public void tearDown() throws Exception {
        Transaction tx = cont.persistence().createTransaction();
        try {
            cont.entityManager()
                    .createNativeQuery("delete from sec_user where id = ?1")
                    .setParameter(1, user.getId())
                    .executeUpdate();
            cont.entityManager()
                    .createNativeQuery("delete from sec_group where id = ?1")
                    .setParameter(1, group.getId())
                    .executeUpdate();
            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testSingleEntityLoad() throws Exception {
        DataSet dataSet = new DataSet();
        Map<String, Object> params = new HashMap<>();
        params.put(SingleEntityDataLoader.DEFAULT_ENTITY_PARAM_NAME, user);

        Transaction tx = cont.persistence().createTransaction();
        try {
            Map<String, Object> entityWrapper = doSingleLoad(dataSet, params);
            Object groupName = entityWrapper.get("group.name");
            assertNull(groupName);
            tx.commit();
        } finally {
            tx.end();
        }

        dataSet.setViewName("user.edit");
        dataSet.setUseExistingView(true);

        tx = cont.persistence().createTransaction();
        try {
            Map<String, Object> entityWrapper = doSingleLoad(dataSet, params);
            Object groupName = entityWrapper.get("group.name");
            assertNotNull(groupName);
            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void testMultiEntityLoad() throws Exception {
        DataSet dataSet = new DataSet();
        Map<String, Object> params = new HashMap<>();
        params.put(MultiEntityDataLoader.DEFAULT_LIST_ENTITIES_PARAM_NAME, Arrays.asList(user));

        Transaction tx = cont.persistence().createTransaction();
        try {
            Map<String, Object> entityWrapper = doMultiLoad(dataSet, params);
            Object groupName = entityWrapper.get("group.name");
            assertNull(groupName);
            tx.commit();
        } finally {
            tx.end();
        }

        dataSet.setViewName("user.edit");
        dataSet.setUseExistingView(true);

        tx = cont.persistence().createTransaction();
        try {
            Map<String, Object> entityWrapper = doMultiLoad(dataSet, params);
            Object groupName = entityWrapper.get("group.name");
            assertNotNull(groupName);
            tx.commit();
        } finally {
            tx.end();
        }
    }

    protected Map<String, Object> doSingleLoad(DataSet dataSet, Map<String, Object> params) {
        BandData parentBand = new BandData(null);
        SingleEntityDataLoader singleEntityDataLoader = new SingleEntityDataLoader();
        List<Map<String, Object>> data = singleEntityDataLoader.loadData(dataSet, parentBand, params);
        assertEquals(1, data.size());
        return data.get(0);
    }

    protected Map<String, Object> doMultiLoad(DataSet dataSet, Map<String, Object> params) {
        BandData parentBand = new BandData(null);
        MultiEntityDataLoader multiEntityDataLoader = new MultiEntityDataLoader();
        List<Map<String, Object>> data = multiEntityDataLoader.loadData(dataSet, parentBand, params);
        assertEquals(1, data.size());
        return data.get(0);
    }
}
