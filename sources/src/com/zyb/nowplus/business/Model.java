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
package com.zyb.nowplus.business;

import java.util.Date;

import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.domain.Email;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.InvalidValueException;
import com.zyb.nowplus.business.domain.ListSelection;
import com.zyb.nowplus.business.domain.LockException;
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.business.domain.Message;
import com.zyb.nowplus.business.domain.MyProfile;
import com.zyb.nowplus.business.domain.Settings;
import com.zyb.nowplus.business.domain.filters.Filter;
import com.zyb.nowplus.business.domain.orders.Order;
import com.zyb.nowplus.data.email.types.EmailMessage;
import com.zyb.util.event.EventListener;

/**
 * The business layer interface to the other layers.
 */
public interface Model 
{	
	public static final int FEEDBACK_CONNECTION_DOWN = 0xFF000000;
	public static final int FEEDBACK_NAB_INTERACTION = 0x00FF0000;
	public static final int FEEDBACK_SAB_INTERACTION = 0x0000FF00;
	
	/**
	 * Gets the settings of the user of the application.
	 */
	public  Settings getSettings();
	
	/**
	 * Gets the (build) version of the application.
	 */
	public String getAppVersion();
	
	/**
	 * Opens the download url of the latest version.
	 * @throws LaunchException 
	 */
	public void upgradeApplication() throws LaunchException;
	
	/**
	 * Gets the username used in the last login.
	 */
	public String getUserName();
	
	/**
	 * Gets the 'stay logged in' flag.
	 */
	public boolean stayLoggedIn();

	/**
	 * Gets the Mobile Subscriber ISDN number on the current SIM.
	 */
	public String getMsisdn();	
	
	/**
	 * Signs up with the given details.
	 */
	public void signup(String userName, String password, String phonenumber,String userEmailAddr,  Date dateOfBirth, boolean acceptedTnC);
	
	/**
	 * Cancels sign up.
	 */
	public void cancelSignUp();
	
	/**
	 * Finishes the sign up.
	 */
	public void finishSignUp(boolean stayLoggedIn);
	
	/**
	 * Rerequest a confirmtion SMS. After the signup timed out (SMS never arrived).
	 */
	public void rerequestConfirmationSMS(String msisdn);

	/**
	 * Logs in with the given username and password.
	 */
	public void login(String userName, String password, boolean stayLoggedIn);
	
	/**
	 * Cancels log in.
	 */
	public void cancelLogin();
	
	/**
	 * Gets the profile of the user of the application.
	 */
	public MyProfile getMe();
	
	/**
	 * Sets the status of the user of the application on Now+.
	 */
	public void setMyStatus(String status) throws LockException;
	
	/**
	 * Sets the availability of the user of the application on Now+.
	 */
	public void setMyNowPlusPresence(int presence);
	
	/**
	 * Gets the available social networks.
	 */
	public ExternalNetwork[] getAvailableSocialNetworks();
	
	/**
	 * Adds a social network account to my profile.
	 * @param importContacts If true, imports contacts.
	 */
	public void addSocialNetworkAccount(ExternalNetwork network, String name, String password, boolean importContacts);
	
	/**
	 * Flag for whether showing Import SN screen
	 */
	public boolean isAddingSocialNetworkAccountsAtStartup();
	
	/**
	 * Finishes adding social networks.
	 */
	public void finishAddingSocialNetworkAccounts();
	
	/**
	 * Skips adding social networks.
	 */
	public void skipAddingSocialNetworkAccounts(); 
	
	/**
	 * Removes a social network account from my profile.
	 */
	public void removeSocialNetworkAccount(Identity account);

	/**
	 * Logs in a social network.
	 */
	public void loginSocialNetworkAccount(Identity account);
	
	/**
	 * Logs out from a social network.
	 */
	public void logoutSocialNetworkAccount(Identity account);
	
	/**
	 * Get possible contact filters.
	 */
	public Filter[] getContactFilters();
	
	/**
	 * Sets the current contacts filter.
	 * @param filter Descriptor of which contacts are to be returned.
	 */
	public void setContactsFilter(Filter filter);
	
	/**
	 * Sets the current text filter.
	 */
	public void setTextFilter(String filter);
		
	/**
	 * Sets the current contacts order.
	 */
	public void setContactsOrder(Order order);
	
	/**
	 * Gets the current contacts order.
	 */
	public Order getContactsOrder();
	
	/**
	 * Gets the first contact who's formatted name comes alphabetically after
	 * the text.
	 */
	public ContactProfile getFirstContact(String text);
	
	/**
	 * Gets the first contact.
	 */
	public ContactProfile getFirstContact();
	
	/**
	 * Gets the last contact.
	 */
	public ContactProfile getLastContact();
	
	/**
	 * Gets a range of filtered and ordered contacts.
	 * @param contact The contact in the center of the range
	 * @param number The number of contacts to return
	 */
	public ListSelection getContacts(ContactProfile contact, int number);
	
	/**
	 * Gets a range of filtered and ordered contacts.
	 * @param from the start index
	 * @param to the end index
	 * @return the resulting ListSelection
	 */
	public ListSelection getContacts(int start, int end);
	
	/**
	 * Returns the total number of contacts
	 * @return the total number of contacts
	 */
	public int getContactsSize();
	
