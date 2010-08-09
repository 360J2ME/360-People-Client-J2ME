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
package com.zyb.util.event;

import com.zyb.util.HashUtil;

/**
 * @author Andre Schmidt
 *
 */
public class Event {

	/**
	 * The context for events
	 * @author Andre Schmidt
	 */
	public static final class Context
	{
		public final static byte ALL = Byte.MAX_VALUE;
		public final static byte APP = 0x01;
		public final static byte AUTHENTICATION = 0x02;
		public final static byte MODEL = 0x03;
		public final static byte NAVIGATION = 0x04;
		public final static byte CONTACTS = 0x05;
		public final static byte PROFILE = 0x06;
		public final static byte TEST = 0x07;
		public final static byte CONTEXTUAL_MENU = 0x08;
		public final static byte CONFIRM = 0x09;
		public final static byte CHAT = 0x0B;
		public final static byte EDIT_PROFILE = 0x0C;
		public final static byte SETTINGS = 0x0D;
		public final static byte GETIMAGE = 0x0E;
		public final static byte WEB_ACCOUNTS= 0x10;
        public static final byte SYNC = 0x11;
        public static final byte ACTIVITY = 0x12;
        public static final byte STATUS_UPDATE = 0x13;
        public static final byte EMAIL = 0x14;
        public static final byte TIMELINE = 0x15;
        
        //#if polish.device.supports.nativesms == false
        public static final byte SMS_EDITOR = Byte.MAX_VALUE - 1;
        //#endif
	}

	/**
	 * application events
	 * @author Andre Schmidt
	 */
	public static final class App
	{
		public final static int START = 1;
		public final static int READY = 2;
		public final static int PAUSE = 4;
		public final static int RESUME = 5;
		public final static int STOP = 6;
		public final static int EXIT = 7;
		public final static int MARK_FOR_FLUSH_AND_EXIT = 9;
		public final static int CONFIRMED_EXIT = 10;
		public static final int MANDATORY_UPDATE_RECEIVED = 11;
		public static final int UPDATE_APPLICATION = 12;
		public static final int SHOW_HELP = 13;
		public static final int BROWSE = 14;
		public static final int CONFIRMED_BROWSE = 15;
		public static final int CONNECTION_UP = 16;
		public static final int CONNECTION_DOWN = 17;
		public static final int CONNECTION_ROAMING = 18;
		public static final int CONTACTS_CORRUPTED = 19;
		public static final int CONTACTS_FILLING_UP = 20;
		public static final int SERVICE_UNAVAILABLE = 21;
//		public static final int TRAFFIC = 22;
		public static final int OPTIONAL_UPDATE_RECEIVED = 23;
		
		//#if polish.blackberry && add.switch.application.form
		public static final int SHOW_LIST_SWITCH_APPS=Byte.MAX_VALUE-6;
		//#endif

		//#ifdef polish.device.requires.polish.browser
		public static final int BROWSE_POLISH = Byte.MAX_VALUE-5;
		//#endif
	}

	/**
	 * authentication events
	 * @author Jens Vesti
	 */
	public static final class Authentication
	{
		/**
		 *  Sign up/log in required because this is the first start up.
		 */
		public final static int FIRST_LOGIN = 1;

		/**
		 * Log in required because user doesn't want to stay logged in.
		 */
		public final static int LOGIN = 3;

		/**
		 * Log in required because authentication failed.
		 */
		public final static int AUTHENTICATION_FAILED = 4;

		/**
		 * User didn't give permission for data connection.
		 */
		public final static int USER_DISALLOWED_CONNECTION = 5;

		/**
		 * User name has changed, please verify and login again.
		 */
		public final static int CONFIRM_USER_NAME_CHANGE = 6;

		/**
		 * Can't change user name when application is running.
		 */
		public final static int USER_NAME_CHANGE_NOT_ALLOWED = 7;

