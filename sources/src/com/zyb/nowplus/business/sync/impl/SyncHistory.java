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


import com.zyb.nowplus.business.sync.impl.SyncItem;
//#debug performancemonitor
import com.zyb.util.PerformanceMonitor;

import javax.microedition.rms.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Sync History persistence for handled items and custom list handling over Vector.
 *
 * @author Andrei Marcut, andrei@zybmail.com
 */

class SyncHistory {

    private final SyncItem originatorSeeker;

    private final SyncItem recipientSeeker;

    private Vector items;

    private static final String EMPTY_STRING = "";

    private RecordStore store;

    private final String name;

    private ByteArrayOutputStream recData;

	private boolean cancel;

    /**
     * SyncHistory constructor
     * @param uniqueName sync history name
     */
    public SyncHistory(String uniqueName) {
        name = uniqueName;

        originatorSeeker = new SyncItem() {
            public boolean equals(Object anObject) {
                return anObject instanceof SyncItem && this.originatorUID != null && this.originatorUID.equals(((SyncItem) anObject).getOriginatorUID());
            }
        };

        recipientSeeker = new SyncItem() {
            public boolean equals(Object anObject) {
                return anObject instanceof SyncItem && this.recipientUID == ((SyncItem) anObject).getRecipientUID();
            }
        };
    }

    /**
     * Open the record store for the current sync history
     * @throws IOException
     */
    public void open() throws IOException {
        close();
        try {
            store = RecordStore.openRecordStore(name, true);
            items = new Vector(100, 100);
            restore();

        } catch (RecordStoreException e) {
            //#mdebug fatal
            System.out.println("Unable to open persistence");
            e.printStackTrace();
            //#enddebug
            close();
            throw new IOException(e.toString());
        }

    }

    /**
     * Close sync history record store
     * @throws IOException
     */
    public void close() throws IOException {
        //#debug debug
        System.out.println("Closing Storage");

        if(items != null){
            items.removeAllElements();
        }
        items = null;

        try {
        	if(recData != null)
        		recData.close();
            recData = null;
        } catch (IOException ioe) {
           /*ignore*/
        }
        
        if (store != null)
            try {
                //#debug debug
                System.out.println("Closing RecordStore");
                store.closeRecordStore();
                //#debug debug
                System.out.println("Closed.");
            } catch (RecordStoreException e) {
                //#debug debug
                e.printStackTrace();
                throw new IOException(e.toString());
            }  catch (RuntimeException r){
           	 //#debug debug
               System.out.println("Runtime exception while closing r");
           	 //#debug debug
               r.printStackTrace();
               throw new IOException(r.toString());
           }finally {
                store = null;
            }


    }

    private void restore() {
        //#debug debug
        System.out.println("SyncHistory.restore()");
        RecordEnumeration savedHistory = null;
        try {
            savedHistory = store.enumerateRecords(null, null, false);
        } catch (RecordStoreNotOpenException e) {
            //#debug debug
            e.printStackTrace();
        }

        int recordId;

        if (savedHistory != null) {
            while (savedHistory.hasNextElement() && !this.cancel) {
                try {
                    recordId = savedHistory.nextRecordId();
                    ByteArrayInputStream input = null;
                    DataInputStream inData = null;
                    try {
                        inData = new DataInputStream((input = new ByteArrayInputStream(store.getRecord(recordId))));
                        int idx;
                        SyncItem toUpdate;
                        String oUID;
                        long rUID;
                        if ((idx = findByOriginatorUID(oUID = inData.readUTF())) != -1) {
                            toUpdate = (SyncItem) items.elementAt(idx);
                            toUpdate.setRecipientUID(inData.readLong());
                        } else if ((idx = findByRecipientUID(rUID = inData.readLong())) != -1) {
                            toUpdate = (SyncItem) items.elementAt(idx);
                            toUpdate.setOriginatorUID(oUID);
                        } else {
                            toUpdate = newSyncItem();
                            toUpdate.setOriginatorUID(oUID);
                            toUpdate.setRecipientUID(rUID);
                        }
                        toUpdate.setSyncRevisions(inData.readLong(), inData.readLong());
                        toUpdate.storageUID = recordId;
                    } catch (RecordStoreException e) {
                        //#debug debug
                        e.printStackTrace();
                    } catch (IOException e) {
                        //#debug debug
                        e.printStackTrace();
                    } finally {
                        if (inData != null) {
                            try {
                                inData.close();
                            } catch (IOException e) {
                                /* ignore */
                            }
                        }
                        if (input != null) {
                            try {
                                input.close();
                            } catch (IOException e) {
                                /* ignore */
                            }
                        }
                    }
                } catch (InvalidRecordIDException e) {
                    //#debug debug
                    e.printStackTrace();
                }
            }
            savedHistory.destroy();
        }
    }

    /**
     * Delete a SyncItem from history record store and items list
     * @param toDelete SyncItem
     */
    public void deleted(SyncItem toDelete) {
        if (toDelete.storageUID != -1) {
            try {
                store.deleteRecord(toDelete.storageUID);
            } catch (RecordStoreException e) {
                //#debug debug
                e.printStackTrace();
            }
        }
        remove(toDelete);
    }