	/**
	 * Selects a contact.
	 */
	public void selectContact(ContactProfile contact);
	
	/**
	 * Gets the selected contact.
	 */
	public ContactProfile getSelectedContact();

	/**
	 * Creates a new, empty contact.
	 */
	public ContactProfile createContact();
	
	/**
	 * Notifies the model that the client is finished editing a profile.
	 */
	public void finishedEditing(ManagedProfile profile);
	
	/**
	 * Deletes a contact.
	 */
	public void delete(ContactProfile contact);

	/**
	 * Invites a contact who is not a Now+ member to Now+.
	 */
	public void invite(ContactProfile contact) throws InvalidValueException;
	
	/**
	 * Connects to a contact who is a Now+ member.
	 */
	public void connect(ContactProfile contact) throws InvalidValueException;
	
	/**
	 * Gets the available standard and custom groups.
	 */
	public Group[] getAvailableGroups();
	
    /**
     * Cancels the sync with the native addressbook
     */
    public void cancelImportFromNab();
	
	/**
	 * Syncs the client address book with the server address book.
	 */
	public void sync();

	/**
	 * Gets a list of all client address book ids.
	 */
	public long[] getSyncableContactCabIds();
	
	/**
	 * Gets a contact by client address book id.
	 */
	public ContactProfile getContact(long cabId);
	
	/**
	 * Gets a range of activities in the life drive.
	 */
	public ListSelection getLifeDrive(int from, int to);
		
	/**
	 * Gets a range of activities in the friends stream.
	 */
	public ListSelection getFriendsStream(int from, int to);
	
	//#if activate.embedded.360email
	/**
	 * Opens an email folder.
	 */
	public void openFolder(String folderName);
	
	/**
	 * Refreshes current email folder.
	 */
	public void refreshFolder();
	
	/**
	 * Closes the current email folder.
	 */
	public void closeFolder();

	/**
	 * Gets the current email folder.
	 */
	public String getCurrentFolder();
	
	/**
	 * Gets the subfolders of the current email folder, if any.
	 */
	public String[] getAvailableFolders();
	
	/**
	 * @deprecated
	 */
	public EmailMessage[] getAvailableMessages();
	
	/**
	 * Gets the messages in the current email folder, if any.
	 */
	public Email[] getAvailableMessages2();
	
	/**
	 * Opens an email message.
	 */
	public void openMessage(int id);
	
	/**
	 * @deprecated
	 */
	public EmailMessage getCurrentMessage();
	
	/**
	 * Gets the current email message.
	 */
	public Email getCurrentMessage2();
	//#endif
	
	/**
	 * Launches communication over the given channel.
	 */
	public void launch(Channel channel) throws LaunchException;
	
	/**
	 * Launches a website.
	 */
	public void launch(String url) throws LaunchException;
	
	/**
	 * Tries to cancel postponed communication.
	 */
	public void launchCancelled(); 
	
	/**
	 * Closes communication over the given channel. Only necessary for
	 * chat channels.
	 */
	public void close(Channel channel);
	
	/**
	 * Sends an SMS via WMA
	 * @param number number to send to
	 * @param message message to be sent
	 * @param data data (Notification) to be sent to controller 
	 */
	public void sendSms(String number, String message, Object data);
	
	/**
	 * Sends a chat message.
	 */
	public void sendChatMessage(Channel channel, Message message) throws LaunchException;
	
	/**
	 * Sets if data connection while roaming is allowed, that is if the user
	 * will be asked for permission (false) or not (true).
	 */
	public void setRoamingDataConnectionAllowed(boolean roamingDataConnectionAllowed);
	
	/**
	 * Indicates if data connection while roaming is allowed.
	 */
	public boolean getRoamingDataConnectionAllowed();
	
	/**
	 * Accepts the application is roaming.
	 */
	public void acceptRoaming();
	
	/**
	 * Gives feedback on what the model is doing, in 
	 * the form of a bitmask.
	 * (feedback & FEEDBACK_CONNECTION_UP != 0) indicates connection is up, etc 
	 */
	public int getFeedback();
	
	/**
	 * Indicates if the network is up.
	 */
	public boolean isConnectionUp();
	
	/**
	 * Gets the size of the data transferred in kb.
	 */
	public int getDataCounter();
	
	/**
	 * Resets the data counter.
	 */
	public void resetDataCounter();
	
	/**
	 * Attaches a listener to the model.
	 */
	public void attach(EventListener listener);
	
	/**
	 * Detaches a listener from the model.
	 */
	public void detach(EventListener listener);	
	
	/**
	 * Attaches a component that may influence the 'busy' state of the model.
	 */
	public void attachActive(Active active);
	
	/**
	 * Detaches a component that may influence the 'busy' state of the model.
	 */
	public void detachActive(Active active);
	
	/**
	 * Indicates the model is busy, that is, one or more of the components 
	 * that have registered with model is busy.
	 */
	public boolean isBusy();
	
	/**
	 * Exits the application.
	 */
	public void exit(boolean deleteStorageBefore);
	
	/**
	 * Indicates if the user has been authenticated by BE
	 */
	public boolean isAuthenticated();
	
	/**
	 * is true if application is ready and shows the ContactTab
	 */
	public boolean isApplicationReady();
}
