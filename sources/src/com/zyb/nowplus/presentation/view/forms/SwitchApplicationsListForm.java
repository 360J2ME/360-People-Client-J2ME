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
//#if polish.blackberry && add.switch.application.form
package com.zyb.nowplus.presentation.view.forms;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Keypad;

import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.ContextualMenuFormIconItem;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.List;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.util.Locale;

public class SwitchApplicationsListForm extends Form implements CommandListener
{
	//#if false
	public SwitchApplicationsListForm(Controller _controller)
	{
		super(null, (Style) null);
	}
	//#endif

	public SwitchApplicationsListForm(Controller _controller, Style style)
	{
		super(/*String title*/null, style);
		
		controller=_controller;
		
		createContentUI();
		
		addCommand(cmdBack);
		setCommandListener(this);
	}
	
	private void createContentUI()
	{
		
		//#style contextualmenu_infobar
		infoBar = new StringItem(null, "" );
		infoBar.setAppearanceMode(Item.PLAIN);
		
		//#style contextualmenu_navbar
		navBar =  new Container(/*focusFirstItem*/false );
		
		ApplicationManager appManager = ApplicationManager.getApplicationManager();
		
		appDescriptors = appManager.getVisibleApplications();
		
		for (int i = 0; i < appDescriptors.length; i++)
		{
			//FIXME add /**//#style switchappsfrom_item**/
			
			//#style contextualmenu_navbar_item_IM
			ContextualMenuFormIconItem appIcon = new ContextualMenuFormIconItem(null,null);
								
			appIcon.discrip=appDescriptors[i].getName();
			appIcon.appDescriptor=appDescriptors[i];
								
			Bitmap appIconBitmap=appIcon.appDescriptor.getIcon();
			
//			//FIXME add new method Image.createImage(Bitmap)
//			if(appIconBitmap!=null)
//			{
//				de.enough.polish.blackberry.ui.Image imageIconApp=de.enough.polish.blackberry.ui.Image.createImage(appIconBitmap);
//					//FIXME transparent issue
//				imageIconApp=ImageUtil.createThumbnail(imageIconApp, MAX_SIZE_ICON_WH, MAX_SIZE_ICON_WH);
//				appIcon.setImage(imageIconApp);
//			}
//			else
//			{
//				//#style contextualmenu_navbar_item_IM
//				appIcon.setImage(null);
//			}
								
			appIcon.setDefaultCommand(List.SELECT_COMMAND);
								
			navBar.add(appIcon);
		}
				
		if(navBar.size() <= 0)
			return;
		
			append(navBar);
			append(infoBar);
			//initial default focus on navbar
			focus(navBar);
			updateInfoItem();
					
	}
	
	
	private void updateInfoItem()
	{
		infoBar.setText(((ContextualMenuFormIconItem)navBar.getFocusedItem()).discrip);	
	}
	public void commandAction(Command cmd, Displayable disp)
	{
		if (disp != this) 
			return;

		if (cmd == List.SELECT_COMMAND)
		{

			ApplicationManager appManager = ApplicationManager.getApplicationManager();

			int processId = appManager.getProcessId(((ContextualMenuFormIconItem)navBar.getFocusedItem()).appDescriptor);

			//current people app
			if(processId == appManager.getForegroundProcessId())
				commandAction(cmdBack, this);
			else
				appManager.requestForeground(processId);
		}
		else if (cmd == cmdBack)
				controller.notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, this);
	}
	
	//overrides
	/**
	 * Handles key events.
	 * 
	 * WARNING: When this method should be overwritten, one need
	 * to ensure that super.keyPressed( int ) is called!
	 * 
	 * @param keyCode The code of the pressed key
	 * @see de.enough.polish.ui.Screen#keyPressed(int)
	 */
	public void keyPressed(int keyCode) 
	{
		if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE )
			commandAction(cmdBack, this); 
		else
			super.keyPressed(keyCode);
		
		//handle navbar state and actions
		if(getCurrentItem()==navBar)
			updateInfoItem();
		 
			
	}
	
	protected Container navBar;
	private StringItem infoBar;
	
	private ApplicationDescriptor[] appDescriptors;
	private Controller controller;
	
	private static final int MAX_SIZE_ICON_WH=30;
	private final static Command cmdBack = new Command(Locale.get("polish.command.back"), Command.BACK, 0);

}
//#endif
