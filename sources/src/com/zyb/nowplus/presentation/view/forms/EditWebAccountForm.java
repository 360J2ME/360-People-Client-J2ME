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
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;
import com.zyb.util.TextUtilities;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextField;
import de.enough.polish.util.Locale;

//#if polish.blackberry
import net.rim.device.api.ui.Keypad;
//#endif

/**
 * This class implements the layout of the 'sign up' form of the WebAccounts pages
 * <p>
 * Used for editing of all SN types.
 *
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class EditWebAccountForm extends BaseFramedForm
{
	public final static Command cmdLogin = new Command(Locale.get("nowplus.client.java.webaccounts.command.login"), Command.SCREEN, 0);

	protected TitleBarItem titleitem;
	
	protected ExternalNetwork network;
	
	protected Identity iden;
	
	protected TextField usernameTextField, passwordTextField;
	
	public EditWebAccountForm(Model model, Controller controller, ExternalNetwork nw, Identity iden)
	{
		this(model, controller, nw, iden, null);
	}
	
	public EditWebAccountForm(Model model, Controller controller, ExternalNetwork nw, Identity iden, Style style) 
	{
		super(model, controller, null, style);
		
		this.network = nw;
		this.iden = iden;
		
		//#style webaccount_edit_headline
		Container info = new Container(false);
		info.setAppearanceMode(Item.PLAIN);
		
		//constructs network icon and sets default style to focused/'white'
		IconItem networkIcon = UiFactory.createNetworkIcon( (null != this.network ? this.network.getNetworkId() : "") ,false);
		networkIcon.setStyle( networkIcon.getFocusedStyle() );
		networkIcon.setAppearanceMode(Item.PLAIN);
		
		info.add(
				networkIcon
		);
		info.add(
				//#style webaccount_edit_headline_text
				 new StringItem(null, (null != this.network ? this.network.getName() : "") + " " + Locale.get("nowplus.client.java.webaccounts.edit.info.suffix"))
		);	
		this.append(info);

		String credentialsLabel = null;
		if (nw.getCredentialsType() == ExternalNetwork.CREDENTIALS_USERNAME) 
		{
			credentialsLabel = Locale.get("nowplus.client.java.webaccounts.edit.username");
		}
		else if (nw.getCredentialsType() == ExternalNetwork.CREDENTIALS_USERNAME_OR_EMAIL) 
		{
			credentialsLabel = Locale.get("nowplus.client.java.webaccounts.edit.username_email");
		}

		//add username textfield
		this.usernameTextField = UiFactory.createTextField(null,null, 128, TextField.ANY | TextField.INITIAL_CAPS_NEVER, this);
		//fix for PBLA-816 Touch device: predictive text while disabling the surepress
		//#if polish.blackberry
			this.usernameTextField.setNoComplexInput(true);
		//#endif
		
		//#if using.native.textfield:defined
		this.usernameTextField.setTitle(credentialsLabel);
			//#if (${lowercase(polish.vendor)}==samsung)
				UiFactory.fixSamsungNativeTextField(this.usernameTextField);
			//#endif
		//#endif
				
		//check for existing username
		if (null == this.iden)
		{
			this.usernameTextField.setHelpText(credentialsLabel);
			
			//#if polish.blackberry
				//#style ui_factory_textfield_helpfont
				usernameTextField.setHelpStyle();
			//#endif
		}
		else
		{
			this.usernameTextField.setString(this.iden.getName());
			
			//username uneditable in 'edit mode'
//			this.usernameTextField.setAppearanceMode(Item.PLAIN);
		}
		
			//add password textfield
			this.passwordTextField = UiFactory.createTextField(null,null, 128, TextField.NON_PREDICTIVE | TextField.INITIAL_CAPS_NEVER | TextField.PASSWORD, this);
	        //#if using.native.textfield:defined
			this.passwordTextField.setTitle(Locale.get("nowplus.client.java.webaccounts.edit.password"));
				//#if (${lowercase(polish.vendor)}==samsung)
					UiFactory.fixSamsungNativeTextField(this.passwordTextField);
				//#elif polish.device.textField.requires.initialCapsNeverFix
					UiFactory.fixS40NativeTextField(this.passwordTextField);
				//#endif
			//#endif
		
			//check for existing password
				this.passwordTextField.setHelpText(Locale.get("nowplus.client.java.webaccounts.edit.password"));
				
				//#if polish.blackberry
					//#style ui_factory_textfield_helpfont
					this.passwordTextField.setHelpStyle();
				//#endif
		
		//#if polish.blackberry
			removeCommand(cmdBack);
		  
		  //#style ui_factory_button_item
		  StringItem loginButton = UiFactory.createButtonItem(null, cmdLogin.getLabel(), (de.enough.polish.ui.Command) cmdLogin, null, null);
		  
		  append(loginButton);
		  
		//#endif
		  
		//adding disclaimer for given network
		if (null != this.network)
		{
			String[] disclaimer = this.network.getDisclaimer();
			
			if (disclaimer[0] != null)
			{
				//#style .webaccount_edit_disclaimer_header
				this.append( disclaimer[0] );
			}
			
			for (int i = 1; i < disclaimer.length; i++)
			{
				//#style .webaccount_edit_disclaimer_text
				this.append( disclaimer[i] );				
			}
		}
		
		//#if !polish.blackberry
		this.addCommand(cmdLogin);
		this.addCommand(cmdBack);
		//#endif
	}

	protected Item createBottomItem() {
		return null;
	}

	protected String createCssSelector() {
		return null;
	}

	protected Item[] createStaticItems() 
	{
		if(null == iden)
			return new Item[]{ this.titleitem = new TitleBarItem( Locale.get("nowplus.client.java.webaccounts.add.title"),getModel() ) };
		else
			return new Item[]{ this.titleitem = new TitleBarItem( Locale.get("nowplus.client.java.webaccounts.edit.title"),getModel() ) };
	}

	protected Item createTopItem() {
		return null;
	}

	public byte getContext() 
	{
		return -1;
	}
	
	public void commandAction(Command c, Displayable d) 
	{
		if(c == cmdLogin)
		{
			getController().notifyEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.SAVE, null);
		}
		else
			super.commandAction(c, d);
	}

	/**
	 * @return
	 */
	public ExternalNetwork getNetwork() {
		return this.network;
	}

	/**
	 * @return
	 */
	public String getUsername() {
		return this.usernameTextField.getText();
	}

	/**
	 * @return
	 */
	public String getPassword() {
		return this.passwordTextField.getString();
	}

	public void showNotify()
	{
		super.showNotify();

		//#if !polish.blackberry
			//#if not using.native.textfield:defined
				TextUtilities.loadTextFieldCharacterset("la");//Defaulting to latin characters as we only allow latin characters in input fields 
			//#endif	
		//#endif
	}	
	
	//#if polish.blackberry
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
		 if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
			commandAction(cmdBack, this); 
		else
			super.keyPressed(keyCode);
	}
	//#endif

}
