/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.reports.gui.datasource;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.data.impl.CollectionPropertyDatasourceImpl;
import com.haulmont.reports.entity.BandDefinition;
import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.Report;

import java.util.*;

public class DatasetsDatasource extends CollectionPropertyDatasourceImpl<DataSet, UUID> {

    public void committed(Set<Entity> entities) {
        if (!State.VALID.equals(masterDs.getState()))
            return;
        Collection<DataSet> collection = __getCollection();
        if (collection != null) {
            for (Entity entity : entities) {
                if (entity instanceof Report) {
                    for (BandDefinition definition : ((Report) entity).getBands()) {
                        if (definition.equals(masterDs.getItem())) {
                            for (DataSet dataset : definition.getDataSets()) {
                                for (DataSet item : new ArrayList<>(collection)) {
                                    if (item.equals(dataset)) {
                                        if (collection instanceof List) {
                                            List list = (List) collection;
                                            list.set(list.indexOf(item), dataset);
                                        } else if (collection instanceof Set) {
                                            Set set = (Set) collection;
                                            set.remove(item);
                                            set.add(dataset);
                                        }

                                        attachListener(dataset);
                                        if (dataset.equals(this.item)) {
                                            this.item = dataset;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        modified = false;
        clearCommitLists();
    }
}