		/**
		 * Sign up succeeded.
		 */
		public final static int SIGNUP_SUCCEEDED = 8;

		/**
		 * Sign up failed.
		 */
		public final static int SIGNUP_FAILED = 9;

		/**
		 * Log in succeeded.
		 */
		public final static int LOGIN_SUCCEEDED = 10;

		/**
		 * Log in failed.
		 */
		public final static int LOGIN_FAILED = 11;

		public final static int SAVE = 12; //Save login

		public static final int SHOW_FIRST_LOGIN = 13;

		/**
		 * Sign up failed as confirmation SMS didn't arrive
		 */
		public final static int SIGNUP_FAILED_WRONG_MSISDN = 14;
		
		/**
		 * Log in required because authentication failed with new password
		 */
		public final static int AUTHENTICATION_FAILED_NEW_PASSWORD = 15;

		public static final int NO_SERVICE = 16;
	}

	/**
	 * Get images events
	 * @author Jens Vesti
	 */
	public static final class GetImages
	{
		public final static int TAKE_IMAGE_WITH_CAMERA = 0x1;
		public final static int ACCEPT_IMAGE = 0x2;
	}

	/**
	 * model events
	 * @author Andre Schmidt
	 */
	public static final class Model
	{
		public final static int LOAD_PROFILE = 1;
		public final static int ME_CHANGED_IN_SAB = 2;
		public final static int MY_IDENTITIES_CHANGED_IN_SAB = 3;
		public final static int ME_CHANGED_IN_CAB = 4;
		public final static int CONTACTS_CHANGED_IN_SAB = 5;
		public final static int CONTACTS_CHANGED_IN_CAB = 6;
		public final static int GROUPS_CHANGED = 7;
		public final static int PRESENCES_CHANGED = 8;
		public final static int TIMELINE_CHANGED = 9;
		public final static int STATUS_STREAM_CHANGED = 10;
	}

	/**
	 * contacts events
	 * @author Andre Schmidt
	 */
	public static final class Contacts
	{
		public final static int REFRESH_LIST = 1;
		public final static int REFRESHING_LIST = 2;
		public final static int ADD_TO_LIST = 3;
		public final static int UPDATE_IN_LIST = 4;
		public final static int REMOVE_FROM_LIST = 5;
		public final static int ADD = 6;
		public final static int UPDATE = 7;
		public final static int REMOVE = 8;
		public final static int FILTER = 9;
		public final static int SEARCH = 10;
		//#if use-connect-invite == true
		public final static int INVITE = 11;
		public final static int CONNECT = 12;
		//#endif
		public final static byte OPEN = 13;

		public static final int DELETE_CONTACT = 15;
		public static final int FILTERS_UPDATED = 16;
		public static final int OPEN_FILTER = 17;
		public static final int FILTER_CHANGED = 18;
	}

	/**
	 * settings events
	 * @author Jens Vesti
	 */
	public static final class Settings
	{
		public final static int SAVE = 0x00;
		public final static int OPEN = 0x01;
		public final static int CLEAR_STORAGE = 0x02;
		public static final int CLEAR_STORAGE_CONFIRM_WARNING = 0x03;
		public final static int RESET_COUNTER= 0x04;
		public final static int RESET_COUNTER_CONFIRM= 0x05;
		public final static int SYNC = 0x06;
		public final static int ORDER = 0x07;
		public final static int ROAMING = 0x08;
	}


	/**
	 * Profile events
	 * @author Jens Vesti
	 */
	public static final class Profile
	{
		public final static int OPEN = 0x01;
		public final static int LAUNCH_CHANNEL = 0x02;
		public final static int DELETE_DETAIL = 0x03;
		public final static int ME = 0x04;
		
//		public final static int CONTACTS_INIT = 0x06;
		public final static int CONTACTS_REFRESH = 0x07;
//		public final static int SEARCH = 0x08;
//		public final static int SEARCH_FINISHED = 0x09;
	}

