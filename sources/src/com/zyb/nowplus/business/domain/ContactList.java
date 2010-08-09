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
package com.zyb.nowplus.business.domain;

import java.util.Vector;

import com.zyb.nowplus.business.domain.filters.Filter;
import com.zyb.nowplus.business.domain.filters.PresenceFilter;
import com.zyb.nowplus.business.domain.orders.FirstLastOrder;
import com.zyb.nowplus.business.domain.orders.LastFirstOrder;
import com.zyb.nowplus.business.domain.orders.Order;
import com.zyb.nowplus.business.event.EventDispatcherTask;
import com.zyb.util.ArrayUtils;
import com.zyb.util.Collator;
import com.zyb.util.HashUtil;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;

/**
 * A list of contacts in memory.
 */
public class ContactList 
{	
	private static final String[] PRESENCES_ALL_NETWORKS = new String[] {
			ExternalNetwork.VODAFONE_360,
			ExternalNetwork.GOOGLE,
			ExternalNetwork.FACEBOOK,
			ExternalNetwork.HYVES,
			ExternalNetwork.WINDOWS_LIVE,
	};

	private static final int[] PRESENCES_ALL_OFFLINE = new int[] {
			Channel.PRESENCE_OFFLINE,
			Channel.PRESENCE_OFFLINE,
			Channel.PRESENCE_OFFLINE,
			Channel.PRESENCE_OFFLINE,
			Channel.PRESENCE_OFFLINE,
	};

	private static final PresenceFilter PRESENCE_FILTER = new PresenceFilter();

	private final Vector cache;
	
	// The current state of the filtered list.
	// The contacts filtered with the primary filter are in 
	// currentResult[0..currentResult1Len].
	// The contacts filtered by both filtes are in 
	// currentResult[0..currentResult2Len].
	private Filter currentFilter1;
	private Filter currentFilter2;
	static Order currentOrder;
	private ContactProfile[] currentResult;
	private int currentResult1Len;
	private int currentResult2Len;
	
	private long currentContactId;

	private final EventDispatcher dispatcher;
	
	/**
	 * Constructs an empty contact list.
	 * @param dispatcher The dispatcher to fire off events.
	 */
	public ContactList(EventDispatcher dispatcher)
	{
		//#debug debug
		System.out.println("Constructing contact list.");
		
		cache = new Vector();
		
		currentResult = new ContactProfile[0];
		currentResult1Len = 0;
		currentResult2Len = 0;
		
		this.dispatcher = dispatcher;
	}
	
	public Vector getCache() {
		return this.cache;
	}

	/**
	 * Initialises the contacts list.
	 */
	public void initialise(int orderType, ContactProfile[] contacts)
	{
		//#debug debug
		System.out.println("Initialise contact list.");
		
		synchronized (cache) {
			for (int i = 0; i < contacts.length; i++) {
				if (contacts[i] != null) {
					cache.addElement(contacts[i]);
				}
			}

			currentResult = new ContactProfile[cache.size()];
			
			currentFilter1 = null;
			currentResult1Len = 0;
			filterContacts();
			
			currentFilter2 = null;
			currentResult2Len = currentResult1Len;
			textFilterContacts();
			
			if (orderType == Order.TYPE_FIRST_LAST) {
				//#debug debug
				System.out.println("Using Order.TYPE_FIRST_LAST");
				
				currentOrder = new FirstLastOrder();
			}
			else if (orderType == Order.TYPE_LAST_FIRST) {
				//#debug debug
				System.out.println("Using Order.TYPE_LAST_FIRST");

				currentOrder = new LastFirstOrder();
			}

			orderContacts();
		}
	}

	public void invalidateAllOnlinePresences()
	{
		for (int i = 0; i < cache.size(); i++) {
			ContactProfile profile = (ContactProfile) cache.elementAt(i);

			if (PRESENCE_FILTER.accepts(profile)) {
				profile.setPresences(PRESENCES_ALL_NETWORKS, PRESENCES_ALL_OFFLINE);
			}
		}
	}

	/**
	 * Sets the primary filter.
	 * @param filter1 Descriptor of which contacts are to be filtered.
	 */
	public void setPrimaryFilter(Filter filter1)
	{
		//#debug debug
		System.out.println("Set primary filter to " + filter1 + " on contact list.");
		
		EventDispatcherTask task = dispatcher.scheduleEvents(Event.Context.CONTACTS, Event.Contacts.REFRESHING_LIST, 2 * 1000);

		if (filter1 != null && filter1.getType() == Filter.TYPE_ALL) {
			filter1 = null;
		}

		boolean updated = refreshResults(filter1, currentFilter2, currentOrder);
		task.cancel();

		if (updated || task.hasFired()) {
			fireEvent(Event.Contacts.FILTER_CHANGED);
		}
	}
	
