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

package com.zyb.nowplus.data.email;

import java.util.Vector;

import com.zyb.nowplus.data.email.types.EmailAddress;
import com.zyb.nowplus.data.email.types.EmailMessage;
import com.zyb.util.Queue;
import com.zyb.util.SafeRunnable;
import com.zyb.util.SafeThread;

/**
 * @author Jens Vesti
 *
 */
public class EmailCommunicationDummyImpl implements EmailCommunicationManager, SafeRunnable {

	private SafeThread thread;
	private final Queue eventQueue;
	private final Vector listeners;

	private final EmailMessage[] emails = new EmailMessage[]{
			new EmailMessage(new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*bcc*/},
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*cc*/}, 
					"Monday",
					new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*from*/, 
					"jens@zyb.com", 
					"10",
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*in-reply-to*/}, 
					new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*sender*/, 
					"Subject text 1",
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*to*/},"Some kind of body text",EmailMessage.SEEN),
			new EmailMessage(new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*bcc*/},
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*cc*/}, 
					"Tuesday",
					new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*from*/, 
					"jens@zyb.com", 
					"112",
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*in-reply-to*/}, 
					new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*sender*/, 
					"Subject text 2",
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*to*/},"Some kind of body text",EmailMessage.SEEN),
			new EmailMessage(new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*bcc*/},
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*cc*/}, 
					"Wednesday",
					new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*from*/, 
					"jens@zyb.com", 
					"1145",
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*in-reply-to*/}, 
					new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*sender*/, 
					"Subject text 3",
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*to*/},"Some kind of body text",EmailMessage.SEEN|EmailMessage.DRAFT),
			new EmailMessage(new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*bcc*/},
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*cc*/}, 
					"Thursday",
					new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*from*/, 
					"jens@zyb.com", 
					"1177",
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*in-reply-to*/}, 
					new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*sender*/, 
					"Subject text 4",
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*to*/},"Some kind of body text",EmailMessage.ANSWERED),
			new EmailMessage(new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*bcc*/},
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*cc*/}, 
					"Friday",
					new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*from*/, 
					"jens@zyb.com", 
					"2888",
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*in-reply-to*/}, 
					new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*sender*/, 
					"Subject text 5",
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*to*/},"Some kind of body text",EmailMessage.SEEN),
			new EmailMessage(new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*bcc*/},
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*cc*/}, 
					"Saturday",
					new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*from*/, 
					"jens@zyb.com", 
					"3000",
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*in-reply-to*/}, 
					new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*sender*/, 
					"Subject text 6",
					new EmailAddress[]{new EmailAddress("Jens Vesti",null,"jens.vesti","vodafone.com")/*to*/},"Some kind of body text",EmailMessage.SEEN|EmailMessage.ANSWERED)
			};
	
	public EmailCommunicationDummyImpl()
	{
		thread = new SafeThread(this);
		thread.start("EmailConnectionDummy");
		eventQueue = new Queue();
		listeners = new Vector();
	}

	public void addListener(EmailListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void appendMessage(String mailbox, EmailMessage message) {
		// TODO Auto-generated method stub
		
	}
	
	public void closeConnection() {
		// TODO Auto-generated method stub
		
	}

	public void fetchFolders() {
		// TODO Auto-generated method stub
		
	}

	public void fetchMessage(String emailNumber, boolean useUids) {
		// TODO Auto-generated method stub
		
	}

	public void fetchMessages(String emailNumbers, boolean useUids) {
		// TODO Auto-generated method stub
		
	}

	public void openConnection(String userName, String passWord, String host,
			int port, boolean useSSL) {
		// TODO Auto-generated method stub
		
	}

	public void start() {
		// TODO Auto-generated method stub
		
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}

	public void useFolder(String folder) {
		// TODO Auto-generated method stub
		
	}

	public void cleanUp() {
		// TODO Auto-generated method stub
		
	}

	public void init() {
		// TODO Auto-generated method stub
		
	}

	public void releaseMemory() {
		// TODO Auto-generated method stub
		
	}

	public void work() {
		// TODO Auto-generated method stub
		
	}


}
