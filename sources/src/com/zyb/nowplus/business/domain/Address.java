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

/**
 * Represents the address in a {@link com.zyb.nowplus.business.domain.Profile profile}
 */
public class Address extends ContactDetail
{
	public static final int TYPE_HOME = 1;
	public static final int TYPE_WORK = 2;
	public static final int TYPE_OTHER = 3;
	
	public static final String TYPE_HOME_LABEL = "home";  
	public static final String TYPE_WORK_LABEL = "work"; 
	public static final String TYPE_OTHER_LABEL = "other"; 
	
	private int type;
	private int nabSubtypes;
	private String POBox;
	private String street;
	private String street2;
	private String town;
	private String region;
	private String postcode;
	private String country;
	
	/**
	 * Constructor for serialisation.
	 */
	public Address()
	{
	}
	
	public static Address createAddress(int type, String pobox, String street, String street2, String town, String postcode, String region, String country)
	{
		return createAddress(type, -1,pobox, street, street2, town, postcode, region, country, 0);
	}
	
	public static Address createAddress(int type, int nabSubtypes, String pobox, String street, String street2, String town, String postcode, String region, String country)
	{
		return createAddress(type, nabSubtypes, pobox, street, street2, town, postcode, region, country, 0);
	}
	
	public static Address createAddress(int type, String pobox, String street, String street2, String town, String postcode, String region, String country, long sabDetailId)
	{
		return createAddress(type, -1, pobox, street, street2, town, postcode, region, country, sabDetailId);
	}

	public static Address createAddress(int type, int nabSubtypes, String pobox, String street, String street2, String town, String postcode, String region, String country, long sabDetailId)
	{
		Address address = new Address();
		address.sabDetailId = sabDetailId;
		address.type = type;
		address.nabSubtypes = nabSubtypes;
		address.POBox = pobox;
		address.street = street;
		address.street2 = street2;
		address.town = town;
		address.region = region;
		address.postcode = postcode;
		address.country = country;
		return address;
	}
	
	public static Address createEmptyAddress(int nabSubtypes, long sabDetailId)
	{
		return createAddress(0, nabSubtypes,null, null, null, null, null, null, null, sabDetailId);
	}
	
	public int getType()
	{
		return type;
	}

    public void setNabSubtypes(int nabSubtypes) 
    {
        this.nabSubtypes = nabSubtypes;
    }
    
	public int getNabSubtypes()
	{
		return nabSubtypes;
	}
	
	public String getPOBox() 
	{
		return POBox;
	}
	
	public String getStreet() 
	{
		StringBuffer s = new StringBuffer();
		if (!Collator.isEmpty(street))
		{
			s.append(street);
			s.append(" ");
		}
		if (!Collator.isEmpty(street2))
		{
			s.append(street2);
			s.append(" ");
		}
		return s.toString();
	}
	
	public String getStreet1() 
	{
		return street;
	}
	
	public String getStreet2()
	{
		return street2;
	}
	
	public String getTown() 
	{
		return town;
	}
	
	public String getRegion() 
	{
		return region;
	}
	
	public String getPostcode() 
	{
		return postcode;
	}
	
	public String getCountry() 
	{
		return country;
	}
	
	public boolean isEmpty()
	{
		return Collator.isEmpty(street)
		&& Collator.isEmpty(street)
		&& Collator.isEmpty(town)
		&& Collator.isEmpty(postcode)
		&& Collator.isEmpty(region)
		&& Collator.isEmpty(country);
	}
	
	public void read(DataInputStream in) throws IOException 
	{
		sabDetailId = in.readLong();
		type = in.readInt();
		nabSubtypes = in.readInt();
		POBox = (String) Serializer.deserialize(in);
		street = (String) Serializer.deserialize(in);
		// street2 = (String) Serializer.deserialize(in);
		town = (String) Serializer.deserialize(in);
		region = (String) Serializer.deserialize(in);
		postcode = (String) Serializer.deserialize(in);
		country = (String) Serializer.deserialize(in);
		cri = in.readLong();
	}

	public void write(DataOutputStream out) throws IOException 
	{
		out.writeLong(sabDetailId);
		out.writeInt(type);
		out.writeInt(nabSubtypes);
		Serializer.serialize(POBox, out);
		Serializer.serialize(getStreet().trim(), out);
		// Serializer.serialize(street2, out);
		Serializer.serialize(town, out);
		Serializer.serialize(region, out);
		Serializer.serialize(postcode, out);
		Serializer.serialize(country, out);
		out.writeLong(cri);
	}
	
	public boolean equals(Object o)
	{
		Address that = (Address) o;
		return (this.type == that.type) 
		&& (this.nabSubtypes == that.nabSubtypes)
		&& HashUtil.equals(this.POBox, that.POBox)
		&& HashUtil.equals(this.street, that.street)
		&& HashUtil.equals(this.street2, that.street2)
		&& HashUtil.equals(this.town, that.town)
		&& HashUtil.equals(this.region, that.region)
		&& HashUtil.equals(this.postcode, that.postcode)
		&& HashUtil.equals(this.country, that.country);
	}
	
	//#mdebug error
	public String toString()
	{
		return "Address[sabDetailId=" + sabDetailId
			+ ",type=" + type
			+ ",nabSubtypes=" + nabSubtypes
			+ ",street=" + street
			+ ",street2=" + street2
			+ ",town=" + town
			+ ",region=" + region
			+ ",postcode=" + postcode
			+ ",country=" + country
			+ ",cri=" + cri
			+ "]";
	}
	//#enddebug
	
	/**
	 * Creates a new array of len elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1].
	 */
	public static Address[] trimArray(Address[] src, int len)
	{
		Address[] dst = new Address[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}		
	
	/**
	 * Extends an array.
	 */
	public static Address[] extendArray(Address[] src)
	{
		Address[] dst = new Address[src.length * 3 / 2 + 1];
		System.arraycopy(src, 0, dst, 0, src.length);
		return dst;
	}	
	
	/**
	 * Converts a type label to a type.
	 */
	public static int toType(String typeLabel)
	{
		int type = 0;
		if (TYPE_HOME_LABEL.equals(typeLabel))
		{
			type = TYPE_HOME;
		}
		else
		if (TYPE_WORK_LABEL.equals(typeLabel))
		{
			type = TYPE_WORK;	
		}							
		else
		if (TYPE_OTHER_LABEL.equals(typeLabel))
		{
			type = TYPE_OTHER;	
		}
		return type;
	}	
	
	/**
	 * Converts a type to a type label.
	 */
	public static String toTypeLabel(int type)
	{
		String typeLabel = null;
		if (TYPE_HOME == type)
		{
			typeLabel = TYPE_HOME_LABEL;
		}
		else
		if (TYPE_WORK == type)
		{
			typeLabel = TYPE_WORK_LABEL;
		}
		else	
		if (TYPE_OTHER == type)
		{
			typeLabel = TYPE_OTHER_LABEL;
		}
		return typeLabel;
	}
}
