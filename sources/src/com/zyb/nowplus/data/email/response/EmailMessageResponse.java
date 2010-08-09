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
package com.zyb.nowplus.data.email.response;

import java.util.Vector;

import com.zyb.nowplus.data.email.types.EmailAddress;
import com.zyb.nowplus.data.email.types.EmailMessage;

/**
 * 
 * @author Jens Vesti
 */
public class EmailMessageResponse extends EmailResponse {
	
	private Vector messages;

	public EmailMessageResponse(String response) {
		super(response);
	}

	protected void parseResponse() {
		
		//TODO: improve and optimise parsing
		
		messages = new Vector();
		parseMessages(0, messages);
	}
	
	public Vector getMessages()
	{
		return messages;
	}
		
	private int parseMessages(int index, Vector messages)
	{
		while (response.charAt(index) == '*')
		{
			EmailMessage message = new EmailMessage();
			messages.addElement(message);
			
			index = parseMessage(index, message);
		}		
		return index;
	}

	private int parseMessage(int index, EmailMessage message)
	{
		index += 2;
		
		index = parseInteger(index);
		message.setId(parsedInt);
		
		// jump to fetch
		index = response.indexOf("FETCH ", index);
		
		index += "FETCH ".length();
		index = parseFetch(index, message);

		return index;
	}
	
	private int parseFetch(int index, EmailMessage message)
	{
		int i = response.indexOf("BODY[1] ");
		if (i != -1)
		{
			index = i + "BODY[1] ".length();
			index = parseBody(index, message);
		}
		else
		{
			index = response.indexOf("ENVELOPE ", index);
			
			index += "ENVELOPE ".length();
			index = parseEnvelope(index, message);
			
			// ignore everything after envelope
			index = parseRest(index);
			
			index += 2; // end-of-line
		}
		return index;		
	}
	
	private int parseBody(int index, EmailMessage message)
	{
		index += 1; // opening bracket
		
		index = parseInteger(index);
		int size = parsedInt;

		index += 1; // closing bracket
		
		String body = decodeQuotedPrintable(response.substring(index, index + size));
		message.setMessageBody(body + " ");
		
		return index + size;
	}
	
    /**
     * Decodes a quoted-printable encoded string. This is used when an 8-bit character set (256 characters) is
     * written by a 7-bit ASCII (128 characters). Characters, that code is in interval 128..255, are displayed
     * by three chars: "=" and their hexadecimal character code (i.e 'A9').

     * @param s String that will be converted
     * @param charset Character coding mapping used in string s
     * @return decoded string s
     */
    public static String decodeQuotedPrintable(String s) {
        StringBuffer output = new StringBuffer();
        int n = 0, strLength = s.length();
        char c;
        while (n < strLength) {
            // decode quoted character
            if (s.charAt(n) == '=') {
                if ((n + 2) < strLength) {
                    if (s.substring(n + 1, n + 3).equals("\r\n")) {
                        n += 3;
                        continue;
                    }

                    // thorws exception if input is uncorrectly encoded
                    try {
                        c = (char) Integer.parseInt(s.substring(n + 1, n + 3), 16);
                    } catch (NumberFormatException ex) {
                        output.append(s.charAt(n));
                        n++;
                        continue;
                    }
                    output.append(c);
                }
                n += 3;
            } // not quoted character
            else {
            	c = s.charAt(n);
                c &= 0x00FF;
                output.append(c);
                n++;
            }
        }
        return output.toString();
    }

	
	private int parseEnvelope(int index, EmailMessage message)
	{
		index += 1; // opening bracket
		
		index = parseString(index);
		message.setDate(parsed);
		
		while (response.charAt(index) == ' ') index++;
		
		index = parseString(index);
		message.setSubject(parsed);
		
		while (response.charAt(index) == ' ') index++;
		
		index = parseAddresses(index, message.getFroms());
		
		while (response.charAt(index) == ' ') index++;
		
		index = parseAddresses(index, message.getSenders());

		while (response.charAt(index) == ' ') index++;

		index = parseAddresses(index, message.getReplyTos());

		while (response.charAt(index) == ' ') index++;
		
		index = parseAddresses(index, message.getTos());
		
		while (response.charAt(index) == ' ') index++;

		index = parseAddresses(index, message.getCCs());
		
		while (response.charAt(index) == ' ') index++;

		index = parseAddresses(index, message.getBCCs());
		
		index = parseRest(index);

		return index;
	}

	private int parseAddresses(int index, Vector addresses)
	{
		if (response.charAt(index) == 'N')
		{
			index += "NIL".length();
			return index;
		}
		index += 1; // opening bracket
		
		while (response.charAt(index) != ')')
		{
			EmailAddress address = new EmailAddress();
			addresses.addElement(address);
			
			index = parseAddress(index, address);
			
			while (response.charAt(index) == ' ') index++;
		}
		
		return index + 1; // closing  bracket
	}
	
	private int parseAddress(int index, EmailAddress address)
	{
		index += 1; // opening bracket
		
		index = parseString(index);
		address.setPersonalName(parsed);
		
		while (response.charAt(index) == ' ') index++;
		
		index = parseString(index);
		address.setSmtpAtDomainList(parsed);
		
		while (response.charAt(index) == ' ') index++;
		
		index = parseString(index);
		address.setMailboxName(parsed);
		
		while (response.charAt(index) == ' ') index++;
		
		index = parseString(index);
		address.setHostName(parsed);
		
		return index + 1; // closing bracket
	}
	
	private int parseRest(int index)
	{
		int brackets = 1;
		while (brackets > 0)
		{
			if (response.charAt(index) == '(')
			{
				brackets++;
			}
			else
			if (response.charAt(index) == ')')
			{
				brackets--;
			}
			index++;
		}
		return index;
	}
	
	private String parsed;
	private int parsedInt;
	
	private int parseString(int start)
	{
		if (response.charAt(start) == 'N')
		{
			start += "NIL".length();
			
			parsed = null;
			return start;
		}
		
		start += 1; // opening quote		
		int end = response.indexOf('\"', start);
		
		parsed = response.substring(start, end);
		return end + 1; // closing quote
	}
	
	private int parseInteger(int start)
	{
		int end = start;
		while (Character.isDigit(response.charAt(end))) end++;
		
		parsedInt = Integer.parseInt(response.substring(start, end));
		return end;
	}
	
	//#mdebug error
	public String toString() {
		return "EmailMessageResponse["
		+ "]";
	}
	//#enddebug	
}
