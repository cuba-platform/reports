/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.reports.core.ReportsTestCase;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.yarg.structure.BandData;

import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class EntityDataLoaderTest extends ReportsTestCase {
    private User user;
    private Group group;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Transaction tx = persistence.createTransaction();
        try {
            user = new User();
            user.setLogin("test");
            user.setLoginLowerCase("test");
            user.setPassword("test");

            group = new Group();
            group.setName("test");
            user.setGroup(group);

            persistence.getEntityManager().persist(group);
            persistence.getEntityManager().persist(user);

            tx.commit();
        } finally {
            tx.end();
        }

        user.setGroup(null);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        Transaction tx = persistence.createTransaction();
        try {
            persistence.getEntityManager()
                    .createNativeQuery("delete from sec_user where id = ?1")
                    .setParameter(1, user.getId())
                    .executeUpdate();
            persistence.getEntityManager()
                    .createNativeQuery("delete from sec_group where id = ?1")
                    .setParameter(1, group.getId())
                    .executeUpdate();
            tx.commit();
        } finally {
            tx.end();
        }
    }

    public void testSingleEntityLoad() throws Exception {
        DataSet dataSet = new DataSet();
        Map<String, Object> params = new HashMap<>();
        params.put(SingleEntityDataLoader.DEFAULT_ENTITY_PARAM_NAME, user);

        Transaction tx = persistence.createTransaction();
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

        tx = persistence.createTransaction();
        try {
            Map<String, Object> entityWrapper = doSingleLoad(dataSet, params);
            Object groupName = entityWrapper.get("group.name");
            assertNotNull(groupName);
            tx.commit();
        } finally {
            tx.end();
        }
    }

    public void testMultiEntityLoad() throws Exception {
        DataSet dataSet = new DataSet();
        Map<String, Object> params = new HashMap<>();
        params.put(MultiEntityDataLoader.DEFAULT_LIST_ENTITIES_PARAM_NAME, Arrays.asList(user));

        Transaction tx = persistence.createTransaction();
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

        tx = persistence.createTransaction();
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
