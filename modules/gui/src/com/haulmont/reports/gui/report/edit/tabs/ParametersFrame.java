package com.haulmont.reports.gui.report.edit.tabs;

import com.haulmont.cuba.gui.components.AbstractFrame;

public class ParametersFrame extends AbstractFrame {

    public void getCrossFieldValidationScriptHelp() {
        showMessageDialog(getMessage("validationScript"), getMessage("crossFieldValidationScriptHelp"),
                MessageType.CONFIRMATION_HTML
                        .modal(false)
                        .width(600));
    }
}