	/**
	 * Sets the secondary filter.
	 * @param filter2 Descriptor of which contacts that are accepted by the 
	 * primary filter are to be filtered.
	 */
	public void setSecondaryFilter(Filter filter2)
	{
		//#debug debug
		System.out.println("Set secondary filter to " + filter2 + " on contact list.");
			
		EventDispatcherTask task = dispatcher.scheduleEvents(Event.Context.CONTACTS, Event.Contacts.REFRESHING_LIST, 2 * 1000);
		
		boolean updated = refreshResults(currentFilter1, filter2, currentOrder);
		task.cancel();
		
		if (updated || task.hasFired())
		{
			fireEvent(Event.Contacts.REFRESH_LIST);
		}
	}
	
	/**
	 * Sets the order.
	 * @param order Descriptor of the order of the contacts in the list.
	 */
	public void setOrder(Order order)
	{
		//#debug debug
		System.out.println("Set order to " + order + " on contact list.");
			
		EventDispatcherTask task = dispatcher.scheduleEvents(Event.Context.CONTACTS, Event.Contacts.REFRESHING_LIST, 2 * 1000);
		
		boolean updated = refreshResults(currentFilter1, currentFilter2, order);
		task.cancel();
		
		if (updated || task.hasFired())
		{
			fireEvent(Event.Contacts.REFRESH_LIST);
		}		
	}
	
	/**
	 * Gets the current order.
	 */
	public Order getOrder()
	{
		return currentOrder;
	}

	/**
	 * Gets the first contact in the current filtered and ordered contacts
	 * whose formatted name comes alphabetically after the given text.
	 */
	public ContactProfile getFirstContact(String text)
	{
		if ((text == null) || (currentOrder == null))
		{
			return null;
		}
	
		int[] sortText = Collator.getInstance().compileSortName(text);
		
		ContactProfile contact = null;
		synchronized (cache)
		{
			for (int i = 0; (i < currentResult2Len) && (contact == null); i++)
			{
				if ((currentOrder.compare(currentResult[i], sortText) >= 0)
						|| (i == currentResult2Len - 1))  // select the final contact if no contact comes after text
				{
					contact = currentResult[i];
				}
			}
		}
		return contact;		
	}
	
	/**
	 * Returns the first contact in the current filtered and ordered contacts.
	 */
	public ContactProfile getFirstContact()
	{
		ContactProfile contact = null;
		synchronized (cache)
		{
			contact = (currentResult2Len == 0) ? null : currentResult[0];
		}
		return contact;
	}
	
	/**
	 * Returns the last contact in the current filtered and ordered contacts.
	 */
	public ContactProfile getLastContact()
	{
		ContactProfile contact = null;
		synchronized (cache)
		{
			contact = (currentResult2Len == 0) ? null : currentResult[currentResult2Len - 1];
		}
		return contact;
	}
	
	/**
	 * Gets a range of filtered and ordered contacts.
	 * @param contactId The id of the contact in the center of the range.
	 * @param number The number of contact to return.
	 */	
	public ListSelection getContacts(long contactId, int number)
	{
		ListSelection contacts = null;
		synchronized (cache)
		{
			int mid = getContactIndex(contactId);
	
			int	from = (mid == -1) ? 0 : (mid - (number - 1) / 2);
			if (from < 0)
			{
				from = 0;
			}
			int to = (mid == -1) ? (number - 1) : (mid + number / 2);
			if (to > currentResult2Len - 1)
			{
				to = currentResult2Len - 1;
			}
			contacts = new ListSelection(currentResult, from, to, currentResult2Len);
		}
		return contacts;
	}
	
	/**
	 * Gets a range of filtered and ordered contacts.
	 * @param from the start index
	 * @param to the end index
	 * @return the resulting ListSelection
	 */
	public ListSelection getContacts(int from, int to) 
	{
		synchronized (cache)
		{
				
			// PP - fix for bug 0017232, to was set to -1 and hence negativearrayexception.
			if (currentResult2Len == 0)
				from = to = 0;
			else {

				if (from < 0)
					from = 0;
				
				if (to > currentResult2Len - 1)
					to = currentResult2Len - 1;

			}

			
			return new ListSelection(currentResult, from, to, currentResult2Len);
		}
	}
	
	/**
	 * Returns the total number of contacts
	 * @return the total number of contacts
	 */
	public int getContactsSize() {
		return currentResult2Len;
	}

