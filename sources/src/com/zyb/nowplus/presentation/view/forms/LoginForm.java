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

//#if polish.blackberry
import net.rim.device.api.ui.Keypad;
import com.zyb.nowplus.presentation.view.forms.LoginForm;
//#endif

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.TextField;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.ChoiceItem;
import de.enough.polish.ui.Choice;
import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.util.DeviceControl;
import de.enough.polish.util.Locale;

//#if ${output.keystrokes}==true
import com.zyb.util.DebugUtils;
//#endif
import com.zyb.util.TextUtilities;

//#if ${visualverification}==true
//# import com.zyb.utils.TestUtil;
//#endif

/** 	
 * @author Jens Vesti
 *
*/
public class LoginForm extends BaseForm// implements ItemCommandListener
{
	public static Command cmdSave = new Command(Locale.get( "nowplus.client.java.loginform.command.save" ),Command.SCREEN,10);
	public static Command cmdCancel = new Command(Locale.get( "nowplus.client.java.loginform.command.cancel" ),Command.CANCEL,10);
	public static Command cmdExit = new Command(Locale.get( "nowplus.client.java.command.exit" ),Command.EXIT,10);

	boolean usernameReadonly = false;
	private TextField userName,passWord;
	private ChoiceItem rememberMeItem;
	private Model model;
	
	/**
	 * Title item displaying both screen name and current time
	 */
	protected TitleBarItem titleitem;
	
	//#if polish.blackberry
	private Command cmdQuit;
	//#endif

	public LoginForm(Model model, Controller controller, final String title, boolean firstLogin, final Style style)
	{
		super(model, controller, null, style);
		this.model = model;
		this.titleitem = new TitleBarItem( title, model );
		
		//#if polish.blackberry.isTouchBuild == true
			this.title = this.titleitem;
		//#else
			//add title item
			this.append(titleitem);
		//#endif

		//#if polish.blackberry
		titleitem.setAppearanceMode(Item.PLAIN);
		//#endif

		initForm(firstLogin);
		
		//focus on TextField of UserName input
		if (!usernameReadonly) {
			UiAccess.focus(this, userName);
		}
	}

	public LoginForm(Model model, Controller controller, final String title, boolean firstLogin) {
		this(model,controller,title,firstLogin,null);
	}
	
	//#if polish.blackberry
	public TextField getUserNameTextField()
	{
		return userName;
	}
	//#endif
	
	public String getUsername()
	{
		return userName.getString();
	}

	public String getPassword()
	{
		return passWord.getString();
	}

	public boolean isRememberMeSet()
	{
		return rememberMeItem.isSelected;
	}

