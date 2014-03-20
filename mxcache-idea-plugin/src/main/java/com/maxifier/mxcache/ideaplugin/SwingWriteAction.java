/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin;

import com.intellij.openapi.application.ApplicationManager;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
class SwingWriteAction implements Runnable {
    private final Runnable action;

    public SwingWriteAction(Runnable action) {
        this.action = action;
    }

    @Override
    public void run() {
        ApplicationManager.getApplication().runWriteAction(action);
    }
}
