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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Image;

/**
 * Class for handling resource fetching from 'solid archive' binary files generated
 * by ./antlib/solidarchive_generator_1.0.jar.
 * <p>
 * See project in svn://office.zyb.local:3691/nowplus/trunk/SolidArchiveGenerator
 * for generator details.
 *
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class SolidArchiveHandler {

	private String packagePath;	
	private String[] packageHeader;
	private int packageHeaderSize;
	
	private final String HEADER_ENTRY_SUB_DIVIDER_CHAR = "!";
	private final String HEADER_ENTRY_DIVIDER_CHAR = "¤";

	public SolidArchiveHandler(String resPath){
		packagePath=resPath;
		packageHeader = fetchHeader(resPath);	
	}

	/*******************************************************************************
	 * Fetches and decodes the header of the file pack to get information of the files
	 * contained herein.
	 *
	 * @param String resPath path of the file pack
	 * @return the ResBinFile header containing information of the file content
	 *******************************************************************************/	
	private final String[] fetchHeader(String resPath){

		byte thisByte=0, lastByte = 0;
		StringBuffer sb = new StringBuffer();
		int numEntry = 0;

		Vector resInfo = new Vector();

		InputStream is = null;
		ByteArrayOutputStream os = null;
		byte[] resourceFile = null;

		try{

			//reading entire resource file
			is = this.getClass().getResourceAsStream(resPath);				
			os = new ByteArrayOutputStream(1024);	

			byte[] tmp = new byte[512];
			int bytesRead;
			while( ( bytesRead = is.read( tmp ) )>0 )
			{
				os.write( tmp, 0, bytesRead );
			}
			// Clear any buffered data
			os.flush();        
			resourceFile = os.toByteArray();
			
			//decoding header information
			int i = 0;
			thisByte=resourceFile[0];
			lastByte=thisByte;
			while(i<resourceFile.length && !(lastByte==HEADER_ENTRY_SUB_DIVIDER_CHAR.getBytes()[0] && thisByte==HEADER_ENTRY_SUB_DIVIDER_CHAR.getBytes()[0]) ){

				//new entry?
				if(thisByte==HEADER_ENTRY_SUB_DIVIDER_CHAR.getBytes()[0]){
					numEntry++;
					resInfo.addElement(sb.toString());
					sb.delete(0,sb.length());
					i++;
					lastByte=resourceFile[i-1];
					thisByte=resourceFile[i];
					continue;
				}

				sb.append((char)thisByte);

				i++;
				lastByte=resourceFile[i-1];
				thisByte=resourceFile[i];
			}
			sb=null;
			resourceFile=null;

			packageHeaderSize = i + HEADER_ENTRY_SUB_DIVIDER_CHAR.getBytes().length;
			
			//#if polish.device.requires.UTF8_workaround == true
			packageHeaderSize+=1;
			//#else
			packageHeaderSize+=HEADER_ENTRY_DIVIDER_CHAR.getBytes().length;
			//#endif
			

		}
		catch(Exception e)
		{
			//#debug error
			System.out.println(e + "problem fetching res from bin.");
		}
		finally
		{
			try {
				if (is != null )
				{
					is.close();
					is=null;
				}
				if (os != null )
				{
					os.close();
					os=null;
				}		    		  
			}
			catch (Exception e)
			{
				//#debug error
				System.out.println(e + " problem closing streams.");
			}			
		}		

		//writing header String[]
		String[] packageHeader = new String[resInfo.size()];

		int i = 0;
		Enumeration e = resInfo.elements();
		for(;e.hasMoreElements();){
			String str = (String)e.nextElement();
			packageHeader[i]=str;
			i++;
		}

		resInfo=null;

		return packageHeader;
	}

	/*******************************************************************************
	 * Finds a desired resource in the ResBinFileHeader.
	 *
	 * @param resPath path of the desired resource
	 * @return the header index of the desired file, -1 if not found
	 *******************************************************************************/	
	private final int findInBinFileHeader(String resPath){
	
		//finding resInfo from header
		int i = 0;
		while(resPath.compareTo("/"+packageHeader[i])!=0){
			i+=3;			
			if(i>packageHeader.length-1)
			{
				//file not found
				i = -1;
				break;
			}
		}
		return i;		
	}

	/*******************************************************************************
	 * Fetches a specific resource from the file pack and returns this as a byte[].
	 *
	 * @param resPath path of the desired resource
	 * @return the desired file as byte[]
	 *******************************************************************************/		
	public final byte[] fetchResourceAsBytes(String resPath)
	{
		byte[] tempRes = null;
		byte[] res = null;
		int resIndex=0;
		int resLength=0;

		InputStream is = null;
		ByteArrayOutputStream os = null;

		//finding resInfo from header
		int i = findInBinFileHeader(resPath);
		
		if(i >= 0 && i<packageHeader.length)
		{
			resIndex=Integer.valueOf(packageHeader[i+1]).intValue();
			resLength=Integer.valueOf(packageHeader[i+2]).intValue();

			try{				
				//reading resource
				is = this.getClass().getResourceAsStream(packagePath);				
				os = new ByteArrayOutputStream(1024);	

				//jump to desired file
				if(is.markSupported())
					is.skip(resIndex+packageHeaderSize);	
				else
					for(int k = resIndex+packageHeaderSize; --k>=0;)
						is.read();
				
				byte[] tmp = new byte[512];
				int bytesRead;
				int totalBytes=0;
				while( ( bytesRead = is.read( tmp ) ) > 0 && totalBytes<=resLength)
				{
					os.write( tmp, 0, bytesRead );
					totalBytes+=tmp.length;
				}

				// Clear any buffered data
				os.flush();
				tempRes = os.toByteArray();
			}
			catch(Exception e)
			{
				//#debug error
				System.out.println(e + " problem fetching res from bin.");
			}
			
			//close streams
			try
			{
				if (is != null ){
					is.close();
					is=null;
				}
			}
			catch (Exception e)
			{
				//#debug error
				System.out.println(e + " problem closing streams.");
			}	
			try
			{
				if (os != null ){
					os.close();
					os=null;
				}		    		  
			}
			catch (Exception e)
			{
				//#debug error
				System.out.println(e + " problem closing streams.");
			}
			
			res = new byte[resLength];		
			System.arraycopy(tempRes,0,res,0,resLength);
		}

		return res;
	}

	/*******************************************************************************
	 * Fetches a specific resource as a unicode string from the file pack.
	 *
	 * @param resPath path of the desired resource
	 * @return the desired file as Unicode converted to String
	 *******************************************************************************/		
	public final String fetchResourceAsUnicode(String resPath){

		byte[] res = fetchResourceAsBytes(resPath);

		StringBuffer sb = new StringBuffer();

		//start from i=2 to aviod header character
		for(int i = 2; i<res.length-2;i+=2){
			char c = (char)((res[i]<<8)|res[i+1]);
			sb.append(c);
		}

		return sb.toString();
	}

	/*******************************************************************************
	 * Fetches a specific resource as a Image from the file pack.
	 *
	 * @param resPath path of the desired resource
	 * @return the desired file as Image object
	 *******************************************************************************/		
	public final Image fetchResourceAsImage(String resPath){

		byte[] res = fetchResourceAsBytes(resPath);

		return Image.createImage(res, 0, res.length);    
	}
}
