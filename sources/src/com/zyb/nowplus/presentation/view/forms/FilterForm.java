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
//#condition polish.blackberry.isTouchBuild == true 

package com.zyb.nowplus.presentation.view.forms;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.filters.Filter;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.FilterContainer;
import com.zyb.nowplus.presentation.view.items.IconSwapItem;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.Style;
import javax.microedition.lcdui.Image;
import net.rim.device.api.ui.Keypad;

public class FilterForm extends BaseForm implements ItemCommandListener
{
	private FilterContainer filterContainer ;
	private Controller controller;
	
	//#style filter_button
	private IconSwapItem activFilter = new IconSwapItem("", null);
	
	public FilterForm( Model model, Controller controller) {
		this(model, controller, null);
	}
	
	public FilterForm(Model model, Controller controller, Style style){
		super(model, controller, null, style);
		this.controller = controller;
	}
	
	public IconSwapItem getButton(){

		return this.activFilter;
	}
	
	public void setButtonText(Image icon, String text){
		this.activFilter.setText(text);
		this.activFilter.setImage(icon);
	}
	
	public void setButtonText(String iconID, String text){
		this.activFilter.setText(text);
	}
	
	

	public void setFilter(FilterContainer filterCont){
			this.filterContainer = filterCont;
			Filter[] filters = this.filterContainer.getFilters();
			// Earlier implementation was returing a nullfilter icon and hence was small part of the bug PBLA-21
			IconSwapItem isi = (IconSwapItem)filterContainer.getIcon(filters[0]);
			if(isi != null)
				setButtonText(isi.getImage(),isi.getText());
			
			this.append(this.filterContainer);
	}
		
    public void commandAction(Command c, Item item) {
    	try
    	{
	        IconSwapItem isi = (IconSwapItem)item;
	        Filter[] filters = this.filterContainer.getFilters();
	        for(int i = 0; i < filters.length; i++){
	                if(isi.getText().toLowerCase().equals(filters[i].getName().toLowerCase())){
	                	
	                		filterContainer.setCurrentFilterIndex(i);
	                		
	                		isi.defocus(isi.getStyle());
	                		setButtonText(isi.getImage(), isi.getText());
	                		isi.focus(isi.getFocusedStyle(),0);
	                        filterContainer.filter();
	
	                        break;
	                	
	                }
	                        
	        }   
	        
	        this.activFilter.defocus(this.activFilter.getStyle());
		 	controller.notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK, null);	
    	}
    	catch(Exception e)
    	{
    		//#debug error
    		System.out.println("FilterForm.commandAction:" + e);
    	}
    }

	public void keyPressed(int keyCode) 
	{
		 if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
			{
			 	controller.notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK, null);	
			}
		 else
			 super.keyPressed(keyCode);
				
	}
	
	protected boolean handleKeyPressed(int keyCode, int gameAction) 
	{
		return true;
	}

	protected void createContent() {
		// TODO Auto-generated method stub
		
	}

	public byte getContext() {
		// TODO Auto-generated method stub
		return 0;
	}			
}
