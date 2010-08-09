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

import de.enough.polish.io.Externalizable;
import de.enough.polish.io.Serializer;

/**
 * A grouping of contacts.
 */
public class Group implements Externalizable
{
	public static GroupManager manager;
	
	public static final int TYPE_STANDARD = 1;
	public static final int TYPE_CUSTOM = 2;
	public static final int TYPE_SOCIAL_NETWORK = 3;
	
	private long groupId;
	private int type;
	private String name;
	private String networkId;
	
	/**
	 * For serialisation.
	 */
	public Group()
	{
	}
	
	public Group(long groupId, int type, String name)
	{
		this(groupId,type,name,null);
	}
	
	public Group(long groupId, int type, String name, String networkId)
	{
		this.groupId = groupId;
		this.type = type;
		this.name = name;
		this.networkId = networkId;
	}

	public long getGroupId()
	{
		return groupId;
	}
	
	public int getType() 
	{
		return type;
	}

	public String getName() 
	{
		return name;
	}
	
	public String getNetworkId() 
	{
		return networkId;
	}

	public void read(DataInputStream in) throws IOException 
	{
		groupId = in.readLong();
		type = in.readInt();
		name = (String) Serializer.deserialize(in);
		networkId = (String) Serializer.deserialize(in);
		in.readInt();  // color
	}

	public void write(DataOutputStream out) throws IOException 
	{
		out.writeLong(groupId);
		out.writeInt(type);
		Serializer.serialize(name, out);
		Serializer.serialize(networkId, out);
	    out.writeInt(0);  // color
	}
	
	public boolean equals(Object o)
	{
		Group that = (Group) o;
		return (this.groupId == that.groupId);
	}	
	
	//#mdebug error
	public String toString()
	{
		return "Group[groupId=" + groupId
			+ ",type=" + type
			+ ",name=" + name
			+ ",network=" + networkId
			+ "]";
	}	
	//#enddebug
	
	/**
	 * Creates a new array of len elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1].
	 */
	public static Group[] trimArray(Group[] src, int len)
	{
		Group[] dst = new Group[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}	
	
	/**
	 * Extends an array.
	 */
	public static Group[] extendArray(Group[] src)
	{
		Group[] dst = new Group[src.length * 3 / 2 + 1];
		System.arraycopy(src, 0, dst, 0, src.length);
		return dst;
	}		
	
	/**
	 * Deserializes an array of Groups.
	 */
	public static Group[] deserializeGroupArray(DataInputStream in) throws IOException 
	{
		int len = in.readInt();
		if (len == -1)
		{
			return null;
		}
		Group[] array = new Group[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = (Group) Serializer.deserialize(in);
		}
		return array;		
	}
}
