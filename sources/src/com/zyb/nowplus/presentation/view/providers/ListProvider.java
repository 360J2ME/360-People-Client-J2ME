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

import java.util.Vector;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.ListSelection;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.forms.BasePeopleForm;
import com.zyb.nowplus.presentation.view.items.FilterContainer;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.ContainerView;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.Screen;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.ArrayList;

/**
 * Used for the abstraction of the creation and management of a list and the
 * virtualization of large lists by using a de.enough.polish.ui.ContainerView. A
 * detailed reference can be found <a
 * href="http://wiki.zyb.local/index.php?title=ListProvider">here</a>.
 * 
 * @author Andre Schmidt
 * 
 */
public abstract class ListProvider extends ContainerView implements ItemCommandListener{

	public class ListRequest
	{
		boolean focusNeeded;
		Object scope;
		long timestamp;
		
		public ListRequest(Object scope, boolean focusNeeded, long timestamp)
		{
			this.scope = scope;
			this.focusNeeded = focusNeeded;
			this.timestamp = timestamp;
		}
		
		public Object getScope()
		{
			return this.scope;
		}
		
		public boolean needsFocus()
		{
			return this.focusNeeded;
		}
		
		public void setScope(Object scope)
		{
			this.scope = scope;
		}
		
		public void setFocusNeeded(boolean focusNeeded)
		{
			this.focusNeeded = focusNeeded;
		}

		public long getTimestamp() {
			return timestamp;
		}
		
		public String toString()
		{
			return 	"ListRequest[" +
					"scope : " + this.scope +
					"focusNeeded : " + this.focusNeeded + 
					"timestamp : " + this.timestamp + 
					"]";
		}
	}
	
	/**
	 * the default interval to be used in a derived class
	 */
	public final static int DEFAULT_INTERVAL = 500;
	
	static final int SCROLLING_INTERVAL = 500;
	
	static final int UPDATE_INTERVAL = 500;

	/**
	 * the key to store data in the attributes of an item
	 */
	final static String KEY = "DATA";

	/**
	 * the model
	 */
	final transient Model model;
	
	/**
	 * the controller
	 */
	final transient Controller controller;
	
	/**
	 * the container to manage
	 */
	final transient Container container;
	
	/**
	 * the container to manage
	 */
	final transient BasePeopleForm form;
	
	/**
	 * the screen of the container
	 */
	final transient Screen screen;

	/**
	 * the item buffer
	 */
	ArrayList buffer;

	/**
	 * the range of entries to retrieve
	 */
	final int range;

	/**
	 * the step to request new data
	 */
	final int step;

	/**
	 * interval to collect events
	 */
	final int interval;

	/**
	 * the current selection
	 */
	transient ListSelection selection;

	/**
	 * the current request
	 */
	transient ListRequest request = null;

	/**
	 * the lock to synchronize access to selection
	 */
	final static Object lock = new Object();

	boolean isShutdownRequested = false;

	/**
	 * the reference height of a single item used in this list
	 */
	int referenceHeight = -1;
		
	/**
	 * the last scroll offset
	 */
	
	int lastContainerSize = -1;
	
		long lastScrollOffset = -1;
	
	long lastScrollTime = -1;
	
	long lastNoticationOffset = -1;
	
	long lastUpdateTime = -1;
	
	boolean updateRequested = false;
	
	/**
	 * the last scroll offset
	 */
	boolean listRefreshed = false;
	
	Vector activeItems;
	
	/**
	 * Creates a new ListProvider instance
	 * 
	 * @param container
	 *            the target container
	 * @param range
	 *            the range to retrieve data entries
	 * @param step
	 *            the step to request new data
	 */
	public ListProvider(final Model model, 
						final Controller controller, 
						final Container container, 
						final BasePeopleForm form,
						final int range,
						final int step, 
						final int interval) {
		this.model = model;
		this.controller = controller;
		this.container = container;
		this.form = form;
		this.screen = container.getScreen();
		this.buffer = new ArrayList();
		this.range = range;
		this.step = step;
		this.interval = interval;
		this.activeItems = new Vector();
		
		initView();
	}

	/**
	 * Selects data objects
	 * 
	 * @param data
	 *            the reference object to retrieve entries for
	 * @param range
	 *            the range for selecting entries
	 * @return the resulting array of entries
	 */
	protected abstract ListSelection select(Object data, int range);

	/**
	 * Creates an item for the specified data
	 * 
	 * @param data
	 *            the data
	 * @return the resulting item
	 */
	protected abstract Item createItem(Object data);