    /**
     * Update originator record
     * @param UID SyncItem id
     * @param revision SyncItem revision
     */
    public synchronized void update(String UID, long revision) {
        int idx;
        SyncItem toUpdate;

        if ((idx = findByOriginatorUID(UID)) != -1) {
            toUpdate = (SyncItem) items.elementAt(idx);
        } else {
            toUpdate = newSyncItem();
            toUpdate.setOriginatorUID(UID);
        }
        toUpdate.setOriginatorRevision(revision);
    }

    /**
     * Update recipient record
     * @param UID SyncItem id
     * @param revision SyncItem revision
     */
    public synchronized void update(long UID, long revision) {
        //#debug debug
        System.out.println("Update Recipient Record " + UID + " rev " + revision);
        int idx;
        SyncItem toUpdate;
        if ((idx = findByRecipientUID(UID)) != -1) {
            toUpdate = (SyncItem) items.elementAt(idx);
        } else {
            //#debug debug
            System.out.println("Not found. Adding new.");
            toUpdate = newSyncItem();
            toUpdate.setRecipientUID(UID);
        }
        toUpdate.setRecipientRevision(revision);
    }

    /**
     * Mark an originator/recipient record as synced
     * @param aChange
     */
    public void synced(SyncItem aChange) {
        if (aChange.getOriginatorUID() == null || EMPTY_STRING.equals(aChange.getOriginatorUID().trim()))
            throw new IllegalStateException("Altered instance - Originator UID is invalid");
        if (aChange.getRecipientUID() == -1)
            throw new IllegalStateException("Altered instance - Recipient UID is invalid");

        aChange.synced();
        try {
            store(aChange);
        } catch (IOException e) {
            //#debug debug
            e.printStackTrace();
        }
    }

    /**
     * Get originator SyncItem by id
     * @param currentID SyncItem id
     * @return SyncItem
     */
    public synchronized SyncItem get(String currentID) {
        int idx;
        if ((idx = findByOriginatorUID(currentID)) != -1)
            return (SyncItem) items.elementAt(idx);

        SyncItem newItem = newSyncItem();
        newItem.setOriginatorUID(currentID);
        return newItem;
    }

    /**
     * Get recipinet SyncItem by id
     * @param currentID SyncItem id
     * @return SyncItem
     */
    public SyncItem get(long currentID) {
        int idx;
        if ((idx = findByRecipientUID(currentID)) != -1)
            return (SyncItem) items.elementAt(idx);
        SyncItem ret = newSyncItem();
        ret.setRecipientUID(currentID);
        return ret;
    }

    /**
     * Create a new SyncItem
     * @return SyncItem
     */
    public SyncItem newSyncItem() {
        SyncItem newItem;

        newItem = new SyncItem();

        items.addElement(newItem);

        return newItem;
    }

    private synchronized int findByOriginatorUID(String id) {
        originatorSeeker.setOriginatorUID(id);
        return items.indexOf(originatorSeeker);
    }

    private synchronized int findByRecipientUID(long id) {
        recipientSeeker.setRecipientUID(id);
        return items.indexOf(recipientSeeker);
    }

    /**
     * Removes the item from the list
     *
     * @param toDelete item id
     */
    private void remove(SyncItem toDelete) {
        items.removeElement(toDelete);
        toDelete.destroy();
    }

    private synchronized void store(SyncItem aChange) throws IOException {
        //#debug debug
        System.out.println("Store " + aChange);

        if(recData == null)
        	recData = new ByteArrayOutputStream(SyncItem.SERIALISED_SIZE);
        else
        	recData.reset();
        
        DataOutputStream dos = new DataOutputStream(recData);

        aChange.serialize(dos);
        
        byte[] serialized = recData.toByteArray();
		//#debug performancemonitor
		PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.RMSWRITE);
        if (aChange.storageUID != -1) {
            try {
                store.setRecord(aChange.storageUID, serialized, 0, serialized.length);
            } catch (RecordStoreException e) {
                //#debug warn
                e.printStackTrace();
            }
        } else {
            try {
                aChange.storageUID = store.addRecord(serialized, 0, serialized.length);
            } catch (RecordStoreException ex) {
                //#debug warn
                ex.printStackTrace();
            }
        }
		//#debug performancemonitor
		PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.RMSWRITE);

//		don't close this data outputstream: it will (bb) or should (se) close the underlying stream		
//        try {
//            dos.close();
//        } catch (IOException ioe) {
//           /*ignore*/
//        }
    }


    //#mdebug error
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append(this.getClass()).append("; size= ").append(items.size()).append("\n");
        Enumeration e = items.elements();
        while (e.hasMoreElements()) {
            SyncItem emb = (SyncItem) e.nextElement();
            ret.append(emb).append("\n");
        }
        return ret.toString();
    }
    //#enddebug

    /**
     * Cleanup history for deleted records from NAB/CAB
     * @return number of records deleted
     */
    public int procesDeletes() {
        int count = 0;
        SyncItem emb;
        Enumeration e = items.elements();
        while (e.hasMoreElements()) {
            emb = (SyncItem) e.nextElement();

            if (emb.originatorUID != null
                    && emb.recipientUID != -1
                    && emb.originatorSyncRevision != -1
                    && emb.originatorRevision == -1
                    && emb.recipientSyncRevision != -1
                    && emb.recipientRevision == -1) {
                count++;
                deleted(emb);
            }
        }
        return count;
    }

	/**
	 * 
	 */
	public void cancel() {
		this.cancel = true;
	}

}