	/**
	 * navigation events
	 * @author Andre Schmidt
	 */
	public final static class Navigation
	{
		public final static int BACK = 0x01;
		public final static int BACK_CHECKPOINT = 0x02;
		public final static int NEXT = 0x03;
		public final static int NEXT_CHECKPOINT = 0x04;
		public final static int SWITCH = 0x05;
		public final static int DISMISS = 0x06;
		public final static int SET_CURRENT = 0x07;
		public final static int HIDE = 0x08;
		public static final int NEXT_GLOBAL = 0x09;
		public static final int BACK_TO_LATEST_PEOPLESPAGE = 0x10;
	}

	/**
	 * Events associated to contextual menu
	 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
	 */
	public static final class ContextualMenu
	{
		public final static int VIEW_PROFILE = 0x01;
		public final static int EDIT_PROFILE = 0x02;
		public final static int OPEN_CHAT = 0x03;
		public final static int OPEN_FILTER = 0x04;
		public final static int ADD_NEW_CONTACT = 0x05;
		public final static int NEW_SMS = 0x06;

//		public final static int INVITE = 0x08;
//		public final static int CONNECT = 0x09;
		public final static int DELETE_CONTACT = 0x0B;
//		public final static int SEARCH = 0x0C;
		public final static int SETTINGS = 0x0D;
		public final static int HELP = 0x0E;
		public final static int EXIT= 0x0F;
		public final static int CHANNEL_CALL = 0x1F;
		public final static int CHANNEL_SMS = 0x2F;
		public final static int CHANNEL_CHAT = 0x3F;
		public final static int CHANNEL_EMAIL = 0x4F;
		public final static int OPEN = 0x5F;
		public final static int CREATE_TEXT = 0x6F;
		public final static int DELETE_SN = 0x7F;
		
		public final static int PROFILE_FROM_CHAT = 0x8F;
		
		//#if polish.blackberry && add.switch.application.form
		public final static int OPEN_FORM_SWITCH_APPS = 0x9F;
        //#endif
		
		//#if activate.embedded.360email
		public final static int NEW_EMAIL = 0x07;
		public final static int GOTO_EMAIL_FOLDERS = 0xAF;
		//#endif
	}

	/**
	 * Events associated to confirmation forms
	 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
	 */
	public static final class Confirm
	{
		public final static int BLOCK = 0x0A;
		public final static int DELETE = 0x0B;
	}

	/**
	 * Events associated to chat
	 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
	 */
	public static final class Chat
	{
		public final static int SEND = 0x01;
		public final static int OPEN = 0x03;
		public final static int CLOSE = 0x04;
		public final static int RECEIVED_MESSAGE = 0x05;

		public final static int SWITCH_NATIVE = 0x06;
		public final static int SWITCH_MULTITAB = 0x07;
		
		public final static int ERROR_USER_NOT_ONLINE = 0x08;
		public final static int ERROR_USER_NOT_SIGNED_IN = 0x09;
		public final static int ERROR_CONTACT_NOT_CONNECTED = 0x10;
		public final static int ERROR_CONTACT_NOT_ONLINE = 0x11;
		
		public final static int READY_FOR_INCOMING_MESSAGES = 0x12;
	}

	/**
	 * Events associated to edit profile
	 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
	 */
	public static final class EditProfile
	{
		public final static int OPEN = 0x01;
		public final static int SAVE = 0x02;

		public final static int EDIT_NAME = 0x03;
		public final static int EDIT_PHONE = 0x04;
		public final static int EDIT_EMAIL = 0x06;
		public final static int EDIT_ADRESS = 0x07;
		public final static int EDIT_GROUP = 0x08;
		public final static int EDIT_URL = 0x09;
		public final static int EDIT_BIRTHDAY = 0x10;
		public final static int EDIT_NOTE = 0x11;
		public final static int EDIT_WORK = 0x12;

