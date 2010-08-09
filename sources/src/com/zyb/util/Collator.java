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
import java.io.IOException;
import java.util.Random;

public final class Collator 
{	
	private static Collator instance = new Collator();
	
	public static boolean isEmpty(String s) 
	{
		return (s == null) || (s.trim().length() == 0);
	}

	public static Collator getInstance()
	{
		return instance;
	}

	private char[] alphabetChars;
	private int[] alphabetRanks;

	public void loadAlphabet(String language)
	{
		//#debug debug
		System.out.println("Loading alphabet");
		
		if (language == null) 
		{
			language = "";
		}
		else
		{
			language = language.substring(0, 2).toLowerCase();
		}
		
		//#debug info
		System.out.println("Loading alphabet table for " + language);
		
		try 
		{
			DataInputStream dis = new DataInputStream(getClass().getResourceAsStream("/a-t"));

			int numberOfLanguages = dis.readInt();
			
			int languageIndex = 0;
			for (int j = 0; (j < numberOfLanguages); j++)
			{
				if (language.equals(dis.readUTF()))
				{
					languageIndex = j;
				}
			}
			
			int numberOfChars = dis.readInt();
			
			alphabetChars = new char[numberOfChars];
			alphabetRanks = new int[numberOfChars];
			for (int i = 0; i < numberOfChars; i++)
			{
				alphabetChars[i] = dis.readChar();

				for (int j = 0; (j < numberOfLanguages); j++)
				{
					int r = dis.readInt();
					if (j == languageIndex)
					{
						alphabetRanks[i] = r;
					}
				}
			}
			
			dis.close();
		}
		catch (IOException e)
		{
			//#debug error
			System.out.println("Error loading alphabet tables" + e);
			
			alphabetChars = new char[0];
			alphabetRanks = new int[0];
		}
	}
	
	public String compileFullName(String firstName, String middleNames, String lastName)
	{
		StringBuffer sb = new StringBuffer();
		if (!isEmpty(firstName))
		{
			sb.append(firstName);
			sb.append(" ");
		}
		if (!isEmpty(middleNames))
		{
			sb.append(middleNames);
			sb.append(" ");
		}
		if (!isEmpty(lastName))
		{
			sb.append(lastName);
			sb.append(" ");
		}
		return sb.toString();
	}
	
	public String compileRandomName(Random r)
	{
		int len = 3 + r.nextInt(10);

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) 
		{
			sb.append(alphabetChars[r.nextInt(alphabetChars.length)]);
		}
		return sb.toString();
	}
	
	public int[] compileSortName(String fullName)
	{
		int[] sortName = new int[fullName.length()];
		for (int i = 0; i < sortName.length; i++)
		{
			sortName[i] = findSortChar(fullName.charAt(i));
		}
		return sortName;
	}
	
	private int findSortChar(char c)
	{
		int min = 0;
		int mid = 0;
		int max = alphabetChars.length - 1;

		while (max - min > 7)
		{
			mid = (min + max) / 2;
			if (alphabetChars[mid] == c)
			{
				return alphabetRanks[mid];
			}
			else
			{
				if (alphabetChars[mid] > c)
				{
					max = mid - 1;
				}
				else
				{
					min = mid + 1;
				}
			}
		}
		
		for (mid = min; mid <= max; mid++)
		{
			if (alphabetChars[mid] == c)
			{
				return alphabetRanks[mid];
			}
		}	
		
		return 0;
	}	
}
