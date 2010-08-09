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

import de.enough.polish.ui.Container;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.TextField;
import de.enough.polish.util.Locale;

//#if polish.device.requires.custom.textfield:defined
import de.enough.polish.ui.xTextField;
//#endif

/**
 * Container type Item that nests a Polish TextField and visualizes
 * the current input mode in accordance to the Now+ design layout.
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class InputModeTextFieldItem extends Container
{
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

	/**
	 * 
	 * @param label
	 * @param text
	 * @param maxSize
	 * @param constraints
	 */
	public InputModeTextFieldItem(String label, String text, int maxSize, int constraints)
	{
		this(label, text, maxSize, constraints, null);
	}

	/**
	 * 
	 * @param label
	 * @param text
	 * @param maxSize
	 * @param constraints
	 * @param style
	 */
	public InputModeTextFieldItem(String label, String text, int maxSize, int constraints, Style style)
	{
		//#style ui_factory_textfield_wrap
		super(false);
		
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
		
		//set label
		if(null != label)
			this.setLabel(label);

		//create textfield respecting passe style
		//#if polish.device.requires.custom.textfield:defined
			textInput = new xTextField(null,"",maxSize,constraints, style);
		//#else
			textInput = new TextField(null,"",maxSize,constraints, style);
		//#endif
		
		textInput.setSuppressCommands(true);
		
		//** TEST FOR NOKIA PHONES, USE NATIVE EDITOR DUE TO ABSENCE OF C-KEY **
//		textInput.setInputMode(TextField.MODE_NATIVE);
		//**
		//for devices that do not have clear key we will enter native mode
		//#if not polish.key.ClearKey:defined
		textInput.setInputMode(TextField.MODE_NATIVE);
		//#endif
		
		//set text or default help text
		if (null != text)
		{
			textInput.setString(text);
		}
		else
		{
			textInput.setHelpText(Locale.get("nowplus.client.java.chat.thread.textfield.helptext"));
			
			//#if polish.blackberry
				//#style ui_factory_textfield_helpfont
				textInput.setHelpStyle();
			//#endif
		}
		
		//create inputmode item
		//#style ui_factory_textfield_inputmode
		inputModeItem = new IconItem(null,null);
		inputModeItem.setAppearanceMode(Item.PLAIN);
		updateInputModeItem(textInput.getInputMode());
		
		this.add(textInput);
		this.add(inputModeItem);
	}
	
	/**
	 * Returns a handle to nested TextField
	 * 
	 * @return
	 */
	public TextField getTextField()
	{
		return textInput;
	}
	
	/**
	 * Updates input mode item according to parameter input mode
	 */
	private void updateInputModeItem(int inputMode)
	{
		if(lastInputMode != inputMode)
		{
			//#if not polish.key.ClearKey:defined
			textInput.setInputMode(inputMode = TextField.MODE_NATIVE);
			//#endif
			
			//#if !polish.BlackBerry
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

	//#if not polish.key.ClearKey:defined
	public void deleteKeyAtCaretPosition() {
		final StringBuffer stringBuffer = new StringBuffer(this.textInput.getText());
		final int caretPosition = this.textInput.getCaretPosition();
		
		if(caretPosition > 0 && caretPosition <= stringBuffer.length() ) {
			this.textInput.setString(stringBuffer.deleteCharAt(caretPosition - 1).toString());
		}
	}
	//#endif
	
	
}
