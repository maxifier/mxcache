package com.maxifier.mxcache.legacy;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Project: Smart Advertising
 * Created by: Yakoushin Andrey
 * Date: 10.12.2008
 * Time: 13:38:30
 * <p/>
 * Copyright (c) 1999-2008 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */

public class MxCacheFlusher extends Thread {
    private static final long SLEEP_TIME = 2000;

    @SuppressWarnings({"CollectionWithoutInitialCapacity"})
    private final List<WeakReference<MxStateHandler>> managers = new ArrayList<WeakReference<MxStateHandler>>();

    public MxCacheFlusher() {
        super("MxCacheFlusher");
        setDaemon(true);
    }

    public void startWork() {
        start();
    }

    public void stopWork() {
        interrupt();
    }

    public synchronized void registerHandler(MxStateHandler manager) {
        managers.add(new WeakReference<MxStateHandler>(manager));
    }

    @Override
    public void run() {
        while (!interrupted()) {
            try {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    return;
                }
                synchronized (this) {
                    for (Iterator<WeakReference<MxStateHandler>> it = managers.iterator(); it.hasNext();) {
                        MxStateHandler h = it.next().get();
                        if (h == null) {
                            it.remove();
                        } else {
                            h.stateHandler();
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
