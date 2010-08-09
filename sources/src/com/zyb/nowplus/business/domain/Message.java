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

import com.zyb.util.HashUtil;

/**
 * The message received or sent over a channel.
 */
public class Message 
{
	private final String text;
	private final long time;
	private final boolean fromMe;
	private boolean queued;
	
	public Message(String text, long time, boolean fromMe)
	{
		this.text = text;
		this.time = time;
		this.fromMe = fromMe;
	}
	
	public String getText() 
	{
		return text;
	}
	
	public long getTime() 
	{
		return time;
	}
	
	public boolean isFromMe() 
	{
		return fromMe;
	}
	
	public void setQueued(boolean queued)
	{
		this.queued = queued;
	}
	
	public boolean isQueued()
	{
		return queued;
	}
	
	public boolean equals(Object o)
	{
		Message that = (Message) o;
		return HashUtil.equals(this.text, that.text) 
			&& (this.time == that.time)
			&& (this.fromMe == that.fromMe);
	}
	
	//#mdebug error
	public String toString()
	{
		return "Message[text=" + text
			+ ",time=" + time
			+ ",fromMe=" + fromMe
			+ "]";
	}
	//#enddebug
	
	/**
	 * Creates a new array of len elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1].
	 */
	public static Message[] trimArray(Message[] src, int len)
	{
		Message[] dst = new Message[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}	
	
	/**
	 * Extends an array.
	 */
	public static Message[] extendArray(Message[] src)
	{
		Message[] dst = new Message[src.length * 3 / 2 + 1];
		System.arraycopy(src, 0, dst, 0, src.length);
		return dst;
	}		
}
