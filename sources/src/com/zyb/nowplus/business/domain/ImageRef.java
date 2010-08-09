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

import javax.microedition.lcdui.Image;

import com.zyb.util.HashUtil;

import de.enough.polish.content.ContentDescriptor;
import de.enough.polish.content.ContentListener;
import de.enough.polish.util.ArrayList;
import de.enough.polish.util.ImageUtil;

//#if polish.blackberry
import com.zyb.util.BlackBerryConnectionSuffix;
//#endif

/**
 * A reference to an image, which always returns an image.
 * If the referenced image is not loaded yet, a default image is
 * returned.
 */
public class ImageRef implements ContentListener
{
	protected String url;
	private boolean listening;
	
	private ArrayList listeners;
	
	private Image image;
	private Image defaultImage;
	private boolean error;
	private String urlPostFix;
	
	/*
	 *  Constant for the minimum image size to be downloaded from the server. The icons are not downloaded if less than 29x29px
	 */
	private static final int MIN_IMAGE_SIZE = 29;
	
	/**
	 * @param defaultImage
	 * 			default image we display until image is loaded
	 * 
	 * cropping is default
	 * encoding defaults to png
	 * height and width defaults to default image
	 */
	public ImageRef(final Image defaultImage)
	{
		this(defaultImage, defaultImage.getWidth(), defaultImage.getHeight(), true, null);
	}

	/**
	 * @param contentSource
	 * 			the source from where we get the content
	 * @param defaultImage
	 * 			default image we display until image is loaded
	 * @param crop
	 * 			if the image should be cropped so it fits the specified width and height
	 * 
	 * encoding defaults to png
	 * height and width defaults to default image
	 */
	public ImageRef(final Image defaultImage, final boolean crop)
	{
		this(defaultImage, defaultImage.getWidth(), defaultImage.getHeight(), crop, null);
	}

	/**
	 * @param contentSource
	 * 			the source from where we get the content
	 * @param defaultImage
	 * 			default image we display until image is loaded
	 * @param width
	 * 			width of the image to retrieve, -1 will not create and pass on parameters 
	 * @param height
	 * 			height of the image to retrieve, -1 will not create and pass on parameters 
	 * @param crop
	 * 			if the image should be cropped so it fits the specified width and height
	 * @param encoding
	 * 			the encoding which the image should be retrieved in
	 */
	public ImageRef(final Image defaultImage, final int width, final int height, final boolean crop, final String encoding)
	{
		this.url = null;
		this.listening = false;
		
		//Adjusting the size of the image. Defaulting to default image size. - Jens
		this.setImageProperties(width, height, crop, encoding);
		
		this.defaultImage = defaultImage;
	}
	
	/**
	 * 
	 */
	private void setImageProperties(int width, int height, boolean crop, String encoding) {
		
		if(width == -1 || height == -1)
			return;
		
		StringBuffer buff = new StringBuffer();
		
		//Appending sizes
		buff.append("?size=");
		buff.append(width >= MIN_IMAGE_SIZE ? width : MIN_IMAGE_SIZE);
		buff.append("x");
		buff.append(height>= MIN_IMAGE_SIZE ? height : MIN_IMAGE_SIZE);
				
		//Appending if we want image cropped
		if(crop)
			buff.append("!");
		else
		{
			/* MUST define a background to retain requested image dimensions
			 * BE will otherwise respect image aspect ratio and crop image to
			 * fit frame.
			 */
			int defaultBG = 0x00000000; //Transparent
			buff.append("&background=");
			buff.append(Integer.toHexString(defaultBG));
		}

		//Appending the encoding
		buff.append("&encoding=");
		if(encoding != null)
			buff.append(encoding);
		else
			buff.append("jpeg");
		//#message Must be changed back to PNG once content server supports it
		//TODO Must be changed back to PNG once content server supports it

		//Appending sync behaviour
		buff.append("&wait");
		
		//#if polish.blackberry
		buff.append(BlackBerryConnectionSuffix.connSuffixStr);
		//#endif
		
		setUrlPostFix(buff.toString().intern());
	}

