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
package com.zyb.nowplus.presentation.view.items;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.zyb.nowplus.presentation.UiFactory;


import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.TextField;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.UiAccess;
//#if polish.device.requires.custom.textfield:defined
import de.enough.polish.ui.xTextField;
//#endif
import de.enough.polish.util.Locale;

/**
 * Simple wrapper class to visualize chat textfield
 *  
 * @author anders
 */
public class ChatTextFieldItem extends Container
{
	/**
	 * Local commands
	 */
	public static final Command cmdSend = new Command(Locale.get("nowplus.client.java.chat.thread.send"),Command.OK,0);
	public static final Command cmdEmoticons = new Command(Locale.get("polish.command.select"),Command.OK,0);
	
	
	
	/**
	 * Icon for emoticons
	 */
	private IconItem emoticonItem;
	
	/**
	 * Icon for inputMode
	 */
	private IconItem inputModeItem;
	private int lastInputMode = -1;
	
	/**
	 * Tetx input field
	 */
	private TextField textInput;
	
	/**
	 * Cashed input mode images
	 */
	private static Image[] inputModeImg;
	
	StringItem submitButton;

	/**
	 * 
	 * @param listener
	 */
	public ChatTextFieldItem(ItemCommandListener listener)
	{
		this(listener, null);
	}
	
	/**
	 * 
	 * @param listener
	 * @param style
	 */
	public ChatTextFieldItem(ItemCommandListener listener, Style style)
	{
		super(false, style);
		
		//load cashed images
		try
		{
			if(null == inputModeImg)
			{
				inputModeImg = new Image[4];
				//#if polish.classes.ImageLoader:defined
				inputModeImg[0] = StyleSheet.getImage("/text_mode_24x12_sentence.png", null, false);
				inputModeImg[1] = StyleSheet.getImage("/text_mode_24x12_lower.png", null, false);
				inputModeImg[2] = StyleSheet.getImage("/text_mode_24x12_upper.png", null, false);
				inputModeImg[3] = StyleSheet.getImage("/text_mode_24x12_numbers.png", null, false);
				//#else
				inputModeImg[0] = Image.createImage("/text_mode_24x12_sentence.png");
				inputModeImg[1] = Image.createImage("/text_mode_24x12_lower.png");
				inputModeImg[2] = Image.createImage("/text_mode_24x12_upper.png");
				inputModeImg[3] = Image.createImage("/text_mode_24x12_numbers.png");
				//#endif
			}
		}
		catch (Exception e)
		{
			//#debug error
			System.out.println("Error loading input mode iamge: "+e.getMessage());
		}
		//#if polish.device.requires.custom.textfield:defined
			//#style chat_inputfield_textfield
			textInput = new xTextField(null,"",1024,TextField.INITIAL_CAPS_SENTENCE | TextField.ANY);
		//#else
			
			//#if polish.blackberry
			
				//#style chat_inputfield_textfield
				InputModeTextFieldItem chatInputModeTextFieldItem = UiFactory.createInputModeTextFieldItem(Locale.get("nowplus.client.java.peoplepage.meview.status.label"),"", 1024, TextField.INITIAL_CAPS_WORD | TextField.ANY, null);
			
				textInput=chatInputModeTextFieldItem.getTextField();
			
			//#else
			
				//#style chat_inputfield_textfield
				textInput = new TextField(null,"",1024,TextField.INITIAL_CAPS_SENTENCE | TextField.ANY);
			//#endif
		
		//#endif
		
		textInput.setSuppressCommands(true);
		
		//#if not polish.BlackBerry
		textInput.setInputMode(TextField.MODE_LOWERCASE);
		//#endif
		
		textInput.setItemCommandListener(listener);
		textInput.setHelpText(Locale.get("nowplus.client.java.chat.thread.textfield.helptext"));
		
		//#if polish.blackberry
			//#style ui_factory_textfield_helpfont
			textInput.setHelpStyle();
    	//#endif
    	
        //#if using.native.textfield:defined
		textInput.setTitle(Locale.get("nowplus.client.java.chat.thread.textfield.helptext"));
		//#endif
		
		//#if polish.blackberry
			textInput.setDefaultCommand(cmdSend);
		//#else
			textInput.addCommand(cmdSend);
		//#endif
		
		//#style chat_inputfield_inputmode_icon
		inputModeItem = new IconItem(null,null);
		inputModeItem.setAppearanceMode(Item.PLAIN);
		
		//#if not polish.BlackBerry
		updateInputModeItem(TextField.MODE_FIRST_UPPERCASE);
		//#endif
		
		/* below inputItem not used for blackberry @see method#updateInputModeItem(int inputMode)*/
	//#if not polish.BlackBerry
		//#style chat_inputfield_textfield_wrap
		Container inputItem = new Container(true);
		inputItem.add(textInput);
		inputItem.add(inputModeItem);
		this.add(inputItem);
	//#endif
		
		
		//#style chat_inputfield_emoticon_icon
		emoticonItem = new IconItem(null,null);
		emoticonItem.setItemCommandListener(listener);
		
		//#if polish.blackberry
			emoticonItem.setDefaultCommand(cmdEmoticons);
		//#else
			emoticonItem.addCommand(cmdEmoticons);
		//#endif
			
		
		

		//#if polish.blackberry
			
			//#style chat_base_columns2_container
			Container textfieldbuttonsContainer = new Container(false);
			textfieldbuttonsContainer.setAppearanceMode(Item.PLAIN);
			
			//#style chat_base_columns2_container
			Container textfieldSmileyContainer = new Container(false);
			textfieldSmileyContainer.setAppearanceMode(Item.PLAIN);
			
			//#style chat_sendButton
			this.submitButton = new StringItem("", Locale.get("nowplus.client.java.chat.thread.send"));
			this.submitButton.setItemCommandListener(listener);
			
			/*Input TextField with smiley in same container */
			textfieldSmileyContainer.add(textInput);
			textfieldSmileyContainer.add(emoticonItem);
			
			textfieldbuttonsContainer.add(textfieldSmileyContainer);
			textfieldbuttonsContainer.add(this.submitButton);
			
			this.add(textfieldbuttonsContainer);
		//#else
			this.add(emoticonItem);
		//#endif
			this.setAppearanceMode(Item.INTERACTIVE);
	}
	