	/**
	 * Sets an item to the specified data
	 * 
	 * @param data
	 *            the data
	 * @param item
	 *            the item to set
	 */
	protected abstract void updateItem(Item item, Object data);

	/**
	 * Returns a sample data object. Used to determine the reference height and
	 * in prefetch()
	 * 
	 * @return a sample data object
	 */
	protected abstract Object getSampleData();

	/**
	 * Applies the settings of a FilterContainer
	 * 
	 * @param filter
	 *            the FilterContainer to get the settings from
	 */
	public abstract void filter(FilterContainer filter);

	/**
	 * Applies a search
	 * 
	 * @param search
	 *            the search string
	 */
	public abstract void search(String search);
	
	/**
	 * Process a command with the data of the firing object
	 * @param cmd 
	 * @param data
	 */
	public abstract void processItemCommand(Command cmd, Object data);
	
	public abstract void notify(Item item, boolean active);
	
	protected Model getModel()
	{
		return this.model;
	}
	
	protected Controller getController()
	{
		return this.controller;
	}

	/**
	 * Prefetches the items for the container by creating a number specified by
	 * range of items and adds them to the container. Should be used when the
	 * items for this list are time consuming in their creation.
	 */
	public void prefetch() {
		// get the sample data
		Object sample = getSampleData();
		
		if(sample != null)
		{
			//#debug error
			System.out.println("sample data is null");
			return;
		}

		for (int i = 0; i < this.range; i++) {
			// create an item from the sample data
			// and add it to the container
			this.container.add(createItem(sample));
		}
	}

	/**
	 * Initializes the view for the container
	 */
	public void initView() {
		// set the view if its not set
		if (this.container.getView() != this) {
			//#debug
			System.out.println("setting view");

			this.container.setView(this);
		}
	}
	
	/**
	 * Sets isSelectRequested to true so when initContent() is called the data
	 * needed can be selected and put in to the container
	 */
	public void update(boolean focus) {
		update(null,focus);
	}
	
	public void reset()
	{
		update(getModel().getFirstContact(),false);
		
		this.lastNoticationOffset = -1;
    	
    	this.lastScrollOffset = -1;
    	
    	this.lastScrollTime = -1;
    	
    	this.updateRequested = false;
    	
    	this.lastUpdateTime = -1;
	}

	/**
	 * Sets isSelectRequested to true so when initContent() is called the data
	 * needed can be selected and put in to the container
	 */
	public void update(Object scope, boolean autofocus)
	{
		//#debug debug
		System.out.println("requesting select for " + scope);
		
		if (!this.isShutdownRequested) {
			synchronized (lock) {
				if(this.request == null) {
					if(this.parentItem != null) {
						this.request = new ListRequest(scope,autofocus,System.currentTimeMillis());
					}
					else {
						//#debug debug
						System.out.println("no parent item set");
					}
				}
				else {
					this.request.scope = scope;
					this.request.focusNeeded = autofocus;
					this.request.timestamp = System.currentTimeMillis();
				}
				
				handleRequest();
			}
		}
		else {
			//#debug error
			System.out.println("ListProvider is shutdown");
		}
	}
	
	public boolean isDataFocused(Object data)
	{
		Object itemData = getDataForItem(this.container.getFocusedItem());
		return (itemData != null) && (data != null) && itemData.equals(data);
	}
	
	/**
	 * handles a request by selecting data for the current selection and setting
	 * flags
	 */
	private void handleRequest()
	{
		// synchronized (lock) { already got lock
			if (selectForItem(this.container.getFocusedItem())) {
				//#debug debug
				System.out.println("requesting init");

				requestInit();
			}
		// }
	}
	
	public boolean isScrolling(int scrollOffset) {
        return ( this.lastScrollOffset == -1 || this.lastScrollTime == -1 || this.lastScrollOffset != scrollOffset);
	}
	
	public void requestUpdate() {
    	this.updateRequested = true;
    }
	
