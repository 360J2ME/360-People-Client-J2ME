/*******************************************************************************
 * CDDL HEADER START
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * src/com/vodafone360/people/VODAFONE.LICENSE.txt or
 * http://github.com/360/360-Engine-for-Android
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 *  include the License file at src/com/vodafone360/people/VODAFONE.LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 * 
 * CDDL HEADER END
 * 
 * Copyright 2010 Vodafone Sales & Services Ltd.  All rights reserved.
 * Use is subject to license terms.
 ******************************************************************************/
package com.zyb.nowplus.business.sync;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.sync.impl.LightSyncEngine;
import com.zyb.nowplus.business.sync.storage.impl.contacts.PIMContactsOCDSConnector;
import com.zyb.nowplus.business.sync.storage.impl.contacts.ProfileContactsRDSConnector;
import com.zyb.nowplus.business.sync.storage.impl.contacts.ContactDataConvertor;
import com.zyb.nowplus.business.sync.domain.SyncConfig;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventListener;
import com.zyb.util.event.EventDispatcher;

import java.util.Vector;

/**
 * Management object for the Internal Sync Engine component
 * <p/>
 *
 * @author Andrei Marcut, andrei@zybmail.com
 */
public class SyncManager implements EventListener, Sync, Runnable
{
    public static final int SYNC_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;

    protected final Model services;

    protected SyncEngine engine;

    private volatile boolean running = false;

    private Thread thread;

    private final SyncStateSemaphore semaphore;

    /* Pause/Resume monitor */
    private final Object activityMonitor = new Object();

    /* Sync queue */
    private final Vector syncQueue = new Vector();

    public static final int CONTACTS = 0;

    /**
     * Public constructor
     */
    public SyncManager(Model services, EventDispatcher dispatcher)
    {
        if (services == null) {
            throw new IllegalArgumentException("Model is null.");
        }

        this.services = services;
        semaphore = new SyncStateSemaphore(services, dispatcher);
        services.attach(this);
    }

    /**
     * @see com.zyb.nowplus.business.sync.Sync#start()
     */
    public void start()
    {
        running = true;
        LightSyncEngine ctts = new LightSyncEngine(new PIMContactsOCDSConnector(semaphore), new ProfileContactsRDSConnector(services), semaphore, CONTACTS);
        services.attach(ctts);
        engine = ctts;

        thread = new Thread(this);
        thread.setPriority(SYNC_THREAD_PRIORITY);
        thread.start();
    }

    /**
     * @see com.zyb.nowplus.business.sync.Sync#stop()
     */
    public void stop()
    {
        running = false;
        cancelSync();

        if (thread != null && thread.isAlive()) {
            try {
                thread.join();
            }
        	catch (InterruptedException e) {
                //#debug error
            	System.out.println("Interrupted." + e);
            }
        }

        //#debug debug
        System.out.println("SyncManager destroyed.");
    }

    /**
     * Interrupts the current sync and releases queued syncs
     * Note: The sync progress is saved.
     */
    public void cancelSync()
    {
        //#debug debug
        System.out.println("#########################################################TRYING TO STOP THE SYNC #1");

        if (engine != null && semaphore.isRunning()) {
            //#debug debug
            System.out.println("#########################################################TRYING TO STOP THE CURRENT SYNC");

            semaphore.dispatch(Event.Sync.CANCELLING, null);
            engine.cancelSync();

            if (semaphore.isSuspended()) {
                semaphore.resume();
            }

            /* Release Wait or Sleep */
            thread.interrupt();
        }

        //#debug debug
        System.out.println("Cleaning-up queue");

        synchronized (syncQueue) {
            syncQueue.removeAllElements();
        }

        /* Notify the 'sync queue listener' */
        synchronized (activityMonitor) {
            activityMonitor.notify();
        }

        //#debug debug
        System.out.println("Cancelled");
    }

    /**
     * @see com.zyb.nowplus.business.sync.Sync#remap(Object input)
     */
    public boolean remap(Object input)
    {
        if (input instanceof ContactProfile) {
            return ContactDataConvertor.remap((ContactProfile) input);
        }

        return false;
    }

