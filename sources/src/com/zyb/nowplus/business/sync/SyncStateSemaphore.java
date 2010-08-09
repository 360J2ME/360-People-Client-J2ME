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

import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.nowplus.business.Model;

public class SyncStateSemaphore
{
    private boolean running = false;
    private boolean paused = false;

    private final Object pauseMonitor = new Object();

    private final EventDispatcher dispatcher;

    public static final long BUZY_WAIT_TIMER = 250;

    private Model services;
    private boolean careAboutBusy = true;

    /**
     * Public constructor
     */
    public SyncStateSemaphore(Model services, EventDispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
        this.services = services;
    }

    /**
     * Suspends the sync ( if any sync is running or if a sync is following - unless resumed ).
     */
    public void suspend()
    {
        //#debug debug
        System.out.println("Executing Suspend Sync");

        paused = true;
        dispatch(Event.Sync.SUSPEND, null);
    }

    /**
     * Resumes the sync
     */
    public void resume()
    {
        //#debug debug
        System.out.println("Executing Resume Sync");

        paused = false;

        synchronized (pauseMonitor) {
            pauseMonitor.notify();
        }

        dispatch(Event.Sync.RESUME, null);
    }

    /**
     * @return suspended state
     */
    public boolean isSuspended()
    {
        return paused;
    }

    /**
     * @return running state
     */
    public boolean isRunning()
    {
        return running;
    }

    public void setBusyWaitFlag(boolean flag)
    {
        careAboutBusy = flag;
    }

    /**
     * Sleeps for some time depending of the sync speed set and Model activity.
     * Hangs and releases on Pause/Resume/Cancel
     *
     * @return true if the Sync is alive
     */
    public boolean acquire()
    {
        if (!paused && !running) {
            return false;
        }

//        if (delay > 0)
//            try {
//                //#debug debug
//                System.out.println("Intentional sleeping for " + delay + " ms");
//
//                Thread.sleep(delay);
//            } catch (InterruptedException e) {
//                //#debug debug
//                System.out.println("Interrupted.");
//
//                return acquire();
//            }

        //#mdebug debug
        if (services.isBusy()) {
            System.out.println("Model is buzy.");
        }
        //#enddebug

        /* Be a bit lazier if the Model is busy */
        while (running && careAboutBusy && services.isBusy()) {
            //#debug debug
            System.out.println("Buzy-Waiting " + BUZY_WAIT_TIMER + " ms");

            try {
                Thread.sleep(BUZY_WAIT_TIMER);
            }
            catch (InterruptedException e) {
                //#debug error
                System.out.println("Interrupted. " + e);

                return acquire();
            }
        }

        if (!paused) {
        	return running;
        }

        synchronized (pauseMonitor) {
            try {
                //#debug debug
                System.out.println("Pause-Wait");

                pauseMonitor.wait();
            }
            catch (InterruptedException e) {
                //#debug error
            	System.out.println("Interrupted. " + e);

            	return acquire();
            }
        }

        return running;
    }

    /**
     * Fires an Event.Sync event
     */
    public void dispatch(int id, Object data)
    {
        switch (id) {
            case Event.Sync.FAILED:
            case Event.Sync.CANCELLED:
            case Event.Sync.CANCELLING:                
            case Event.Sync.SUCCESSFULL:
                running = false;
                break;

            case Event.Sync.SYNCING:
                break;
        }

        if (dispatcher != null) {
            dispatcher.notifyEvent(Event.Context.SYNC, id, data);
        }
    }

    public void start()
    {
        running = true;
    }
}
