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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.Font;

import de.enough.polish.ui.TextField;
import de.enough.polish.util.Locale;
import de.enough.polish.util.TextUtil;

public class TextUtilities {

	public static final String CONT = "...";
	private static final int MIN_WIDTH = 20;
	private static String currentUsedKeymappingLanguage;
	private static Hashtable CachedCharactersHT;
	
	public final static String VALID_PHONE_NUMBER_CHARS="1234567890+*#";
	
	public final static char CHAR_NEWLINE = '\n';
	public final static char CHAR_RETURN = '\r';

	
	public static String[] wrap(String txt, int width, Font font)
	{
		return wrap(txt, width, width, font, false);
	}
	
	public static String[] wrap(String txt, int width, Font font, boolean concat)
	{
		return wrap(txt, width, width, font, concat);
	}
	
	public static String[] wrap(String txt, int firstLineWidth, int lineWidth, Font font, boolean concat)
	{
		String[] messageLines = null;

		if(firstLineWidth > MIN_WIDTH && lineWidth > MIN_WIDTH)//We do not want to wrap lines if width is too small. Will result in an out of mem error.
			messageLines = TextUtil.wrap(txt, font, firstLineWidth, lineWidth);
		else
		{
			messageLines = new String[1];
			messageLines[0] = "";
		}
		
		if(concat)
		{
			if(messageLines.length > 1)
			{
				if(font.stringWidth((messageLines[0]+CONT))<=firstLineWidth)
					messageLines[0]+=CONT;
				else
					messageLines[0] = TextUtil.wrap(messageLines[0], font, firstLineWidth-font.stringWidth(CONT), lineWidth)[0]+CONT;
			}
		}

		//#debug debug
		System.out.println("to:"+messageLines[0]);

		return messageLines;
	}
	
	/**
	 * Checks if a text string is a valid email adress
	 * <p>
	 * The method presumes that input is constrained to TextField.EMAILADDR
	 */
	public static boolean isValidEmail(String email)
	{
	    
		if(email == null || email.length() < 5 
				||email.indexOf(" ") != -1) //emtpy space
			return false;
		
		int indexAt = email.indexOf("@");
		
		//no '@'?
		if(indexAt < 0)
			return false;
		
		
		 String[] emailSplits =  split(email,"@");
		 
		 //FIX: e.g. instance: xxx@cxx.xx@ is possible to register with.
		 if(emailSplits==null||emailSplits.length!=2
				 ||emailSplits[0]==null||emailSplits[1]==null 
				 ||emailSplits[0].trim().length() <=0 ||emailSplits[1].trim().length() <=0
				 ||emailSplits[1].indexOf(".") == -1)//no '.' e.g. name@emailcom
		    return false;
		 
		 //'.' before '@'
		if (email.charAt(indexAt - 1) == '.')//e.g. 'name.@domain.com
			return false;

		//'.' after '@'
		if (email.charAt(indexAt + 1) == '.')////e.g. 'name@.email.com
			return false;

//		 eu|com|org|net|edu|gov|mil|biz|info|mobi|name|aero|asia|jobs|museum
//		int index = emailSplits[1].lastIndexOf(".", 1);//last occurrence of the specified substring, starting at index 1
		int index =de.enough.polish.util.TextUtil.lastIndexOf(emailSplits[1],".");
		
		String domainSuffix = emailSplits[1].substring(index + 1);

		if (domainSuffix.length() <= 1 || domainSuffix.length() >= 7)// .[a-z]{2,6}
			return false;
		
		for (int i = 0; i < domainSuffix.length(); i++)
		{
			char mChar = domainSuffix.charAt(i);

			if (!((mChar >= 0x41 && mChar <= 0x5a)// 'A'~'Z'
			|| (mChar >= 0x61 && mChar <= 0x7a) // 'a'~'z'
			))
				return false;
		}

		
		char prevChar = 0;
		for (int i = 0; i < emailSplits[0].length(); i++)
		{
			char curChar = emailSplits[0].charAt(i);
			
			if (!((curChar >= 0x30 && curChar <= 0x39) //'0'~'9'
					|| (curChar >= 0x41 && curChar <= 0x5a)//'A'~'Z'
					|| (curChar >= 0x61 && curChar <= 0x7a) //'a'~'z'
					|| (curChar == 0x2e)//'.'
					|| (curChar == 0x2d) //'-' minus
					|| (curChar == 0x5f)))//'_'underscore
				return false;
			
			if ((curChar == 0x2e) && (curChar == prevChar))//'..'
				return false;
			
			prevChar = curChar;
		}
		
		 
		//check for consecutive dots
		int indexFirstDot =  email.indexOf(".", 0);
		int indexDot = indexFirstDot;
		
		//check first letter is not '.' 
		if(indexFirstDot == 0)
			return false;
		
		//check for two consecutive dots
		if(indexDot != -1){
			int newIndex;
			while( (newIndex = email.indexOf(".", indexDot+1)) != -1)
			{
				if(newIndex == indexDot+1)
					return false;
				else
					indexDot = newIndex;
			}
		}
		
		//check last letter is not '.' 
		if(indexDot >= (email.length()-1))
			return false;

		//find first and last occouring '.' after '@'
		indexFirstDot = email.indexOf(".", indexAt);
		indexDot = indexFirstDot;
		if(indexDot != -1){
			int newIndex;
			while( (newIndex = email.indexOf(".", indexDot+1)) != -1)
			{
				indexDot = newIndex;
			}
		}
		
		/*if
		 * there text before '@',
		 * there text after '@',
		 * letter before '@' is not a '.'
		 * letter after '@' is not a '.'
		 * '.' after '@' exists
		 */
		if(indexAt != 0 && indexAt < (email.length()-1) && email.charAt(indexAt - 1) != '.' && email.charAt(indexAt + 1) != '.' && indexAt < indexDot)
			return true;
		else 
			return false;
	}
	