	public void setSubmitButtonActive(boolean active) {
		//#if polish.blackberry
		if(active) {
			//#style chat_sendButton
			this.submitButton.setStyle();
			this.submitButton.setDefaultCommand(cmdSend);
		} else {
			//#style chat_sendButtonInactive
			this.submitButton.setStyle();
			this.submitButton.setDefaultCommand(null);
		}
		//#endif
	}
	
	/**
	 * Get text of nested TextField item.
	 * 
	 * @return
	 */
	public String getString()
	{
		return textInput.getString();
	}
	
	/**
	 * Set text of nested TextField item.
	 * 
	 * @return
	 */
	public void setString(String string)
	{
		if(null != string)
			textInput.setString(string);
	}
	
	/**
	 * Clear text of nested TextField item.
	 */
	public void clear()
	{
		
		//#if polish.blackberry
		 	textInput.delete(0, textInput.getString().length());
		 //#else
		 	textInput.delete(0, textInput.size());
		 //#endif
		
	}
	
	/**
	 * Focus textfield element
	 */
	public void focusTextfield()
	{
		if(this.size() >= 1)
		{
			this.focusChild(-1); //clear focus
			this.focusChild(0);
		}
	}
	
	public boolean isTextfieldFocused(){
		return this.get(0).isFocused;
	}
	
	public void defocusTextfield(){
		UiAccess.defocus(this.textInput, this.textInput.getStyle());
	}
	
	/**
	 * Focus emoticon element
	 */
	public void focusEmoticon()
	{
		if(this.size() >= 2)
		{
			this.focusChild(-1); //clear focus
			this.focusChild(1);
		}
	}
	
	
	/**
	 * Set the input mode of the TextField
	 */
	public void setInputMode(int mode)
	{
		if(textInput.getInputMode() != mode)
			textInput.setInputMode(mode);
	}
	
	/**
	 * Get TextField input mode e.g. TextField.NATIVE, TextField.MODE_FIRST_UPPERCASE
	 *  
	 * @return int
	 */
	public int getInputMode()
	{
		return textInput.getInputMode();
	}
	
	/**
	 * Updates input mode item according to parameter input mode
	 */
	private void updateInputModeItem(int inputMode)
	{
		if(lastInputMode != inputMode)
		{
			//#if using.native.textfield:defined
			// For devices that do not have clear key we use native mode always and do not allow to change. 
			textInput.setInputMode(inputMode = TextField.MODE_NATIVE);
			//#endif
			
			//#if not polish.BlackBerry
			if(inputMode == TextField.MODE_FIRST_UPPERCASE)
				inputModeItem.setImage(inputModeImg[0]);
			else
			if(inputMode == TextField.MODE_LOWERCASE)
				inputModeItem.setImage(inputModeImg[1]);
			else
			if(inputMode == TextField.MODE_UPPERCASE)
				inputModeItem.setImage(inputModeImg[2]);
			else
			if(inputMode == TextField.MODE_NUMBERS)
				inputModeItem.setImage(inputModeImg[3]);
			//#endif
			
			lastInputMode = inputMode;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Container#paintContent(int, int, int, int, javax.microedition.lcdui.Graphics)
	 */
	protected void paintContent(int x, int y, int leftBorder, int rightBorder,
			Graphics g) 
	{
		//make sure that inputmode Item is up to date at every repaint
		updateInputModeItem(textInput.getInputMode());
		
		super.paintContent(x, y, leftBorder, rightBorder, g);
	}
}