    /**
     * @see Sync#sync(int, int)
     */
    public void sync(int mode, int resolution)
    {
        SyncConfig cfg = new SyncConfig(CONTACTS, mode, resolution);

        //#debug debug
        System.out.println("Queueing sync " + cfg);

        /* Add to Queue*/
        synchronized (syncQueue) {
            syncQueue.addElement(cfg);
        }

        /* Cancel if continuous */
        if (engine != null && engine.getMode() == SyncEngine.SYNCMODE_EXTERNAL) {
            engine.cancelSync();
        }

        /* Notify the 'sync queue listener' */
        synchronized (activityMonitor) {
            activityMonitor.notify();
        }
    }

    public void run()
    {
        //#debug debug
        System.out.println("SyncManager starting.");

        boolean initialized = false;

        while (running) {
            SyncConfig sync;
            boolean ready;

            synchronized (syncQueue) {
                ready = !syncQueue.isEmpty();
            }

            if (!ready) {
                try {
                    synchronized (activityMonitor) {
                        //#debug debug
                        System.out.println("Waiting for a sync request...");

                        activityMonitor.wait();
                    }
                }
                catch (InterruptedException e) {
                    //#debug error
                	System.out.println("Interrupted. " + e);
                }

                continue;
            }
            else {
                synchronized (syncQueue) {
                    sync = (SyncConfig) syncQueue.firstElement();
                    syncQueue.removeElement(sync);
                    ready = syncQueue.isEmpty() || SyncEngine.SYNCMODE_EXTERNAL != sync.getMode();
                }

                if (!ready) {
                    continue;
                }
            }

            semaphore.setBusyWaitFlag(sync.getMode() != SyncEngine.SYNCMODE_UPDATE_FROM_ORIGINATOR_ONLY);
            semaphore.start();

            //#debug debug
            System.out.println("Starting sync from queue: " + sync);

            try {
                engine.setMode(sync.getMode());
                engine.setResolution(sync.getResolution());

                if (sync.getMode() != SyncEngine.SYNCMODE_UPDATE_FROM_ORIGINATOR_ONLY) {
                    //#debug debug
                    System.out.println("Dispatching Event.Sync.START");

                    semaphore.dispatch(Event.Sync.START, new int[]{sync.getResolution()});
                }

                if (!(initialized || semaphore.acquire() && (initialized = engine.init()))) {
                    //#debug warn
                    System.out.println("Unable to initialize due to unknown reasons. Continuing.");

                    semaphore.dispatch(Event.Sync.FAILED, "Init failure");
                    continue;
                }

                engine.sync();
            }
            catch (Throwable th) {
                //#debug fatal 
                System.out.println("General sync exception! " + th);

                //#debug debug
                System.out.println("Aborting current sync.");

                if (semaphore.isRunning()) {
                    engine.cancelSync();
                }
            }
            finally {
                synchronized (syncQueue) {
                    ready = !syncQueue.isEmpty();
                }

                if (!ready) {
                    //#debug debug
                    System.out.println("There are no queued syncs. Releasing resources.");

                    engine.uninit();
                    initialized = false;
                }
            }
        }

        engine = null;
    }


    /**
     * @see com.zyb.util.event.EventListener#getContext()
     */
    public byte getContext()
    {
        return Event.Context.APP;
    }

    /**
     * @see com.zyb.util.event.EventListener#handleEvent(byte, int, Object)
     */
    public void handleEvent(byte context, int event, Object data)
    {
        if (engine != null && Event.Context.APP == context)
            switch (event) {
                case Event.App.PAUSE:
                    semaphore.suspend();
                    break;

                case Event.App.RESUME:
                    semaphore.resume();
                    break;
            }
    }

    public boolean isInteractingWithSource()
    {
    	return engine != null && engine.isInteractingWithSource();
    }
    
	//#mdebug error
    public String toString()
    {
        return "SyncManager[]";
    }
	//#enddebug
}