	public void addListener(RefListener listener)
	{
		//initialize only when nessesary
		if(null == this.listeners)
			this.listeners = new ArrayList(1,1); //initial size 1, growth factor 1
		
		if(!this.listeners.contains(listener))
			this.listeners.add(listener);
	}
	
	public void removeListener(RefListener listener)
	{
		if(null != listeners && !this.listeners.contains(listener))
			this.listeners.remove(listener);
	}
	
	public void clearListeners()
	{
		if(null != listeners && listeners.size() > 0)
			this.listeners.clear();
	}
	
	public synchronized void setUrl(String url)
	{
		if (!HashUtil.equals(this.url, url))
		{
			this.url = url;
			if (listening || hasContent())
			{
				setContent(null, false);
				request();
			}
		}
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public Image getImage()
	{
		return (image == null) ? defaultImage : image;
	}

	public String getUrlPostFix() {
		return (urlPostFix==null) ? "" : urlPostFix;
	}

	public void setUrlPostFix(String urlPostFix) 
	{
		this.urlPostFix = urlPostFix;
	}
	
	protected String getUrlToRequest() {
		return url+getUrlPostFix();
	}
	
	public synchronized void onContentLoaded(ContentDescriptor descriptor, Object data) 
	{
		if (listening && null != listeners && HashUtil.equals(descriptor.getUrl(), this.getUrlToRequest()))
		{
			setContent(data, false);
			listening = false;
			
			for (int index = 0; index < this.listeners.size(); index++) {
				RefListener listener = (RefListener)this.listeners.get(index);
				listener.contentAvailable();
			}

			this.listeners.clear();
		}
	}
	
	public synchronized void onContentError(ContentDescriptor descriptor, Exception exception) 
	{
		//#debug error
		System.out.println("error loading content : " + exception);
		exception.printStackTrace();
		
		if (listening && null != listeners && HashUtil.equals(descriptor.getUrl(), this.getUrlToRequest()))
		{
			setContent(null, true);
			listening = false;
			
			this.listeners.clear();
		}
	}
	
	protected void setContent(Object data, boolean error) 
	{
		this.error = error;
	
		if (data instanceof Image)
		{
			image = (Image) data;
		}
		else
		if (data instanceof byte[])
		{
			byte[] bytes = (byte[]) data;
			try{
				image = Image.createImage(bytes, 0, bytes.length);
			}catch(IllegalArgumentException e){
				//#debug error
				System.out.println("Request EXception: " +e);
			}
		}
		else
		{
			image = null;
		}
		//#if polish.device.scale.avatar.icon
		if(null!=image && null!=defaultImage)
		image= ImageUtil.scale(image, defaultImage.getWidth(),defaultImage.getHeight());
		//#endif
	}
	
	/**
	 * Loads the referenced content.
	 */
	public synchronized void load()
	{
		//#debug debug
		System.out.println("Load");

		
		if (url == null)
		{
			// nothing to load
		}
		else
		if (listening || hasContent())
		{
			// already loading or loaded
		}
		else
		{
			request();
		}
	}
	
	public synchronized void cancel()
	{
		if (listening)
		{
			//#debug debug
			System.out.println("cancelling:"+this.getUrlToRequest());
			
			Settings.contentLoader.cancelContent(new ContentDescriptor(getUrlToRequest()));
			listening = false;
		}
	}
	
	/**
	 * Releases the referenced image.
	 */
	public synchronized void release()
	{
		cancel();
		setContent(null, false);
	}	
	
	private void request()
	{
		//#debug debug
		System.out.println("Request");
		
		listening = true;
		if (Settings.contentLoader != null)
		{
			//#debug debug
			System.out.println("Requesting:"+this.getUrlToRequest());
			
			//remove thumbnail request for s40, 
			//due to ioException: out of response entries on content loading.
			//#if !polish.removeThumbnails
			Settings.contentLoader.requestContent(new ContentDescriptor(getUrlToRequest()), this);
			//#endif
		}
	}
	
	public boolean hasContent()
	{
		return (image != null) || error;
	}
	
	public boolean equals(Object o)
	{
		ImageRef that = (ImageRef) o;
		return HashUtil.equals(this.url, that.url);
	}
	
	//#mdebug error
	public String toString()
	{
		return "ImageRef[url=" + url
		+ "]";
	}
	//#enddebug
}
