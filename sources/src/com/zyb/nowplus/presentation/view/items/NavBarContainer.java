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

import com.zyb.nowplus.presentation.view.forms.BasePeopleForm;

import de.enough.polish.ui.AnimationThread;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;

/**
 * Wrapper class necessary to make navbar animatable
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class NavBarContainer extends Container
{
	
	/**
	 * saving the index of the next focus tab
	 */
	public static int nextFocus = 0;
	
	/**
	 * 
	 * @param focusFirstElement
	 */
	public NavBarContainer(boolean focusFirstElement) 
	{		
		this(focusFirstElement, null);
	}

	/**
	 * 
	 * @param focusFirstElement
	 * @param style
	 */
	public NavBarContainer(boolean focusFirstElement, Style style)
	{	
		super(focusFirstElement, style);
		
		//#debug debug
		System.out.println("NavbarStyle " + style);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#showNotify()
	 */
	public void showNotify() 
	{
		//#if polish.blackberry.isTouchBuild == false
			AnimationThread.addAnimationItem(this);
			super.showNotify();
		//#endif
	}
	
//	/*
//	 * NOTE: Do not use! Results in unexpected behavior in NowPlus app as 
//	 * navbar is shared amongst several screens (tabs). 
//	 * (non-Javadoc)
//	 * @see de.enough.polish.ui.ItemView#hideNotify()
//	 */
//	public void hideNotify() 
//	{
//		AnimationThread.removeAnimationItem(this);
//		super.hideNotify();
//	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ItemView#releaseResources()
	 */
	public void releaseResources() 
	{
		//#if polish.blackberry.isTouchBuild == false
			AnimationThread.removeAnimationItem(this);
			super.releaseResources();
		//#endif
	}
	
	

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Container#focusChild(int, de.enough.polish.ui.Item, int)
	 */
	public void focusChild( int index, Item item, int direction, boolean force ) {
		// hack to correctly focus the first item
		//#if polish.blackberry && polish.blackberry.isTouchBuild == false
			if(		this.getStyle() == BasePeopleForm.navBarActiveUnfocusedStyle || 
					this.getStyle() == BasePeopleForm.navBarFocusedStyle ||
					this.getStyle() == BasePeopleForm.navBarUnfocusedStyle ){ 
				this.focusedIndex = nextFocus;
	         	Item[] items = this.getItems();
	         	super.focusChild( this.focusedIndex,  items[this.focusedIndex], direction, force);
			}else{
				this.focusedIndex = -1;
				super.focusChild(index, item, direction, force);
				
			}
		//#else
			this.focusedIndex = -1;
			super.focusChild(index, item, direction, force);
		//#endif
			
		
		
		//#if polish.blackberry.isTouchBuild == true
		//set the next focus index value for BaseTabForm.handlePointerPressed
		nextFocus = getFocusedIndex();
		//#endif
	}

	//#if polish.blackberry.isTouchBuild == true
	public void defocus(Style originalStyle) {
		//do nothing
	}
	//#endif
	
	
	
}
