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
package com.zyb.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.zyb.nowplus.business.Active;
import com.zyb.nowplus.business.domain.Address;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.Note;
import com.zyb.nowplus.data.protocol.types.ServiceObject;

import de.enough.polish.io.Serializer;
import de.enough.polish.util.Comparator;

public class ArrayUtils
{
	public static String toString(boolean[] array)
	{
		if (array == null)
		{
			return "null";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		if (array.length > 0)
		{
			sb.append(array[0]);
			for (int i = 1; i < array.length; i++)
			{
				sb.append(",");
				sb.append(array[i]);
			}
		}
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * Creates a new array of len elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1].
	 */
	public static int[] trimArray(int[] src, int len)
	{
		int[] dst = new int[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}
	
	/**
	 * Extends an array.
	 */
	public static int[] extendArray(int[] src)
	{
		int[] dst = new int[src.length * 3 / 2 + 1];
		System.arraycopy(src, 0, dst, 0, src.length);
		return dst;
	}	

	public static String toString(int[] array)
	{
		if (array == null)
		{
			return "null";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		if (array.length > 0)
		{
			sb.append(array[0]);
			for (int i = 1; i < array.length; i++)
			{
				sb.append(",");
				sb.append(array[i]);
			}
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Creates a new array of len elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1].
	 */
	public static long[] trimArray(long[] src, int len)
	{
		long[] dst = new long[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}

	/**
	 * Extends an array.
	 */
	public static long[] extendArray(long[] src)
	{
		long[] dst = new long[src.length * 3 / 2 + 1];
		System.arraycopy(src, 0, dst, 0, src.length);
		return dst;
	}

    public static String toString(long[] array) {
		if (array == null)
		{
			return "null";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		if (array.length > 0)
		{
			sb.append(array[0]);
			for (int i = 1; i < array.length; i++)
			{
				sb.append(",");
				sb.append(array[i]);
			}
		}
		sb.append("}");
		return sb.toString();
    }
    
	/**
	 * Creates a new array of (len + 1) elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1]. Element len will be filled with srcL.
	 */
	public static char[] trimArray(char[] src, int len, char srcL)
	{
		char[] dst = new char[len + 1];
		System.arraycopy(src, 0, dst, 0, len);
		dst[len] = srcL;
		return dst;
	}

	/**
	 * Creates a new array of len elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1].
	 */
	public static String[] trimArray(String[] src, int len)
	{
		String[] dst = new String[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}

	/**
	 * Creates a new array of (len + 1) elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1]. Element len will be filled with srcL.
	 */
	public static String[] trimArray(String[] src, int len, String srcL)
	{
		String[] dst = new String[len + 1];
		System.arraycopy(src, 0, dst, 0, len);
		dst[len] = srcL;
		return dst;
	}

	/**
	 * Creates a new array of (len + 1) elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1]. Element len will be filled with srcL.
	 */
	public static Object[] trimArray(Object[] src, int len, Object srcL)
	{
		Object[] dst = new Object[len + 1];
		System.arraycopy(src, 0, dst, 0, len);
		dst[len] = srcL;
		return dst;
	}

	/**
	 * Creates a new array of (len + 1) elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1]. Element len will be filled with srcL.
	 */
	public static ServiceObject[] trimArray(ServiceObject[] src, int len)
	{
		ServiceObject[] dst = new ServiceObject[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}

	/**
	 * Sorts the elements 0..len-1.
	 */
    public static void shellSort(Object[] array, int len, Comparator comparator) {
		int i, j, increment;
		Object temp;
		increment = (int)len/2 + 1; //value according to the algorithm, previous>> //=3 
		while (true) {
			for (i=0; i < len; i++) {
				j = i;
				temp = array[i];
				while ((j >= increment) && ( comparator.compare( array[j-increment], temp) > 0 ) ) {
					array[j] = array[j - increment];
					j -= increment;
				}
				array[j] = temp;
			}
			if (increment == 1) {
				break;
			} else {
				increment >>=  1;
			}
		}
    }

	public static void serializeArray(long[] array, int len, DataOutputStream out) throws IOException
	{
		if (array == null)
		{
			out.writeInt(-1);
		}
		else
		{
			out.writeInt(len);
			for (int i = 0; i < len; i++)
			{
				out.writeLong(array[i]);
			}
		}
	}
	
	public static void serializeArray(int[] array, int len, DataOutputStream out) throws IOException
	{
		if (array == null)
		{
			out.writeInt(-1);
		}
		else
		{
			out.writeInt(len);
			for (int i = 0; i < len; i++)
			{
				out.writeInt(array[i]);
			}
		}
	}
	
	public static void serializeArray(Object[] array, DataOutputStream out) throws IOException
	{
		if (array == null)
		{
			out.writeInt(-1);
		}
		else
		{
			out.writeInt(array.length);
			for (int i = 0; i < array.length; i++)
			{
				Serializer.serialize(array[i], out);
			}
		}
	}

	/**
	 * Deserializes an array of longs.
	 */
	public static long[] deserializeLongArray(DataInputStream in) throws IOException
	{
		int len = in.readInt();
		if (len == -1)
		{
			return null;
		}
		long[] array = new long[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = in.readLong();
		}
		return array;
	}

	/**
	 * Deserializes an array of ints.
	 */
	public static int[] deserializeIntArray(DataInputStream in) throws IOException
	{
		int len = in.readInt();
		if (len == -1)
		{
			return null;
		}
		int[] array = new int[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = in.readInt();
		}
		return array;
	}
	
	/**
	 * Deserializes an array of Strings.
	 */
	public static String[] deserializeStringArray(DataInputStream in) throws IOException
	{
		int len = in.readInt();
		if (len == -1)
		{
			return null;
		}
		String[] array = new String[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = (String) Serializer.deserialize(in);
		}
		return array;
	}

	/**
	 * Deserializes an array of Identities.
	 */
	public static Identity[] deserializeIdentityArray(DataInputStream in) throws IOException
	{
		int len = in.readInt();
		if (len == -1)
		{
			return null;
		}
		Identity[] array = new Identity[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = (Identity) Serializer.deserialize(in);
		}
		return array;
	}

	/**
	 * Deserializes an array of Addresses.
	 */
	public static Address[] deserializeAddressArray(DataInputStream in) throws IOException
	{
		int len = in.readInt();
		if (len == -1)
		{
			return null;
		}
		Address[] array = new Address[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = (Address) Serializer.deserialize(in);
		}
		return array;
	}

	/**
	 * Deserializes an array of Notes.
	 */
	public static Note[] deserializeNoteArray(DataInputStream in) throws IOException
	{
		int len = in.readInt();
		if (len == -1)
		{
			return null;
		}
		Note[] array = new Note[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = (Note) Serializer.deserialize(in);
		}
		return array;
	}
	
	/**
	 * Compares two arrays element by element.
	 */
	public static boolean equals(Object[] o1, Object[] o2)
	{
		if (o1 == null)
		{
			return (o2 == null);
		}
		else
		{
			if (o1.length != o2.length)
			{
				return false;
			}
			for (int i = 0; i < o1.length; i++)
			{
				if (!HashUtil.equals(o1[i], o2[i]))
				{
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Compares two arrays element by element.
	 */
	public static boolean equals(Object[][] o1, Object[][] o2)
	{
		if (o1 == null)
		{
			return (o2 == null);
		}
		else
		{
			if (o1.length != o2.length)
			{
				return false;
			}
			for (int i = 0; i < o1.length; i++)
			{
				if (!equals(o1[i], o2[i]))
				{
					return false;
				}
			}
			return true;
		}
	}

	public static String toString(Object[] array)
	{
		if (array == null)
		{
			return "null";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		if (array.length > 0)
		{
			sb.append(array[0]);
			for (int i = 1; i < array.length; i++)
			{
				sb.append(",");
				sb.append(array[i]);
			}
		}
		sb.append("}");
		return sb.toString();
	}

	public static String toString(Object[][] array)
	{
		if (array == null)
		{
			return "null";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		if (array.length > 0)
		{
			sb.append(toString(array[0]));
			for (int i = 1; i < array.length; i++)
			{
				sb.append(",");
				sb.append(toString(array[i]));
			}
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Extends an array.
	 */
	public static Active[] extendArray(Active[] src)
	{
		Active[] dst = new Active[src.length * 3 / 2 + 1];
		System.arraycopy(src, 0, dst, 0, src.length);
		return dst;
	}
	
    public static int firstIndexOf(int value, int[] array) {
        if(null == array || array.length == 0)
            return -1;
        for (int i = 0; i < array.length; i++)
            if (array[i] == value)
                return i;
        return -1;
    }

    public static int indexOf(int value, int ref, int[] array){
        if(null == array || array.length == 0 || ref < 0)
            return -1;
        for (int i = 0; i < array.length; i++){
            if (array[i] == value && --ref == -1){
                return i;
            }
        }
        return -1;
    }

    public static int firstIndexOf(long value, long[] array) {
        if(null == array || array.length == 0)
            return -1;
        for (int i = 0; i < array.length; i++)
            if (array[i] == value)
                return i;
        return -1;
    }

}