	public static String[] split(String _text, String _seperatedStr)
	{
		if(_text==null || _seperatedStr==null)
			return null;
		
		java.util.Vector vector = new java.util.Vector();

		int index;
		
		int preIndex = 0;

		while ((index = _text.indexOf(_seperatedStr, preIndex)) > -1)
		{
			vector.addElement(_text.substring(preIndex, index));
			
			preIndex = index + 1;
		}
		
		vector.addElement(_text.substring(preIndex));

		String[] result = new String[vector.size()];
		
		vector.copyInto(result);

		return result;
	}
	
	public static boolean isValidUsername(String un)
	{
		return isValidPassword(un);
	}
	
	public static boolean isValidPassword(String pw)
	{
		if(pw == null || pw.length() < 6)
			return false;
		
		return true;
	}
	
	public static String invalidChars(final String validChars, final String txtToValidate)
	{
		StringBuffer buff = null;
		for(int i=0; i<txtToValidate.length(); i++)
			if(validChars.indexOf(txtToValidate.charAt(i)) == -1)
			{
				if(buff == null)
					buff = new StringBuffer();
			
				buff.append(txtToValidate.charAt(i));
			}
		return buff==null?"":buff.toString();
	}
	
	/*
	 * Strips for chars that are not valid
	 * @param validChars are the valid characters that are _not_ stripped
	 * @param txtToStrip the string that should be stripped for invalid characters
	 * @return the valid string
	 * */
	public static String stripNonValidChars(final String validChars, final String txtToStrip)
	{
		StringBuffer buff = null;
		for(int i=0; i<txtToStrip.length(); i++)
			if(validChars.indexOf(txtToStrip.charAt(i)) != -1)
			{
				if(buff == null)
					buff = new StringBuffer();
			
				buff.append(txtToStrip.charAt(i));
			}
		return buff==null?"":buff.toString();
	}
	/**
	 * Returns a full and localized name for the parameter month
	 * 
	 * @param monthNum
	 * @param monthString
	 * @return localized full name of month
	 */
	public static String getMonthFullName(int monthNum, String monthString)
	{
		if(null == monthString)
			monthString = "";
		
		monthString = monthString.toLowerCase();
		
		if(monthNum == 1 || monthString.equals("jan"))
			return Locale.get("nowplus.client.java.profilepage.content.date.month.jan");
		else
		if(monthNum == 2 || monthString.equals("feb"))
			return Locale.get("nowplus.client.java.profilepage.content.date.month.feb");
		else
		if(monthNum == 3 || monthString.equals("mar"))
			return Locale.get("nowplus.client.java.profilepage.content.date.month.mar");
		else
		if(monthNum == 4 || monthString.equals("apr"))
			return Locale.get("nowplus.client.java.profilepage.content.date.month.apr");
		else
		if(monthNum == 5 || monthString.equals("may"))
			return Locale.get("nowplus.client.java.profilepage.content.date.month.may");
		else
		if(monthNum == 6 || monthString.equals("jun"))
			return Locale.get("nowplus.client.java.profilepage.content.date.month.jun");
		else
		if(monthNum == 7 || monthString.equals("jul"))
			return Locale.get("nowplus.client.java.profilepage.content.date.month.jul");
		else
		if(monthNum == 8 || monthString.equals("aug"))
			return Locale.get("nowplus.client.java.profilepage.content.date.month.aug");
		else
		if(monthNum == 9 || monthString.equals("sep"))
			return Locale.get("nowplus.client.java.profilepage.content.date.month.sep");
		else
		if(monthNum == 10 || monthString.equals("oct"))
			return Locale.get("nowplus.client.java.profilepage.content.date.month.oct");
		else
		if(monthNum == 11 || monthString.equals("nov"))
			return Locale.get("nowplus.client.java.profilepage.content.date.month.nov");
		else
		if(monthNum == 12 || monthString.equals("dec"))
			return Locale.get("nowplus.client.java.profilepage.content.date.month.dec");
		else
			return null;
	}
	
