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
package com.zyb.nowplus.business.sync.impl;

import com.zyb.nowplus.business.sync.SyncEngine;
import com.zyb.nowplus.business.sync.SyncStateSemaphore;
import com.zyb.nowplus.business.sync.domain.SyncResults;
import com.zyb.nowplus.business.sync.domain.conflicts.SyncConflictPrompter;
import com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector;
import com.zyb.nowplus.business.sync.storage.RecipientDataStorageConnector;
import com.zyb.nowplus.business.sync.storage.exception.SyncDataStorageFullException;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventListener;
import com.zyb.util.ArrayUtils;
import com.zyb.util.HashUtil;

import java.io.IOException;
import java.util.Vector;

/**
 * Sync business logic
 *
 * @author Andrei Marcut, andrei@zybmail.com
 */
public class LightSyncEngine implements EventListener, SyncEngine
{
	public static final String STORE = "SYNCHIST_";
	
    private int syncMode;
    private int conflictResolution;

    private final SyncHistory history;
    private final OriginatorDataStorageConnector originator;
    private final RecipientDataStorageConnector recipient;

    private SyncResults results = new SyncResults();

    private final SyncStateSemaphore semaphore;
    private final int section;

    private final Vector externalSyncQueue = new Vector();
    private final Object activityMonitor = new Object();

    private SyncConflictPrompter prompter = null;

    /**
     * LightSyncEngine constructor
     *
     * @param source    OriginatorDataStorageConnector
     * @param target    RecipientDataStorageConnector
     * @param semaphore SyncStateSemaphore
     * @param section   sync section as int value
     */
    public LightSyncEngine(OriginatorDataStorageConnector source, RecipientDataStorageConnector target, SyncStateSemaphore semaphore, int section)
    {
        if (source == null) {
            throw new IllegalArgumentException("Null originator");
        }

        if (target == null) {
            throw new IllegalArgumentException("Null recipient");
        }

        this.section = section;
        this.semaphore = semaphore;
        originator = source;
        recipient = target;
        history = new SyncHistory(STORE + section);
    }

    /**
     * @see com.zyb.nowplus.business.sync.SyncEngine#cancelSync()
     */
    public void cancelSync()
    {
        //#debug debug
        System.out.println("Executing Cancel Sync");

        if (history != null) {
        	history.cancel();
        }
        
        synchronized (externalSyncQueue) {
            externalSyncQueue.removeAllElements();
        }

        synchronized (activityMonitor) {
            activityMonitor.notify();
        }

        semaphore.dispatch(Event.Sync.CANCELLED, null);
    }

    /**
     * @see com.zyb.nowplus.business.sync.SyncEngine#setMode(int mode)
     */
    public void setMode(int mode)
    {
        if (mode < SYNCMODE_UPDATE_BOTH || mode > SYNCMODE_EXTERNAL) {
            throw new IllegalArgumentException("Unsupported Sync Mode " + mode);
        }

        syncMode = mode;
    }

    /**
     * Get the sync mode representing the sync direction
     * e.g. from originator/recipient/external...
     *
     * @return sync mode
     */
    public int getMode()
    {
        return syncMode;
    }

    /**
     * Set the conflict resolutions used for two-way sync
     *
     * @param resolution
     */
    public void setResolution(int resolution)
    {
        if (resolution < CONFLICT_ORIGINATOR_WINS || resolution > CONFLICT_USER_PROMPTS) {
            throw new IllegalArgumentException("Unsupported Conflict Resolution " + resolution);
        }

        conflictResolution = resolution;
    }

    /**
     * Get the conflict resolutions used for two-way sync
     *
     * @return resolutions
     */
    public int getResolution()
    {
        return conflictResolution;
    }

    public void setPrompter(SyncConflictPrompter prompter)
    {
        this.prompter = prompter;
    }    

    /**
     * Get the sync section
     * e.g. CONTACTS
     *
     * @return section sync section
     */
    public int getSection()
    {
        return section;
    }

