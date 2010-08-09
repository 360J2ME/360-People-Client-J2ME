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
package com.zyb.nowplus.business.sync.storage.impl.contacts;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.LockException;
import com.zyb.nowplus.business.sync.storage.RecipientDataStorageConnector;
import com.zyb.nowplus.business.sync.storage.exception.SyncDataStorageFullException;

import javax.microedition.pim.Contact;
import java.io.IOException;

/**
 * ZYB Client Contacts Recipient Sync Data Storage Connector Implementation
 *
 * @author Andrei Marcut, andrei@zybmail.com
 */
public class ProfileContactsRDSConnector implements RecipientDataStorageConnector {

    private final Model services;

    private ContactProfile cached;


    /**
     * ProfileContactsRDSConnector constructor
     * @param services Model
     */
    public ProfileContactsRDSConnector(Model services) {
        if (services == null)
            throw new IllegalArgumentException("Model is null.");
        this.services = services;
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.RecipientDataStorageConnector#open()
     */
    public void open() throws IOException {
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.RecipientDataStorageConnector#close()
     */
    public void close() throws IOException {
        if (cached != null) {
           cached.unload();
           cached = null;
        }
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.RecipientDataStorageConnector#allIds()
     */
    public long[] allIds() {
        //#todo FILTER ID SELECTION TO RETURN SYNCABLE ITEMS ONLY
        return services.getSyncableContactCabIds();
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.RecipientDataStorageConnector#getRevision(long uid)
     */
    public long getRevision(long uid) {
        try {
            Object item = get(uid);
            if (item != null)
                return ContactDataConvertor.calcCheckSum((ContactProfile) item);

        } catch (IOException e) {
            //#debug debug
            System.out.println("Failed to get revision" + e);
        }
        return -1;
    }

    /**
     * Get a short description for a ContactProfile
     * @param uid ContactProfile id
     * @return String
     */
    public String getShortDescription(long uid) {
        String ret = "";
        try {
            Object item = get(uid);
            if (item != null) {
                ret = ((ContactProfile) item).getFullName().trim();
            }
        } catch (IOException e) {
            //#debug error
            System.out.println("Failed to get short description" + e);
        }
        return "".equals(ret) ? "CAB " + uid : ret;
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.RecipientDataStorageConnector#create(Object source)
     */
    public long create(Object source) throws SyncDataStorageFullException, IOException {
        if (cached != null) {
            cached.unload();
        }

        cached = services.createContact();
        //#debug debug
        System.out.println("Loading contact");
        cached.load(true);

        //#debug debug
        System.out.println("Locking");
        try {
            lock(cached, 1);
        } catch (LockException e) {
            throw new IOException("Unable to acquire Lock.");
        }


        //#debug debug
        System.out.println("Overwriting");
        try {
            ContactDataConvertor.overwrite(cached, (Contact) source);
        } catch (Throwable th) {
            //#debug error
            System.out.println("Generic error on contact write : " + th);
        }

        //#debug debug
        System.out.println("Committing.");
        try {
            cached.commit();
        } catch (Throwable th) {
            //#debug error
            System.out.println("Unable to commit new item." + th);
            throw new SyncDataStorageFullException("FULL???");
        }

        //#debug debug
        System.out.println("Done.");

        return cached.getCabId();
    }

    /**
     * Trying to acquirie lock for a ContactProfile
     * @param contact ContactProfile
     * @param retryCount
     * @throws LockException
     */
    public static void lock(ContactProfile contact, int retryCount) throws LockException {
        try {
            //#debug debug
            System.out.println("Trying to acquirie lock.");
            contact.lock();
        } catch (LockException e) {
            if (retryCount > 0) {
                //#debug debug
                System.out.println("Retrying - " + retryCount);
                lock(contact, retryCount - 1);
            } else {
                throw e;
            }
        }
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.RecipientDataStorageConnector#get(long uid)
     */
    public Object get(long uid) throws IOException {
        if (cached != null) {
            if (cached.getCabId() == uid) {
                return cached;
            }
            cached.unload();
        }
        cached = services.getContact(uid);
        if (cached != null) {
            cached.load(true);
        }
        return cached;
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.RecipientDataStorageConnector#update(long uid, Object source)
     */
    public void update(long uid, Object source) throws IOException {
        Object item = null;
        try {
            item = get(uid);
        } catch (Exception e) {
            //#debug debug
            System.out.println(e.getClass() + " " + e.getMessage() + " " + e);
        }
        if (item != null) {
            ContactProfile contact = (ContactProfile) item;
            try {
                lock(contact, 1);
            } catch (LockException e) {
                throw new IOException("Unable to acquire Lock.");
            }
            ContactDataConvertor.overwrite(contact, (Contact) source);
            contact.commit();
        } else
            throw new IOException("NOT FOUND");
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.RecipientDataStorageConnector#delete(long uid)
     */
    public void delete(long uid) throws IOException
    {
        ContactProfile contactProfile = services.getContact(uid);
		services.delete(contactProfile);

		//#debug debug
		System.out.println(contactProfile.getFullName());
    }
}
