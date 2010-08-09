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

package com.zyb.nowplus.data.email.types;

/**
 * @author Jens Vesti
 *
 */
public class EmailAddress {

	private String hostName;
	private String mailboxName;
	private String personalName;
	private String smtpAtDomainList; 

	public EmailAddress() {
	}
	
	public EmailAddress(String hostName, String mailboxName,
			String personalName, String smtpAtDomainList) {
		super();
		this.hostName = hostName;
		this.mailboxName = mailboxName;
		this.personalName = personalName;
		this.smtpAtDomainList = smtpAtDomainList;
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public String getMailboxName() {
		return mailboxName;
	}
	
	public void setMailboxName(String mailboxName) {
		this.mailboxName = mailboxName;
	}
	
	public String getPersonalName() {
		return personalName;
	}
	
	public void setPersonalName(String personalName) {
		this.personalName = personalName;
	}
	
	public String getSmtpAtDomainList() {
		return smtpAtDomainList;
	}
	
	public void setSmtpAtDomainList(String smtpAtDomainList) {
		this.smtpAtDomainList = smtpAtDomainList;
	}

	public String getEmailAddress()
	{
		return this.mailboxName+"@"+this.hostName;
	}

	public String toString() {
		return "EmailAddress[personalName=" + personalName
		+ ",email=" + getEmailAddress()
		+ "]";
	}
}
