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

import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector;
import com.zyb.nowplus.business.sync.storage.exception.SyncDataStorageFullException;
import com.zyb.nowplus.business.sync.SyncStateSemaphore;
//#debug performancemonitor
import com.zyb.util.PerformanceMonitor;
import com.zyb.util.event.Event;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Native PIM Contacts Originator Sync Data Storage Connector Implementation
 *
 * @author Andrei Marcut, andrei@zybmail.com
 */
public class PIMContactsOCDSConnector implements OriginatorDataStorageConnector
{
	//  polish.pim.sync.always 
		//#if polish.pim.sync.firstrun==IMPORT && !polish.pim.sync.always:defined
			//#define tmp.importFirstTimeOnly
		int contactCounter=0;
		//#endif
	
    private ContactList contactList;
    private Enumeration loopList;

    private Contact currentItem;
    private String currentItemId;

    private final SyncStateSemaphore dispatcher;

    private int interactions;
    
    /**
     * PIMContactsOCDSConnector constructor
     * @param semaphore SyncStateSemaphore
     */
    public PIMContactsOCDSConnector(SyncStateSemaphore semaphore)
    {
        dispatcher = semaphore;
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector#hasMoreElements()
     */
    public boolean hasMoreElements()
    {
        return loopList != null && loopList.hasMoreElements();
    }

    public void loadElement(String uid)
    {
        //#debug debug
        System.out.println("Load element " + uid);
       
        if (uid == null) {
        	currentItemId = null;
        	currentItem = null;
        }
        else {
			//#debug performancemonitor
	        PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.PIM);
	       
	    	Contact matcher = contactList.createContact();
	    	matcher.addString(Contact.UID, Contact.ATTR_NONE, uid);

	    	try {    	
		    	Enumeration items = contactList.items(matcher);

		    	if (items.hasMoreElements()) {
		    		currentItem = (Contact) items.nextElement();
		        	currentItemId = uid;
		    	}
		    	else {
		    		currentItem = null;
		    		currentItemId = null;
		    		
		    		//#debug debug
		    		System.out.println("Can't find element");
		    	}
	    	}
	    	catch (PIMException e) {
	    		//#debug error
	    		System.out.println("Error loading element " + uid);
	    	}
	    	
			//#debug performancemonitor
	        PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.PIM);
        }
    }
    
    /**
     * @see com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector#nextElement()
     */
    public void nextElement()
    {
        //#debug debug
        System.out.println("Next contact");

        interactions++;

        //#debug performancemonitor
        PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.PIM);

        currentItem = (Contact) loopList.nextElement();

        //#debug performancemonitor
        PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.PIM);

        //#if (${lowercase(polish.vendor)}==huawei)
        //#debug error
        System.out.println("Huawei devices don't support UID field at all");
        //#else
        currentItemId = currentItem.getString(Contact.UID, Contact.ATTR_NONE);
        //#endif
        
        //#ifdef tmp.importFirstTimeOnly
        currentItemId=String.valueOf(++contactCounter);
        
        //Workaround for Nokia's reused UID problem. Custom UID is combination of original UID, first name, last name
        //first name hash and last name hash
        
        //#elif (${lowercase(polish.vendor)}==nokia)
        if(currentItem.countValues(Contact.NAME)>0){
        	String names[] = currentItem.getStringArray(Contact.NAME, 0);
            StringBuffer customUID = new StringBuffer(200);
            customUID.append(currentItemId);
            customUID.append( null == names[Contact.NAME_GIVEN] ? "" : names[Contact.NAME_GIVEN].trim());
            customUID.append( null == names[Contact.NAME_GIVEN] ? "" : Integer.toString(names[Contact.NAME_GIVEN].trim().hashCode()));
            customUID.append( null == names[Contact.NAME_FAMILY] ? "" : names[Contact.NAME_FAMILY].trim());
            customUID.append( null == names[Contact.NAME_FAMILY] ? "" : Integer.toString(names[Contact.NAME_FAMILY].trim().hashCode()));
            currentItemId = customUID.toString();
        }else{
        	//only when the contact has the last name and count value of contact name is 0
        	String fnames = currentItem.getString(Contact.FORMATTED_NAME,0);
        	StringBuffer customUID = new StringBuffer(200);
            customUID.append(currentItemId);
        	customUID.append( null == fnames ? "" : fnames.trim());
            customUID.append( null == fnames ? "" : Integer.toString(fnames.trim().hashCode()));
            currentItemId = customUID.toString();
        }
        
        //#elif (${lowercase(polish.vendor)}==samsung)
        while ("".equals(currentItemId.trim())) {
            currentItem = (Contact) loopList.nextElement();
            currentItemId = currentItem.getString(Contact.UID, Contact.ATTR_NONE);
        }

        //#endif  

        interactions--;
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector#reset()
     */
    public void reset() throws IOException
    {
        interactions++;

        try {
            loopList = contactList.items();
        }
        catch (PIMException e) {
            //#debug error
            System.out.println("Could not open Default Contact List : " + e);

            contactList = null;
            loopList = null;
            throw new IOException("Could not open.");
        }
        catch (SecurityException se) {
            dispatcher.dispatch(Event.Sync.USER_DISALLOWED_OPEN, null);
            contactList = null;
            loopList = null;

            //#debug fatal
            se.printStackTrace();

            throw new IOException("Not allowed to open.");
        }
        finally {
        	interactions--;
        }
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector#getElementId()
     */
    public String getElementId()
    {
        return currentItemId;
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector#getElementData()
     */
    public Object getElementData()
    {
        return currentItem;
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector#getElementRevision()
     */
    public long getElementRevision()
    {
        return ContactDataConvertor.calcCheckSum(currentItem);
    }

    public String getElementShortDescription()
    {
        if (currentItem == null)
            return "";

        return ContactDataConvertor.getShortDescription(currentItem);
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector#open()
     */
    public void open() throws IOException
    {
        interactions++;
        
        if (contactList != null) {
            try {
                contactList.close();
            } catch (PIMException e) {
                //#debug warn
                e.printStackTrace();
            }
        }

        try {
			//#debug performancemonitor
            PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.PIM);

            contactList = (ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST, PIM.READ_WRITE);

            //#debug performancemonitor
            PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.PIM);
        }
        catch (PIMException e) {
            //#debug error
            System.out.println("Could not open Default Contact List : " + e);

            contactList = null;
            throw new IOException("Could not open.");
        }
        catch (SecurityException se) {
            dispatcher.dispatch(Event.Sync.USER_DISALLOWED_OPEN, null);
            contactList = null;

            //#debug fatal
            se.printStackTrace();

            throw new IOException("Not allowed to open.");
        }
        finally {
            interactions--;
        }
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector#close()
     */
    public void close() throws IOException
    {
        interactions++;

        if (contactList != null) {
            try {
                contactList.close();
            } catch (PIMException e) {
                //#debug debug
                e.printStackTrace();
            }
        }

        contactList = null;
        loopList = null;
        currentItem = null;
        currentItemId = null;
        interactions--;
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector#create(Object input)
     */
    public String create(Object input) throws SyncDataStorageFullException, IOException
    {
    	//#debug debug
    	System.out.println("create contact " + ((ContactProfile) input).getFullName());

		//#debug performancemonitor
        PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.PIM);

        interactions++;
        currentItem = contactList.createContact();

        //#if (${lowercase(polish.vendor)}==samsung)
	        currentItemId = String.valueOf(System.currentTimeMillis());
	        currentItem.addString(Contact.UID, Contact.ATTR_NONE, currentItemId);
        //#endif  

	    interactions--;
        update(input);
        currentItemId = currentItem.getString(Contact.UID, 0);

		//#debug performancemonitor
        PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.PIM);

        return currentItemId;
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector#update(Object input)
     */
    public void update(Object input) throws IOException
    {
    	//#debug debug
    	System.out.println("update contact " + ((ContactProfile) input).getFullName());

		//#debug performancemonitor
        PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.PIM);

        ContactProfile source = (ContactProfile) input;
        ContactDataConvertor.overwrite(currentItem, source);
        interactions++;

        try {
            currentItem.commit();
        }
        catch (PIMException e) {
            throw new IOException(e.getMessage());
        }
        catch (SecurityException se) {
            dispatcher.dispatch(Event.Sync.USER_DISALLOWED_CONTENT_WRITE, currentItemId);
            throw new IOException(se.toString());
        }
        finally {
            interactions--;

            //#debug performancemonitor
            PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.PIM);
        }

//        /* Workaround to map the NAB-CAB ids */
//        String tid = currentItem.getString(Contact.UID, 0);
//        if (!tid.equalsIgnoreCase(source.getNabId()))
//            try {
//                //#debug debug
//                System.out.println("Mapping NAB-CAB for " + tid + " : ");
//                ProfileContactsRDSConnector.lock(source, 1);
//                source.setNabId(tid);
//                source.commit();
//            } catch (LockException e) {
//                //#debug error
//                e.printStackTrace();
//                //#debug error
//                System.out.println("Could not obtain lock on contact after remapping - commit failed.");
//            }
    }

    /**
     * @see com.zyb.nowplus.business.sync.storage.OriginatorDataStorageConnector#delete()
     */
    public void delete() throws IOException
    {
    	//#debug debug
    	System.out.println("delete contact");

        interactions++;

        try {
			//#debug performancemonitor
            PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.PIM);
            contactList.removeContact(currentItem);
			//#debug performancemonitor
            PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.PIM);
        }
        catch (PIMException e) {
            //#debug debug
            e.printStackTrace();
        }
        catch (SecurityException se) {
            //#debug fatal
            se.printStackTrace();
            dispatcher.dispatch(Event.Sync.USER_DISALLOWED_CONTENT_DELETE, currentItemId);
            throw new IOException(se.toString());
        }
        finally {
            currentItem = null;
            currentItemId = null;
            interactions--;
        }
    }
    
    public boolean isInteracting()
    {
    	return (interactions > 0);
    }
}
