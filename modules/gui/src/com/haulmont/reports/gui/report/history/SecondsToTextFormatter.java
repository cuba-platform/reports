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

package com.haulmont.reports.gui.report.history;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Formats period in form of: {hours} h {minutes} min {seconds} sec. Zero most significant values are not displayed: e.g
 * '13 h 45 min 38 sec', '45 min 38 sec', '38 sec'
 */
public class SecondsToTextFormatter implements Function<Long, String> {

    private Messages messages;

    public SecondsToTextFormatter() {
        messages = AppBeans.get(Messages.NAME);
    }

    @Override
    public String apply(Long value) {
        if (value == null) {
            return null;
        }

        long hours = TimeUnit.SECONDS.toHours(value);
        long minutes = TimeUnit.SECONDS.toMinutes(value) % 60;
        long seconds = value % 60;

        if (hours != 0) {
            return messages.formatMessage(getClass(), "duration.format.withHours", hours, minutes, seconds);
        } else if (minutes != 0) {
            return messages.formatMessage(getClass(), "duration.format.withMinutes", minutes, seconds);
        } else {
            return messages.formatMessage(getClass(), "duration.format.onlySeconds", seconds);
        }
    }
}
