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

import com.zyb.nowplus.business.domain.ListSelection;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.Screen;
import de.enough.polish.ui.UiAccess;

/**
 * @author marcels
 * 
 */
public abstract class VirtualListProvider extends BaseListProvider {

	VirtualListView view;

	public VirtualListProvider(Container container, Screen screen,
			ItemCommandListener commandListener, int bufferSize) {
		super(container, screen, commandListener);

		this.view = new VirtualListView(this, bufferSize);

		initView();
	}

	/**
	 * Initializes the VirtualListView
	 */
	public void initView() {
		// set the view if its not set
		if (this.container.getView() != this.view) {
			//#debug
			System.out.println("setting view");

			this.container.setView(this.view);
		}
	}
	
	public void requestUpdate() {
		this.view.requestUpdate();
	}

	/**
	 * Updates the current selection
	 * 
	 * @param doFocus
	 *            true if a focus should be set otherwise false
	 */
	public void update(boolean doFocus)
	{
		update(null, doFocus);
	}

	/**
	 * Updates the list with a selection fitting to the given scope
	 * 
	 * @param scope
	 *            the scope
	 * @param doFocus
	 *            true if a focus should be set otherwise false
	 */
	public void update(Object scope, boolean doFocus)
	{
		try
		{
			synchronized (this.container)
			{
				VirtualListRange range = this.view.getRange();
				ListSelection selection;
				
				// when using pointer events ...
				scope = getScope(scope);
	
				// if no scope is given and focused item is null ...
				if (scope == null)
				{
					// select for given range
					selection = select(range);
				}
				else
				{
					// select for scope
					selection = select(scope, range.getRange());
					
					// set the new range
					range.setRange(selection.getStart(), selection.getEnd(),
							selection.getTotal());			
				}
	
				// apply selection
				apply(null, selection.getEntries(), selection.size(), scope, doFocus);
		         
				// init the container
				UiAccess.init(this.container,this.container.getAvailableWidth(),this.container.getAvailableWidth(),this.container.getAvailableHeight());
			}
		}
		catch(Exception e)
		{
			//#debug error
			System.out.println(this+"thrown exception:"+e.toString());
		}
	}
	
	/**
	 * Resets the lists
	 */
	public abstract void reset();

	/**
	 * Returns sample data to calculate the reference height
	 * 
	 * @return
	 */
	abstract Object getSampleData();

	/**
	 * Returns the total amount of entries
	 * 
	 * @return the total amount of entries
	 */
	protected abstract int total();

	/**
	 * Selects entries fitting to the given range
	 * 
	 * @param range
	 *            the range
	 * @return the resulting selection
	 */
	protected abstract ListSelection select(VirtualListRange range);

	/**
	 * Selects entries fitting to the given scope
	 * 
	 * @param scope
	 *            the scope
	 * @param range
	 *            the range
	 * @return the resulting selection
	 */
	protected abstract ListSelection select(Object scope, int number);

	/**
	 * Notifies the container item if they are active or not
	 * 
	 * @param item
	 *            the item
	 * @param active
	 *            true if the item is active otherwise false
	 */
	protected abstract void notify(Item item, boolean active);

	/**
	 * Applies a search
	 * 
	 * @param search
	 *            the search string
	 */
	public abstract void search(String search);
}
