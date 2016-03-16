/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.wizard;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.reports.app.EntityTree;

/**
 */
public interface EntityTreeModelBuilderApi {
    String NAME = "report_EntityTreeModelBuilder";
    EntityTree buildEntityTree(MetaClass metaClass);
}