    /**
     * @see com.zyb.nowplus.business.sync.SyncEngine#sync()
     */
    public void sync()
	{
		if (!semaphore.acquire()) {
			return;
		}

		if (conflictResolution == CONFLICT_USER_PROMPTS && prompter == null) {
			throw new IllegalArgumentException("Conflict Resolution set on User Prompt, but no prompter was provided.");
		}
		
		try {
			onSyncBegin();
			
			/* Invoke the sync only if not an external sync */
			if (syncMode != SYNCMODE_EXTERNAL && semaphore.acquire()) {
				doSync();
			}
			else {
				doListen();
			}
		}
		catch (IOException ioe) {
			//#debug error
			System.out.println("Sync threw an exception: " + ioe);

			//#debug warn
			ioe.printStackTrace();

			failedSync("Unable to prepare for sync.");
		}
		catch (Exception e) {
			//#debug error
			System.out.println("Sync threw an exception: " + e);

			//#debug error
			e.printStackTrace();
		}
		finally
		{
			onSyncEnd();

			if (semaphore.acquire()) {
				syncSuccessfull();
			}
		}
	}

    private void onSyncBegin() throws IOException
    {
        results.reset();

        if (syncMode != SYNCMODE_EXTERNAL) {
            originator.reset();
        }
    }

    private void doListen()
    {
    	//#debug debug
    	System.out.println("doListen");

        do {
            //#debug debug
            System.out.println("Listening for a change event...");

            boolean ready;

            synchronized (externalSyncQueue) {
                ready = !externalSyncQueue.isEmpty();
            }

            if (!ready) {
                try {
                    synchronized (activityMonitor) {
                        activityMonitor.wait();
                    }
                }
                catch (InterruptedException e) {
                    //#debug warn
                    System.out.println("Listen Interrupted " + e);

                    //#debug debug
                    e.printStackTrace();
                }
            }
            else {
                if (!semaphore.acquire()) {
                	return;
                }

                Object externalChange;

                synchronized (externalSyncQueue) {
                    externalChange = externalSyncQueue.firstElement();
                    externalSyncQueue.removeElement(externalChange);
                }

                SyncItem aChange = history.get(((Long) externalChange).longValue());
                aChange.setRecipientRevision(recipient.getRevision(aChange.getRecipientUID()));
                detectSoftChangeAndProcess(aChange);
            }
        }
        while (semaphore.isRunning());
    }

    private void failedSync(Object message)
    {
        //#debug fatal
        System.out.println("Sync failed due to : " + message);

        semaphore.dispatch(Event.Sync.FAILED, message);
    }

    public boolean init()
    {
        //semaphore.dispatch(Event.Sync.INIT, null);
    	
        //#debug debug
        System.out.println("Initializing Sync");

        //#debug debug
        System.out.println("Opening History");

        try {
            history.open();
        }
        catch (IOException e) {
            //#debug fatal
            e.printStackTrace();

            failedSync("Could not open history persistence. Aborting.");
            return false;
        }

        if (!semaphore.acquire()) {
        	return false;
        }

        //#debug debug
        System.out.println("Opening Originator");

        try {
            originator.open();
        }
        catch (IOException e) {
            try {
                originator.close();
            }
            catch (IOException e1) {
                //#debug debug
                e1.printStackTrace();
            }

            failedSync("Could not open Originator Data Storage.");

            //#debug fatal
            e.printStackTrace();

            return false;
        }

        if (!semaphore.acquire()) {
        	return false;
        }

        //#debug debug
        System.out.println("Opening Recipient");

        try {
            recipient.open();
        }
        catch (IOException e) {
            try {
                recipient.close();
            }
            catch (IOException e1) {
                //#debug debug
                e1.printStackTrace();
            }
            //#debug fatal
            System.out.println("Could not open Recipient Data Storage");

            failedSync("Could not open Recipient Data Storage. " + e);
            //#debug debug
            e.printStackTrace();
        }

        return semaphore.acquire();
    }

