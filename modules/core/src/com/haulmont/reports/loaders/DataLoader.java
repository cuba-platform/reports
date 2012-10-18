/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Eugeniy Degtyarjov
 * Created: 28.05.2010 17:11:57
 *
 * $Id$
 */
package com.haulmont.reports.loaders;

import com.haulmont.reports.entity.DataSet;
import com.haulmont.reports.entity.Band;

import java.util.List;
import java.util.Map;

/**
 * Interface to be implemented by loaders for {@link com.haulmont.reports.app.service.ReportService}
 *
 * @author artamonov
 * @version $id$
 */
public interface DataLoader {
    List<Map<String, Object>> loadData(DataSet dataSet, Band parentBand);
}