	private void initForm(boolean firstLogin)
	{
		//#if !polish.blackberry
		this.addCommand(cmdSave);
		
		if(firstLogin)
			this.addCommand(cmdCancel);
		else
			this.addCommand(cmdExit);
		//#endif
		
		StringItem headLine;
		//#if polish.blackberry 
			//#if polish.blackberry.isTouchBuild == false
				//#style .login_headline
				headLine = new StringItem(null,Locale.get( "nowplus.client.java.loginform.headline" ));
				this.append(headLine);
		    //#endif
		//#else
			//#style .login_headline
			headLine = new StringItem(null,Locale.get( "nowplus.client.java.loginform.headline" ));
			this.append(headLine);
	
		//#endif
		
		//#if polish.blackberry && polish.blackberry.isTouchBuild == false
		headLine.setAppearanceMode(Item.PLAIN);
		//#endif
		
		String usernameString = model.getUserName();
		String passwordString = "";

		//#if username:defined
		//#message Username is set
		//#= usernameString = "${username}";
		//#endif
		
		//#if password:defined
		//#message Password is set
		//#= passwordString = "${password}";
		//#endif
		
		if(usernameString == null)
			usernameString = "";
		if(passwordString == null)
			passwordString = "";
	
		//username textfield
		
		//#if polish.blackberry
			userName = UiFactory.createTextField(Locale.get("nowplus.client.java.loginform.username.hint"), usernameString, 255, TextField.NON_PREDICTIVE, this);
			//fix for PBLA-816 Touch device: predictive text while disabling the surepress
			userName.setNoComplexInput(true);
		//#else
			userName = UiFactory.createTextField(null, usernameString, 255, TextField.NON_PREDICTIVE, this);
		//#endif
			
			UiAccess.setTextfieldHelp(userName, Locale.get( "nowplus.client.java.loginform.username.hint" ));
	        //#if using.native.textfield:defined
				userName.setTitle(Locale.get( "nowplus.client.java.loginform.username.hint"));
				//#if (${lowercase(polish.vendor)}==samsung) 
					UiFactory.fixSamsungNativeTextField(userName);
				//#endif
			//#endif
				
		if(userName.getText().length()>0)
		{
			//Change username textfield into uneditable field once authenticated once to keep users from switching username
				userName.setConstraints(TextField.NON_PREDICTIVE | TextField.UNEDITABLE);
					
				usernameReadonly = true;
		       
				//#if not polish.blackberry
					//#style .login_textfield_readonly
					//#= userName.setStyle();
				//#endif
		}
			
			//password textfield
		
		//#if polish.blackberry
			passWord = UiFactory.createTextField(Locale.get("nowplus.client.java.loginform.password.hint"),passwordString,255, TextField.NON_PREDICTIVE | TextField.INITIAL_CAPS_NEVER | TextField.PASSWORD, this);
		//#else
			passWord = UiFactory.createTextField(null,passwordString,255, TextField.INITIAL_CAPS_NEVER | TextField.PASSWORD, this);
		//#endif
						
			UiAccess.setTextfieldHelp(passWord, Locale.get( "nowplus.client.java.loginform.password.hint" ));
	        //#if using.native.textfield:defined 
				passWord.setTitle(Locale.get( "nowplus.client.java.loginform.password.hint" ));
				//#if (${lowercase(polish.vendor)}==samsung)  
					UiFactory.fixSamsungNativeTextField(passWord);
				//#elif polish.device.textField.requires.initialCapsNeverFix:defined
					UiFactory.fixS40NativeTextField(passWord);
				//#endif
			//#endif
				
		rememberMeItem = UiFactory.createChoiceCheckBoxItem(Locale.get( "nowplus.client.java.loginform.rememberme.label" ), Choice.MULTIPLE);
		rememberMeItem.select(model.stayLoggedIn());
		UiFactory.createChoiceGroup(null, ChoiceGroup.MULTIPLE, new ChoiceItem[]{rememberMeItem}, 0, rememberMeItem.isSelected, null, this);
		
		//#if !polish.blackberry
			if(usernameReadonly) {
				this.focus(passWord);
			}
		//#endif
		
		//#if polish.blackberry
			//#style ui_factory_button_item
			StringItem loginButton = UiFactory.createButtonItem(null, cmdSave.getLabel(), (de.enough.polish.ui.Command) cmdSave, null, null);
			append(loginButton);
			  
			if (firstLogin) {
				cmdQuit = cmdCancel;
			}
			else {
				cmdQuit = cmdExit;
			}
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
	 */
	public void keyPressed(int keyCode) 
	{
		if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
			getCommandListener().commandAction(cmdQuit, this);
		else
			super.keyPressed(keyCode);
	}
	//#endif
	
	public void showNotify()
	{
		super.showNotify();

		//#if polish.blackberry.isTouchBuild == true
			DeviceControl.hideSoftKeyboard();
		//#endif

		//#if !polish.blackberry
			//#if not using.native.textfield:defined
				TextUtilities.loadTextFieldCharacterset("la");//Defaulting to latin characters as we only allow latin characters in input fields 
			//#endif	
		//#endif

	}	

	
	//Following snippet of code makes a screen dump of what is being displayed and saves it as a file in default graphics directory
	//#if ${visualverification}==true
	//#public void paint(Graphics g)
	//#{
	//#	if(TestUtil.doPaint())
	//#	{
	//#		super.paint(TestUtil.getGraphics(g));
	//#		TestUtil.flush();
	//#	}
	//#	else
	//#		super.paint(g);
	//#}
	//#endif

	
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
		return super.handleKeyPressed(keyCode, gameAction);
	}
	//#endif
	
	public void destroy()
	{
	}
	
	//#if polish.fix_uncaught_startup_exception
	public void create()
	{
		synchronized (this)
		{
			if(!this.isContentCreated)
			{
				createContent();
				
				this.isContentCreated = true;
			}
		}
	}
	//#endif
	
	protected void createContent()
	{
	}

	public byte getContext()
	{
		return 0;
	}
}