	public static String getMonth(final long time)
	{
		Date date = new Date(time);
		return date.toString().substring(4,7);
	}
	
	public static String getDay(final long time)
	{
		Date date = new Date(time);
		return date.toString().substring(0,3);
	}
	
	public static String getTime(final long time)
	{
		Date date = new Date(time);
		return date.toString().substring(11, 16);
	}
	
	/**
	 * Returns a short and localized name for the parameter day
	 * 
	 * @param dayNum
	 * @param dayString
	 * @return localized short name of day
	 */
	public static String getDayShortName(int dayNum, String dayString)
	{
		if(null == dayString)
			dayString = "";
		
		dayString = dayString.toLowerCase();
		
		if(dayNum == 1 || dayString.equals("mon"))
			return Locale.get("nowplus.client.java.chat.thread.date.day.mon");
		else
		if(dayNum == 2 || dayString.equals("tue"))
			return Locale.get("nowplus.client.java.chat.thread.date.day.tue");
		else
		if(dayNum == 3 || dayString.equals("wed"))
			return Locale.get("nowplus.client.java.chat.thread.date.day.wed");
		else
		if(dayNum == 4 || dayString.equals("thu"))
			return Locale.get("nowplus.client.java.chat.thread.date.day.thu");
		else
		if(dayNum == 5 || dayString.equals("fri"))
			return Locale.get("nowplus.client.java.chat.thread.date.day.fri");
		else
		if(dayNum == 6 || dayString.equals("sat"))
			return Locale.get("nowplus.client.java.chat.thread.date.day.sat");
		else
		if(dayNum == 7 || dayString.equals("sun"))
			return Locale.get("nowplus.client.java.chat.thread.date.day.sun");
		else
			return null;
	}
	
	
	/**
	 * 
	 * <p>Splits a String into substrings by looking up all the tokens found in source.
	 * If ignoreEscapedToken is true source will not be split at token if escapeToken
	 * is found one position before it.</p>
	 * 
	 * <p>E.g. source "A;cool\;company" with token ';', escapeToken '\' and 
	 * ignoreEscapedToken true will give back 2 Strings: "A" and "cool\;company" 
	 * while calling the same method with ignoreEscapedToken false will result
	 * in 3 Strings: "A", "cool\" and "company".</p>
	 * 
	 * @param source The source string to split into substrings.
	 * @param token The token to split by.
	 * @param escapeToken The escape token. If ignoreEscapedToken is true the string
	 * will not be split at the found token if there is an escapeToken before it.
	 * @param ignoreEscapeToken True if the escape token should be ignored when splitting
	 * strings.
	 * 
	 * @return An array holding the split substrings.
	 * 
	 */
	public static final String[] getSubstrings(final String source, final char token,
												final char escapeToken, 
												final boolean ignoreEscapeToken) {
		if (null == source) {
			return null;
		}
		
		int counter = 0;
		String[] substrings = null;
		for (int i = 0; i < source.length(); i++) {
			if ((source.charAt(i) == token)) {
				int pos = ((i < 1) ? 1 : i);
				
				if (ignoreEscapeToken ||
					((!ignoreEscapeToken) && (source.charAt(pos - 1) != escapeToken))) {
					counter++;
				}
			} else if (i == (source.length() - 1)) {
				// collect the last characters
				counter++;
			}
		}

		if (counter > 0) {
			substrings = new String[counter];
			
			int prevOccurence = 0;
			int offset = 0;	
			for (int i = 0; i < source.length(); i++) {
				if (source.charAt(i) == token) {
					int pos = ((i < 1) ? 1 : i);
					
					if (ignoreEscapeToken ||
							((!ignoreEscapeToken) && 
									(source.charAt(pos - 1) != escapeToken))) {				
						substrings[offset] = source.substring(prevOccurence, i);
						prevOccurence = i + 1;
						offset++;
					}
				} else if (i == (source.length() - 1)) {
					// we found the last char and will return the substr for it
					substrings[offset] = source.substring(prevOccurence, (i + 1));
				}
			}
		}
		
		return substrings;
	}
	
	
	public static String escapeTokens(String source, char escapeToken) {
		if (null == source) {
			return null;
		}
		
		char[] sourceChars = source.toCharArray();
		
		// count number of tokens to escape
		int foundTokens = 0;
		for (int i = 0; i < sourceChars.length; i++) {
			if (needsEscaping(sourceChars[i])) {
				foundTokens++;
			}
		}
		
		char[] escapedChars = new char[sourceChars.length + foundTokens];
		
		// escape the tokens now
		int tokenCounter = 0;
		for (int j = 0; j < sourceChars.length; j++) {
			if (needsEscaping(sourceChars[j])) {
				escapedChars[j + tokenCounter] = escapeToken;
				escapedChars[j + tokenCounter + 1] = sourceChars[j];
				tokenCounter++;
			} else {
				escapedChars[j + tokenCounter] = sourceChars[j];
			}
		}
		
		sourceChars = null;
		
		return new String(escapedChars);
	}
	
	
	public static String unescapeTokens(String source, char unescapeToken) {
		if (null == source) {
			return null;
		}

		char[] sourceChars = source.toCharArray();

		// count number of tokens to escape
		int foundTokens = 0;
		for (int i = 1; i < sourceChars.length; i++) {
			if ((sourceChars[i - 1] == unescapeToken) && 
					needsEscaping(sourceChars[i])) {
				foundTokens++;
			}
		}

		char[] unescapedChars = new char[sourceChars.length - foundTokens];

		// escape the tokens now
		int tokenCounter = 0;
		for (int j = 0; j < sourceChars.length; j++) {			
			if ((sourceChars[j] == unescapeToken) && 
						needsEscaping(sourceChars[j + 1])) {
				unescapedChars[j - tokenCounter] = sourceChars[j + 1];
				tokenCounter++;
			} else {
				unescapedChars[j - tokenCounter] = sourceChars[j];
			}
		}

		sourceChars = null;

		return new String(unescapedChars);
	}
	