	public boolean animate() {
		boolean animated = super.animate();
		long currentTime = System.currentTimeMillis();
		
		 if(null != this.container)
         {
				 // if the last update is more than 500 ms ago ...
		         if( this.updateRequested && currentTime - this.lastUpdateTime > UPDATE_INTERVAL) {
		        	 // update the list
			   		 update(form.isActive(this.container));
			   		 UiAccess.init(this.container, this.container.getAvailableWidth(), this.container.getAvailableWidth(), this.container.getAvailableWidth());
                     
			   		 this.lastUpdateTime = currentTime;
			   		 this.updateRequested = false;
		         }
             
                 // get the scroll offsets
                 int scrollOffset = Math.abs(this.container.getScrollYOffset());
                 
                 if( isScrolling(scrollOffset) ) {
                	 this.lastScrollTime = currentTime; 
                     
                     // notify the inactive items
                     notifyInactiveItems();
                 } else {
                	 if(currentTime - this.lastScrollTime > SCROLLING_INTERVAL && this.lastNoticationOffset != scrollOffset) {
           		      // notify the active items
                         notifyActiveItems();
                         this.lastNoticationOffset = scrollOffset;
                	 }
                 }
                                  
                 this.lastScrollOffset = scrollOffset;
         }
		 
		 return animated;
	}
	
	/**
	 * Notifies active items
	 */
	public void notifyActiveItems()
	{
		synchronized (this.container)
		{
			for (int i = 0; i < this.container.size(); i++) {
				Item item = this.container.get(i);
				if(isItemShown(item))
				{
					notify(item, true);
					this.activeItems.addElement(item);
				}
			}
		}
	}
	
	/**
	 * Notifies inactive items
	 */
	public void notifyInactiveItems()
	{
		for (int i = 0; i < this.activeItems.size(); i++) {
			Item item = (Item)this.activeItems.elementAt(i);
			if(!isItemShown(item)) {
				notify(item, false);
				this.activeItems.removeElementAt(i);
				i--;
			}
		}
	}
	
