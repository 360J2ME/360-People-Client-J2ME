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
package com.zyb.nowplus.data.email.commands;

import java.util.Vector;

import com.zyb.nowplus.data.email.EmailCommunicationManagerImpl;
import com.zyb.nowplus.data.email.response.EmailAppendMessageResponse;
import com.zyb.nowplus.data.email.response.EmailResponse;
import com.zyb.nowplus.data.email.types.EmailAddress;
import com.zyb.nowplus.data.email.types.EmailMessage;

/**
 * 
 * @author Jens Vesti
 */
public class EmailAppendMessageCommand extends EmailCommand {
	
	private static final String MIME_VERSION = "1.0";
	private static final String CONTENT_TYPE = "TEXT/PLAIN";
	
	private final String mailbox;
	private final EmailMessage message;
	
	public EmailAppendMessageCommand(final String mailbox, final EmailMessage message)
	{
		this.mailbox = mailbox;
		this.message = message;
	}
	
	public byte[][] getSerialisedCommand() 
	{		
		byte[] msg = new byte[2048];
		
		int len = add(msg, 0, "Date: " + message.getDate());
		len = add(msg, len, "Subject: " + message.getSubject());
		len = add(msg, len, "From: " + toString(message.getFroms()));
		len = add(msg, len, "To: " + toString(message.getTos()));
		len = add(msg, len, "MIME-Version: " + MIME_VERSION);
		len = add(msg, len, "Content-Type: "+ CONTENT_TYPE);
		len = add(msg, len, "");
		len = add(msg, len, message.getMessageBody());
		
		String command = this.getCurrentId() + " APPEND " + mailbox 
			+ " {" + len + "}";
		
		byte[] msg2 = new byte[len];
		System.arraycopy(msg, 0, msg2, 0, len);
		return new byte[][] {command.getBytes(), msg2};
	}

	private int add(byte[] buffer, int len, String s)
	{
		byte[] b = s.getBytes();
		System.arraycopy(b, 0, buffer, len, b.length);
		len += b.length;
		buffer[len++] = EmailCommunicationManagerImpl.ENDLINE[0];
		buffer[len++] = EmailCommunicationManagerImpl.ENDLINE[1];
		return len;
	}
	
	private String toString(Vector vector)
	{
		StringBuffer sb = new StringBuffer();
		if (vector.size() > 0)
		{
			EmailAddress address = (EmailAddress) vector.elementAt(0);
			sb.append(address.getEmailAddress());
			
			for (int i = 1; i < vector.size(); i++)
			{
				sb.append("; ");
		
				address = (EmailAddress) vector.elementAt(i);
				sb.append(address.getEmailAddress());
			}
		}
		return sb.toString();
	}
	
	public EmailResponse createResponse(String response)
	{
		return new EmailAppendMessageResponse(response);
	}
	
	//#mdebug error
	public String toString() {
		return "EmailAppendMessageCommand[mailbox=" + mailbox
		+ ";message=" + message
		+ "]";
	}
	//#enddebug
}


//a3 fetch 1 all
//* 1 FETCH (INTERNALDATE "26-Oct-2007 08:42:36 +0200" RFC822.SIZE 4675 ENVELOPE ("Thu, 25 Oct 2007 23:42:36 -0700" "Gmail er anderledes. Her
//er, hvad du skal vide." (("Gmail-teamet" NIL "mail-noreply" "google.com")) (("Gmail-teamet" NIL "mail-noreply" "google.com")) (("Gmail-teame
//t" NIL "mail-noreply" "google.com")) (("Jens Vesti" NIL "jenstest20" "gmail.com")) NIL NIL NIL "<18adb0810710252342y1abacc3av@mail.gmail.com
//>") FLAGS (\Seen))
//a3 OK FETCH completed



//a6 fetch 1 fast
//* 1 FETCH (INTERNALDATE "26-Oct-2007 08:42:36 +0200" RFC822.SIZE 4675 FLAGS (\Seen))
//a6 OK FETCH completed


//a8 fetch 1 full
//* 1 FETCH (INTERNALDATE "26-Oct-2007 08:42:36 +0200" RFC822.SIZE 4675 BODY (("TEXT" "PLAIN" ("CHARSET" "ISO-8859-1") NIL NIL "QUOTED-PRINTAB
//LE" 1775 45)("TEXT" "HTML" ("CHARSET" "ISO-8859-1") NIL NIL "QUOTED-PRINTABLE" 2103 48) "ALTERNATIVE") ENVELOPE ("Thu, 25 Oct 2007 23:42:36
//-0700" "Gmail er anderledes. Her er, hvad du skal vide." (("Gmail-teamet" NIL "mail-noreply" "google.com")) (("Gmail-teamet" NIL "mail-norep
//ly" "google.com")) (("Gmail-teamet" NIL "mail-noreply" "google.com")) (("Jens Vesti" NIL "jenstest20" "gmail.com")) NIL NIL NIL "<18adb08107
//10252342y1abacc3av@mail.gmail.com>") FLAGS (\Seen))
//a8 OK FETCH completed


//a15 FETCH 2 (UID RFC822.SIZE BODY[text] BODY.PEEK[HEADER.FIELDS (Subject From Date Message-Id References In-Reply-To Xref)])
//* 2 FETCH (UID 2883 RFC822.SIZE 10288 BODY[TEXT] {8105}
//
//--==_MIME-Boundary-1_==
//Content-Type: text/plain; charset=utf-8
//Content-transfer-encoding: quoted-printable
//
//
//
//
//
//--==_MIME-Boundary-1_==
//Content-Type: text/html; charset=utf-8
//Content-transfer-encoding: quoted-printable
//
//
//
//<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" =
//...
//
//--==_MIME-Boundary-1_==--
//
//
//
// BODY[HEADER.FIELDS (SUBJECT FROM DATE MESSAGE-ID REFERENCES IN-REPLY-TO XREF)] {218}
//Message-Id: <474ae6d8.4b27360a.2596.ffff9dc5SMTPIN_ADDED@mx.google.com>
//Date: 26 Nov 2007 07:31:35 -0800
//From: my-yahoo-register@yahoo-inc.com
//Subject: =?ascii?Q?Welcome_to_Yahoo!_Please_Activate_Your_Account.?=
//
//)
//a15 OK FETCH completed



