package com.maxifier.mxcache.ideaplugin;

import com.intellij.openapi.application.ApplicationManager;

/**
 * Created by IntelliJ IDEA.
* User: dalex
* Date: 01.05.2010
* Time: 18:53:47
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