		public final static int NEW_NAME = 0x13;
		public final static int NEW_PHONE = 0x14;
		public final static int NEW_EMAIL = 0x16;
		public final static int NEW_ADRESS = 0x17;
		public final static int NEW_GROUP = 0x18;
		public final static int NEW_URL = 0x19;
		public final static int NEW_BIRTHDAY = 0x20;
		public final static int NEW_NOTE = 0x21;
		public final static int NEW_WORK = 0x22;

		public final static int DELETE_DETAIL = 0x23;
		public final static int DELETE_DETAIL_CONTEXTUAL = 0x24;
	}

    public static final class Sync
    {
        /* Generic Sync States */
        public static final int START = 1;
        public static final int SUSPEND = 2;
        public static final int RESUME = 3;
        public static final int CANCELLING = 4;
        public static final int CANCELLED = 5;
        public static final int FAILED = 6;
        public static final int SUCCESSFULL = 7;
        public static final int SYNCING = 8;
        public static final int IDLE = 9;

        /* Change Detection */
       // public static final int DETECTING_CHANGE = 10;

        /* Change Processation */
        public static final int IN_ADD = 11;
        public static final int IN_MOD = 12;
        public static final int IN_DEL = 13;
        public static final int OUT_ADD = 14;
        public static final int OUT_MOD = 15;
        public static final int OUT_DEL = 16;

        /* More concrete state events for Import */
        public static final int START_IMPORT = 17;
        public static final int CANCEL_IMPORT = 18;
        
        /* Security confirmations */
        public static final int USER_DISALLOWED_OPEN = 19;
        public static final int USER_DISALLOWED_CONTENT_READ = 20;
        public static final int USER_DISALLOWED_CONTENT_WRITE = 21;
        public static final int USER_DISALLOWED_ALLOCATE_NEW = 22;
        public static final int USER_DISALLOWED_CONTENT_DELETE = 23;

        /* External events */
        public static final int CAB_CHANGED = 24;
        
        /**
         * Contacts have been send to the server during the cab-sab sync at first start up. 
         * data contains the percentage of the changes to be send that have been send.
         */
        public static final int CONTACTS_SEND = 25;
        
        /**
         * The sending of contacts during the cab-sab sync failed.
         */
        public static final int CONTACTS_NOT_SEND = 26;
        
        /**
         * Contacts have been received from the server during the cab-sab sync at first start up. 
         * data contains the percentage of the changes to be received that have been received.
         */
        public static final int CONTACTS_RECEIVED = 27; 
        
        /**
         * The receiving of contacts during the cab-sab sync failed.
         */
        public static final int CONTACTS_NOT_RECEIVED = 28;
        
        /**
         * update text for UI
         */
        public static final int UPDATE_PROGRESS_INDICATOR_DETAIL_CONTACTS_SEND=29;
        
        /**
         * update text for UI
         */
        public static final int UPDATE_PROGRESS_INDICATOR_DETAIL_CONTACTS_RECEIVED=30;
        
        /**
         * When the cab-sab sync at first start up met error, user could ignore error and dismiss the notification, 
         * app goto main screen, rest of sync works running in background
         */
        public static final int DISMISS_CAB_SAB_SYNC_NOTIFICATION=31;
    }

	/**
	 * Events associated to web accounts view
	 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
	 */
	public static final class WebAccounts
	{
		public final static int OPEN = 1;
		public final static int EDIT = 2;
		public final static int SAVE = 3;
		public final static int LOGOUT = 4;
		public final static int LOGIN = 5;
		public final static int REMOVE = 6;

		/**
		 * The user has started the application and has never added
		 * a social network account.
		 */
		public final static int NO_WEB_ACCOUNT_ADDED = 7;

		/**
		 * A social network account has successfully been added to my profile.
		 * MyProfile.getIdentities() may or may not return the just added account.
		 */
		public final static int ADD_SUCCEEDED = 8;