	/**
	 * Gets a list of client address book ids of syncable contacts.
	 */	
	public long[] getSyncableContactCabIds()	
	{
		long[] cabIds = null;
		int len = 0;
		synchronized (cache)
		{
			cabIds = new long[cache.size()];
			for (int i = 0; i < cache.size(); i++)
			{
				ContactProfile contact = (ContactProfile) cache.elementAt(i);
				if (contact.syncToNab())
				{
					cabIds[len++] = contact.getCabId();
				}
			}
		}
		return ArrayUtils.trimArray(cabIds, len);	
	}
	
	/**
	 * Gets a number of contacts by client address book id.
	 */
	public ContactProfile[] getContacts(long[] cabIds, int[] changeLogTypes)
	{
		if (cabIds == null)
		{
			return new ContactProfile[0];
		}
		// TODO this should be optimised
		ContactProfile[] contacts = new ContactProfile[cabIds.length];
		for (int i = 0; i < contacts.length; i++)
		{
			if (changeLogTypes[i] == Settings.CHANGELOG_ADD_OR_UPDATE)
			{
				contacts[i] = getContact(cabIds[i]);
			}
		}
		return contacts;
	}
	
	/**
	 * Gets a contact by client address book id.
	 */	
	public ContactProfile getContact(long cabId)
	{
		ContactProfile contact = null;
		synchronized (cache)
		{
			for (int i = 0; (i < cache.size()) && (contact == null); i++)
			{
				ContactProfile candidate = (ContactProfile) cache.elementAt(i);
				if (candidate.getCabId() == cabId)
				{
					contact = candidate;
				}
			}
		}
		return contact;
	}
	
	/**
	 * Gets a contact by server address book id.
	 */
	public ContactProfile getContactBySabId(long sabId)
	{
		ContactProfile contact = null;
		synchronized (cache)
		{
			for (int i = 0; (i < cache.size()) && (contact == null); i++)
			{
				ContactProfile candidate = (ContactProfile) cache.elementAt(i);
				if (candidate.getSabId() == sabId)
				{
					contact = candidate;
				}
			}
		}
		return contact;		
	}
	
	/**
	 * Gets the contact by user id.
	 */
	public ContactProfile getContactByUserId(long userId)
	{
		ContactProfile contact = null;
		synchronized (cache)
		{
			for (int i = 0; (i < cache.size()) && (contact == null); i++)
			{
				ContactProfile candidate = (ContactProfile) cache.elementAt(i);
				if (candidate.getUserId() == userId)
				{
					contact = candidate;
				}
			}
		}
		return contact;
	}
	
	/**
	 * Selects a contact.
	 */	
	public void selectContact(long id)
	{
		currentContactId = id;
	}
	
	/**
	 * Gets the selected contact.
	 */	
	public ContactProfile getSelectedContact()
	{
		return getContact(currentContactId);
	}
	
	private int getContactIndex(long cabId)
	{
		int index = -1;
		for (int i = 0; (i < currentResult2Len) && (index == -1); i++)
		{
			if (currentResult[i].getCabId() == cabId)
			{
				index = i;
			}
		}
		return index;		
	}
	
	/**
	 * Adds a contact to the list.
	 */
	public void addContact(ContactProfile contact, boolean silent)
	{
		//#debug debug
		System.out.println("Add " + contact);
		
		synchronized (cache)
		{
			cache.addElement(contact);
			if (currentResult.length < cache.size())
			{
				currentResult = ContactProfile.extendArray(currentResult);
			}
		
			if (inResults(contact))
			{
				addToResults(contact, silent);
			}
		}
		
		if (!silent)
		{
			fireEvent(Event.Contacts.ADD, contact.getCabId());
		}
	}
	
	/**
	 * Updates a contact in the list.
	 */
	public void updateContact(ContactProfile oldContact, ContactProfile contact, boolean silent)
	{
		//#debug debug
		System.out.println("Update contact " + oldContact + " to " + contact);
		
		synchronized (cache)
		{
			if (inResults(oldContact))
			{
				if (inResults(contact))
				{
					orderContacts();
					
					if (!silent)
					{
						fireEvent(Event.Contacts.UPDATE_IN_LIST);
					}
				}
				else
				{
					removeFromResults(contact.getCabId(), silent);
				}
			}
			else
			{
				if (inResults(contact))
				{
					addToResults(contact, silent);
				}
			}
		}
		
		if (!silent)
		{
			fireEvent(Event.Contacts.UPDATE, contact.getCabId());
		}
	}
	
	/**
	 * Removes a contact from the list.
	 * @param cabId Identifier of the contact.
	 */
	public void removeContact(long cabId, boolean silent)
	{
		//#debug debug
		System.out.println("Remove contact with id=" + cabId);
		
		ContactProfile contact = getContact(cabId);
		if (contact == null) {
			//#debug info
			System.out.println("Contact is already removed.");
			return;
		}
		
		synchronized (cache)
		{		
			cache.removeElement(contact);

			if (inResults(contact))
			{
				removeFromResults(contact.getCabId(), silent);
			}
		}
		
		if (!silent)
		{
			fireEvent(Event.Contacts.REMOVE, cabId);
		}
	}

