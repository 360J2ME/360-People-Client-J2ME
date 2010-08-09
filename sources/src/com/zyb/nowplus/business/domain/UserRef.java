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

/**
 * A reference to a user's profile. Used as a stand-in for 
 * a not yet loaded profile.
 */
public class UserRef implements ProfileSummary
{
	private String fullName;
	private Identity email;
	
	public UserRef(String fullName, String email)
	{
		this.fullName = (fullName == null) ? email : fullName;
		try
		{
			this.email = Identity.createEmail(Identity.SUBTYPE_HOME, email, true);
		}
		catch (InvalidValueException e)
		{
			//#debug error
			System.out.println("Invalid email address");
		}
	}
	
	public String getFullName() 
	{
		return fullName;
	}

	public ImageRef getProfileImage() 
	{
		return null;
	}
	
	public int getNowPlusPresence() 
	{
		return 0;
	}

	public Channel getPrimaryCallChannel() 
	{
		return null;
	}

	public Channel getPrimarySMSChannel() 
	{
		return null;
	}

	public Channel getPrimaryEmailChannel()
	{
		return (email == null) ? null : email.getChannel(Channel.TYPE_EMAIL);
	}


	public String getStatus() 
	{
		return null;
	}

	public ExternalNetwork getStatusSource() 
	{
		return null;
	}

	public void load()
	{
	}
	
	public void load(boolean immediately) 
	{
	}

	public void unload()
	{
	}
	
	//#mdebug error
	public String toString()
	{
		return "UserRef[fullName=" + fullName
		+ ";email=" + email
		+ "]";
	}
	//#enddebug
}
