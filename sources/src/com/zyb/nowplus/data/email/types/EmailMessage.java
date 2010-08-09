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

import java.util.Vector;

/**
 * @author Jens Vesti
 *
 */
public class EmailMessage 
{
	private int id;
	private String date; 
	private String subject; 
	private Vector froms; 
	private Vector senders; 
	private Vector replyTos; 
	private Vector tos; 
	private Vector ccs; 
	private Vector bccs;
    private String inReplyTo; 
    private String messageId;
	private String messageBody;

	public static final int ANSWERED = 0x1;
	public static final int FLAGGED = 0x2;
	public static final int DELETED = 0x4;
	public static final int SEEN = 0x8;
	public static final int DRAFT = 0x10;
	
	private int flags = 0; 	

	public EmailMessage() {
		froms = new Vector();
		senders = new Vector();
		replyTos = new Vector();
		tos = new Vector();
		ccs = new Vector();
		bccs = new Vector();
	}

	public EmailMessage(EmailAddress[] bcc, EmailAddress[] cc, String date,
			EmailAddress from, String inReplyTo, String messageId,
			EmailAddress[] replyTo, EmailAddress sender, String subject,
			EmailAddress[] to, String messageBody, int flags) {
		super();
		this.date = date;
		this.subject = subject;
		this.froms = new Vector();
		this.froms.addElement(from);
		this.senders = new Vector();
		this.senders.addElement(sender);
		this.replyTos = new Vector();
		this.replyTos.addElement(replyTo);
		this.ccs = new Vector();
		this.ccs.addElement(cc);
		this.bccs = new Vector();
		this.bccs.addElement(bcc);
		this.inReplyTo = inReplyTo;
		this.messageId = messageId;
		this.messageBody = messageBody;
		this.flags = flags;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}

	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}	
	
	public Vector getFroms() {
		return froms;
	}

	public Vector getSenders() {
		return senders;
	}	
	
	public Vector getReplyTos() {
		return replyTos;
	}
	
	public Vector getTos() {
		return tos;
	}
	
	public Vector getCCs() {
		return ccs;
	}
	
	public Vector getBCCs() {
		return bccs;
	}

	public String getInReplyTo() {
		return inReplyTo;
	}
	
	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}
	
	public String getMessageId() {
		return messageId;
	}
	
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}
	
	public boolean isFlagged()
	{
		return (flags & FLAGGED) == FLAGGED;
	}

	public boolean isDeleted()
	{
		return (flags & DELETED) == DELETED;
	}

	public boolean isSeen()
	{
		return (flags & SEEN) == SEEN;
	}

	public boolean isDraft()
	{
		return (flags & DRAFT) == DRAFT;
	}

	public boolean isAnswered()
	{
		return (flags & ANSWERED) == ANSWERED;
	}

	public String toString() {
		return "EmailMessage[date=" + date
		+ ",subject=" + subject
		+ ",from=" + froms
		+ ",sender=" + senders
		+ ",replyTo=" + replyTos
		+ ",to=" + tos
		+ ",cc=" + ccs
		+ ",bcc=" + bccs
		+ ",inReplyTo=" + inReplyTo
		+ ",messageId=" + messageId
		+ ",messageBody=" + messageBody
		+ ",flags=" + flags
		+ "]";
	}
}
