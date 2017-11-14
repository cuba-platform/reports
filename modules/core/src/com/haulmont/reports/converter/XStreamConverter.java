/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.converter;

import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.global.ViewProperty;
import com.haulmont.cuba.core.sys.CubaXStream;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.reports.entity.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.SerializableConverter;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import java.util.*;

public class XStreamConverter {
    protected XStream createXStream() {
        XStream xStream = new CubaXStream(Collections.singletonList(SerializableConverter.class)) {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @Override
                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        return super.shouldSerializeMember(definedIn, fieldName);
                    }

                    @Override
                    public Class realClass(String elementName) {
                        return super.realClass(elementName);
                    }
                };
            }
        };
        xStream.registerConverter(new CollectionConverter(xStream.getMapper()) {
            @Override
            public boolean canConvert(Class type) {
                return ArrayList.class.isAssignableFrom(type) ||
                        HashSet.class.isAssignableFrom(type) ||
                        LinkedList.class.isAssignableFrom(type) ||
                        LinkedHashSet.class.isAssignableFrom(type);

            }
        }, XStream.PRIORITY_VERY_HIGH);

        xStream.registerConverter(new DateConverter() {
            @Override
            public boolean canConvert(Class type) {
                return Date.class.isAssignableFrom(type);
            }
        });

        xStream.alias("report", Report.class);
        xStream.alias("band", BandDefinition.class);
        xStream.alias("dataSet", DataSet.class);
        xStream.alias("parameter", ReportInputParameter.class);
        xStream.alias("template", ReportTemplate.class);
        xStream.alias("screen", ReportScreen.class);
        xStream.alias("format", ReportValueFormat.class);
        xStream.addDefaultImplementation(LinkedHashMap.class, Map.class);
        xStream.aliasSystemAttribute(null, "class");

        xStream.omitField(Report.class, "xml");
        xStream.omitField(Report.class, "deleteTs");
        xStream.omitField(Report.class, "deletedBy");
        xStream.omitField(Report.class, "detached");
        xStream.omitField(ReportTemplate.class, "content");
        xStream.omitField(ReportTemplate.class, "defaultFlag");
        xStream.omitField(ReportTemplate.class, "templateFileDescriptor");
        xStream.omitField(ReportTemplate.class, "deleteTs");
        xStream.omitField(ReportTemplate.class, "deletedBy");
        xStream.omitField(ReportTemplate.class, "detached");
        xStream.omitField(ReportInputParameter.class, "localeName");
        xStream.omitField(ReportGroup.class, "detached");
        xStream.omitField(View.class, "includeSystemProperties");
        xStream.omitField(ViewProperty.class, "lazy");
        xStream.omitField(Role.class, "detached");
        xStream.omitField(ReportScreen.class, "detached");

        xStream.aliasField("customFlag", ReportTemplate.class, "custom");
        xStream.aliasField("customClass", ReportTemplate.class, "customDefinition");
        xStream.aliasField("uuid", BandDefinition.class, "id");
        xStream.aliasField("uuid", DataSet.class, "id");
        xStream.aliasField("uuid", ReportInputParameter.class, "id");
        xStream.aliasField("uuid", ReportScreen.class, "id");
        xStream.aliasField("definedBy", ReportTemplate.class, "customDefinedBy");
        xStream.aliasField("uuid", ReportValueFormat.class, "id");

        return xStream;
    }

    public String convertToString(Report report) {
        XStream xStream = createXStream();
        //noinspection UnnecessaryLocalVariable
        String xml = xStream.toXML(report);
        return xml;
    }

    public Report convertToReport(String xml) {
        XStream xStream = createXStream();
        return (Report) xStream.fromXML(xml);
    }
}
