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

package com.zyb.nowplus.data.protocol.apihelpers;

import com.zyb.nowplus.MIDletContext;
import com.zyb.util.md5.MD5;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

//#if not polish.blackberry
import org.bouncycastle.crypto.params.RSAKeyParameters;
//#endif

/**
 * 
 * 
 *
 * @author Rudy Norff (rudy.norff@vodafone.com)
 */
public class Toolkit {	
	/**
	 * Returns the list of keys of an hash ordered alphabetically.
	 * 
	 * @param values
	 *            the enumeration of keys of an hash
	 * @return a list of key strings
	 */
	public static String[] sort(Hashtable h) {
		if (h == null) {
			return null;
		}
		
		Enumeration values = h.keys();
		Vector v = new Vector();
		
		while (values.hasMoreElements()) {
			boolean inserted = false;
			String value = (String) values.nextElement();
			String valueslug = value.toLowerCase();
			
			for (int i = 0; i < v.size(); i++) {
				String v2 = (String) v.elementAt(i);
				String v2slug = v2.toLowerCase();
				
				if (valueslug.compareTo(v2slug) < 0) {
					v.insertElementAt(value, i);
					inserted = true;
					break;
				}
			}
			
			if (!inserted) {
				v.addElement(value);
			}
		}
		
		String[] s = new String[v.size()];
		
		for (int i = 0; i < v.size(); i++) {
			s[i] = (String) v.elementAt(i);
		}
		
		return s;
	}
	
	
	
	public static String MD5(String s) {
		if (s == null) {
			return null;
		}
		
		byte[] b = null;
		
		try {
			b = s.getBytes("utf-8");
		} catch (java.io.UnsupportedEncodingException e) {
			b = s.getBytes();
		}
		
		return MD5(b);
	}
	
	

	public static String MD5(byte[] b) {
		MD5 md5 = new MD5(b);
		
		return md5.toHex();
	}
	
	
	/**
	 * Generates a PasswordHash for the GetSessionByCredentials()
	 * 
	 * @param ts The current timestamp in seconds.
	 * @param username The user's username.
	 * @param password The user's password.
	 * @param appKeySecret The application's secret.
	 * 
	 * @return the hash of the credentials
	 */
	public static String getPasswordHash(long ts, String username, String password,
					String appKeySecret) {
		if ((ts <= 0) || (username == null) || (username.length() == 0) || 
			(password == null) || (password.length() == 0) || (appKeySecret == null) ||
			(appKeySecret.length() == 0)) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(appKeySecret.toLowerCase());
		sb.append('&');
		sb.append(Long.toString(ts));
		sb.append('&');
		sb.append(username.toLowerCase());
		sb.append('&');
		sb.append(password.toLowerCase());
		
		return MD5(sb.toString());
	}
	
    public static byte[] getEncryptedPassword( final long ts, 
    								final long userid, final String password ) {
        return getEncryptedPassword( ts, String.valueOf( userid ), password );
    }