    private void doSync()
    {
    	//#debug debug
    	System.out.println("doSync");

        semaphore.dispatch(Event.Sync.SYNCING, null);
        long[] all = recipient.allIds();

        //#mdebug debug
        System.out.println("LightSyncEngine.doSync()# Starting " + resolveCurrentSync());
        System.out.println("Starting lazy change detection");
        System.out.println("Gathering Recipient meta data");
        System.out.println("Found " + all.length + " items");
        //#enddebug

        if (!semaphore.acquire()) {
            return;
        }

        /* OUT_ADD | OUT_MOD | IN_MOD | IN_DEL */
        while (originator.hasMoreElements()) {
            if (!semaphore.acquire()) {
            	return;
            }

            originator.nextElement();
            // semaphore.dispatch(Event.Sync.DETECTING_CHANGE, originator.getElementShortDescription());

            //#debug debug
            System.out.println("Processing next item");

            if (!semaphore.acquire()) {
            	return;
            }

            SyncItem aChange = history.get(originator.getElementId());

            //#debug debug
            System.out.println("Calculating revision [uid= " + originator.getElementId() + "]");

            aChange.setOriginatorRevision(originator.getElementRevision());

            if (aChange.originatorRevision == -1) {
            	throw new RuntimeException("Broken Originator item for " + aChange);
            }

            aChange.setRecipientRevision(recipient.getRevision(aChange.getRecipientUID()));

            aChange.setSourceDesc(originator.getElementShortDescription());
            aChange.setTargetDesc(recipient.getShortDescription(aChange.getRecipientUID()));

            //#debug debug
            System.out.println("Detecting change for " + aChange);

            detectCurrentChangeAndProcess(aChange);

            if (aChange.recipientUID > 0) {
                /* Mark as 'synced' so we don't process this item again. */
                int ridx = ArrayUtils.firstIndexOf(aChange.recipientUID, all);

                if (ridx > -1) {
                    all[ridx] = -1;
                }
            }
        }

        if (!semaphore.acquire()) {
        	return;
        }

        /*
         * At this point, the Originator storage is iterated, which means the History
         * cache is up-to-date with Originator revisions and the pairs.
         * It's time to detect the offline changes
         */

        //#debug debug
        System.out.println("Detecting soft changes");

        /* IN_ADD | OUT_DEL */
        for (int i = 0; i < all.length; i++) {
            if (all[i] > 0) {
                if (!semaphore.acquire()) {
                	return;
                }

                SyncItem aChange = history.get(all[i]);
                aChange.setRecipientRevision(recipient.getRevision(aChange.getRecipientUID()));
                aChange.setSourceDesc(null);
                aChange.setTargetDesc(recipient.getShortDescription(aChange.getRecipientUID()));
                detectSoftChangeAndProcess(aChange);
            }
        }

        if (!semaphore.acquire()) {
        	return;
        }

        results.deletes += history.procesDeletes();
    }

    private void detectSoftChangeAndProcess(SyncItem aChange)
    {
    	if (!HashUtil.equals(aChange.getOriginatorUID(), originator.getElementId())) {
    		originator.loadElement(aChange.getOriginatorUID());
    	}
    	
        //#debug debug
        System.out.println("detectSoftChangeAndProcess(" + aChange + ")");

        switch (syncMode) {
            case SyncEngine.SYNCMODE_UPDATE_FROM_ORIGINATOR_ONLY:
                /*
                 * Changes: OUT_DEL
                 */

                if (aChange != null
                        && -1 == aChange.originatorRevision
                        && -1 != aChange.originatorSyncRevision
                        && -1 != aChange.recipientSyncRevision) {
                    if (aChange.recipientRevision == -1) {
                        results.deletes++;
                        history.deleted(aChange);
                    }
                    else if (prompter == null || prompter.confirm(aChange, Event.Sync.OUT_DEL)) {
                    	processOutgoingDel(aChange);
                    }
                }
                break;

            case SyncEngine.SYNCMODE_EXTERNAL:
                /*
                 * Changes: IN_ADD
                 */

            	if (-1 == aChange.recipientSyncRevision) {
            		if (null == aChange.originatorUID
                        && -1 != aChange.recipientRevision) {
            			processIncomingAdd(aChange);
            		}
            	}
            	else if (-1 == aChange.recipientRevision) {
            		processIncomingDel(aChange);
                }
            	else if (aChange.recipientRevision != aChange.recipientSyncRevision) {
            		processIncomingMod(aChange);
                }    	
                break;

            case SyncEngine.SYNCMODE_UPDATE_FROM_RECIPIENT_ONLY:
                if (-1 == aChange.originatorRevision && -1 != aChange.recipientRevision) {
                    processIncomingAdd(aChange);
                }
                break;

            case SyncEngine.SYNCMODE_UPDATE_BOTH:
                /*
                 * Changes: OUT_DEL, IN_ADD
                 */

                if (-1 == aChange.recipientSyncRevision) {
                    if (-1 != aChange.recipientRevision)
                        processIncomingAdd(aChange);
                }
                else { /* OUT_DEL |? IN_MOD */
                    switch (conflictResolution) {
                        case CONFLICT_ORIGINATOR_WINS:
                            if (-1 == aChange.originatorRevision
                                && aChange.originatorRevision != aChange.originatorSyncRevision) {
                                processOutgoingDel(aChange);
                            }
                            break;

                        case CONFLICT_RECIPIENT_WINS:
                            if (-1 == aChange.originatorRevision
                                    && aChange.originatorRevision != aChange.originatorSyncRevision) {
                                if (aChange.recipientSyncRevision != aChange.recipientRevision) {
                                    /* Win over Delete */
                                    processIncomingAdd(aChange);
                                }
                                else if (aChange.recipientRevision == -1) {
                                	results.deletes++;
                                }
                                else {
                                	processOutgoingDel(aChange);
                                }
                            }
                            break;

                        default:
                            throw new RuntimeException("Unrecognized Conflict Resolution");
                    }
                }
                break;

            default:
                throw new RuntimeException("Unrecognized Sync Mode");
        }
    }

