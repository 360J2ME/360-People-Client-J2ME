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
//#condition polish.device.supports.nativesms == false
/**
 * 
 */
package com.zyb.nowplus.presentation.view.forms;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.business.domain.Profile;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.ContactSummarizedItem;
import com.zyb.nowplus.presentation.view.items.ContactSummarizedSMSInsteadStatusItem;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;

import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.TextField;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.Locale;

//#if ${output.keystrokes}==true
import com.zyb.util.DebugUtils;
//#endif

/**
 * Sms composer form for devices that do not support sending messages via platformRequest call.
 * 
 * 
 * @author bartosz.stanczykowski@mobica.com
 * @author marek.defecinski@mobica.com
 * 
 */
public class SmsForm extends BaseFramedForm
{
	private TextField textarea;
	
	public final static Command cmdSubmit = new Command(Locale.get("polish.command.submit"), Command.OK, 0);
	public final static Command cmdBack = new Command(Locale.get("polish.command.back"), Command.BACK, 0);
	public final static Command cmdOptions = new Command(Locale.get("nowplus.client.java.command.options"), Command.SCREEN, 0);

	private ManagedProfile profile;

	private Channel channel;
	
	/**
	 * Title item displaying both screen name and current time
	 */
	protected TitleBarItem titleitem;
	protected ContactSummarizedItem csi;
	
	private String number;

	public SmsForm(final Model model, final Controller controller, final ManagedProfile profile)
	{
		this(model, controller, profile, null, null);
	}
	
	public SmsForm(final Model model, final Controller controller, final ManagedProfile profile, final Style style)
	{
		this(model, controller, profile, null, style);
	}
	
	public SmsForm(final Model model, final Controller controller, final ManagedProfile profile, final Channel chosenChannel) 
	{
		this(model, controller, profile, chosenChannel, null);
	}
	
	public SmsForm(final Model model, final Controller controller, final ManagedProfile profile, final Channel chosenChannel, final Style style) 
	{
		super(model, controller, null, style);
		
		//defined contact
		if(null != (this.channel = chosenChannel) )
		{
			this.channel = chosenChannel;
			this.profile = (ManagedProfile) chosenChannel.getProfile();
		}
		else
			this.profile = profile;
		
		//ensure contact holds all needed detail
		checkValidityOfContactProfile(this.profile);
		
		initForm();
	}

	private void initForm()
	{
		this.addCommand(cmdSubmit);
		this.addCommand(cmdBack);
		this.addCommand(cmdOptions);
		
		//#style .sms_textfield
		textarea = UiFactory.createTextField(null, "", 160, TextField.ANY | TextField.INITIAL_CAPS_SENTENCE, this);
		
		//#if not polish.key.ClearKey:defined
		int constraints = textarea.getConstraints();
		constraints = constraints & ~TextField.NON_PREDICTIVE;
		textarea.setConstraints(constraints);
		//#endif

		UiAccess.setTextfieldHelp(textarea, Locale.get("nowplus.client.java.smsform.message.hint"));
		textarea.setTitle(Locale.get("nowplus.client.java.smsform.message.hint"));
		
		//#if using.native.textfield:defined && ${lowercase(polish.vendor)}==samsung
		UiFactory.fixSamsungNativeTextField(textarea);
		//#endif
		
		this.focus(textarea);
	}
	
	private void checkValidityOfContactProfile(Profile contact)
	{
		if (this.profile == null)
			throw new IllegalArgumentException("Contact cannot be null");
		
		if(contact instanceof Profile)
			((Profile)contact).load(true);
		
		if(null == channel)
		{
			this.channel = this.profile.getPrimarySMSChannel();
			
			if (this.channel == null)
				throw new IllegalArgumentException("No sms channel");
		}

		this.number = this.channel.getName();

		if (this.number == null)
			throw new IllegalArgumentException("Cannot obtain the number");

		if (this.profile.getFullName() == null)
			throw new IllegalArgumentException("No contact name");
		
		if(contact instanceof Profile)
			((Profile)contact).unload();
	}

	// Following snippet of code makes a screen dump of what is being displayed
	// and saves it as a file in default graphics directory
	// #if ${visualverification}==true
	public void paint(Graphics g) {
		super.paint(g);
	}

	// #endif

	
	
	//#if ${output.keystrokes}==true
	protected boolean handleKeyReleased(int keyCode, int gameAction)
	{
		System.out.println(DebugUtils.addKeystrokeReleased(keyCode));
		return super.handleKeyReleased(keyCode, gameAction);
	}
	
	protected boolean handleKeyRepeated(int keyCode, int gameAction)
	{
		System.out.println(DebugUtils.addKeystrokeRepeated(keyCode));
		return super.handleKeyRepeated(keyCode, gameAction);
	}
	
	protected boolean handleKeyPressed(int keyCode, int gameAction)
	{
		System.out.println(DebugUtils.addKeystrokePressed(keyCode));
		//#if using.native.textfield:defined 
		if (gameAction == Canvas.FIRE && keyCode != Canvas.KEY_NUM5
				&& textarea != null && textarea.getString() != null
				&& textarea.getString().length() > 0) {
			return false;
		}
		//#endif
		return super.handleKeyPressed(keyCode, gameAction);
	}
	//#elif using.native.textfield:defined 
	//#= protected boolean handleKeyPressed(int keyCode, int gameAction) {
	//#=	if (gameAction == Canvas.FIRE && keyCode != Canvas.KEY_NUM5
	//#=			&& textarea != null && textarea.getString() != null
	//#=			&& textarea.getString().length() > 0) {
	//#=		return false;
	//#=	} 
	//#=		return super.handleKeyPressed(keyCode, gameAction);
	//#= }
	//#endif

	public String getNumber() {
		return number;
	}

	public String getMessage() {
		return textarea.getString();
	}

	public byte getContext() 
	{
		return Event.Context.SMS_EDITOR;
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
		// add title item
		this.titleitem = new TitleBarItem(Locale.get("nowplus.client.java.smscomposer.title"), model);

		//#style contextualmenu_profile_item
		this.csi = new ContactSummarizedSMSInsteadStatusItem(this.channel, ContactSummarizedItem.NETWORK_ICON_MODE_ALL, ContactSummarizedItem.PRESENCE_MODE_NONE);
		
		return new Item[]{this.titleitem, this.csi};
	}

	protected Item createTopItem() 
	{
		return null;
	}
	public ManagedProfile getProfile() {
		return profile;
	}
}
