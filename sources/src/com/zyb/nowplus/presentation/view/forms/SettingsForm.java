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
package com.zyb.nowplus.presentation.view.forms;

import com.zyb.nowplus.business.Model;

//#if polish.blackberry
import net.rim.device.api.ui.Keypad;
import com.zyb.nowplus.presentation.BlackBerryOptionsHandler;
//#endif

import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;
import com.zyb.util.event.Event;
import com.zyb.util.event.Event.ContextualMenu;

import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.util.Locale;

/**
 * @author Jens Vesti
 *
 */
public class SettingsForm extends BaseFramedForm
//#if polish.blackberry
implements BlackBerryOptionsHandler
//#endif
{
	/**
	 * Commands
	 */
	private static final Command cmdSave = new Command(Locale.get("nowplus.client.java.settings.command.save"),Command.SCREEN,0);
	
	private static final Command cmdResetCounter = new Command(Locale.get("polish.command.select"),Command.ITEM,0);
	private static final Command cmdClearRMS = new Command(Locale.get("polish.command.select"),Command.ITEM,0);
	private static final Command cmdSync = new Command(Locale.get("polish.command.select"),Command.ITEM,0);
	
	private TitleBarItem titleitem;
	
	protected ChoiceGroup roaming, order;
	
	public SettingsForm(final Model model, final Controller controller) 
	{
		this(model,controller,null);
	}
	
	public SettingsForm(final Model model, final Controller controller, Style style) 
	{
		super(model,controller,null,style);
		
		update();
		
		//no save function untill more possibilities under settings
		/*this.addCommand(cmdSave);*/
		
		//#if not polish.blackberry
			this.addCommand(cmdBack);
		//#endif
	}
	
	/**
	 * 
	 */
	public void update() {

		this.deleteAll();
		
		//sync settings
		
		//#style ui_factory_button_item
		StringItem syncButton = UiFactory.createButtonItem(Locale.get("nowplus.client.java.settings.sync.title"), Locale.get("nowplus.client.java.settings.sync.syncitem"), cmdSync, this, null);
		append(syncButton);
		
		//Clear RMS settings

		//#style ui_factory_button_item
		StringItem clearButton = UiFactory.createButtonItem(Locale.get("nowplus.client.java.settings.removedataitem.title"), Locale.get("nowplus.client.java.settings.removedataitem"), cmdClearRMS, this, null);
		append(clearButton);

		UiFactory.createStringItem(Locale.get("nowplus.client.java.settings.about.title"), Locale.get("nowplus.client.java.settings.about.text2010"), null, null, this);

		//#ifdef branch.name:defined
		//#message branch name is ${branch.name}
		//#= String branchName = "${branch.name}";
		//#else
		//#message branch name isn't set
		String branchName = "";
		//#endif

		
        UiFactory.createStringItem(null, Locale.get("nowplus.client.java.settings.software.title") + " " + model.getAppVersion()+branchName, null, null, this);
	}

	public byte getContext() 
	{
		return Event.Context.SETTINGS;
	}
	
	public void commandAction(Command c, Displayable d)
	{
		if(c == cmdBack || c == cmdSave)
		{
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT);		
		}
		else
			super.commandAction(c, d);
	}

	public void commandAction(Command c, Item item)
	{
		if(c == cmdClearRMS)
		{
			getController().handleEvent(Event.Context.SETTINGS, Event.Settings.CLEAR_STORAGE, null);
		}
		if(c == cmdSync)
		{
			getController().handleEvent(Event.Context.SETTINGS, Event.Settings.SYNC, null);
		}
		if(c == cmdResetCounter)
		{
			getController().handleEvent(Event.Context.SETTINGS, Event.Settings.RESET_COUNTER_CONFIRM, null);
		}
		else
			super.commandAction(c, item);
	}

	protected Item createBottomItem()
	{
		return null;
	}

	protected String createCssSelector() 
	{
		return null;
	}

	protected Item[] createStaticItems() 
	{
		this.titleitem = new TitleBarItem( Locale.get("nowplus.client.java.settings.title"), getModel() ); 
		
		return new Item[]{ this.titleitem };
	}

	protected Item createTopItem()
	{
		return null;
	}
	
	public void itemStateChanged(Item itm) 
	{
		if(itm == roaming)
		{
			getController().handleEvent(Event.Context.SETTINGS, Event.Settings.ROAMING, new Boolean(roaming.isSelected(0)));
		}
		else
		if(itm == order)
		{
			getController().handleEvent(Event.Context.SETTINGS, Event.Settings.ORDER, new Integer(order.getSelectedIndex()));
		}
		else
			super.itemStateChanged(itm);
	}
	
	//#if polish.blackberry
	public boolean handleShowOptions() {
		//TODO timon is this right?
    	getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, ContextualMenu.OPEN,null);
    	return true;
	}
	//#endif
	
	//#if polish.blackberry
	//overrides
	/**
	 * Handles key events.
	 * 
	 * WARNING: When this method should be overwritten, one need
	 * to ensure that super.keyPressed( int ) is called!
	 * 
	 * @param keyCode The code of the pressed key
	 */
	public void keyPressed(int keyCode) 
	{
		 if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
			this.getCommandListener().commandAction(cmdBack,this);
		else
			super.keyPressed(keyCode);
	}
	//#endif
}
