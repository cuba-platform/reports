/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.app;

import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.testsupport.TestContainer;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportTemplate;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 */
public class EntityMapTest {

    @ClassRule
    public static TestContainer cont = new TestContainer();

    @Test
    public void testGetValue() throws Exception {
        User user = new User();
        user.setName("The User");
        user.setLogin("admin");
        Group group = new Group();
        group.setName("The Group");
        user.setGroup(group);

        EntityMap entityMap = new EntityMap(user);

        Assert.assertEquals("admin", entityMap.get("login"));
        Assert.assertEquals("The User", entityMap.get("name"));
        Assert.assertEquals("The User [admin]", entityMap.get(EntityMap.INSTANCE_NAME_KEY));
        Assert.assertEquals("The Group", entityMap.get("group." + EntityMap.INSTANCE_NAME_KEY));
        Assert.assertNull(entityMap.get("group...." + EntityMap.INSTANCE_NAME_KEY));
    }
}
