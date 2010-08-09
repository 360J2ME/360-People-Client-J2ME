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
package com.zyb.nowplus.business.domain;

import com.zyb.util.ArrayUtils;

public class Email 
{
	public static EmailManager manager;
	
	private int id;
	private ProfileSummary[] froms;
	private ProfileSummary[] tos;
	private String subject;
	private String body;
	private String date;

	public void setId(int id) 
	{
		this.id = id;
	}

	public int getId() 
	{
		return id;
	}
	
	public void setFroms(ProfileSummary[] froms) 
	{
		this.froms = froms;
	}

	public ProfileSummary[] getFroms()
	{
		return froms;
	}

	public void setTos(ProfileSummary[] tos)
	{
		this.tos = tos;
	}

	public ProfileSummary[] getTos() 
	{
		return tos;
	}
	
	public void setSubject(String subject) 
	{
		this.subject = subject;
	}
	
	public String getSubject() 
	{
		return subject;
	}
	
	public void setBody(String body) 
	{
		this.body = body;
	}
	
	public String getBody() 
	{
		return body;
	}
	
	public void setDate(String date) 
	{
		this.date = date;
	}
	
	public String getDate() 
	{
		return date;
	}
	
	//#mdebug error
	public String toString()
	{
		return "Email[id=" + id
		+ ";froms=" + ArrayUtils.toString(froms)
		+ ";tos=" + ArrayUtils.toString(tos)
		+ ";subject=" + subject
		+ "]";
	}
	//#enddebug
}
