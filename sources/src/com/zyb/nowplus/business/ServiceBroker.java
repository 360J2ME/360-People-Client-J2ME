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

import java.util.Calendar;
import java.util.Date;

import com.zyb.nowplus.MIDletContext;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ContactList;
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
import com.zyb.nowplus.business.domain.Profile;
import com.zyb.nowplus.business.domain.Settings;
import com.zyb.nowplus.business.domain.filters.Filter;
import com.zyb.nowplus.business.domain.filters.NameFilter;
import com.zyb.nowplus.business.domain.orders.Order;
import com.zyb.nowplus.business.event.EventDispatcherTask;
import com.zyb.nowplus.business.event.RunnableEventDispatcher;
import com.zyb.nowplus.business.sync.Sync;
import com.zyb.nowplus.business.sync.impl.LightSyncEngine;
import com.zyb.nowplus.data.email.EmailListener;
import com.zyb.nowplus.data.email.response.EmailAppendMessageResponse;
import com.zyb.nowplus.data.email.response.EmailFolderResponse;
import com.zyb.nowplus.data.email.response.EmailLoginResponse;
import com.zyb.nowplus.data.email.response.EmailMessageResponse;
import com.zyb.nowplus.data.email.response.EmailResponse;
import com.zyb.nowplus.data.email.response.EmailUseFolderResponse;
import com.zyb.nowplus.data.email.types.EmailAddress;
import com.zyb.nowplus.data.email.types.EmailMessage;
import com.zyb.nowplus.data.protocol.AuthenticationListener;
import com.zyb.nowplus.data.protocol.CommunicationManager;
import com.zyb.nowplus.data.protocol.CommunicationManagerImpl;
import com.zyb.nowplus.data.protocol.NetworkListener;
import com.zyb.nowplus.data.protocol.response.ResponseListener;
import com.zyb.nowplus.data.protocol.transport.RPGConnection;
import com.zyb.nowplus.data.protocol.types.APIEvent;
import com.zyb.nowplus.data.protocol.types.Presence;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.nowplus.data.protocol.types.Update;
import com.zyb.nowplus.data.storage.DataStore;
import com.zyb.nowplus.data.storage.KeyValueStore;
import com.zyb.nowplus.data.storage.StorageException;
import com.zyb.util.ArrayUtils;
import com.zyb.util.TextUtilities;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.util.event.EventListener;

import de.enough.polish.content.ContentLoader;
import de.enough.polish.content.source.impl.HttpContentSource;
import de.enough.polish.content.source.impl.RMSContentStorage;
import de.enough.polish.content.source.impl.RMSStorageIndex;
import de.enough.polish.content.storage.StorageIndex;
import de.enough.polish.util.Locale;

//#if polish.blackberry
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.system.GlobalEventListener;
import com.zyb.util.BlackBerryConnectionSuffix;
//#endif

/**
 * Handles system events and provides services to the presentation layer.
 */
public class ServiceBroker implements Model, AuthenticationListener, ResponseListener, NetworkListener, EventListener
//#if activate.embedded.360email
, EmailListener
//#endif

