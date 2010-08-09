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
package com.zyb.nowplus.data.storage.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import com.zyb.nowplus.data.storage.DataRecord;

import de.enough.polish.io.Serializer;

public class TestRecord implements DataRecord
{
	private long id;
	private int attribute1;
	private boolean attribute2;
	private String attribute3;
	private Date attribute4;
	private int complexAttribute1;
	private boolean complexAttribute2;
	private String complexAttribute3;
	private Date complexAttribute4;
	
	public TestRecord()
	{
	}
	
	public TestRecord(long id)
	{	
		this.id = id;
		
		attribute1 = 37;
		attribute2 = true;
		attribute3 = "Test";
		attribute4 = new Date(1000000);

		complexAttribute1 = 42;
		complexAttribute2 = true;
		complexAttribute3 = "Complex Test";
		complexAttribute4 = new Date(2000000);		
	}
		
	public long getId()
	{
		return id;
	}
	
	public int getAttribute1()
	{
		return attribute1;
	}
	
	public boolean getAttribute2()
	{
		return attribute2;
	}
	
	public String getAttribute3()
	{
		return attribute3;
	}
	
	public Date getAttribute4()
	{
		return attribute4;
	}
	
	public int getComplexAttribute1()
	{
		return complexAttribute1;
	}
	
	public boolean getComplexAttribute2()
	{
		return complexAttribute2;
	}
	
	public String getComplexAttribute3()
	{
		return complexAttribute3;
	}
	
	public Date getComplexAttribute4()
	{
		return complexAttribute4;
	}
	
	public void release()
	{
		complexAttribute1 = 0;
		complexAttribute2 = false;
		complexAttribute3 = null;
		complexAttribute4 = null;				
	}
	
	public void read(DataInputStream in) throws IOException
	{
		id = in.readLong();
		
		attribute1 = in.readInt();
		attribute2 = in.readBoolean();
		attribute3 = (String) Serializer.deserialize(in);
		attribute4 = (Date) Serializer.deserialize(in);

		complexAttribute1 = in.readInt();
		complexAttribute2 = in.readBoolean();
		complexAttribute3 = (String) Serializer.deserialize(in);
		complexAttribute4 = (Date) Serializer.deserialize(in);
	}

	public void write(DataOutputStream out) throws IOException 
	{
		out.writeLong(id);
		
		out.writeInt(attribute1);
		out.writeBoolean(attribute2);
		Serializer.serialize(attribute3, out);
		Serializer.serialize(attribute4, out);
		
		out.writeInt(complexAttribute1);
		out.writeBoolean(complexAttribute2);
		Serializer.serialize(complexAttribute3, out);
		Serializer.serialize(complexAttribute4, out);
	}	
}
