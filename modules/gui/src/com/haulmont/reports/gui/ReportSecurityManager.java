/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.gui;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.QueryTransformer;
import com.haulmont.cuba.core.global.QueryTransformerFactory;
import com.haulmont.cuba.core.global.QueryUtils;
import com.haulmont.cuba.security.entity.RoleType;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.UserRole;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

@Component("cuba_ReportSecurityManager")
public class ReportSecurityManager {
    @Inject
    protected QueryTransformerFactory queryTransformerFactory;

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
            boolean superRole = userRoles.stream().anyMatch(userRole -> userRole.getRole().getType() == RoleType.SUPER);
            if (!superRole) {
                StringBuilder roleCondition = new StringBuilder("r.rolesIdx is null");
                for (int i = 0; i < userRoles.size(); i++) {
                    UserRole ur = userRoles.get(i);
                    String paramName = "role" + (i + 1);
                    roleCondition.append(" or r.rolesIdx like :").append(paramName).append(" escape '\\'");
                    lc.getQuery().setParameter(paramName, wrapIdxParameterForSearch(ur.getRole().getId().toString()));
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
}