//#if polish.blackberry
, GlobalEventListener
//#endif
{	
	private static final int PHASE_ROAMING_CHECK = 1;
	private static final int PHASE_REGISTERING = 3;
	private static final int PHASE_REGISTRATION_FAILED = 4;
	private static final int PHASE_UNAUTHENTICATED = 5;
	private static final int PHASE_AUTHENTICATING = 6;
	private static final int PHASE_AUTHENTICATION_FAILED = 7;
	private static final int PHASE_AUTHENTICATED = 8;
	private static final int PHASE_STOPPING = 9;
	
	public static final long SYNC_DELAY = 3 * 60 * 1000;
	private static final long SERVICE_UNAVAILABLE_WARNING_DELAY = 10000;

	private final MIDletContext context;
	private final EventDispatcher dispatcher;
	private final CommunicationManager protocol;	
	
	private final Settings settings;
	private final ContactManager contactsManager;
	private final ContactList contacts;
	private final ChatManager chatManager;
	//#if !polish.remove_status_tab
	private final ActivityManager activityManager;
	//#endif
	//#if activate.embedded.360email
	private final EmailManager emailManager;
	//#endif
	private final ChannelLauncher launcher;
	
	private boolean paused;
	private int authenticationPhase;
	private String currentUserName;
	private boolean currentStayLoggedIn;	
	private boolean addingSocialNetworkAccountsAtStartup;
	private boolean requestedMyChangesAtStartUp;
	private boolean requestedContactChangesAtStartup;
	private String upgradeUrl;
	
	/**
	 * So the user doesn't have to retype the password, we keep the 
	 * password entered in the sign up for the first authentication.
	 */
	private String signUpPassword;

	// temporary till authentication is fixed
	//#if activate.embedded.360email
	private String emailPassword;
	//#endif
	
	private Active[] actives;
	private int activesLen;
	
	private EventDispatcherTask meSyncTask;
	private EventDispatcherTask contactsSyncTask;
	
	private boolean isApplicationReady = false;
	
	/**
	 * Constructs a service broker.
	 */
	public ServiceBroker(MIDletContext context)
	{	
		//#debug info
		System.out.println("Constructing service broker (" + RPGConnection.RPG_URL + ").");
		
		this.context = context;				
		this.dispatcher = new RunnableEventDispatcher();

		this.protocol = new CommunicationManagerImpl(context); 
		this.protocol.registerListeners(this, this, this);
				 
		this.settings = new Settings();
		this.contactsManager = new ContactManager(this, dispatcher); 
		this.contacts = contactsManager.getContactList();
		this.chatManager = new ChatManager(this, dispatcher);
		//#if !polish.remove_status_tab
		this.activityManager = new ActivityManager(this, dispatcher);
		//#endif
		//#if activate.embedded.360email
		this.emailManager = new EmailManager(this);
		//#endif
		this.launcher = new ChannelLauncher(context, this);
		
		init();
	}

	/**
	 * Constructs a service broker with the given dependencies.
	 */
	public ServiceBroker(MIDletContext context, 
			KeyValueStore settingsStore, 
			DataStore contactsStore, 
			DataStore lifeDriveStore,
			DataStore friendsStreamStore,
			Sync syncManager,
			CommunicationManager protocol, 
			EventDispatcher dispatcher)
	{
		//#debug debug
		System.out.println("Constructing service broker.");
		
		this.context = context;
		this.dispatcher = dispatcher;
		
		this.protocol = protocol;
		this.protocol.registerListeners(this, this, this);

		this.settings = new Settings(settingsStore);
		this.contactsManager = new ContactManager(this, contactsStore, syncManager, dispatcher); 
		this.contacts = contactsManager.getContactList();
		this.chatManager = new ChatManager(this, dispatcher);
		//#if !polish.remove_status_tab
		this.activityManager = new ActivityManager(this, lifeDriveStore, friendsStreamStore, dispatcher);
		//#endif
		//#if activate.embedded.360email
		this.emailManager = new EmailManager(this);
		//#endif
		this.launcher = new ChannelLauncher(context, this);
		
		init();
	}
	
	private void init()
	{
		this.actives = new Active[4];
		this.activesLen = 0;
		
		Profile.manager = this.contactsManager;
		ExternalNetwork.manager = this.chatManager;
		Group.manager = this.contactsManager;
		//#if activate.embedded.360email
		Email.manager = this.emailManager;
		//#endif
	}
	
	/**
	 * Handles the application being started. 
	 */
	public void applicationStarted()
	{
		if (!paused) {
			//#debug info
			System.out.println("Starting service broker.");
			
			boolean fileCheck = context.checkFiles(new String[] {
					Settings.STORE, 
					ContactManager.STORE + "0", 
					//#if !polish.remove_status_tab
					ActivityManager.TIMELINE_STORE + "0", 
					ActivityManager.STATUSSTREAM_STORE + "0", 
					//#else
					null,null,
					//#endif
					CommunicationManagerImpl.AUTH_STORE});
			
			settings.init();
			
			if(settings.reauthenticate()) {
				context.deleteRecordStores(new String[] {CommunicationManagerImpl.AUTH_STORE});
			}
			
			if ((!settings.firstLogin() && !fileCheck)) {
				settings.flush();				
				context.deleteRecordStores(null);
				settings.init();
			}
			else if (settings.isUpgraded()) {	
				settings.flush();
				context.deleteRecordStores(new String[] {Settings.STORE, ContactManager.STORE, LightSyncEngine.STORE});
				settings.init();
			}

			dispatcher.notifyEvent(Event.Context.APP, Event.App.START);
			
			authenticationPhase = PHASE_ROAMING_CHECK;
			
			//new thread for avoiding delaying UI thread showing UI
			new Thread() {
				public void run()
				{
					if (protocol.isRoaming()) {
						roamingActive();
					}
					else {
						roamingAccepted();
					}
				}
			}.start();
		}
		else {			
			//#debug info
			System.out.println("Resuming service broker.");
			
			paused = false;
			dispatcher.notifyEvent(Event.Context.APP, Event.App.RESUME);
		}
	}

	private void roamingAccepted()
	{
		protocol.autodetectConnection(context.supportsTcp());
	}
	
	public synchronized void autodetectConnectionFinished() 
	{	
		if (Settings.contentLoader == null) {
			//#debug info
			System.out.println("Starting content loader");
			
			HttpContentSource http = new HttpContentSource("http");
			
			//#if testversion == false
			RMSContentStorage rms = new RMSContentStorage("rms", new RMSStorageIndex(500000));
			
			rms.attachSource(http);		
			
			Settings.contentLoader = new ContentLoader(new StorageIndex(500000),rms);
			//#else
			Settings.contentLoader = new ContentLoader(new StorageIndex(500000),http);
			//#endif
		}

		// Start update check in background thread.
		//#if polish.blackberry
			Thread updateThread = new Thread() {
				public void run()
				{
					protocol.checkForUpdates(getAppVersion(), null);
				}
			};
			updateThread.start();
		//#else
			// For unit tests we need to have a predictable order of events.
			protocol.checkForUpdates(getAppVersion(), null);
		//#endif

		protocol.clientInitialized();

		if (settings.firstLogin() || !settings.stayLoggedIn() || settings.reauthenticate()) {
			//#debug info
			System.out.println("Wait for upgrade check");
		}
		else {
			authenticate(null);
		}
	}

	public synchronized void clientUpdateAvailable(Update upgradeInfo) 
	{
		//#debug info
		System.out.println("Upgrade from " + getAppVersion() + " to " + upgradeInfo.getVersionNumber() + " available.");

		int updateType = Event.App.OPTIONAL_UPDATE_RECEIVED;

		if (upgradeInfo.isForcedUpdate()) {
			//#debug debug
			System.out.println("Update is mandatory");

			updateType = Event.App.MANDATORY_UPDATE_RECEIVED;
		}
		
		upgradeUrl = upgradeInfo.getUrl();
		String[] updateInfo = {upgradeInfo.getTitle(), upgradeInfo.getMessage(), upgradeInfo.getVersionNumber(), upgradeUrl};
		dispatcher.notifyEvent(Event.Context.APP, updateType, updateInfo);
	}
	
	public synchronized void clientIsUpToDate() 
	{
		//#debug info
		System.out.println("Client is up-to-date.");
	}

	public synchronized void clientInitialized()
	{
		if (settings.firstLogin()) {
			authenticationPhase = PHASE_UNAUTHENTICATED;		
			dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.FIRST_LOGIN);
		}
		else if (!settings.stayLoggedIn() || settings.reauthenticate()) {
			//#debug info
			System.out.println("settings.reauthenticate():"+settings.reauthenticate());

			authenticationPhase = PHASE_UNAUTHENTICATED;
			dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN);
		}
		else {
			protocol.startConnections();
		}
	}
	
	private void authenticated()
	{
		//#debug info
		System.out.println("Authenticated.");
		
		authenticationPhase = PHASE_AUTHENTICATED;			

		settings.setReauthenticate(false);
		
		chatManager.start();
		
		if (chatManager.hasNetworks()) {
			networksReceived();
		}
		else {
			//#debug info
			System.out.println("Waiting for networks...");
		}
	}
	
	void networksReceived()
	{
		//#debug info
		System.out.println("Networks available.");

		try {
			contactsManager.start();
	
			if (contactsManager.hasGroups()) {
				groupsReceived();
			}
			else {
				//#debug info
				System.out.println("Waiting for groups...");
			}		
		}
		catch (StorageException e) {
			//#debug error
			System.out.println("Failed to open contact store." + e);
	
			fireEvent(Event.Context.APP, Event.App.CONTACTS_CORRUPTED, null);
		}
	}
	
	void groupsReceived()
	{
		//#debug info
		System.out.println("Groups available.");
		
		// while importing from nab, get my profile from server
		requestedMyChangesAtStartUp = true;
		dispatcher.notifyEvent(Event.Context.MODEL, Event.Model.ME_CHANGED_IN_SAB);
		
		if (settings.firstLogin()) {
			dispatcher.attach(this);
			contactsManager.importFromNab(true);
		}
		else {
			contactsManager.importFromNab(false);
			nabImportFinished();
		}
	}

	void nabImportFinished()
	{
		//#debug info
		System.out.println("Contacts imported.");

		meSyncTask = dispatcher.scheduleEvents(Event.Context.MODEL, Event.Model.ME_CHANGED_IN_CAB, 0, SYNC_DELAY);
		contactsSyncTask = dispatcher.scheduleEvents(Event.Context.MODEL, Event.Model.CONTACTS_CHANGED_IN_CAB, 0, SYNC_DELAY);
		
		requestedContactChangesAtStartup = true;
		//#if !polish.remove_status_tab
		try {
			activityManager.start();
		}
		catch (StorageException e) {
			//#debug error
			System.out.println("Failed to open activities store." + e);
	
			fireEvent(Event.Context.APP, Event.App.CONTACTS_CORRUPTED, null);
		}
		//#endif	
		chatManager.setMyPresences(Channel.PRESENCE_ONLINE);
		
		if (contactsManager.getMe() == null 
			|| !chatManager.hasExternalNetworks()
			|| settings.hasAddedSocialNetworkAccount()) {
			addingSocialNetworkAccountsFinished();
		}
		else {
			addingSocialNetworkAccountsAtStartup = true;
			dispatcher.notifyEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.NO_WEB_ACCOUNT_ADDED);
		}
	}

	//overrided from super class Model
	public boolean isAddingSocialNetworkAccountsAtStartup()
	{
		return addingSocialNetworkAccountsAtStartup;
	}
	
	void addingSocialNetworkAccountsFinished()
	{
		//#debug info
		System.out.println("Social networks offered.");

		dispatcher.notifyEvent(Event.Context.MODEL, Event.Model.PRESENCES_CHANGED, null);
		
		//#debug info
		System.out.println("Application ready");
		
		dispatcher.notifyEvent(Event.Context.APP, Event.App.READY);
		
		//#if activate.embedded.360email
		emailManager.start(settings.getUserName(), emailPassword);
		//#endif
		
		fireEvent(Event.Context.CHAT, Event.Chat.READY_FOR_INCOMING_MESSAGES, null);
	}
	
	/**
	 * Handles the application being paused. 
	 */
	public void applicationPaused()
	{
		if (!paused) {
			dispatcher.notifyEvent(Event.Context.APP, Event.App.PAUSE);
			paused = true;
			
			//#debug info
			System.out.println("Service broker paused.");
		}
	}
	
	/**
	 * Handles the application being stopped.
	 */
	public void applicationStopped(boolean deleteStorageBefore)
	{
		authenticationPhase = PHASE_STOPPING;
		
		try {
			contactsManager.stop();
		}
		catch (Exception e) {
			//#debug error
			System.out.println("Failed to stop contacts manager." + e);
		}
		
		try {
			if (meSyncTask != null) {
				meSyncTask.cancel();
			}

			if (contactsSyncTask != null) {
				contactsSyncTask.cancel();
			}

			//#debug info
			System.out.println("Sync timers stopped.");
		} 
		catch (Exception e) {
			//#debug error
			System.out.println("Failed to stop sync timers." + e);
		}
		
		dispatcher.notifyEvent(Event.Context.APP, Event.App.STOP);
		//#if !polish.remove_status_tab
		try {
			activityManager.stop();
		}
		catch (Exception e) {
			//#debug error
			System.out.println("Failed to stop activity manager." + e);			
		}
		//#endif
		try {
			chatManager.stop();
		}
		catch (Exception e) {
			//#debug error
			System.out.println("Failed to stop chat manager." + e);
		}

		//#if activate.embedded.360email
		try {
			emailManager.stop();
		}
		catch (Exception e) {
			//#debug error
			System.out.println("Failed to stop email protocol." + e);			
		}
		//#endif	
		
		try {
			launcher.stop();
		}
		catch (Exception e) {
			//#debug error
			System.out.println("Failed to stop launcher." + e);			
		}
		
		try {
			if (Settings.contentLoader != null) {
				Settings.contentLoader.shutdown();
			}
		}
		catch (Exception e) {
			//#debug error
			System.out.println("Failed to stop content source." + e);
		}

		try {
			protocol.stopConnections(true);
		}
		catch (Exception e) {
			//#debug error
			System.out.println("Failed to stop protocol." + e);
		}
		
		settings.flush();
		
		dispatcher.waitForFinish();
		
		if (deleteStorageBefore) {
			context.deleteRecordStores(null);
		}
		
		TextUtilities.cleanUpStaticResources();
		
		//#debug info
		System.out.println("Service broker stopped.");
	}

	public String getAppVersion()
	{
		return settings.getAppVersion();
	}
	
	public String getUserName()
	{
		return (currentUserName != null) ? currentUserName : settings.getUserName();
	}
	
	public synchronized boolean stayLoggedIn()
	{
		return (currentUserName != null) ? currentStayLoggedIn : settings.stayLoggedIn();
	}
	
	public String getMsisdn()
	{
		return context.getMsisdn();
	}
	public synchronized void signup(String userName, String password, String msisdn,String userEmailAddr, Date dateOfBirth, boolean acceptedTnC)
	{
		//#debug info
		System.out.println("Signing up...");

		authenticationPhase = PHASE_REGISTERING;
		
		currentUserName = userName;
		currentStayLoggedIn = settings.stayLoggedIn();
		
		signUpPassword = password;
		
		if (msisdn.startsWith("+"))
		{
			msisdn = msisdn.substring(1);
		}
		
		Calendar c = Calendar.getInstance();
		c.setTime(dateOfBirth);
		
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		
		String dateOfBirthAsString = year + ((month < 10) ? "-0" : "-") + month 
		+ ((day < 10) ? "-0" : "-") + day + "T00:00:00";
		
		String language = context.getCurrentLanguage();
		
		// optional fields
		String fullName = null;
		boolean subscribeToNewsletter = false;
		
		String countryCode = null; 
		String email=userEmailAddr;
		String timezone = null;
		
		int mobileOperatorId = 0;
		int mobileModelId = 0;
		
		//#debug info
		System.out.println("Register user " + userName + " " + password + " " + fullName + " " + dateOfBirthAsString 
				+ " " + msisdn + " " + acceptedTnC + " " + countryCode + " "+email+" "+ timezone + " " + language 
				+ " " + mobileOperatorId + " " + mobileModelId + " " + subscribeToNewsletter);
		
		protocol.registerUser(userName, password, fullName, dateOfBirthAsString,
				msisdn, acceptedTnC, countryCode, email, timezone, language, 
				mobileOperatorId, mobileModelId, subscribeToNewsletter, null);
	}

	public synchronized void cancelSignUp()
	{
		if (authenticationPhase == PHASE_REGISTERING) {
			currentUserName = null;
			authenticationPhase = PHASE_REGISTRATION_FAILED;
		}
	}
	
	public synchronized void rerequestConfirmationSMS(String msisdn)
	{
		//#debug info
		System.out.println("Rerequesting sms...");

		authenticationPhase = PHASE_REGISTERING;

		if (msisdn.startsWith("+")) {
			msisdn = msisdn.substring(1);
		}
		
		//#debug info
		System.out.println("Rerequesting user " + currentUserName+ " " + msisdn);
		
		protocol.rerequestActivationCodeForUser(currentUserName,msisdn);
	}

	public synchronized void registrationSucceeded(long userId)
	{
		if (authenticationPhase == PHASE_REGISTERING) {
			//#debug info
			System.out.println("Sign up OK (user id = " + userId + ")");
			
			dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.SIGNUP_SUCCEEDED);	
		}
	}
	
	public synchronized void registrationFailed(int errorCode)
	{
		if (authenticationPhase == PHASE_REGISTERING) {
			//#debug info
			System.out.println("Sign up FAILED (" + errorCode + ")");
			
			authenticationPhase = PHASE_REGISTRATION_FAILED;
			
			String errorText = null;
			
			if (errorCode == AuthenticationListener.ACTIVATION_TIMED_OUT) {
				if (Settings.WAIT_FOR_ACTIVATION_SMS) {
					dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.SIGNUP_FAILED_WRONG_MSISDN, errorText);	
				}
				else {
					dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.SIGNUP_SUCCEEDED);	
				}

				return;
			}
			
			//TODO move localisation to presentation layer
			switch (errorCode) {
				case AuthenticationListener.USERNAME_MISSING :
				errorText = Locale.get("nowplus.client.java.signup.signupfailed.usernamenotspecified");
				break;
					
				case AuthenticationListener.USERNAME_BLACKLISTED :
				errorText = Locale.get("nowplus.client.java.signup.signupfailed.usernameblacklisted");
				break;
				
				case AuthenticationListener.USERNAME_FORBIDDEN :
				errorText = Locale.get("nowplus.client.java.signup.signupfailed.usernameforbidden");
				break;
				
				case AuthenticationListener.USERNAME_IN_USE :
				errorText = Locale.get("nowplus.client.java.signup.signupfailed.usernamealreadytaken");
				break;
				
				case AuthenticationListener.PASSWORD_MISSING :
				errorText = Locale.get("nowplus.client.java.signup.signupfailed.passwordnotspecified");
				break;
				
				case AuthenticationListener.PASSWORD_INVALID :
				errorText = Locale.get("nowplus.client.java.signup.signupfailed.passwordinvalid");
				break;
				
				case AuthenticationListener.ACCEPT_TC_MISSING :
				errorText = Locale.get("nowplus.client.java.signup.signupfailed.tcnotaccepted");
				break;
			
				case AuthenticationListener.D_O_B_INVALID :
				errorText = Locale.get("nowplus.client.java.signup.signupfailed.dobinvalid");
				break;
	
				case AuthenticationListener.MSISDN_MISSING :
				errorText = Locale.get("nowplus.client.java.signup.signupfailed.msisdnmissing");
				break;
	
				case AuthenticationListener.MSISDN_INVALID :
				errorText = Locale.get("nowplus.client.java.signup.signupfailed.msisdninvalid");
				break;
			
				case AuthenticationListener.MOBILE_MODEL_INVALID :
				errorText = Locale.get("nowplus.client.java.signup.signupfailed.mobilemodelinvalid");
				break;
			
				case AuthenticationListener.LANGUAGE_INVALID :
				errorText =	Locale.get("nowplus.client.java.signup.signupfailed.languageinvalid");
				break;
	
				default :
				errorText = Locale.get("nowplus.client.java.signup.signupfailed.internalerror");
			}
			
			dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.SIGNUP_FAILED, errorText);
		}
	}
	
	public synchronized void finishSignUp(boolean stayLoggedIn)
	{
//		settings.setStayLoggedIn(stayLoggedIn);
//		currentStayLoggedIn = false;
		
//		applicationStarted3();
		
		currentStayLoggedIn = stayLoggedIn;
		authenticate(signUpPassword);
		
		signUpPassword = null;
	}
	
	public synchronized void login(String userName, String password, boolean stayLoggedIn)
	{
		if (settings.getUserName() == null || settings.getUserName().equals(userName)) {
			currentUserName = userName;
			currentStayLoggedIn = stayLoggedIn;
			authenticate(password);
		}
		else {
			if (contactsManager.isRunning()) {
				dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.USER_NAME_CHANGE_NOT_ALLOWED);
			}
			else {
				if (currentUserName != null && currentUserName.equals(userName)) {
					settings.flush();
					context.deleteRecordStores(null);
					settings.init();
					
					currentStayLoggedIn = stayLoggedIn;
					authenticate(password);
				}
				else {
					currentUserName = userName;
					dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.CONFIRM_USER_NAME_CHANGE);
				}
			}
		}
	}
	
	public synchronized void cancelLogin()
	{
		if (authenticationPhase == PHASE_AUTHENTICATING) {
			authenticationPhase = PHASE_AUTHENTICATION_FAILED;
		}
	}
	
	private void authenticate(String password)
	{	
		authenticationPhase = PHASE_AUTHENTICATING;

		//#debug info
		System.out.println("Authenticating...");
		
		//#if activate.embedded.360email
		emailPassword = password;
		//#endif
		
		if (password == null) {
			// auto login
			authenticated();
		}
		else {
			protocol.authenticate(currentUserName, password, currentStayLoggedIn, null);			
		}
	}

	public synchronized void authenticationSucceeded() 
	{
		if (authenticationPhase == PHASE_AUTHENTICATING) {
			//#debug info
			System.out.println("Authenticated OK (user id = " + RPGConnection.userID + ")");
			
			settings.setUserDetails(currentUserName);
			currentUserName = null;
			
			settings.setStayLoggedIn(currentStayLoggedIn);
			currentStayLoggedIn = false;
			
			dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN_SUCCEEDED);

			protocol.startConnections();
			
			authenticated();
		}
	}
	
	public synchronized void authenticationFailed(int authCode) 
	{
		//#debug info
		System.out.println("Authentication FAILED (" + authCode + ")");

		if (authenticationPhase == PHASE_AUTHENTICATING) {			
			authenticationPhase = PHASE_AUTHENTICATION_FAILED;

			if (authCode == AUTH_FAILED_UNKNOWN) {
				dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.NO_SERVICE);
				fireServiceUnavailableWarning();
			}
			else {
				dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.LOGIN_FAILED);
			}
		}
		else if (authenticationPhase == PHASE_AUTHENTICATED) {
			authenticationPhase = PHASE_AUTHENTICATION_FAILED;

			if (authCode == AUTH_FAILED_UNKNOWN) {
				fireServiceUnavailableWarning();
			}
			else if(authCode == AUTH_NEW_PASSWORD) {
				//If there is a new password we need to wipe secret and restart
				settings.setReauthenticate(true);

				protocol.stopConnections(false);

				dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.AUTHENTICATION_FAILED_NEW_PASSWORD);
			}
			else {
				dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.AUTHENTICATION_FAILED);
			}
		}
	}
	
	public synchronized void userDisallowedConnection()
	{
		//#debug info
		System.out.println("Upgrade check failed (user disallowed connection)");
		
		// authenticationPhase = PHASE_AUTHENTICATION_FAILED;
		dispatcher.notifyEvent(Event.Context.AUTHENTICATION, Event.Authentication.USER_DISALLOWED_CONNECTION);
	}
	
	public void upgradeApplication() throws LaunchException
	{
		if (upgradeUrl != null) {
			launch(upgradeUrl);
		}
	}
		
	public MyProfile getMe()
	{
		return contactsManager.getMe();
	}
	
	public void setMyStatus(String status) throws LockException
	{
		contactsManager.setMyStatus(status);
		//#if !polish.remove_status_tab
		activityManager.addStatusActivity(getMe(), status, ExternalNetwork.VODAFONE_360);
		//#endif
	}
	
	public void setMyNowPlusPresence(int presence)
	{
		chatManager.setMyPresences(presence);
	}
	
	public ExternalNetwork[] getAvailableSocialNetworks()
	{
		return chatManager.getAvailableExternalNetworks();
	}
	
	public void addSocialNetworkAccount(ExternalNetwork network, String name, String password, boolean importContacts)
	{
		chatManager.addSocialNetworkAccount(network, name, password, importContacts);
	}
	
	public void finishAddingSocialNetworkAccounts()
	{		
		settings.setSocialNetworkAccountAdded();

		if (addingSocialNetworkAccountsAtStartup) {
			addingSocialNetworkAccountsAtStartup = false;
			addingSocialNetworkAccountsFinished();
		}
	}
	
	public void skipAddingSocialNetworkAccounts()
	{
		finishAddingSocialNetworkAccounts();  // don't ask again
		
//		if (addingSocialNetworkAccountsAtStartup) {
//			addingSocialNetworkAccountsAtStartup = false;
//			addingSocialNetworkAccountsFinished();
//		}
	}
	
	public void removeSocialNetworkAccount(Identity account)
	{
		chatManager.removeSocialNetworkAccount(account);
	}
	
	public void loginSocialNetworkAccount(Identity account)
	{
		chatManager.loginSocialNetworkAccount(account);
	}
	
	public void logoutSocialNetworkAccount(Identity account)
	{
		chatManager.logoutSocialNetworkAccount(account);
	}

	public Filter[] getContactFilters()
	{
		return contactsManager.getAvailableFilters();
	}
	
	public void setContactsFilter(Filter filter)
	{
		contacts.setPrimaryFilter(filter);
	}
		
	public void setTextFilter(String text)
	{
		if (text == null || text.equals("")) {
			contacts.setSecondaryFilter(null);
		}
		else {
			contacts.setSecondaryFilter(new NameFilter(text));
		}
	}
	
	public void setContactsOrder(Order order)
	{
		settings.setContactsOrder(order.getType());
		contacts.setOrder(order);
	}
	
	public Order getContactsOrder()
	{
		return contacts.getOrder();
	}
	
	public ContactProfile getFirstContact(String text)
	{
		return contacts.getFirstContact(text);
	}
	
	public ContactProfile getFirstContact()
	{
		return contacts.getFirstContact();
	}
	
	public ContactProfile getLastContact()
	{
		return contacts.getLastContact();
	}
	
	public ListSelection getContacts(ContactProfile contact, int number)
	{
		return contacts.getContacts((contact == null) ? 0L : contact.getCabId(), number);
	}
	
	public ListSelection getContacts(int start, int end) {
		return contacts.getContacts(start,end);
	}
	
	public int getContactsSize() {
		return contacts.getContactsSize();
	}
	
	public void selectContact(ContactProfile contact)
	{
		contacts.selectContact((contact == null) ? 0 : contact.getCabId());
	}
	
	public ContactProfile getSelectedContact()
	{
		return contacts.getSelectedContact();
	}

	public ContactProfile createContact()
	{
		return contactsManager.createContact();
	}
	
	public void finishedEditing(ManagedProfile profile)
	{
		contactsManager.finishedEditing(profile);
	}
	
	public void delete(final ContactProfile contact)
	{
	
	/*
	 * This is an unnecessary hack. Events are already threaded in the Controller class
	 * so there is absolutely no need to add even more threads into that cocktail.
	 * 
	//Puts Event of Deleting Contact into this application's event queue.
	//for fixing bug of 'application freezes'
		
	//#if polish.blackberry
		net.rim.device.api.ui.UiApplication.getUiApplication().invokeLater(new Runnable()
		{
			public void run()
			{
	//#endif
	 */
			contactsManager.delete(contact, false);

	/*
	//#if polish.blackberry
			}
		});
	//#endif
	 */
	}
	
	public void invite(ContactProfile contact) throws InvalidValueException
	{
		contactsManager.invite(contact);
	}
	
	public void connect(ContactProfile contact) throws InvalidValueException
	{
		contactsManager.connect(contact);
	}

	public Group[] getAvailableGroups()
	{
		return contactsManager.getAvailableGroups();
	}
	
    public void cancelImportFromNab() 
    {
        contactsManager.cancelImportFromNab();
    }
	
	public void sync()
	{
		dispatcher.notifyEvent(Event.Context.MODEL, Event.Model.ME_CHANGED_IN_CAB, Boolean.TRUE);
		dispatcher.notifyEvent(Event.Context.MODEL, Event.Model.CONTACTS_CHANGED_IN_CAB, Boolean.TRUE);
	}
	
	public long[] getSyncableContactCabIds()
	{
		return contacts.getSyncableContactCabIds();
	}
	
	public ContactProfile getContact(long cabId)
	{
		return contacts.getContact(cabId);
	}
		
	public ListSelection getLifeDrive(int from, int to)
	{
		//#if !polish.remove_status_tab
		return activityManager.getTimeline(from,to);
		//#else
		return null;
		//#endif
	}
	
	public ListSelection getFriendsStream(int from, int to)
	{
		//#if !polish.remove_status_tab
		return activityManager.getStatusStream(from,to);
		//#else
		return null;
		//#endif
	}
	
	//#if activate.embedded.360email
	public void openFolder(String folderName) 
	{
		emailManager.openFolder(folderName);
	}
	
	public void refreshFolder() 
	{
		emailManager.refreshFolder();
	}
	
	public void closeFolder() 
	{
		emailManager.closeFolder();
	}

	public String getCurrentFolder()
	{
		return emailManager.getCurrentFolder();
	}
	
	public String[] getAvailableFolders()
	{
		return emailManager.getAvailableFolders();
	}
	
	public EmailMessage[] getAvailableMessages()
	{
		return new EmailMessage[0];
	}
	
	public Email[] getAvailableMessages2()
	{
		return emailManager.getAvailableMessages();
	}
	
	public void openMessage(int id)
	{
		emailManager.openMessage(id);
	}
	
	public EmailMessage getCurrentMessage()
	{
		return null;
	}
	
	public Email getCurrentMessage2()
	{
		return emailManager.getCurrentMessage();
	}
	
	public void sendMessage()
	{
		// to test
		EmailMessage message = new EmailMessage();
		message.setDate(new Date().toString());
		message.setSubject("Test");
		
		EmailAddress from = new EmailAddress();
		from.setHostName("webmail.sp.vodafone.com");
		from.setMailboxName("gjmgjm");
		message.getFroms().addElement(from);
		
		EmailAddress to = new EmailAddress();
		from.setHostName("tesco.net");
		from.setMailboxName("mark.hoogenboom");
		message.getTos().addElement(to);
		
		message.setMessageBody("This is a test.");
		
		emailManager.sendMessage(message);
	}
	//#endif
	
	public void launch(String url) throws LaunchException
	{
		launcher.launch(url);
	}
	
	public void launch(Channel channel) throws LaunchException
	{
		launcher.launch(channel);
	}
	
	public void launchCancelled()
	{
		launcher.launchCancelled();
	}
	
	public void close(Channel channel)
	{
		launcher.close(channel);
	}

	public void sendSms(String number, String message, Object data) 
	{
		//#if polish.device.supports.nativesms == false
			launcher.sendSms(number, message, data);
		//#endif
	}
	
	public void sendChatMessage(Channel channel, Message message) throws LaunchException
	{
		chatManager.sendMessage(channel, message);
	}
	
	public void setRoamingDataConnectionAllowed(boolean roamingDataConnectionAllowed)
	{
		settings.setRoamingAllowed(roamingDataConnectionAllowed);
	}
	
	public boolean getRoamingDataConnectionAllowed()
	{
		return settings.isRoamingAllowed();
	}
	
	public void acceptRoaming()
	{
		if (authenticationPhase == PHASE_ROAMING_CHECK) {
			roamingAccepted();
		}
	}
	
	public int getFeedback()
	{
		int feedback = 0;

		if (isConnectionUp()) {
			if (contactsManager.isInteractingWithNab()) {
				feedback |= FEEDBACK_NAB_INTERACTION;
			}
			
			if (contactsManager.isInteractingWithSab()) {
				feedback |= FEEDBACK_SAB_INTERACTION;
			}
		}
		else {
			feedback |= FEEDBACK_CONNECTION_DOWN;
		}

		return feedback;
	}
	
	public boolean isConnectionUp()
	{
		return settings.isConnectionUp();
	}
	
	public int getDataCounter()
	{
		return settings.getDataCounter();
	}
	
	public void resetDataCounter()
	{
		settings.resetDataCounter();
		
		// fireEvent(Event.Context.APP, Event.App.TRAFFIC, null);
	}
	
	public void attach(EventListener listener)
	{
		dispatcher.attach(listener);
	}

	public void detach(EventListener listener) 
	{
		dispatcher.detach(listener);
	}

	public void attachActive(Active active) 
	{
		synchronized (actives) {
			if (activesLen == actives.length) {
				actives = ArrayUtils.extendArray(actives);
			}

			actives[activesLen++] = active;
		}
	}
	
	public void detachActive(Active active)
	{
		synchronized (actives) {
			int i = 0;

			for (int j = 0; (j < activesLen); j++) {
				if (actives[j] == active) {
					// remove
				}
				else {
					actives[i++] = actives[j];
				}
			}

			activesLen = i;
		}
	}
	
	public boolean isBusy()
	{
	//#if polish.blackberry
		boolean isForeground=net.rim.device.api.system.Application.getApplication().isForeground();
		
		if(!isForeground)//app running in background
			return false;//free app to be in low power mode for saving battery @see class HttpRPGConnection#run()
		
		boolean isBackLightOn=net.rim.device.api.system.Backlight.isEnabled();
		  
		if(!isBackLightOn)//backlight is off 
			return false;//free app (Not Busy) @see class HttpRPGConnection#run()
	//#endif
		
		boolean busy = false;
		
		synchronized (actives) {
			for (int i = 0; (i < activesLen) && !busy; i++) {
				busy = actives[i].isBusy();
			}
		}
		
		return busy;
	}
	
	public void exit(final boolean deleteStorageBefore)
	{
		//#debug closedown
		System.out.println("Exit requested");
		
		new Thread(new Runnable() {
			public void run()
			{
				context.exit(deleteStorageBefore);				
			}
		}, "exit").start();
	}

	public void itemsReceived(int requestId, ServiceObject[] serviceObjects, byte serviceObjectType)
	{			
		if (authenticationPhase == PHASE_STOPPING) {
			return;
		}

		if (serviceObjects == null) {
			//#debug error
			System.out.println("Received null response of type " + serviceObjectType + " to request " + requestId);
		}
		else {
			//#debug info
			System.out.println("Received " + serviceObjects.length + " responses of type " + serviceObjectType + " to request " + requestId);

			try {
				if (serviceObjectType == ServiceObject.AVAILABLE_IDENTITY)
				{
					chatManager.identitiesReceived(requestId, serviceObjects);
				}
				else
				if (serviceObjectType == ServiceObject.GROUPS)
				{
					contactsManager.groupsReceived(requestId, serviceObjects);
				}
				else
				if (serviceObjectType == ServiceObject.MY_CHANGES)
				{
					contactsManager.meChangesReceived(requestId, serviceObjects);
				}
				else
				if (serviceObjectType == ServiceObject.SET_ME_RESULT)
				{
					contactsManager.meChangesResultReceived(requestId, serviceObjects);
				}
				else
				if (serviceObjectType == ServiceObject.CONTACT_CHANGES)
				{
					contactsManager.contactChangesReceived(requestId, serviceObjects);
				}
				else
				if (serviceObjectType == ServiceObject.BULK_UPDATE_CONTACTS_RESULT)
				{
					contactsManager.contactChangesResultReceived(requestId, serviceObjects);
				}
				else 
				if (serviceObjectType == ServiceObject.SET_CONTACT_GROUP_RELATIONS_RESULT)
				{
					contactsManager.contactGroupChangesResultReceived(requestId, serviceObjects);
				}
			    if (serviceObjectType == ServiceObject.MY_IDENTITY)
			    {
			    	chatManager.myIdentitiesReceived(requestId, serviceObjects);
			    }
			    else
			    if (serviceObjectType == ServiceObject.ADD_IDENTITY_RESULT)
			    {
			    	chatManager.socialNetworkAccountAddedResultReceived(requestId, serviceObjects);
			    }
			    else
			    if (serviceObjectType == ServiceObject.DELETE_IDENTITY_RESULT)
			    {
			    	chatManager.socialNetworkAccountRemovedResultReceived(requestId, serviceObjects);
			    }
			    else
			    if (serviceObjectType == ServiceObject.SET_IDENTITY_STATUS_RESULT)
			    {
			    	chatManager.socialNetworkLoginLogoutResultReceived(requestId, serviceObjects);
			    }
			    if (serviceObjectType == ServiceObject.START_CHAT_CONVERSATION)
			    {
			    	chatManager.conversationStarted(requestId, serviceObjects);
			    }
			    else
			    if (serviceObjectType == ServiceObject.CHAT_MESSAGE)
			    {
			    	chatManager.messageReceived(requestId, serviceObjects);
			    }
			    //#if !polish.remove_status_tab
			    else 
			    if (serviceObjectType == ServiceObject.ACTIVITY) {
			    	activityManager.activitiesReceived(requestId, serviceObjects);
			    }
			    //#endif
			}
			catch (Exception e) {
				//#debug error
				System.out.println("Exception while processing response." + e);
			}
		}
	}

	public void itemsReceived(int requestId, byte[] data, byte itemType)
	{
		// empty (obviously)
	}
		
	public void presenceChangeReceived(int requestId, Presence presencesList)
	{
		if (authenticationPhase == PHASE_STOPPING) {
			return;
		}

		if (presencesList == null) {
			//#debug error
			System.out.println("Received null presence list to request " + requestId);
		}
		else {
			//#debug info
			System.out.println("Received presence list to request " + requestId);
		
			chatManager.presencesChanged(presencesList);
		}
	}
	
	public void pushReceived(APIEvent apiEvent)
	{
		if (authenticationPhase == PHASE_STOPPING) {
			return;
		}

		if (null == apiEvent) {
			//#debug error
			System.out.println("Received null push");
		}
		else {
			//#debug info
			System.out.println("Received push of type " + apiEvent.getType());
		
			if (apiEvent.getType() == APIEvent.PROFILE_CHANGE) {
				if (requestedMyChangesAtStartUp) {
					dispatcher.notifyEvent(Event.Context.MODEL, Event.Model.ME_CHANGED_IN_SAB);
				}
			}
			else if (apiEvent.getType() == APIEvent.IDENTITY_CHANGE) {
				dispatcher.notifyEvent(Event.Context.MODEL, Event.Model.MY_IDENTITIES_CHANGED_IN_SAB);
			}
			else if (apiEvent.getType() == APIEvent.CONTACTS_CHANGE) {
				if (requestedContactChangesAtStartup) {
					dispatcher.notifyEvent(Event.Context.MODEL, Event.Model.CONTACTS_CHANGED_IN_SAB);
				}
			}
			else if (apiEvent.getType() == APIEvent.GROUP_CHANGE) {
				dispatcher.notifyEvent(Event.Context.MODEL, Event.Model.GROUPS_CHANGED);
			}
			else if (apiEvent.getType() == APIEvent.TIMELINE_CHANGE) {
				dispatcher.notifyEvent(Event.Context.MODEL, Event.Model.TIMELINE_CHANGED);
			}
			else if (apiEvent.getType() == APIEvent.STATUS_CHANGE) {
				dispatcher.notifyEvent(Event.Context.MODEL, Event.Model.STATUS_STREAM_CHANGED);
			}
		}
	}

	public void errorReceived(int requestId, byte errorCode)
	{
		if (authenticationPhase == PHASE_STOPPING) {
			return;
		}

		//#debug info
		System.out.println("Received error " + errorCode + " to request " + requestId);

		if (errorCode == REQUEST_TIMED_OUT) {
			if (!contactsManager.timeOutReceived(requestId)) {
				if (!chatManager.timeOutReceived(requestId)) {
					//#if !polish.remove_status_tab
					if (!activityManager.timeOutReceived(requestId)) {
						// no one is interested
					}
					//#endif
				}
			}
		}
		else {
			if (errorCode == REQUEST_SERVER_UNREACHABLE) {
				fireServiceUnavailableWarning();
			}

			// the managers need to be informed of problems too!
			if (!contactsManager.errorReceived(requestId, errorCode)) {
				if (!chatManager.errorReceived(requestId, errorCode)) {
					//#if !polish.remove_status_tab
					if (!activityManager.errorReceived(requestId, errorCode)) {
						// no one is interested
					}
					//#endif
				}
			}
		}
	}
	
	private long latestServiceUnavailableWarning;
	
	private void fireServiceUnavailableWarning() 
	{
		if (latestServiceUnavailableWarning + SERVICE_UNAVAILABLE_WARNING_DELAY < System.currentTimeMillis()) {
			latestServiceUnavailableWarning = System.currentTimeMillis();
			dispatcher.notifyEvent(Event.Context.APP, Event.App.SERVICE_UNAVAILABLE);
		}
	}
	
	public synchronized void networkDown()
	{
		if (authenticationPhase == PHASE_STOPPING) {
			return;
		}

		if (settings.isConnectionUp()) {
			//#debug info
			System.out.println("Network connection went down.");
			
			settings.setConnectionUp(false);
		    fireEvent(Event.Context.APP, Event.App.CONNECTION_DOWN, null);
		}
	}

	public synchronized void networkUp() 
	{
		if (authenticationPhase == PHASE_STOPPING) {
			return;
		}

		if (!settings.isConnectionUp()) {
			//#debug info
			System.out.println("Network connection came up.");
			
			settings.setConnectionUp(true);
			fireEvent(Event.Context.APP, Event.App.CONNECTION_UP, null);
		}
	}

	public void roamingActive() 
	{
		if (authenticationPhase == PHASE_STOPPING) {
			return;
		}

		if (settings.isRoamingAllowed() || settings.isRoamingAccepted()) {
			acceptRoaming();
		}
		else {
			settings.setRoamingAccepted();
			fireEvent(Event.Context.APP, Event.App.CONNECTION_ROAMING, null);
		}
	}

	public void dataTransmitted(int dataCounter) 
	{
		if (authenticationPhase == PHASE_STOPPING) {
			return;
		}

		settings.addDataCounter(dataCounter);
		// fireEvent(Event.Context.APP, Event.App.TRAFFIC, null);
	}

	//#if activate.embedded.360email
	public void emailResponseReceived(EmailResponse response)
	{
		if (response.getState() != EmailResponse.STATE_OK) {
			emailManager.errorOccurred();
		}
		else if (response instanceof EmailLoginResponse) {
			emailManager.loggedIn((EmailLoginResponse) response);
		}
		else if (response instanceof EmailFolderResponse) {	
			emailManager.receivedFolders((EmailFolderResponse) response);
		}
		else if (response instanceof EmailUseFolderResponse) {
			emailManager.selectedFolder((EmailUseFolderResponse) response);
		}
		else if (response instanceof EmailMessageResponse) {
			emailManager.receivedMessages((EmailMessageResponse) response);
		}
		else if (response instanceof EmailAppendMessageResponse) {
			emailManager.appendedMessage((EmailAppendMessageResponse) response);
		}
		else {
			//#debug error
			System.out.println("Received email response " + response);
		}
	}
	//#endif
	
	/**
	 * Gets the settings of the user of the application.
	 */
	public Settings getSettings()
	{
		return settings;
	}
	
	/**
	 * Gets the time in ms the current session started.
	 */
	public final long getStartOfSession()
	{
		return settings.getStartOfSession();
	}
	
	/**
	 * Gets the chat manager this servicebroker uses to manage chat.
	 */
	final ChatManager getChatManager()
	{
		return chatManager;
	}
	
	/**
	 * Gets the activity manager this servicebroker uses to manage activities.
	 */
	//#if !polish.remove_status_tab
	final ActivityManager getActivityManager()
	{
		return activityManager;
	}
	//#endif
	/**
	 * Gets the email manager this servicebroker uses to manage email.
	 */
	//#if activate.embedded.360email
	final EmailManager getEmailManager()
	{
		return emailManager;
	}	
	//#endif
	
	/**
	 * Gets the communication manager this servicebroker uses to
	 * communicate with the server.
	 */
	final CommunicationManager getProtocol()
	{
		return protocol;
	}
	
	final void fireEvent(byte context, int eventId, Object data)
	{
		dispatcher.notifyEvent(context, eventId, data);
	}
	
	final void cancelEvent(byte context, int eventId, Object data)
		throws InterruptedException
	{
		dispatcher.cancelEvent(context, eventId, data);
	}
	
	final EventDispatcherTask scheduleEvents(byte context, int eventId, long delay)
	{
		return dispatcher.scheduleEvents(context, eventId, delay);
	}

	public byte getContext() {
		return 0;
	}

	public void handleEvent(byte context, int event, Object data)
	{
		if (context == Event.Context.SYNC) {
			if (event == Event.Sync.SUCCESSFULL || event == Event.Sync.FAILED || event == Event.Sync.CANCELLED) {
				dispatcher.detach(this);
				nabImportFinished();
			}
		}
		else if (context == Event.WebAccounts.ADD_SUCCEEDED) {
			this.contactsManager.requestGroups();
		}else if(context == Event.App.READY){
			isApplicationReady = true;
		}
	}
	
	
	public boolean isApplicationReady(){
		return this.isApplicationReady;
	}
	
	public boolean isAuthenticated()
	{
		return authenticationPhase == PHASE_AUTHENTICATED;
	}

	//#mdebug error
	public String toString()
	{
		return "ServiceBroker[protocol=" + protocol
			+ ",dispatcher=" + dispatcher
			+ ",settings=" + settings
			+ ",contactsManager=" + contactsManager
//			+ ",searchManager=" + searchManager
//			+ ",activityManager=" + activityManager
			+ ",chatManager=" + chatManager
			+ ",launcher=" + launcher
			+ "]";
	}
	//#enddebug
	
	public void msisdnReceived(String msisdn) 
	{
		// TODO
	}

	public void invalidateAllOnlinePresences()
	{
		this.contacts.invalidateAllOnlinePresences();
	}

	//#if polish.blackberry
	/**
	 * Invoked when the specified global event occured. The eventOccurred method
	 * provides two object parameters and two integer parameters for supplying
	 * details about the event itself. The developer determines how the
	 * parameters will be used.
	 * 
	 * For example, if the event corresponded to sending or receiving a mail
	 * message, the object0 parameter might specify the mail message itself,
	 * while the data0 parameter might specify the identification details of the
	 * message, such as an address value.
	 * 
	 * @param guid
	 *            - The GUID of the event.
	 * @param data0
	 *            - Integer value specifying information associated with the
	 *            event.
	 * @param data1
	 *            - Integer value specifying information associated with the
	 *            event.
	 * @param object0
	 *            - Object specifying information associated with the event.
	 * @param object1
	 *            - Object specifying information associated with the event.
	 */
	public void eventOccurred(long guid, int data0, int data1, Object object0,
			Object object1)
	{
		if (guid == ServiceBook.GUID_SB_ADDED
				|| guid == ServiceBook.GUID_SB_CHANGED
				|| guid == ServiceBook.GUID_SB_OTA_SWITCH
				|| guid == ServiceBook.GUID_SB_OTA_UPDATE
				|| guid == ServiceBook.GUID_SB_POLICY_CHANGED
				|| guid == ServiceBook.GUID_SB_REMOVED)
			BlackBerryConnectionSuffix.checkConnectionSuffixStr();
	}
	//#endif
}
