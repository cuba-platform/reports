/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.wizard.step;

import com.haulmont.cuba.gui.components.IFrame;

import java.util.List;

/**
 * @author fedorchenko
 * @version $Id$
 */
public class StepFrameManager {
    protected final List<StepFrame> stepFrames;
    private MainWizardFrame mainWizardFrame;
    private int currentFrameIdx = 0;

    public StepFrameManager(MainWizardFrame reportWizardCreatorFrame, List<StepFrame> stepFrames) {
        this.mainWizardFrame = reportWizardCreatorFrame;
        this.stepFrames = stepFrames;
        //frames initialization is in showCurrentFrame() method
    }

    public List<StepFrame> getStepFrames() {
        return stepFrames;
    }

    public void showCurrentFrame() {
        setMainWindowProps();
        getCurrentStepFrame().initFrame();
        getCurrentStepFrame().beforeShow();
        getCurrentStepFrame().getFrame().setVisible(true);
    }

    protected StepFrame getCurrentStepFrame() {
        return stepFrames.get(currentFrameIdx);
    }

    public IFrame getCurrentIFrame() {
        return stepFrames.get(currentFrameIdx).getFrame();
    }

    public void setMainWindowProps() {
        String newWindowCaption = getCurrentStepFrame().getName() + " " + mainWizardFrame.formatMessage("stepNo", currentFrameIdx + 1, stepFrames.size());
        mainWizardFrame.getMainEditorFrame().getWrappedWindow().getWindowManager().setWindowCaption(mainWizardFrame.getMainEditorFrame().getWrappedWindow(), newWindowCaption, "");
        setNavigationButtonProps();
    }

    protected void setNavigationButtonProps() {
        if (getCurrentStepFrame().isLast()) {
            mainWizardFrame.getForwardBtn().setVisible(false);
        } else if (currentFrameIdx + 1 >= stepFrames.size()) {
            mainWizardFrame.getForwardBtn().setEnabled(false);
        } else {
            mainWizardFrame.getForwardBtn().setVisible(true);
            mainWizardFrame.getForwardBtn().setEnabled(true);
        }

        if (getCurrentStepFrame().isFirst()) {
            mainWizardFrame.getBackwardBtn().setVisible(false);
        } else if (currentFrameIdx - 1 < 0) {
            mainWizardFrame.getBackwardBtn().setEnabled(false);
        } else {
            mainWizardFrame.getBackwardBtn().setVisible(true);
            mainWizardFrame.getBackwardBtn().setEnabled(true);
        }
        mainWizardFrame.removeBtns();
        if (mainWizardFrame.getBackwardBtn().isVisible())
            mainWizardFrame.addBackwardBtn();
        if (mainWizardFrame.getForwardBtn().isVisible())
            mainWizardFrame.addForwardBtn();
        mainWizardFrame.addSaveBtn();
    }

    public boolean prevFrame() {
        if (currentFrameIdx == 0) {
            throw new ArrayIndexOutOfBoundsException("Previous frame is not exists");
        }
        if (!getCurrentStepFrame().isValidateBeforePrev() || validateCurrentFrame()) {
            hideCurrentFrame();
            currentFrameIdx--;
            showCurrentFrame();
            return true;
        } else {
            return false;
        }
    }

    public boolean nextFrame() {
        if (currentFrameIdx > stepFrames.size()) {
            throw new ArrayIndexOutOfBoundsException("Next frame is not exists");
        }
        if (!getCurrentStepFrame().isValidateBeforeNext() || validateCurrentFrame()) {
            hideCurrentFrame();
            currentFrameIdx++;
            showCurrentFrame();
            return true;
        } else {
            return false;
        }
    }

    protected boolean validateCurrentFrame() {
        List<String> validationErrors = getCurrentStepFrame().validateFrame();
        if (!validationErrors.isEmpty()) {
            mainWizardFrame.getMainEditorFrame().showNotification(org.springframework.util.StringUtils.arrayToDelimitedString(validationErrors.toArray(), "<br/>"), IFrame.NotificationType.TRAY_HTML);
            return false;
        }
        return true;
    }

    protected void hideCurrentFrame() {
        getCurrentStepFrame().beforeHide();
        getCurrentStepFrame().getFrame().setVisible(false);
    }
}
