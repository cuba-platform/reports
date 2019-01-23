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

package com.haulmont.reports.app;

import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.reports.testsupport.ReportsTestContainer;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

public class EntityMapTest {

    @ClassRule
    public static ReportsTestContainer cont = ReportsTestContainer.Common.INSTANCE;

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
