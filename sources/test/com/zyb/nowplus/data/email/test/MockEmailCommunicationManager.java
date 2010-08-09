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
package com.zyb.nowplus.data.email.test;

import com.zyb.nowplus.data.email.EmailCommunicationManager;
import com.zyb.nowplus.data.email.EmailListener;
import com.zyb.nowplus.data.email.types.EmailMessage;

public class MockEmailCommunicationManager implements EmailCommunicationManager
{
	public void addListener(EmailListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void closeConnection() {
		// TODO Auto-generated method stub
		
	}

	public void fetchFolders() {
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

	public void fetchMessage(String emailNumber, boolean useUids) {
		// TODO Auto-generated method stub
		
	}

	public void appendMessage(String mailbox, EmailMessage message) {
		// TODO Auto-generated method stub
		
	}
}
