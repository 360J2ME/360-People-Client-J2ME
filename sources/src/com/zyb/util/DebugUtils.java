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



public class DebugUtils {
	static StringBuffer keystrokes;
	
	public static String addKeystrokePressed(int keyCode)
	{
		if(keystrokes == null || keyCode == -14)
			keystrokes = new StringBuffer();
		
		if(keyCode != -14)
			keystrokes.append("{0,"+keyCode+"},");
		
		return keystrokes.toString();
	}


	public static String addKeystrokeReleased(int keyCode)
	{
		if(keystrokes == null)
			keystrokes = new StringBuffer();
		
		if(keyCode != -14 && keyCode != -13)
			keystrokes.append("{1,"+keyCode+"},");
		
		return keystrokes.toString();
	}

	public static String addKeystrokeRepeated(int keyCode)
	{
		if(keystrokes == null)
			keystrokes = new StringBuffer();
		
		if(keyCode != -14 && keyCode != -13)
			keystrokes.append("{2,"+keyCode+"},");
		
		return keystrokes.toString();
	}

	
	static long timebefore = 0;
	public static void timediff(String string, String separator) {
		if(string != null)
			System.out.println(string+separator+(System.currentTimeMillis()-timebefore));
		timebefore = System.currentTimeMillis();
	}

}