	/**
	 * Returns true if an item is in the visible area of the managed container
	 * @param item the item
	 * @return true if an item is in the visible area of the managed container otherwise false
	 */
	public boolean isItemShown(Item item)
	{
		int verticalMin = this.container.getAbsoluteY() + Math.abs(this.container.getScrollYOffset());
		int verticalMax = verticalMin + this.container.getScrollHeight();
		
		int itemTop= item.getAbsoluteY();
		int itemBottom = item.getAbsoluteY() + item.itemHeight;
		
		return !(itemBottom < verticalMin || itemTop > verticalMax);
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ItemView#hideNotify()
	 */
	public void hideNotify() {
		super.hideNotify();
		
		// cancel active items on hide
		notifyInactiveItems();
	}

	/**
	 * Sets the items of the list by calling select() and creating/ setting an
	 * item for every entry
	 * 
	 * @param object
	 *            the data object to retrieve an item from
	 * @return true, if the selection is not empty, otherwise false
	 */
	boolean selectForItem(Item requestItem) {
		try {
			Object scope = this.request.getScope(); 
			
			if(scope == null)
			{
				scope = getDataForItem(this.container.getFocusedItem());
				this.request.setScope(scope);
			}
			
			this.selection = select(scope, this.range);

			//#debug debug
			System.out.println("new selection : " + this.selection);

			//selection might be null
			Object[] entries = null;
			if(null != this.selection)
				entries = selection.getEntries();

			//#debug debug
			System.out.println("entries : " + entries);

			if (entries == null || (entries != null && entries.length == 0))
			{
				//#debug info
				System.out.println("selection is empty");
			}
			else
			{
				//#debug debug
				System.out.println("selected " + entries.length
						+ " data entries for " + scope);
				
				setItems(entries);
			}

			//#debug debug
			System.out.println("parent item = " + this.parentItem);
			
			return true;
		}
		catch (Exception e)
		{
			//#debug error
			System.out.println("error in requestForItem() " + e);

			return false;
		}
	}

	/**
	 * Set the focus of the container by using the previously focused data
	 * 
	 * @param focusedData
	 *            the previously focused data
	 */
	void setFocus(Object focusedData) {
		//#debug debug
		System.out.println("setting items");

		int indexToFocus = -1;
		if(this.request.needsFocus())
		{
			indexToFocus = 0;
			
			if (focusedData != null) {
				Item item = getItemForData(focusedData);
				indexToFocus = this.container.indexOf(item);
			}
		}
		
		if(this.container.size() > 0)
		{
			//#debug debug
			System.out.println("focussing item " + indexToFocus);
			
			this.container.focusChild(indexToFocus);
		}
	}

	/**
	 * Fills the buffer with the items resulting from the specified entries
	 * 
	 * @param entries
	 *            the entries
	 */
	void setItems(Object[] entries) 
	{
		int offset = this.container.getScrollYOffset();
		int focIndex = this.container.getFocusedIndex();
		this.container.focusChild(-1);
		
		Item item = null;
		
		for (int index = 0; index < entries.length; index++) {
			Object data = entries[index];
			
			//#debug debug
			System.out.println("handling " + data);
			
			// if there is no item for this entry index ...
			if (index >= (this.buffer.size())) {
				try {
					// create a new item
					item = createItem(data);
					UiAccess.showNotify(item);
					item.setItemCommandListener(this);
				} catch (Exception e) {
					//#debug error
					System.out.println("error in createItem() : " + e);
					return;
				}
				
				this.buffer.add(item);
				
				//#debug debug
				System.out.println("added item " + item + " for " + data);
			} else {
				item = (Item) this.buffer.get(index);
				/*if(item.isFocused) {
					UiAccess.defocus(item, item.getNormalStyle());
				}*/
				UiAccess.hideNotify(item);
					
				try {
					// set the item
					updateItem(item, data);
					UiAccess.showNotify(item);
				} catch (Exception e) {
					//#debug error
					System.out.println("error in setItem() : " + e);
					return;
				}
				
				//#debug debug
				System.out.println("reused item " + item + " for " + data);
			}
			
			// attach the data to its item
			item.setAttribute(KEY, data);
		}
		//#if polish.blackberry.isTouchBuild == false
			this.container.focusChild(focIndex);
		//#endif
		this.container.setScrollYOffset(offset, false);
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ItemCommandListener#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Item)
	 */
	public void commandAction(Command cmd, Item item) {
		Object data = getDataForItem(item);
		
		//#debug debug
		System.out.println("handle command " + cmd + " for data " + data);
		
		processItemCommand(cmd, data);
	}

	/**
	 * Adds or removes items from the container depending on the count of the
	 * current selection
	 * 
	 * @param selection
	 *            the current selection
	 */
	public void setContainer(ListSelection selection) {
		
		if(null != selection)
		{
			Object[] entries = selection.getEntries();
			
			synchronized (this.container)
			{
				// adopt container size to entries count
				if (this.container.size() < entries.length) 
				{
					for (int i = container.size(); i < entries.length; i++) {
						this.container.add((Item) this.buffer.get(i));
						
						//#debug debug
						System.out.println("added item to container");
					}
				}
				else
				{
					while (this.container.size() > entries.length) {
						this.container.remove(this.container.size() - 1);
						
						//#debug debug
						System.out.println("removed item from container");
					}
				}
			}
		}
	}

	/**
	 * Returns the item that is associated with the specified data
	 * 
	 * @param data
	 *            the specified data
	 * @return the resulting item
	 */
	public Item getItemForData(Object data) 
	{
		synchronized (this.container)
		{
			for (int index = 0; index < this.container.size(); index++) {
				Item item = this.container.get(index);
				Object itemData = item.getAttribute(KEY);
				if (itemData.equals(data)) {
					return item;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the associated data of an item
	 * 
	 * @param item
	 *            the item
	 * @return the associated data
	 */
	public Object getDataForItem(Item item) {
		if (item != null) {
			return item.getAttribute(KEY);
		} else {
			return null;
		}
	}

	/**
	 * Returns the container
	 * 
	 * @return the container
	 */
	public Container getContainer() {
		return this.container;
	}

	/**
	 * Returns the range of items to retrieve
	 * 
	 * @return the range the range
	 */
	public int getRange() {
		return range;
	}

	/**
	 * Returns the height of the first item
	 * 
	 * @return the height
	 */
	int getReferenceHeight(int width) {
		if (referenceHeight == -1) {
			Item sampleItem = createItem(getSampleData());
			this.referenceHeight = sampleItem.getItemHeight(width,width);
			//#debug debug
			System.out.println("reference height : " + this.referenceHeight);
		}

		return this.referenceHeight;
	}

	/**
	 * Returns true, if a request is needed to be done
	 * 
	 * @return true, if a request is needed to be done, otherwise false
	 */
	boolean isSelectNeeded() {
		int realIndex = this.container.getFocusedIndex()
		+ this.selection.getStart();
		
		//#if polish.hasPointerEvents == true
		//#=	return (realIndex > (this.selection.getEnd() - this.step) || realIndex < (this.selection.getStart() + this.step))
		//#=		&& (realIndex - this.step > 0 && realIndex + this.step < this.selection.getTotal());
		//#else
			return (realIndex % this.step == 0) && this.container.getFocusedIndex() != 0;
		//#endif
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#focusItem(int, de.enough.polish.ui.Item, int, de.enough.polish.ui.Style)
	 */
	public Style focusItem(int index, Item item, int direction,
			Style focusedStyle) {
		
		Style style = super.focusItem(index, item, direction, focusedStyle);
		
		//#if polish.hasPointerEvents == true
		synchronized (lock) {
			if (isSelectNeeded()) {
				this.request = new ListRequest(null,true,System.currentTimeMillis());
				
				//#debug debug
				System.out.println("focus needs new selection");
				handleRequest();
			}
		}
		//#endif
		
		return style;
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#shiftFocus(boolean, int, de.enough.polish.ui.Item[])
	 */
	protected Item shiftFocus(boolean forwardFocus, int steps, Item[] items, boolean allowCycle) {
		Item item = super.shiftFocus(forwardFocus, steps, items, false);
		
		//#if !polish.hasPointerEvents || polish.hasPointerEvents == false
		synchronized (lock) {
			if (isSelectNeeded()) {
				this.request = new ListRequest(null,true,System.currentTimeMillis());
				
				//#debug debug
				System.out.println("focus needs new selection");
				handleRequest();
			}
		}
		//#endif
		
		return item;
	}



	int initContentCount = 0;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.enough.polish.ui.ContainerView#initContent(de.enough.polish.ui.Item,
	 * int, int, int)
	 */
	protected void initContent(Item parentContainerItem, int firstLineWidth,
			int lineWidth, int availableWidth) {
		//#debug debug
		System.out.println("initcontent called");
		
		synchronized (lock) {
			if (this.request != null) {
				int offset = getListOffset();
				int referenceHeight = getReferenceHeight(firstLineWidth);
				
				setContainer(this.selection);
				setFocus(this.request.getScope());
							
				super.initContent(parentContainerItem, firstLineWidth, lineWidth,
						availableWidth);
				initListContent(referenceHeight);
				
				setScrollOffset(this.parentContainer, offset, referenceHeight);
				
				this.listRefreshed = true;
				
				this.request = null;
			}
			else
			{
				super.initContent(parentContainerItem, firstLineWidth, lineWidth,
						availableWidth);
				initListContent(referenceHeight);
			}
		}
	}
	
	public int getListOffset()
	{
		Item item = container.getFocusedItem();
		if(item != null)
		{
			return item.relativeY - (container.getScrollYOffset() * -1);
		}
		else
		{
			return -1;
		}
	}
	
	/**
	 * Adjusts the scroll offset
	 */
	protected void setScrollOffset(Container container, int focusOffset, int referenceHeight)
	{
		Item item = container.getFocusedItem();
		int index = container.getFocusedIndex();
		
		int visibleHeight = this.container.getScrollHeight();
		int listHeight = this.selection.getTotal() * referenceHeight;
		
		int yOffset = 0;
		
		if(listHeight > visibleHeight && this.container.getFocusedIndex() != 0)
		{
			if(index == this.container.size() - 1)
			{
				yOffset = (listHeight - visibleHeight) * -1;
			}
			else if(item != null && item.relativeY > focusOffset)
			{
				yOffset = -item.relativeY + focusOffset;
				
				if(item.relativeY > 0 && focusOffset == -1)
				{
					yOffset = -item.relativeY + referenceHeight;
				}
			}
		}
		
		this.parentContainer.setScrollYOffset(yOffset,false);
	}

	/**
	 * Initializes the container settings to simulate a list consisting of the
	 * total count of entries
	 * 
	 * @param selection
	 *            the selection to initialize the container with
	 */
	void initListContent(int referenceHeight) {
		//#debug debug
		System.out.println("initialising list content");

		if(null != this.parentContainer)
		{
			synchronized (this.parentContainer)
			{
				if (this.parentContainer.size() > 0 && this.selection != null) {
					//#debug debug
					System.out.println("setting container for " + this.selection);
					
					int height = referenceHeight;
					int start = this.selection.getStart();
					int total = this.selection.getTotal();
					
					int itemOffset = height * start;
					
					for (int i = 0; i < this.parentContainer.size(); i++) {
						
						Item item = this.parentContainer.get(i);
						item.relativeY = itemOffset;
						
						itemOffset += height;
					}
					
					this.contentHeight = total * height;
					
					//#debug debug
					System.out.println("resulting content height : " + this.contentHeight);
				}
			}
		}
	}
}