	private static boolean needsEscaping(char c)
	{
		return (c == ';') || (c == ',') || (c == '\\');
	}
	
	/**
	 * This is returning one of a series of known and supported languages for links
	 * @return
	 */
	public static String getCurrentLanguageForLinks() 
	{
		
		String locale = System.getProperty("microedition.locale");

		//#debug
		System.out.println("microedition.locale:"+locale);
		
		if(locale == null || locale.length()<2)
			return "EN";

		locale = locale.substring(0, 2).toLowerCase();

		if(locale.compareTo("da")==0)
			return "DA";
		else
		if(locale.compareTo("de")==0)
			return "DE";
		else
		if(locale.compareTo("nl")==0)
			return "NL";
		else
		if(locale.compareTo("fr")==0)
			return "FR";
		else
		if(locale.compareTo("dn")==0)
			return "DN";
		else
		if(locale.compareTo("es")==0)
			return "ES";
		else
		if(locale.compareTo("it")==0)
			return "IT";
		else
		if(locale.compareTo("tr")==0)
			return "TR";
		else
		if(locale.compareTo("gr")==0 || locale.compareTo("el")==0)
			return "EL";
		else
		if(locale.compareTo("pt")==0)
			return "PT";
		else
			if(locale.compareTo("ru")==0)
				return "RU";
		
		return "EN";
	}

