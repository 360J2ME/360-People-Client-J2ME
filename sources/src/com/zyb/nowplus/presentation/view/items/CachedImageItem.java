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
package com.zyb.nowplus.presentation.view.items;

import com.zyb.nowplus.business.domain.ImageRef;
import com.zyb.nowplus.business.domain.RefListener;

import javax.microedition.lcdui.Graphics;
import de.enough.polish.ui.AnimationThread;
import de.enough.polish.ui.ImageItem;
import de.enough.polish.ui.Style;

public class CachedImageItem extends ImageItem implements RefListener
{
	private ImageRef imageref;
	protected boolean request = false;
	
	public CachedImageItem() {
		super(null,null,Graphics.TOP | Graphics.LEFT, null);
	}
	
	public CachedImageItem(Style style) {
		super(null,null,Graphics.TOP | Graphics.LEFT, null, style);
	}
	
	public CachedImageItem(final String label, final ImageRef imageref, final int layout, final String altText) 
	{
		this(label, imageref, layout, altText, null);
	}

	public CachedImageItem(final String label, final ImageRef imageref, final int layout, final String altText, final Style style )
	{
		super(label, imageref.getImage(), layout, altText, style);
		
		//#debug debug
		System.out.println("Setting ImageRef:"+imageref);
		
		setImageRef(imageref);
	}

	public void setImageRef(ImageRef imageref)
	{
		//#debug debug
		System.out.println("Setting ImageRef:"+imageref);

		if(this.imageref == imageref)
			return; //no change, return
		
		this.imageref = imageref;
		
		if(null != this.imageref)
		{
			if(!this.imageref.hasContent())
			{
				this.request = true;
				this.imageref.addListener(this);
				setImage(this.imageref.getImage()); //set default image, while loading
				AnimationThread.addAnimationItem(this);
			}
			else
			{
				this.request = false;
				AnimationThread.removeAnimationItem(this);
				this.imageref.clearListeners();
				setImage(this.imageref.getImage());
			}
		}
		else
		{
			this.request = false;
			AnimationThread.removeAnimationItem(this);
		}
	}
	

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#showNotify()
	 */
	protected void showNotify() {
		if(this.request)
		{
			AnimationThread.addAnimationItem(this);
		}
		super.showNotify();
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#hideNotify()
	 */
	protected void hideNotify() {
		if(this.request)
		{
			AnimationThread.removeAnimationItem(this);
		}
		super.hideNotify();
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#animate()
	 */
	public boolean animate() {
		if(this.request && this.imageref != null)
		{
			AnimationThread.removeAnimationItem(this);
			this.imageref.load();
			this.request = false;
		}
		
		return super.animate();
	}

	/**
	 * Returns the image reference
	 * @return the image reference
	 */
	public ImageRef getImageRef()
	{
		return this.imageref;
	}
	
	/* (non-Javadoc)
	 * @see com.zyb.nowplus.business.domain.ImageRefListener#imageAvailable()
	 */
	public void contentAvailable() 
	{
		//#debug debug
		System.out.println("contentAvailable()");
		
		if(null != this.imageref) {
			setImage(this.imageref.getImage());
		}
	}
	
	public void setRequest(boolean request) {
		this.request = request;
	}

}
