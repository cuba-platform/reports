/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

/**
 *
 */
package com.haulmont.reports.libintegration;

import com.haulmont.cuba.core.app.FileStorageAPI;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.yarg.exception.ReportFormattingException;
import com.haulmont.yarg.formatters.impl.inline.AbstractInliner;

import java.util.UUID;
import java.util.regex.Pattern;

public class FileStorageContentInliner extends AbstractInliner {
    private final static String REGULAR_EXPRESSION = "\\$\\{imageFileId:([0-9]+?)x([0-9]+?)\\}";

    public FileStorageContentInliner() {
        tagPattern = Pattern.compile(REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Pattern getTagPattern() {
        return tagPattern;
    }

    @Override
    protected byte[] getContent(Object paramValue) {
        try {
            DataManager dataManager = AppBeans.get(DataManager.class);
            FileStorageAPI fileStorageAPI = AppBeans.get(FileStorageAPI.class);

            FileDescriptor file = dataManager.load(new LoadContext<>(FileDescriptor.class).setId(UUID.fromString(paramValue.toString())));
            byte[] bytes = fileStorageAPI.loadFile(file);
            return bytes;
        } catch (FileStorageException e) {
            throw new ReportFormattingException(String.format("Unable to get image from file storage. File id [%s]", paramValue), e);
        }
    }
}
