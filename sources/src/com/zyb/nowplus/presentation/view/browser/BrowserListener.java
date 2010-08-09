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
//#condition polish.device.requires.polish.browser==true
package com.zyb.nowplus.presentation.view.browser;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.forms.NotificationForm;
import com.zyb.nowplus.presentation.view.items.ProgressIndicatorItem;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Form;
import de.enough.polish.util.Locale;

/**
 * Implementation of de.enough.polish.browser.BrowserListener interface.
 * Is meant to display and dismiss NotificationForm during page load.
 * 
 * @author jakub.kalisiak@mobica.com
 *
 */
public class BrowserListener implements
		de.enough.polish.browser.BrowserListener,CommandListener {

	Form browserForm;
	Controller controller;
	NotificationForm nf;
	private Command cmdQuitBrowserLoading;
	private Model model;
	
	public BrowserListener(Model model, Controller controller, Form browserForm) {
		this.model=model;
		this.controller=controller;
		this.browserForm=browserForm;
	}
	
	public void notifyDownloadEnd() {
		//#debug debug
		System.out.println("download end");

	}

	public void notifyDownloadStart(String arg0) {
		//#debug debug
		System.out.println("download start");

	}

	public void notifyPageEnd() {
		//#debug debug
		System.out.println("page end");
		
		//dismiss notification
		controller.notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
		cleanUp();

	}

	public void notifyPageError(String arg0, Exception arg1) {
		//#debug debug
		System.out.println("Page error");

	}

	public void notifyPageStart(String arg0) {
		//#debug debug
		System.out.println("page start");
		cmdQuitBrowserLoading= new Command(Locale.get("polish.command.cancel"), Command.CANCEL, 9);
		
		if (nf!=null)
			controller.notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
		
		//#style .notification_form_progress
		nf = new NotificationForm(model, controller, 
				Locale.get("nowplus.client.java.browser.notification.title"),				
				null, 
				cmdQuitBrowserLoading, 
				0, 
				true,  
				ProgressIndicatorItem.PROGRESS_INFINITE);
		
		nf.setCommandListener(this);
		
		controller.notifyEvent(Event.Context.NAVIGATION, 
				Event.Navigation.NEXT_GLOBAL, nf);
	}

	public void commandAction(Command c, Displayable d) {
		if(cmdQuitBrowserLoading==c){
			//#debug debug
			System.out.println("QUIT BROWSER LOADING");
			//dismiss notificationform
			controller.notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, nf);
			//dismiss browser
			controller.notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, browserForm);
			cleanUp();
		}
		
	}

	public void cleanUp(){
		browserForm=null;
		nf=null;
		cmdQuitBrowserLoading=null;
		controller=null;
		model=null;
	}
	
}
