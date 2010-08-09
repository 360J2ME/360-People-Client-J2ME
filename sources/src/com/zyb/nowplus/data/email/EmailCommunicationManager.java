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

import com.zyb.nowplus.data.email.types.EmailMessage;

public interface EmailCommunicationManager {

	/**
	 * Starts the communication manager.
	 */
	public void start();
	
	/**
	 * Stops the communication manager.
	 */
	public void stop();
	
	/**
	 * Opens a connection to the imap server and logs in.
	 * Listeners will be notified with an EmailLoginResponse.
	 */
	public void openConnection(String userName, String passWord, String host, int port, boolean useSSL);
	
	/**
	 * Closes the connection to the imap server.
	 */
	public void closeConnection();
	
	/**
	 * Fetches the mail folders.
	 * Listeners will be notified with an EmailFolderResponse.
	 */
	public void fetchFolders();
	
	/**
  	 * Selects a mail folder for use.
  	 * Listeners will be notified with an EmailUseFolderResponse.
	 */
	public void useFolder(String folder);
	
	/**
	 * Fetches mail messages.
	 * Listeners will be notified with an EmailMessageResponse.
	 * 
	 * @param emailNumbers are the index numbers or uids of the emails being requested. 
	 * It is on the format of either a seq-number or seq-range as defined in http://www.faqs.org/rfcs/rfc3501.html
	 * and _not_ sequence-set (TODO in time it should be implemented)
	 * Please do not request out of range, and be careful about requesting too many emails in one go as you will get what you request.
	 * @param useUids defines if emailNumbers are uids or index numbers.
	 */
	public void fetchMessages(String emailNumbers, boolean useUids);
	
	public void fetchMessage(String emailNumber, boolean useUids);
	
	public void appendMessage(String mailbox, EmailMessage message);
	
	/**
	 * Adds a listener to the communication manager.
	 */
	public void addListener(EmailListener listener);
}
