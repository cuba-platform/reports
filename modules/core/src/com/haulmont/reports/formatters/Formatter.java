/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.reports.formatters;

import com.haulmont.reports.entity.Band;

/**
 * @author degtyarjov
 * @version $Id$
 */
public interface Formatter {

    byte[] createDocument(Band rootBand);
}
