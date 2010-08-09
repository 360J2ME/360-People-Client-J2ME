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
//#condition activate.embedded.360email
package com.zyb.nowplus.business;

import java.util.Vector;

import com.zyb.nowplus.business.domain.Email;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.Profile;
import com.zyb.nowplus.business.domain.ProfileSummary;
import com.zyb.nowplus.business.domain.UserRef;
import com.zyb.nowplus.data.email.EmailCommunicationManager;
import com.zyb.nowplus.data.email.EmailCommunicationManagerImpl;
import com.zyb.nowplus.data.email.response.EmailAppendMessageResponse;
import com.zyb.nowplus.data.email.response.EmailFolderResponse;
import com.zyb.nowplus.data.email.response.EmailLoginResponse;
import com.zyb.nowplus.data.email.response.EmailMessageResponse;
import com.zyb.nowplus.data.email.response.EmailUseFolderResponse;
import com.zyb.nowplus.data.email.types.EmailAddress;
import com.zyb.nowplus.data.email.types.EmailMessage;
import com.zyb.util.ArrayUtils;
import com.zyb.util.Index;
import com.zyb.util.event.Event;

public class EmailManager implements com.zyb.nowplus.business.domain.EmailManager
{
	private static final int INITIALISED = 1;
	private static final int LOGGED_IN = 2;
	private static final int FOLDERS_LISTED = 3;
	private static final int FOLDER_SELECTED = 4;
	private static final int MESSAGES_FETCHED = 5;
	private static final int MESSAGE_FETCHED = 6;
	
	private static final char SEPARATOR = '/';
	
	//TODO: unify endpoint urls in one place
	private static final String EMAIL_HOST_IP = 
		//#if ip.mail:defined
		//#= "${ip.mail}"
		//#else				
			"85.205.92.121"
		//#endif
		;
	
	private static final String EMAIL_HOST_URL = 
		//#if url.mail:defined
		//#= "${url.mail}"
		//#else				
			"360.com"
		//#endif
		;

	private static final int EMAIL_PORT = 143;
	
	//#if activate.email.tab
	private final ServiceBroker services;
	private final EmailCommunicationManager emailProtocol;
	
	private int phase;
	
	private String userName;
	private String passWord;
	
	private String[] folderNames;
	
	private String currentFolderName;
	private int currentFolderSize;

	private Email[] emails;

	private int currentEmailId;
	
	private int errors; // after 3 errors, don't retry anymore
	
	private Index index;
	//#endif
	
	public EmailManager(ServiceBroker services)
	{
		//#if activate.email.tab
		this.services = services;
		
		this.emailProtocol = new EmailCommunicationManagerImpl();
		this.emailProtocol.addListener(services);
		
		this.index = Index.create();
		//#endif
	}
	
	public EmailManager(ServiceBroker services, EmailCommunicationManager emailProtocol)
	{
		//#if activate.email.tab
		this.services = services;
		
		this.emailProtocol = emailProtocol;
		this.emailProtocol.addListener(services);
		
		this.index = Index.create();
		//#endif
	}
	
	public void start(String userName, String passWord)
	{
		//#if activate.email.tab
		emailProtocol.start();
		
		this.userName = userName;
		this.passWord = passWord;
		
		phase = INITIALISED;
		openFolder("INBOX");
		//#endif
	}
	
	public void stop()
	{
		//#if activate.email.tab
		phase = 0;
		
		emailProtocol.stop();
		//#endif
	}
	
	public void index(Profile profile)
	{		
		//#if activate.email.tab
		Identity[] emailAccounts = profile.getIdentities(Identity.TYPE_EMAIL);
		for (int i = 0; i < emailAccounts.length; i++)
		{
			index(profile, emailAccounts[i]);
		}
		//#endif
	}

	public void index(Profile profile, Identity emailAccount)
	{
		//#if activate.email.tab
		if (emailAccount.getName() != null)
		{
			index = index.set(emailAccount.getName(), profile);
		}
		//#endif
	}	
	
	public void openFolder(String folderName)
	{
		//#if activate.email.tab
		if (phase > FOLDERS_LISTED)
		{
			phase = FOLDERS_LISTED;
		}
		
		if (currentFolderName == null)
		{
			currentFolderName = folderName;
		}
		else
		{
			currentFolderName += SEPARATOR + folderName;
		}
		currentFolderSize = 0;
		
		emails = null;
		
		currentEmailId = 0;
		
		request();
		//#endif
	}
	