	private boolean refreshResults(Filter filter1, Filter filter2, Order order)
	{
		boolean refreshed = false;
		
		synchronized (cache)
		{
			if (!HashUtil.equals(currentFilter1, filter1))
			{
				currentFilter1 = filter1;
				filterContacts();
				currentFilter2 = null;
				currentResult2Len = currentResult1Len;
				currentOrder = null;
				refreshed = true;
			}
			if (!HashUtil.equals(currentFilter2, filter2))
			{
				currentFilter2 = filter2;
				currentResult2Len = currentResult1Len;
				textFilterContacts();
				currentOrder = null;
				refreshed = true;
			}
			if (!HashUtil.equals(currentOrder, order))
			{
				currentOrder = order;
				resetFullNames();
				orderContacts();
				refreshed = true;
			}
		}
		return refreshed;
	}
	
	private boolean inResults(ContactProfile contact)
	{
		return (((currentFilter1 == null) || currentFilter1.accepts(contact))
			 && ((currentFilter2 == null) || currentFilter2.accepts(contact)));
	}
	
	private void addToResults(ContactProfile contact, boolean silent)
	{
		currentResult[currentResult1Len++] = currentResult[currentResult2Len];
		currentResult[currentResult2Len++] = contact;
		orderContacts();
		
		if (!silent)
		{
			fireEvent(Event.Contacts.ADD_TO_LIST);
		}
	}
	
	private void removeFromResults(long cabId, boolean silent)
	{
		boolean found = false;
		
		for (int i = 0; i < currentResult1Len; i++)
		{
			if (found)
			{
				currentResult[i - 1] = currentResult[i];
			}
			else
			{
				found = (currentResult[i].getCabId() == cabId);
				
				if (found && !silent)
				{
					fireEvent(Event.Contacts.REMOVE_FROM_LIST, currentResult[i]);
				}
			}
		}
		if (found)
		{
			currentResult[--currentResult1Len] = null;
			currentResult2Len--;
		}
	}
	
	private void filterContacts()
	{
		while (currentResult1Len > 0) {
			currentResult[--currentResult1Len] = null;
		}

		if (currentFilter1 != null) {
			for (int i = 0; i < cache.size(); i++) {
				ContactProfile candidate = (ContactProfile) cache.elementAt(i);

				if (currentFilter1.accepts(candidate)) {
					currentResult[currentResult1Len++] = candidate;
				}
			}
		}
		else {
			for (int i = 0; i < cache.size(); i++) {
				currentResult[currentResult1Len++] = (ContactProfile) cache.elementAt(i);
			}
		}
	}

	private void textFilterContacts()
	{
		//TODO: if the new filter is a subset of the old one, only filter the old result
		if (currentFilter2 != null)
		{
			int i = 0;
			while (i < currentResult2Len)
			{
				if (currentFilter2.accepts(currentResult[i]))
				{
					i++;
				}
				else
				{
					currentResult2Len--;
					// swap i and currentResult2Len
					ContactProfile c = currentResult[i];
					currentResult[i] = currentResult[currentResult2Len];
					currentResult[currentResult2Len] = c;
				}
			}
		}
	}
	
	private void resetFullNames()
	{
		synchronized (cache)
		{
			for (int i = 0; i < cache.size(); i++)
			{
				ContactProfile contact = (ContactProfile) cache.elementAt(i);
				contact.resetSortName();
			}
		}
	}
	
	private void orderContacts()
	{
		if ((currentOrder != null) && (currentResult2Len > 1))
		{			
			ArrayUtils.shellSort(currentResult, currentResult2Len, currentOrder);
		}
	}
	
	private void fireEvent(int id)
	{
		dispatcher.notifyEvent(Event.Context.CONTACTS, id);
	}
	
	private void fireEvent(int id, long cabId)
	{
		dispatcher.notifyEvent(Event.Context.CONTACTS, id, new Long(cabId));
	}
	
	private void fireEvent(int id, Object data)
	{
		dispatcher.notifyEvent(Event.Context.CONTACTS, id, data);
	}
	
	//#mdebug error
	public String toString()
	{
		return "ContactList[currentFilter1=" + currentFilter1
			+ ",currentFilter2=" + currentFilter2
			+ ",currentOrder=" + currentOrder
			+ ",results=" + currentResult2Len + "/" + currentResult1Len
			+ "]";
	}
	//#enddebug
}