    public static byte[] getEncryptedPassword( final long ts, 
    						final String username, final String password ) {
        if( ts <= 0 ||
                username == null || username.trim().length() == 0 ||
                password == null || password.trim().length() == 0) {
            return null;
        }
        
        final String passwordT = password.trim();
        final String usernameT = username.trim();
        final StringBuffer sb = new StringBuffer();
        
        sb.append( MIDletContext.APP_KEY_SECRET.toLowerCase() );
        sb.append( '&' );
        sb.append( Long.toString( ts ) );
        sb.append( '&' );
        sb.append( usernameT );
        sb.append( '&' );
        sb.append( passwordT );
        
        byte[] cryptedPass = null;
        
        //#if not polish.blackberry
        final RSAKeyParameters pubKey = CryptoRSA.getRSAPubKey( CryptoRSA.defaultPubKeyModulo,
                CryptoRSA.defaultPubKeyExponential );
        //#endif
        
        try {
        	//#debug debug
        	System.out.println("String to be encrypted: " + sb.toString());
        	
        	byte[] pass = sb.toString().getBytes("utf-8");
        	
        	//#if polish.blackberry
        	final net.rim.device.api.crypto.RSAPublicKey mRSAPublicKey=new net.rim.device.api.crypto.RSAPublicKey(new net.rim.device.api.crypto.RSACryptoSystem( ),defaultPubKeyExponential,defaultPubKeyModulo);
			cryptedPass = encrypt( mRSAPublicKey, pass );
        	//#else
        	cryptedPass = CryptoRSA.encryptRSA(pubKey, pass);
        	//#endif
        	
        	pass = null;
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}
        
        return cryptedPass;
    }
	
	
	/**
	 * 
	 * Turns 4 signed bytes into an integer in little endian order.
	 * 
	 * @param byte1 The 1st byte (gets shifted 24 bit to the left).
	 * @param byte2 The 2nd byte (gets shifted 16 bit to the left).
	 * @param byte3 The 3rd byte (gets shifted 8 bit to the left).
	 * @param byte4  The 4th byte.
	 * 
	 * @return The integer with the shifted signed bytes.
	 */
	public static final int signedBytesToInt(byte byte1, byte byte2, 
												byte byte3, byte byte4) {
		int result = 0;
		
		result += (byte1 & 0x000000FF) << 24;
		result += (byte2 & 0x000000FF) << 16;
		result += (byte3 & 0x000000FF) << 8;
		result += (byte4 & 0x000000FF);
		
		return result;
	}
	
	
	/**
	 * Splits an integer into a signed byte-array.
	 * 
	 * @param integer The int to convert into bytes.
	 * 
	 * @return The byte-array containing the bytes of the int.
	 */
	public static final byte[] intToSignedBytes(int integer) {
		return new byte[]{ 
			(byte)(integer >>> 24),
			(byte)(integer >> 16 & 0xFF),
			(byte)(integer >> 8 & 0xFF),
			(byte)(integer & 0xFF) 
		};
	}
	
	
	public static void printHessian(InputStream is) {
		StringBuffer sb1 = new StringBuffer("$$$$$$$$$$$$$$$$$$$$$$$$$$ ");
		int tag = 0;
		
		try {
			while ((tag = is.read()) != -1) {
				//sb1.append(" (" + ((int) tag) + " " + ((char) tag) + ") ");
				char hessChar = ((char) tag);
				
				if ( ((hessChar >= 'a') && (hessChar <= 'z')) || 
					 ((hessChar >= 'A') && (hessChar <= 'Z')) ||
					 ((hessChar >= '0') && (hessChar <= '9')) ) {
					sb1.append(hessChar);
				} else {
					sb1.append("[" + tag + "] ");
				}
			}
		} catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
		}
		
		//#debug debug
		System.out.println("REQ/RESP: " + sb1.toString());
	}
	
	//#if polish.blackberry
	private static final byte[] defaultPubKeyModulo = new byte[] { 0, -93, -23,
		8, -55, 66, -20, 39, 17, 40, -86, 22, -120, -78, -13, 24, -35, 79,
		-49, -64, -104, -31, -125, -3, 39, 2, 88, 41, -70, 47, -41, -99,
		-15, 40, -105, -27, -95, -99, 99, -32, 44, 4, -18, -40, -5, -87,
		82, -33, 40, -4, 122, 15, 7, 3, -73, -84, 13, 32, -115, 41, 51, 27,
		101, 95, -120, 65, 16, 16, 40, 57, -64, -12, 8, 17, 80, -80, 21,
		98, 97, 40, -100, 114, 90, 121, 24, -11, -47, -33, 85, -16, 67, -2,
		-87, -18, -59, 24, -83, 127, -123, -99, -39, -35, 111, -90, 27, -9,
		-64, -111, -116, -71, -82, 5, -116, 73, -38, 117, -39, -113, -16,
		-115, 37, 5, -128, 68, 108, -106, 82, -44, 9 };

private static final byte[] defaultPubKeyExponential = new byte[] { 1, 0, 1 };
/**
 * Encrypt the plaintext passed into this method using the public key. The
 * ciphertext should be returned from the method.
 * 
 * @param publicKey
 *            an RSAPublicKey that should be used for encrypting the data.
 * @param plaintext
 *            the data to be encrypted.
 * @return the ciphertext or encrypted data.
 */
private  static byte[] encrypt(net.rim.device.api.crypto.RSAPublicKey publicKey, byte[] plaintext)
{
	java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
	
	try
	{
		net.rim.device.api.crypto.RSAEncryptorEngine engine = new net.rim.device.api.crypto.RSAEncryptorEngine( publicKey );

		net.rim.device.api.crypto.PKCS1FormatterEngine fengine = new net.rim.device.api.crypto.PKCS1FormatterEngine( engine );


		net.rim.device.api.crypto.BlockEncryptor encryptor = new net.rim.device.api.crypto.BlockEncryptor( fengine, output );

	    encryptor.write( plaintext );
	    encryptor.close();
	    output.close();
	}
	catch(Exception e)
	{
		//#debug debug
		System.out.println(" -------------------encrypt exception:"+e.toString()+"------");
	}


    return output.toByteArray();
}
//#endif
}
