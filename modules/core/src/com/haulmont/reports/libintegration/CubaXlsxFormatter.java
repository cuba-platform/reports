/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.XlsxFormatter;
import org.xlsx4j.sml.Cell;

public class CubaXlsxFormatter extends XlsxFormatter {
    public CubaXlsxFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
    }

    @Override
    protected Cell copyCell(Cell cell) {
        Object parent = cell.getParent();
        try {
            cell.setParent(null);
            return (Cell) SerializationSupport.getKryoSerialization().copy(cell);
        } finally {
            cell.setParent(parent);
        }
    }
}
