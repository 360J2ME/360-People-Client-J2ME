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

import com.zyb.nowplus.data.email.response.EmailMessageResponse;
import com.zyb.nowplus.data.email.response.EmailResponse;

/**
 * 
 * @author Jens Vesti
 */
public class EmailFetchMessagesCommand extends EmailCommand {

	private final String emailNumbers;
	private final String format;
	private final boolean useUids;
	
	public EmailFetchMessagesCommand(final String emailNumbers, String format, final boolean useUids)
	{
		this.emailNumbers = emailNumbers;
		this.format = format;
		this.useUids = useUids;
	}
	
	public byte[][] getSerialisedCommand() 
	{		
		String command = this.getCurrentId() + (useUids ? " UID " : " ") 
				+ "FETCH " + emailNumbers + " " + format;
		return new byte[][]{command.getBytes()};
	}

	public EmailResponse createResponse(String response)
	{
		return new EmailMessageResponse(response);
	}
	
	//#mdebug error
	public String toString() {
		return "EmailFetchMessagesCommand[emailNumbers=" + emailNumbers
		+ ";format=" + format
		+ ";useUids=" + useUids
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



