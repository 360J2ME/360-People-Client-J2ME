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
package com.zyb.nowplus.presentation.controller;

/**
 * @author Jens Vesti
 *
 */

import javax.microedition.lcdui.Displayable;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.ExtendedScreenHistory.DisplayableContainer;
import com.zyb.nowplus.presentation.view.forms.BasePeopleForm;
import com.zyb.nowplus.presentation.view.forms.BaseTabForm;
import com.zyb.nowplus.presentation.view.forms.NotificationForm;
import com.zyb.util.event.Event;

//#if polish.device.requires.polish.browser
import com.zyb.nowplus.presentation.view.browser.xPolishBrowser;
import de.enough.polish.ui.Form;
//#endif

/**
 * The application controller
 * @author Jens Vesti
 *
 */
public class NavigationController extends ContextController
{
	public NavigationController(Model model, Controller controller, ExtendedScreenHistory history) 
	{
		super(model, controller, history);
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#getContext()
	 */
	public byte getContext() {
		return Event.Context.NAVIGATION;
	}
	
	//#if polish.device.requires.polish.browser
	private void cleanBrowser(Object data){
		Form fData = (Form)data;
		//#debug debug
		System.out.println("PRE BROWSER SIZE: "+(new Integer(fData.size())).toString());
		if (fData.size()==1){
			//#debug debug
			System.out.println("Trying to get item");
			Object i =null;
			i= fData.get(0);
			if (i!=null)
				if (i instanceof xPolishBrowser){
					//#debug debug
					System.out.println("ITEM CLASS: " +i.getClass().getName());
					((xPolishBrowser) i).cancel();
					((xPolishBrowser) i).requestStop();
					((xPolishBrowser) i).releaseResources();
					((xPolishBrowser) i).clear();

					fData.deleteAll();
					//#debug debug
					System.out.println("Finished cleaning");
				}
		}
		
	}
	//#endif

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) 
	{
		//#debug debug
		System.out.println("context:"+context+"/event:"+event);
		
		//#debug screenstacktrace
		history.printDisplayHistory();
		
		//#debug screenstacktrace
		history.printAlertHistory();
	
		if(context == getContext())
		{
			switch (event) {
			case Event.Navigation.BACK:
				history.back();
				break;
			case Event.Navigation.BACK_CHECKPOINT:
				history.back(true);
				break;
			case Event.Navigation.NEXT:
				if(null != data && data instanceof Displayable){

					// check if it is a notification form
					if(data instanceof NotificationForm)
					{
						NotificationForm form =((NotificationForm)data);
						if(form.isDismissed())
						{
							//#debug debug
							System.out.println(" NotificationForm form alredy dismissed not displaying");
							form.destroy();
							break;
						}
					}
					history.next((Displayable)data, null, false);
				}	
				break;
			case Event.Navigation.NEXT_GLOBAL:
				if(null != data && data instanceof Displayable)
					history.displayGlobal((Displayable)data, null);
				break;
			case Event.Navigation.NEXT_CHECKPOINT:
				if(null != data && data instanceof Displayable)
				{
					//#debug debug
					System.out.println("Showing next screen:"+data);
					history.next((Displayable)data, null, true);
				}
				break;
			case Event.Navigation.DISMISS:

				//#if polish.device.requires.polish.browser
				if (data instanceof Form){
					cleanBrowser(data);
				}
				//#endif
				
				if(data instanceof de.enough.polish.ui.Displayable)
				{
					if(null != data)
					{
						// check if it is a notification form
						if(data instanceof NotificationForm)
						{
							((NotificationForm)data).setDismissed(true);
							//#debug debug
							System.out.println("NotificationForm set dismissed");
						} 
						
						//dismiss parameter
						history.dismiss((de.enough.polish.ui.Displayable)data);
					}
					else
					{
						//dismiss current
						DisplayableContainer dc = history.currentGlobalAndLocalDisplayableContainer();
						history.dismiss((de.enough.polish.ui.Displayable)dc.disp);
					}
				}
				break;	
			case Event.Navigation.BACK_TO_LATEST_PEOPLESPAGE:
				Object disp;
				while( null != history.currentLocalDisplayableContainer() && 
						null != (disp = history.currentLocalDisplayableContainer().disp) && 
						!(disp instanceof BasePeopleForm)
						)
					history.backSilent();
				history.displayCurrent(true);
				break;
			case Event.Navigation.SWITCH:
				if(null != data && data instanceof BaseTabForm)
				{
					BaseTabForm tabForm = (BaseTabForm)data;
					history.replaceCurrentDisplayable(tabForm);
				}
				break;
			case Event.Navigation.SET_CURRENT:
				if(null != data && data instanceof Displayable)
					history.replaceCurrentDisplayable((Displayable)data);
				break;	
			case Event.Navigation.HIDE:
				history.hide();
				break;				
			default:
				break;
			}
		}
	}
	
	//#mdebug error
	public String toString()
	{
		return "NavigationController[]";
	}
    //#enddebug

}