	/**
	 * @return
	 */
	public static String getCurrentHeaderPrefix()
	{
		String smsc = System.getProperty("wireless.messaging.sms.smsc");
		
		if(smsc == null || smsc.length()<3 || !smsc.startsWith("+"))
			return null;
		
		if(smsc.startsWith("+351")) //Portugal
			return "+351";
		if(smsc.startsWith("+353")) //Eire
			return "+353";

		return smsc.substring(0, 3);
	}
	
	//#if !polish.blackberry
	/**
	 * @param language is the language to load, if null current phones language is used
	 */
	public static void loadTextFieldCharacterset(String language) 
	{
		//#debug debug
		System.out.println("Loading text field characters " + language);
		
		//#if polish.TextField.useDynamicCharset
		
		//#debug debug
		System.out.println("loadTextFieldCharacterset(String language):"+language);

		//#debug debug
		System.out.println("before currentUsedKeymappingLanguage:"+currentUsedKeymappingLanguage);
		
		//#debug debug
		System.out.println("Loading keymappings for language "+language+"(!="+currentUsedKeymappingLanguage+")");
		
		//#if activate.solidarchive.charset
		
		if(language == null)
			language = getCurrentLanguageOnly();
		
		if(language != null && (currentUsedKeymappingLanguage == null || !currentUsedKeymappingLanguage.equals(language)))
		{

			try
			{
				//loading key mapping files from 'solid archive'
				ByteArrayInputStream low_bin = null;
				DataInputStream low_din = null;
				ByteArrayInputStream up_bin = null;
				DataInputStream up_din = null;
				try 
				{
					if(CachedCharactersHT == null)
						CachedCharactersHT = new Hashtable(4);//Initially 2 languages with upper+lowercase
					
					low_din = (DataInputStream)CachedCharactersHT.get(language+"low");
					up_din = (DataInputStream)CachedCharactersHT.get(language+"up");
						
					if(low_din == null || up_din == null)
					{
						//#debug debug
						System.out.println("Loading from solidarchive");

						SolidArchiveHandler rbfh = new SolidArchiveHandler("/keymap.bin");
						byte[] lower = rbfh.fetchResourceAsBytes("/keymappings."+language+".km");
						byte[] upper = rbfh.fetchResourceAsBytes("/keymappings."+language+".upper.km");
						
						if(null == lower || null == upper )
						{
							//use default
							lower = rbfh.fetchResourceAsBytes("/keymappings.en.km");
							upper = rbfh.fetchResourceAsBytes("/keymappings.en.upper.km");
						}
						
						low_bin = new ByteArrayInputStream( lower );
						low_din = new DataInputStream( low_bin );
						up_bin = new ByteArrayInputStream( upper );
						up_din = new DataInputStream( up_bin );
					
						CachedCharactersHT.put(language+"low",low_din);
						CachedCharactersHT.put(language+"up",up_din);
					}
					
					TextField.loadCharacterSets(low_din,up_din
					//#if polish.device.encoding:defined
					//#= ,"${polish.device.encoding}"
					//#endif
					);
				}
				catch (Exception e)
				{
					//#debug error
					System.out.println("error loading textkeys file" + e);
				}
				
				try 
				{
					if (low_bin != null )
					{
						low_bin.close();
						low_bin=null;
					}	
				}
				catch (Exception e){}
				try 
				{
					if (up_bin != null )
					{
						up_bin.close();
						up_bin=null;
					}	
				}
				catch (Exception e){}
				
				currentUsedKeymappingLanguage = language;
				
				//#debug debug
				System.out.println("after currentUsedKeymappingLanguage:"+currentUsedKeymappingLanguage);
			}
			catch(Exception e)
			{
				//#debug debug
				System.out.println("No mappings exist for language "+language);
			}
		}
		
		//#else
		
		if(language == null)
			language = getCurrentLanguageOnly();
		
		if(currentUsedKeymappingLanguage == null || !currentUsedKeymappingLanguage.equals(language))
		{
			TextField.loadCharacterSets("/keymappings."+language+".km","/keymappings."+language+".upper.km"
					//#if polish.device.encoding:defined
					//#= ,"${polish.device.encoding}"
					//#endif
			);
			
			currentUsedKeymappingLanguage = language;
		}
		
		//#endif
		
		//#endif
	}
	//#endif
	