	public void refreshFolder() 
	{
		//#if activate.email.tab
		if (phase > FOLDERS_LISTED)
		{
			phase = FOLDERS_LISTED;
		}
		request();
		//#endif
	}
	
	public void closeFolder()
	{
		//#if activate.email.tab
		if (phase > FOLDERS_LISTED)
		{
			phase = FOLDERS_LISTED;
		}
		
		int index = lastIndexOf(currentFolderName, SEPARATOR);
		if (index == -1)
		{
			currentFolderName = null;
		}
		else
		{
			currentFolderName = currentFolderName.substring(0, index);
		}
		
		currentFolderSize = 0;
		
		emails = null;
		
		currentEmailId = 0;
		
		request();
		//#endif
	}

	public String getCurrentFolder()
	{
		String currentFolder = null;
		//#if activate.email.tab
		currentFolder = getName(currentFolderName);
		//#endif
		return currentFolder;
	}
	
	public String[] getAvailableFolders()
	{
		String[] availableFolders = null;
		//#if activate.email.tab
		if (phase < FOLDERS_LISTED)
		{
			return null;
		}
		if (currentFolderName == null)
		{
			return null;
		}
		String[] subfolderNames = new String[folderNames.length];
		int len = 0;
		for (int i = 0; i < folderNames.length; i++)
		{
			String subfolderName = getName(folderNames[i]);
			if (folderNames[i].equals(currentFolderName + SEPARATOR + subfolderName))
			{
				subfolderNames[len++] = folderNames[i];             
			}
		}
		availableFolders = ArrayUtils.trimArray(subfolderNames, len);
		//#endif
		return availableFolders;
	}
	
	public Email[] getAvailableMessages()
	{
		Email[] availableMessages = null;
		//#if activate.email.tab
		if (phase < MESSAGES_FETCHED)
		{
			// none
		}
		else
		{
			availableMessages = emails;
		}
		//#endif
		return availableMessages;
	}
	
	public void openMessage(int id) 
	{
		//#if activate.email.tab
		if (phase > MESSAGES_FETCHED)
		{
			phase = MESSAGES_FETCHED;
		}
		
		currentEmailId = id;
		
		request();
		//#endif
	}
	
	public Email getCurrentMessage()
	{
		Email currentMessage = null;		
		//#if activate.email.tab
		if (phase < MESSAGE_FETCHED)
		{
			// none
		}
		else
		{
			for (int i = 0; (i < emails.length) && (currentMessage == null); i++)
			{
				if (emails[i].getId() == currentEmailId)
				{
					currentMessage = emails[i];
				}
			}
		}
		//#endif
		return currentMessage;
	}
	
	public void sendMessage(EmailMessage message)
	{
		//#if activate.email.tab
		// TODO: actually send the message
	
		emailProtocol.appendMessage("Sent", message);
		//#endif
	}
	
	private synchronized void request()
	{
		//#if activate.email.tab
		if (phase == INITIALISED)
		{
			emailProtocol.openConnection(userName + "@" + EMAIL_HOST_URL, passWord, EMAIL_HOST_IP, EMAIL_PORT, false);
		}
		else
		if (phase == LOGGED_IN)
		{
			emailProtocol.fetchFolders();
		}
		else
		if (phase == FOLDERS_LISTED)
		{
			if (currentFolderName == null)
			{
				services.fireEvent(Event.Context.EMAIL, Event.Email.UPDATE_EMAIL, null);
			}
			else
			{
				emailProtocol.useFolder(currentFolderName);
			}
		}
		else
		if (phase == FOLDER_SELECTED)
		{			
			if (currentFolderSize == 0)
			{
				emails = new Email[0];
				
				phase = MESSAGES_FETCHED;
				request();
			}
			else
			{
				int start = currentFolderSize - 19;
				if (start < 1) 
				{
					start = 1;
				}
				emailProtocol.fetchMessages(start + ":*", false);
			}
		}
		else
		if (phase == MESSAGES_FETCHED)
		{
			if (currentEmailId == 0)
			{
				services.fireEvent(Event.Context.EMAIL, Event.Email.UPDATE_EMAIL, null);
			}
			else
			{
				emailProtocol.fetchMessage(Integer.toString(currentEmailId), false);
			}
		}
		else
		if (phase == MESSAGE_FETCHED)
		{
			services.fireEvent(Event.Context.EMAIL, Event.Email.UPDATE_EMAIL, null);
		}
		//#endif
	}
	
