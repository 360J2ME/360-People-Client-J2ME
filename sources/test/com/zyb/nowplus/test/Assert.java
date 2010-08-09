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
package com.zyb.nowplus.test;

import com.zyb.nowplus.business.domain.ListSelection;

public class Assert extends com.sonyericsson.junit.framework.Assert
{
	public static void assertEquals(long[] expected, long[] actual)
	{
		assertEquals("Unexpected length.", expected.length, actual.length);
		
		for (int i = 0; i < expected.length; i++)
		{
			assertEquals("Unexpected element " + i, expected[i], actual[i]);
		}
	}
	
	public static void assertEquals(int[] expected, int[] actual)
	{
		assertEquals("Unexpected length.", expected.length, actual.length);
		
		for (int i = 0; i < expected.length; i++)
		{
			assertEquals("Unexpected element " + i, expected[i], actual[i]);
		}
	}

	public static void assertEquals(byte[] expected, byte[] actual)
	{
		assertEquals("Unexpected length.", expected.length, actual.length);
		
		for (int i = 0; i < expected.length; i++)
		{
			assertEquals("Unexpected element " + i, expected[i], actual[i]);
		}
	}
	
	public static void assertEquals(Object[] expected, Object[] actual)
	{
		assertEquals("", expected, actual);
	}
	
	public static void assertEquals(String message, Object[] expected, Object[] actual)
	{
		assertEquals(message, expected, actual, 0);
	}
	
	/** 
	 * Compares the expected array with the actual array. 
	 * The order of the elements 0 to orderedFrom-1 is not significant. This allows 
	 * us to test that certain events have occurred, without checking in what order. 
	 */
	public static void assertEquals(String message, Object[] expected, Object[] actual, int orderedFrom)
	{
		assertEquals(message + ": length", expected.length, actual.length);
		
		for (int i = 0; i < orderedFrom; i++)
		{
			for (int j = i; j < orderedFrom; j++)
			{
				if (expected[i].equals(actual[j]))
				{
					Object tmp = actual[i];
					actual[i] = actual[j];
					actual[j] = tmp;
					break;
				}
			}
			assertEquals(message + ": element " + i, expected[i], actual[i]);
		}
		
		for (int i = orderedFrom; i < expected.length; i++)
		{
			if (expected[i] == null)
			{
				assertNull(message + ": element " + i + " is not null", actual[i]);
			}
			else
			if (actual[i] == null)
			{
				fail(message + ": element " + i + " is null");
			}
			else
			{
				assertEquals(message + ": element " + i, expected[i], actual[i]);
			}
		}
	}
	
	public static void assertEquals(ListSelection expected, ListSelection actual) 
	{
		assertEquals(expected.getEntries(), actual.getEntries());
		
		assertEquals("Unexpected start.", expected.getStart(), actual.getStart());
		assertEquals("Unexpected end.", expected.getEnd(), actual.getEnd());
		assertEquals("Unexpected total.", expected.getTotal(), actual.getTotal());
	}
}