	/*
	 Cleaning up whatever resources were created in TextUtilities during execution
	 * */
	public static void cleanUpStaticResources()
	{
		//#debug closedown
		System.out.println("Cleaning up resources");

		if(CachedCharactersHT != null)
		{
			Enumeration elements = CachedCharactersHT.elements();
			while(elements.hasMoreElements())
			try 
			{
				//#debug debug
				System.out.println("Cleaning up resource");

				DataInputStream dis = (DataInputStream)elements.nextElement();
				if (dis != null )
					dis.close();
			}
			catch (Exception e)
			{
				//#debug error
				System.out.println("Failed closing down datainputstream:"+e);
			}
		}		
	}
	
	public static void loadLanguageTextKeys(String language)
	{
		loadLanguageTextKeys(language, "en");
	}
	
	/**
	 * Loads language specific textkeys from 'solid archive' file.
	 * <p>
	 * If none of the argument languages are found, this method default to english.
	 *  
	 * @param language language to load
	 * @param defaultLanguage language to load if non other found
	 */
	public static void loadLanguageTextKeys(String language, String defaultLanguage)
	{
		//#debug debug
		System.out.println("Loading translations " + language + " " + defaultLanguage);
		
		//#if activate.solidarchive.textkeys
		
		ByteArrayInputStream bin = null;
		DataInputStream din = null;
		
		try 
		{
			SolidArchiveHandler rbfh = new SolidArchiveHandler("/locale.bin");
			byte[] locFileBytes = rbfh.fetchResourceAsBytes("/"+language+".loc");
			
			if(null == locFileBytes && null != defaultLanguage)
				//try default parameter
				locFileBytes = rbfh.fetchResourceAsBytes("/"+defaultLanguage+".loc");
			
			if(null == locFileBytes)
				//use default
				locFileBytes = rbfh.fetchResourceAsBytes("/en.loc");
			
			bin = new ByteArrayInputStream( locFileBytes );
			din = new DataInputStream( bin );
			
			Locale.loadTranslations(din);
		}
		catch (Exception e)
		{
			//#debug error
			System.out.println("error loading textkeys file" + e);
		}
		
		try 
		{
			if (bin != null )
			{
				bin.close();
				bin=null;
			}	
		}
		catch (Exception e){}
		try 
		{
			if (din != null )
			{
				din.close();
				din=null;
			}
		}
		catch (Exception e){}
		
		//#else
		
		try
		{
			Locale.loadTranslations("/"+language+".loc");
		}
		catch (Exception e) 
		{
			//#debug debug
			System.out.println("error loading preferred textkeys file");
			
			try
			{
				Locale.loadTranslations("/"+defaultLanguage+".loc");
			}
			catch (Exception e2) 
			{
				//#debug debug
				System.out.println("error loading default textkeys file");
				
				try
				{
					Locale.loadTranslations("/en.loc");
				}
				catch (Exception e3) 
				{
					//#debug error
					System.out.println("error loading textkeys file" + e3);
				}
			}
		}
		
		//#endif
	}

	public static String getCurrentLanguageOnly()
	{
		String locale = System.getProperty("microedition.locale");
		if(locale == null)
		{
			return null;
		}
		else
		{
			return locale.substring(0, 2);
		}
	}

}