    private void detectCurrentChangeAndProcess(SyncItem aChange) {
        //#debug debug
        System.out.println("detectCurrentChangeAndProcess(" + aChange + ")");

        switch (syncMode) {
            case SyncEngine.SYNCMODE_UPDATE_FROM_ORIGINATOR_ONLY:
                /*
                 * Changes: OUT_ADD, OUT_MOD
                 */
                if (-1 == aChange.recipientUID) {
                    if (prompter == null || prompter.confirm(aChange, Event.Sync.OUT_ADD))
                        processOutgoingAdd(aChange);
                }
                else if (aChange.originatorRevision != aChange.originatorSyncRevision) {
                	if (aChange.recipientRevision == -1) {
                		if (prompter == null || prompter.confirm(aChange, Event.Sync.OUT_ADD)) {
                			processOutgoingAdd(aChange);
                		}
                	}
                	else if (prompter == null || prompter.confirm(aChange, Event.Sync.OUT_MOD)) {
                		processOutgoingMod(aChange);
                	}
                }
                break;

            case SyncEngine.SYNCMODE_UPDATE_FROM_RECIPIENT_ONLY:
                /*
                 * Changes: IN_MOD, IN_DEL
                 */
                if (aChange.recipientSyncRevision != -1) {
                    if (-1 == aChange.recipientRevision) {
                        processIncomingDel(aChange);
                    }
                    else if (aChange.recipientRevision != aChange.recipientSyncRevision) {
                        processIncomingMod(aChange);
                    }
                }// else not synced
                break;

            case SyncEngine.SYNCMODE_UPDATE_BOTH:
                /*
                 * Changes: OUT_ADD, OUT_MOD, IN_MOD, IN_DEL
                 */
                /* Additions are agnostic of conflict resolution */
                if (-1 == aChange.recipientUID) {
                    processOutgoingAdd(aChange);
                }
                else {
                    switch (conflictResolution) {
                        case CONFLICT_ORIGINATOR_WINS:
                            /* OUT_MOD WINS vs IN_MOD and IN_DEL */
                            if (aChange.originatorRevision != aChange.originatorSyncRevision) {
                                if (-1 == aChange.recipientRevision) { /* Win over Delete */
                                    //#debug debug
                                    System.out.println("Originator won over Delete");

                                    processOutgoingAdd(aChange);
                                }
                                else {
                                    processOutgoingMod(aChange);
                                }
                            }
                            else if (aChange.recipientSyncRevision != -1) {
                                if (-1 == aChange.recipientRevision) {
                                    processIncomingDel(aChange);
                                }
                                else if (aChange.recipientRevision != aChange.recipientSyncRevision) {
                                    processIncomingMod(aChange);
                                }
                            }
                            break;

                        case CONFLICT_RECIPIENT_WINS:
                            if (aChange.recipientSyncRevision != -1) {
                                if (-1 == aChange.recipientRevision) {
                                    processIncomingDel(aChange);
                                }
                                else if (aChange.recipientSyncRevision != aChange.recipientRevision) {
                                	processIncomingMod(aChange);
                                }
                                else if (aChange.originatorRevision != aChange.originatorSyncRevision) {
                                	processOutgoingMod(aChange);
                                }
                            }
                            break;

                        default:
                            throw new RuntimeException("Unrecognized Conflict Resolution");
                    }
                }
                break;

            case SYNCMODE_EXTERNAL:
                break;

            default:
                throw new RuntimeException("Unrecognized Sync Mode");
        }
    }