	public void loggedIn(EmailLoginResponse response)
	{
		//#if activate.email.tab
		errors = 0;
		
		phase = LOGGED_IN;
		request();
		//#endif
	}
	
	public void receivedFolders(EmailFolderResponse response)
	{
		//#if activate.email.tab
		errors = 0;
		
		phase = FOLDERS_LISTED;
		folderNames = response.getFolderNames();
		request();
		//#endif
	}
	
	public void selectedFolder(EmailUseFolderResponse response)
	{
		//#if activate.email.tab
		errors = 0;
		
		phase = FOLDER_SELECTED;
		currentFolderSize = response.getNumberOfMessages();
		request();
		//#endif
	}

	public void receivedMessages(EmailMessageResponse response)
	{
		//#if activate.email.tab
		errors = 0;
		
		Vector messages = response.getMessages();
		
		if (phase < MESSAGES_FETCHED)
		{
			if (messages.size() > 20)
			{			
				emails = new Email[20];
			}
			else
			{
				emails = new Email[messages.size()];
			}
			
			for (int i = 0; i < emails.length; i++)
			{
				int j = messages.size() - 1 - i;
				emails[i] = createEmail((EmailMessage) messages.elementAt(j));
			}
			
			phase = MESSAGES_FETCHED;
			request();
		}
		else
		{
			if (messages.size() == 1)
			{
				EmailMessage message = (EmailMessage) messages.elementAt(0);
				for (int i = 0; i < emails.length; i++)
				{
					if (emails[i].getId() == message.getId())
					{
						updateEmail(emails[i], message);
						break;
					}
				}
				phase = MESSAGE_FETCHED;
				request();
			}
		}
		//#endif
	}
	
	public void appendedMessage(EmailAppendMessageResponse response)
	{
		//#if activate.email.tab
		errors = 0;
		//#endif
	}
	
	public void errorOccurred() 
	{
		//#if activate.email.tab
		if (errors < 3)
		{
			errors++;
			if (phase > INITIALISED) 
			{
				emailProtocol.closeConnection();
			
				phase = INITIALISED;
			}
			request();
		}
		//#endif
	}

	public void newEmailReceived()
	{
		//#if activate.email.tab
		if (phase > FOLDERS_LISTED)
		{
			phase = FOLDERS_LISTED;
		}
		request();
		//#endif
	}
	
	//#if activate.email.tab
	private String getName(String fullName)
	{
		if (fullName == null)
		{
			return null;
		}
		int index = lastIndexOf(fullName, SEPARATOR);
		if (index == -1)
		{
			return fullName;
		}
		else
		{
			return fullName.substring(index);
		}
	}
	
	private int lastIndexOf(String s, char c)
	{
		int index = s.length();
		while (index > 0)
		{
			index--;
			if (s.charAt(index) == c)
			{
				return index;
			}
		}
		return -1;
	}
	
	private Email createEmail(EmailMessage source)
	{
		Email email = new Email();
		email.setId(source.getId());
		email.setFroms(findProfiles(source.getFroms()));
		email.setTos(findProfiles(source.getTos()));
		email.setSubject(source.getSubject());
		email.setDate(source.getDate());
		return email;
	}
	
	private void updateEmail(Email email, EmailMessage source)
	{
		email.setBody(source.getMessageBody());
	}
	
	private ProfileSummary[] findProfiles(Vector addresses)
	{
		ProfileSummary[] profiles = new ProfileSummary[addresses.size()];
		for (int i = 0; i < profiles.length; i++)
		{
			EmailAddress address = (EmailAddress) addresses.elementAt(i);
			
			profiles[i] = (ProfileSummary) index.get(address.getEmailAddress());
			if (profiles[i] == null)
			{
				profiles[i] = new UserRef(address.getPersonalName(), address.getEmailAddress());
			}
		}
		return profiles;
	}
	//#endif
}
