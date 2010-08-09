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
package com.zyb.nowplus.presentation.view.providers;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.Screen;
import de.enough.polish.ui.StringItem;
import de.enough.polish.util.ArrayList;

public abstract class ExtendedListProvider extends BaseListProvider {

	public ExtendedListProvider(Container container, Screen screen, ItemCommandListener commandListener) {
		super(container, screen, commandListener);
	}
	
	protected abstract Item getHeader(Object collection);
	
	protected abstract Item getTail(Object collection);
	
	protected abstract String getSection(Object data);
	
	protected int applyBufferToContainer(Object collection, ArrayList buffer, Object[] list, int count, int scopeIndex, boolean doFocus) {
		int additional = 0;
		
		Item header = getHeader(collection);
		if(header != null) {
			applyItemToContainer(0, header);
			additional++;
		}
		
		String currentSection = null;
		
		int index;
		for (index = 0; index < list.length; index++) {
			Item item = (Item) buffer.get(index);

			Object data = list[index];
			
			String section = getSection(data);
			if(null != section && !section.equals(currentSection))
			{
				currentSection = section;
				
				//#style activitylist_section
				Item sectionItem = new StringItem(null,section);
				
				applyItemToContainer(index + additional, sectionItem);
				additional++;
			}
		
			applyItemToContainer(index + additional, item);
			
			if(index == scopeIndex) {
				setFocusToScope(index + additional, doFocus);
			}
		}
		
		Item tail = getTail(collection);
		if(tail != null) {
			applyItemToContainer(index + additional, tail);
			additional++;
		}
		
		return count + additional;
	}

}