		/**
		 * Social network account could not be added because service is not available,
		 * or credentials are invalid.
		 */
		public final static int ADD_FAILED = 9;

		/**
		 * Adding a social network account timed out. The account may, or may not have 
		 * been added.
		 */
		public final static int ADD_TIMED_OUT = 10;
		
		/**
		 * A social network account has successfully been removed from my profile.
		 * MyProfile.getIdentities() may or may not return the just removed account.
		 */
		public final static int REMOVE_SUCCEEDED = 11;

		/**
		 * Social network account could not be removed.
		 */
		public final static int REMOVE_FAILED = 12;

		/**
		 * Removing a social network account timed out. The account may, or may not have 
		 * been removed.
		 */
		public final static int REMOVE_TIMED_OUT = 13;
		
		/**
		 * Social network successfully logged into
		 */
		public final static int LOGIN_SUCCEEDED = 14;
		
		/**
		 * Could not log in to social network
		 */
		public final static int LOGIN_FAILED = 15;

		/**
		 * Log in to a social network account timed out. 
		 */
		public final static int LOGIN_TIMED_OUT = 16;

		/**
		 * Successfully logged out from social network
		 */
		public final static int LOGOUT_SUCCEEDED = 17;

		/**
		 * Could not log out from social network
		 */
		public final static int LOGOUT_FAILED = 18;
		
		/**
		 * Log out from a social network account timed out. 
		 */
		public final static int LOGOUT_TIMED_OUT = 19;
		
		public static final int BACK = 20;	
		public static final int SKIP = 21;
	}

	public static final class Activities
	{
		public final static int TIMELINE_CHANGED = 1;
		public final static int STATUS_STREAM_CHANGED = 2;
		
		/**new message received or sent then update the content of item of timeline tab */
		public final static int TIMELINE_TIMELINE_CHAT_UPDATE = 3;
		public final static int OUTGOING_SMS = 4;
		public final static int OUTGOING_PHONECALL = 5;
	}
	
	public static final class Email
	{
		public final static int UPDATE_EMAIL = 1;
		public final static int OPEN_EMAIL = 2;
		public final static int COMPOSE_EMAIL = 3;
		public final static int SEND_EMAIL = 4;
	}
	
    //#if polish.device.supports.nativesms == false
	public static final class SmsEditor 
	{
		public final static int OPEN = 0;
		public final static int SEND_OK = 1;
		public final static int SEND_FAILED = 2;
		public final static int SEND_STARTED = 3;
	}
	//#endif

	/**
	 * the context of an event
	 */
	private final byte context;

	/**
	 * the event
	 */
	private final int id;

	/**
	 * the data of an event
	 */
	private Object data;

	/**
	 * Creates a new Event instance
	 * @param context the context
	 * @param id the event identifer
	 * @param data the data
	 */
	public Event(byte context, int id, Object data)
	{
		this.context = context;
		this.id = id;
		this.data = data;
	}

	/**
	 * Returns the context
	 * @return the context
	 */
	public byte getContext() {
		return context;
	}

	/**
	 * Returns the event identifier
	 * @return the event identifier
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Sets the data.
	 * @param data
	 */
	public void setData(Object data)
	{
		this.data = data;
	}

	/**
	 * Returns the data
	 * @return the data
	 */
	public Object getData()
	{
		return data;
	}

	public boolean equals(Object o)
	{
		Event that = (Event) o;
		return (this.context == that.context)
			&& (this.id == that.id)
			&& HashUtil.equals(this.data, that.data);
	}

	//#mdebug error
	public String toString()
	{
		return "Event[context=" + context
			+ ";id=" + id
			+ ";data=" + data
			+ "]";
	}
	//#enddebug

	/**
	 * Creates a new array of len elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1].
	 */
	public static Event[] trimArray(Event[] src, int len)
	{
		Event[] dst = new Event[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}
}