    //#mdebug debug
    private String resolveCurrentSync()
    {
        return resolveSync(syncMode, conflictResolution);
    }

    public static String resolveSync(int syncMode, int conflictResolution)
    {
        StringBuffer sMode = new StringBuffer("[ ").append(syncMode).append(" - ");

        switch (syncMode) {
            case SYNCMODE_UPDATE_FROM_ORIGINATOR_ONLY:
                sMode.append("One-Way from Originator");
                break;

            case SYNCMODE_UPDATE_FROM_RECIPIENT_ONLY:
                sMode.append("One-Way from Recipient");
                break;

            case SYNCMODE_UPDATE_BOTH:
                sMode.append("Two-Ways");
                break;

            case SYNCMODE_EXTERNAL:
                sMode.append("EXT");
                break;

            default:
                sMode.append("UNKNOWN");
                break;
        }

        sMode.append(" | ").append(conflictResolution).append(" - ");

        switch (conflictResolution) {
            case CONFLICT_ORIGINATOR_WINS:
                sMode.append("Originator Wins");
                break;

            case CONFLICT_RECIPIENT_WINS:
                sMode.append("Recipient Wins");
                break;

            default:
                sMode.append("UNKNOWN");
                break;
        }

        sMode.append("]");
        return sMode.toString();
    }
    //#enddebug

    private void processIncomingAdd(SyncItem aChange)
    {
        semaphore.dispatch(Event.Sync.IN_ADD, recipient.getShortDescription(aChange.getRecipientUID()));

        //#debug debug
        System.out.println("Incoming ADD for " + aChange);

        try {
//            aChange.setRecipientRevision(recipient.getRevision(aChange.getRecipientUID()));
            Object recipientItem = recipient.get(aChange.getRecipientUID());

            if (recipientItem != null) {
	            aChange.setOriginatorUID(originator.create(recipientItem));
	            aChange.setOriginatorRevision(originator.getElementRevision());
	            history.synced(aChange);
	            results.in_add++;
            }
            else {
                //#debug error
                System.out.println("unexpected null item");
            }
        }
        catch (SyncDataStorageFullException e) {
            failedSync("DataStorage is full");

            //#debug debug
            e.printStackTrace();

            //#debug error
            System.out.println(e);
        }
        catch (IOException e) {
            failedSync("Unknown IOE");

            //#debug debug
            e.printStackTrace();

            //#debug error
            System.out.println(e);
        }
    }

    private void processOutgoingDel(SyncItem aChange)
    {
        semaphore.dispatch(Event.Sync.OUT_DEL, recipient.getShortDescription(aChange.getRecipientUID()));

        //#debug debug
        System.out.println("Outgoing DEL for " + aChange);

        try {
            recipient.delete(aChange.recipientUID);
            history.deleted(aChange);
            results.out_del++;
        }
        catch (IOException e) {
            //#debug debug
            e.printStackTrace();
        }
    }

    private void processIncomingDel(SyncItem aChange)
    {
        semaphore.dispatch(Event.Sync.IN_DEL, originator.getElementShortDescription());

        //#debug debug
        System.out.println("Incoming DEL for " + aChange);

        try {
            originator.delete();
            history.deleted(aChange);
            results.in_del++;
        }
        catch (IOException e) {
            //#debug debug
        	e.printStackTrace();

        	//#debug warn
        	System.out.println("Failed to delete from Source : " + aChange);
        }
    }

