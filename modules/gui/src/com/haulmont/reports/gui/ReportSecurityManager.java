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

package com.haulmont.reports.gui;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.QueryTransformer;
import com.haulmont.cuba.core.global.QueryTransformerFactory;
import com.haulmont.cuba.core.global.QueryUtils;
import com.haulmont.cuba.security.entity.*;
import com.haulmont.cuba.security.role.*;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

@Component("cuba_ReportSecurityManager")
public class ReportSecurityManager {
    @Inject
    protected QueryTransformerFactory queryTransformerFactory;
    @Inject
    protected RolesService rolesService;
    @Inject
    protected ReportingClientConfig reportingClientConfig;

    /**
     * Apply security constraints for query to select reports available by roles and screen restrictions
     */
    public void applySecurityPolicies(LoadContext lc, @Nullable String screen, @Nullable User user) {
        QueryTransformer transformer = queryTransformerFactory.transformer(lc.getQuery().getQueryString());
        if (screen != null) {
            transformer.addWhereAsIs("r.screensIdx like :screen escape '\\'");
            lc.getQuery().setParameter("screen", wrapIdxParameterForSearch(screen));
        }
        if (user != null) {
            List<UserRole> userRoles = user.getUserRoles();
            boolean superRole = false;
            if (reportingClientConfig.getAllReportsAvailableForAdmin()) {
                Collection<Role> roles = rolesService.getRolesForUser(user);
                superRole = roles.stream().anyMatch(this::isSuperRole);
            }
            if (!superRole) {
                StringBuilder roleCondition = new StringBuilder("r.rolesIdx is null");
                for (int i = 0; i < userRoles.size(); i++) {
                    UserRole ur = userRoles.get(i);
                    String paramName = "role" + (i + 1);
                    roleCondition.append(" or r.rolesIdx like :").append(paramName).append(" escape '\\'");
                    if (ur.getRole() != null) {
                        lc.getQuery().setParameter(paramName, wrapIdxParameterForSearch(ur.getRole().getId().toString()));
                    } else {
                        lc.getQuery().setParameter(paramName, wrapIdxParameterForSearch(ur.getRoleName()));
                    }
                }
                transformer.addWhereAsIs(roleCondition.toString());
            }
        }
        lc.getQuery().setQueryString(transformer.getResult());
    }

    /**
     * Apply constraints for query to select reports which have input parameter with class matching inputValueMetaClass
     */
    public void applyPoliciesByEntityParameters(LoadContext lc, @Nullable MetaClass inputValueMetaClass) {
        if (inputValueMetaClass != null) {
            QueryTransformer transformer = queryTransformerFactory.transformer(lc.getQuery().getQueryString());
            StringBuilder parameterTypeCondition = new StringBuilder("r.inputEntityTypesIdx like :type escape '\\'");
            lc.getQuery().setParameter("type", wrapIdxParameterForSearch(inputValueMetaClass.getName()));
            List<MetaClass> ancestors = inputValueMetaClass.getAncestors();
            for (int i = 0; i < ancestors.size(); i++) {
                MetaClass metaClass = ancestors.get(i);
                String paramName = "type" + (i + 1);
                parameterTypeCondition.append(" or r.inputEntityTypesIdx like :").append(paramName).append(" escape '\\'");
                lc.getQuery().setParameter(paramName, wrapIdxParameterForSearch(metaClass.getName()));
            }
            transformer.addWhereAsIs(String.format("(%s)", parameterTypeCondition.toString()));
            lc.getQuery().setQueryString(transformer.getResult());
        }
    }

    protected String wrapIdxParameterForSearch(String value) {
        return "%," + QueryUtils.escapeForLike(value) + ",%";
    }

    protected boolean isSuperRole(Role role) {
        return role.getDefaultEntityCreateAccess() == Access.ALLOW
                && role.getDefaultEntityReadAccess() == Access.ALLOW
                && role.getDefaultEntityUpdateAccess() == Access.ALLOW
                && role.getDefaultEntityDeleteAccess() == Access.ALLOW
                && role.getDefaultEntityAttributeAccess() == EntityAttrAccess.MODIFY
                && role.getDefaultScreenAccess() == Access.ALLOW
                && role.getDefaultSpecificAccess() == Access.ALLOW;
    }
}
