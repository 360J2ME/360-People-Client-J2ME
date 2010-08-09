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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.zyb.util.Collator;
import com.zyb.util.HashUtil;

import de.enough.polish.io.Serializer;

public class Note extends ContactDetail
{
	private String content;
	
	// for serialisation
	public Note()
	{
	}
	
	public Note(String content, long sabDetailId)
	{
		this.sabDetailId = sabDetailId;
		this.content = content;
	}
	
	public String getContent()
	{
		return content;
	}
	
	public boolean isEmpty()
	{
		return Collator.isEmpty(content);
	}
	
	public void read(DataInputStream in) throws IOException 
	{
		sabDetailId = in.readLong();
		content = (String) Serializer.deserialize(in);
		cri = in.readLong();
	}

	public void write(DataOutputStream out) throws IOException 
	{
		out.writeLong(sabDetailId);
		Serializer.serialize(content, out);
		out.writeLong(cri);
	}	
	
	public boolean equals(Object o)
	{
		Note that = (Note) o;
		return (this.sabDetailId == that.sabDetailId) 
			&& HashUtil.equals(this.content, that.content);
	}
	
	//#mdebug error
	public String toString()
	{
		return "Note[sabDetailId=" + sabDetailId
			+ ",content=" + content
			+ ",cri=" + cri
			+ "]";
	}	
	//#enddebug
	
	/**
	 * Creates a new array of len elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1].
	 */
	public static Note[] trimArray(Note[] src, int len)
	{
		Note[] dst = new Note[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}		
	
	/**
	 * Extends an array.
	 */
	public static Note[] extendArray(Note[] src)
	{
		Note[] dst = new Note[src.length * 3 / 2 + 1];
		System.arraycopy(src, 0, dst, 0, src.length);
		return dst;
	}	
}