    private void processIncomingMod(SyncItem aChange)
    {
        semaphore.dispatch(Event.Sync.IN_MOD, recipient.getShortDescription(aChange.getRecipientUID()));

        //#debug debug
        System.out.println("Incoming MOD for " + aChange);

        try {
            Object recipientItem = recipient.get(aChange.getRecipientUID());

            if (recipientItem != null) {
	            originator.update(recipientItem);
	            aChange.setOriginatorRevision(originator.getElementRevision());
	            history.synced(aChange);
	            results.in_mod++;
            }
            else {
                //#debug error
                System.out.println("unexpected null item");
            }
        }
        catch (IOException e) {
            //#debug debug
            e.printStackTrace();

            //#debug error
            System.out.println(e);
        }
    }

    private void processOutgoingMod(SyncItem aChange)
    {
        semaphore.dispatch(Event.Sync.OUT_MOD, originator.getElementShortDescription());

        //#debug debug
        System.out.println("Outgoing MOD for " + aChange);

        try {
            Object source = originator.getElementData();
            recipient.update(aChange.getRecipientUID(), source);
            aChange.setRecipientRevision(recipient.getRevision(aChange.getRecipientUID()));
            history.synced(aChange);
            results.out_mod++;
        }
        catch (IOException e) {
            //#debug debug
            e.printStackTrace();
        }
    }

    private void processOutgoingAdd(SyncItem aChange)
    {
        semaphore.dispatch(Event.Sync.OUT_ADD, originator.getElementShortDescription());

        //#debug debug
        System.out.println("Outgoing ADD for " + aChange);

        try {
            Object source = originator.getElementData();
            aChange.setRecipientUID(recipient.create(source));

            //#debug debug
            System.out.println("Updating revision.");

            aChange.setRecipientRevision(recipient.getRevision(aChange.getRecipientUID()));

            //#debug debug
            System.out.println("Synced!");

            history.synced(aChange);
            results.out_add++;
        }
        catch (SyncDataStorageFullException e) {
            failedSync("DataStorage is full");

            //#debug debug
            e.printStackTrace();
        }
        catch (IOException e) {
            failedSync("Unknown IOException");

            //#debug debug
            e.printStackTrace();
        }
    }

    private void syncSuccessfull()
    {
        //#debug debug
        System.out.println("Sync successfull.");

        semaphore.dispatch(Event.Sync.SUCCESSFULL, results);
    }

    private synchronized void onSyncEnd()
    {
        //#mdebug info
        System.out.println("Sync terminated.");
        System.out.println(results);
        //#enddebug
    }

    /**
     * Releasing resources after sync is completed
     */
    public void uninit()
    {
        //#debug debug
        System.out.println("Uninitializing.");

        try {
            history.close();
        }
        catch (IOException e) {
            //#debug debug
            e.printStackTrace();
        }

        try {
            originator.close();
        }
        catch (IOException e) {
            //#debug debug
            e.printStackTrace();
        }

        try {
            recipient.close();
        }
        catch (IOException e) {
            //#debug debug
            e.printStackTrace();
        }

        //#debug debug
        System.out.println("Uninitializion completed.");
    }

    //#mdebug error
    public String toString()
    {
        return "LightSyncEngine[]";
    }
    //#enddebug

    /**
     * @see com.zyb.util.event.EventListener#getContext()
     */
    public byte getContext()
    {
        return Event.Context.SYNC;
    }

    /**
     * @see com.zyb.util.event.EventListener#handleEvent(byte context, int event, Object data)
     */
    public void handleEvent(byte context, int event, Object data)
    {
        if (syncMode == SYNCMODE_EXTERNAL) {
            switch (context) {
                case Event.Context.SYNC: {
                    switch (event) {
                        case Event.Sync.CAB_CHANGED:
                            synchronized (externalSyncQueue) {
                                externalSyncQueue.addElement(data);
                            }

                            synchronized (activityMonitor) {
                                activityMonitor.notify();
                            }

                            break;
                    }
                }
            }
        }
    }

    public boolean isInteractingWithSource()
    {
        return originator.isInteracting();
    }
}
