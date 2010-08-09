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
import de.enough.polish.util.ArrayList;

public abstract class BaseListProvider {
	
	ArrayList buffer;
	Container container;
	Screen screen;
	Object lock;
	ItemCommandListener commandListener;

	public BaseListProvider(Container container, Screen screen, ItemCommandListener commandListener) {
		this.buffer = new ArrayList();
		this.container = container;
		this.screen = screen;
		this.lock = screen.getPaintLock();
		this.commandListener = commandListener;
	}
	
	public Container getContainer() {
		return this.container;
	}
	
	public Screen getScreen() {
		return this.screen;
	}

	abstract void updateItem(Item item, Object data);

	abstract Item createItem(Object data, ItemCommandListener commandListener);
	
	abstract Object getData(Item item);

	public synchronized void apply(Object collection, Object[] list, int count, Object scope, boolean doFocus) {
		//#debug debug
		System.out.println("apply with " + list.length + " entries");
		if (count > 0) {
			scope = getScope(scope);
			
			this.container.focusChild(-1);
			int scopeIndex = applyListToBuffer(this.buffer, list, scope);
			
			count = applyBufferToContainer(collection, this.buffer, list, count, scopeIndex, doFocus);
			trimContainer(this.container, count);
			trimBuffer(this.buffer, list.length);
		} else {
			clear();
		}
	}
	
	Object getScope(Object scope) {
		if(scope == null) {
			Item item = this.container.getFocusedItem();
			if(item != null) {
				return getData(item);
			}
		}
		
		return scope;
	}

	protected int applyListToBuffer(ArrayList buffer, Object[] list, Object scope) {
		int scopeIndex = -1; 
		for (int index = 0; index < list.length; index++) {
			Object data = list[index];

			Item item;
			if (index >= this.buffer.size()) {
				item = createItem(data, this.commandListener);
				buffer.add(item);
			} else {
				item = (Item) buffer.get(index);
				updateItem(item, data);
				
			}
			
			if(scope != null && data.equals(scope)) {
				scopeIndex = index;
			}
		}
		
		return scopeIndex;
	}

	protected int applyBufferToContainer(Object collection, ArrayList buffer, Object[] list, int count, int scopeIndex, boolean doFocus) {
		for (int index = 0; index < list.length; index++) {
			Item item = (Item) buffer.get(index);
			applyItemToContainer(index, item);
		}
		
		setFocusToScope(scopeIndex, doFocus);
		
		return count;
	}
	
	protected void setFocusToScope(int scopeIndex, boolean doFocus) {
		if(doFocus) {
			this.container.focusChild(scopeIndex);
		} else {
			this.container.setScrollYOffset(0);
		}
	}

	protected void applyItemToContainer(int index, Item item) {
		if (index >= container.size()) {
			this.container.add(item);
		} else {
			this.container.set(index, item);
		}
	}

	protected void trimBuffer(ArrayList buffer, int size) {
		while (buffer.size() > size) {
			Item item = (Item)buffer.remove(buffer.size() - 1);
			item.destroy();
		}
	}
	
	protected void clearBuffer(ArrayList buffer) {
		for (int i = 0; i < buffer.size(); i++) {
			Item item = (Item)buffer.get(i);
			item.destroy();
		}
		
		buffer.clear();
	}
	
	public void trimContainer(Container container, int size) {
		while (this.container.size() > size) {
			this.container.remove(this.container.size() - 1);
		}
	}

	public void clear() {
		if(this.container.size() > 0) {
			this.container.clear();
		}
		
		clearBuffer(this.buffer);
	}
}
