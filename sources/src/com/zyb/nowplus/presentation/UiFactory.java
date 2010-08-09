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
package com.zyb.nowplus.presentation;

import java.util.Date;

import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.ProfileSummary;
import com.zyb.nowplus.business.domain.filters.Filter;
import com.zyb.nowplus.presentation.view.items.CascadeItem;
import com.zyb.nowplus.presentation.view.items.ContactSummarizedItem;
import com.zyb.nowplus.presentation.view.items.IconSwapItem;
import com.zyb.nowplus.presentation.view.items.InputModeTextFieldItem;

import de.enough.polish.ui.Choice;
import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.ChoiceItem;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandItem;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.DateField;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextField;

/**
 * Factory for creating common UI elements in the Now+ app 
 * @author Ander Bo Pedersen, anders@zyb.com
 */
public class UiFactory
{
	/**
	 * Factory method for creating textfields
	 */
	public final static TextField createTextField(String label, String text, int maxSize, int constraints, Form form)
	{
		return createTextField(label, text, maxSize, constraints, form, null);
	}
	
//	/** 
//	 * Factory method for creating textfields
//	 */
//	public final static TextField createTextField(String label, String text, int maxSize, int constraints, Form form, Style style)
//	{
//		//#style ui_factory_textfield_wrap
//		Container c = new Container(false);
//		c.setLabel(label);
//		
//		//use parameter style or fall back to default
//		TextField tf = null;
//		if(null != style)
//			tf = new TextField(null, text, maxSize, constraints, style);
//		else
//			//#style ui_factory_textfield
//			tf = new TextField(null, text, maxSize, constraints);
//		
//		//supress delete, clear & add symbol commands
//		tf.setSuppressCommands(true);
//		
//		c.add(tf);
//		
//		if(null != form)
//			form.append(c);
//		
//		return tf;
//	}
	
	/**
	 * Factory method for creating textfields
	 */
	public final static TextField createTextField(String label, String text, int maxSize, int constraints, Form form, Style style)
	{
		//#if not polish.key.ClearKey:defined
		constraints |= TextField.NON_PREDICTIVE;
		//#endif
		
		//create inputmode item
		InputModeTextFieldItem imtfi;
		if(null != style)
			imtfi = new InputModeTextFieldItem(label, text, maxSize, constraints, style);
		else
			//#style ui_factory_textfield
			imtfi = new InputModeTextFieldItem(label, text, maxSize, constraints);
		
		if(null != form)
			form.append(imtfi);
		
		//fetch and return TextField
		return imtfi.getTextField();
	}
	
	//#if polish.blackberry
	public final static InputModeTextFieldItem createInputModeTextFieldItem(String label, String text, int maxSize, int constraints, Form form)
	{
		return createInputModeTextFieldItem(label, text, maxSize, constraints, form, null);
	}
	
	/**
	 * Factory method for creating InputModeTextFieldItem
	 */
	public final static InputModeTextFieldItem createInputModeTextFieldItem(String label, String text, int maxSize, int constraints, Form form, Style style)
	{
		//#if not polish.key.ClearKey:defined
		constraints |= TextField.NON_PREDICTIVE;
		//#endif
		
		//create inputmode item
		InputModeTextFieldItem imtfi;
		if(null != style)
			imtfi = new InputModeTextFieldItem(label, text, maxSize, constraints, style);
		else
			//#style ui_factory_textfield
			imtfi = new InputModeTextFieldItem(label, text, maxSize, constraints);
		
		if(null != form)
			form.append(imtfi);
		
		//fetch and return TextField
		return imtfi;
	}
	//#endif
	
	/**
	 * Factory method for creating datefields
	 */
	public final static DateField createDateField(String label, Date d, int mode, Form form)
	{
		return createDateField(label, d, mode, form, null);
	}
	
	/**
	 * Factory method for creating datefields
	 */
	public final static DateField createDateField(String label, Date d, int mode, Form form, Style style)
	{
		//#style ui_factory_textfield_wrap
		Container c = new Container(false);
		c.setLabel(label);
		
		//use parameter style or fall back to default
		DateField df;
		if(null != style)
			df = new DateField(null,mode,style);
		else
			//#style ui_factory_textfield
			df = new DateField(null,mode);
		
		//remove clear command
		df.removeCommand(TextField.CLEAR_CMD);
		
		//use existing date if any
		if(null != d)
			df.setDate(d);
		
		c.add(df);
		
		if(null != form)
			form.append(c);
		
		return df;
	}
	
	/**
	 * Factory method for creating choice group
	 */
	public final static ChoiceGroup createChoiceGroup(String label, int choiceType, ChoiceItem[] choiceItems, int selectedIndex, boolean isSelected, ItemStateListener listener, Form form)
	{
		return createChoiceGroup(label, choiceType, choiceItems, selectedIndex, isSelected, listener, form, null);
	}
	
	/**
	 * Factory method for creating choice group
	 */
	public final static ChoiceGroup createChoiceGroup(String label, int choiceType, ChoiceItem[] choiceItems, int selectedIndex, boolean isSelected, ItemStateListener listener, Form form, Style style)
	{
		//use parameter style or fall back to default
		ChoiceGroup cg = null;
		if(null != style)
			cg = new ChoiceGroup(label, choiceType, choiceItems, style);
		else
			//#style ui_factory_choicegroup
			cg = new ChoiceGroup(label, choiceType, choiceItems);
		
		if(selectedIndex >= 0)
			cg.setSelectedIndex(selectedIndex, isSelected);
		
		if(null != listener)
			cg.setItemStateListener(listener);
		
		if(null != form)
			form.append(cg);
		
		return cg;
	}
	
	/**
	 * Factory method for creating ChoiceItem of type 'radio button'
	 */
	public final static ChoiceItem createChoiceRadioItem(String text, int choiceType)
	{
		return createChoiceRadioItem(text,choiceType,null);
	}
	
	/**
	 * Factory method for creating ChoiceItem of type 'radio button'
	 */
	public final static ChoiceItem createChoiceRadioItem(String text, int choiceType, Style style)
	{
		ChoiceItem ci = null;
		if(null != style)
			ci = new ChoiceItem(text,null,choiceType, style);
		else
		{
			if(choiceType == Choice.EXCLUSIVE)
			{
				//#style .ui_factory_radio_item
				ci = new ChoiceItem(text,null,choiceType);
			}
			else
			{
				//#style .ui_factory_checkbox_item
				ci = new ChoiceItem(text,null,choiceType);
			}
		}
		
		return ci;
	}
	
	/**
	 * Factory method for creating ChoiceItem of type 'checkbox'
	 */
	public final static ChoiceItem createChoiceCheckBoxItem(String text, int choiceType)
	{
		return createChoiceCheckBoxItem(text, choiceType,null);
	}
	
	/**
	 * Factory method for creating ChoiceItem of type 'checkbox'
	 */
	public final static ChoiceItem createChoiceCheckBoxItem(String text, int choiceType, Style style)
	{
		if(null != style)
			return createChoiceRadioItem(text,choiceType,style);
		else
			//#style .ui_factory_checkbox_item
			return createChoiceRadioItem(text,choiceType);
	}
	
	/**
	 * Factory method for creating a interactive button item
	 */
	public final static StringItem createButtonItem(String label, String text, Command cmd, ItemCommandListener listener, Form form)
	{
		return createButtonItem(label, text, cmd, listener, form, null);
	}
	
	/**
	 * Factory method for creating a interactive button item
	 */
	public final static StringItem createButtonItem(String label, String text, Command cmd, ItemCommandListener listener, Form form, Style style)
	{
		StringItem si = null;
		
		if(null != style)
			si = new StringItem(label, text, style);
		else
			//#style ui_factory_button_item
			si = new StringItem(label, text);

		si.setAppearanceMode(Item.INTERACTIVE);
		
		if(null != cmd)
		{
			//#if polish.blackberry
				si.setDefaultCommand(cmd);
			//#else
				si.addCommand(cmd);
			//#endif
		}
		
		if(null != listener)
			si.setItemCommandListener(listener);
		
		if(null != form)
			form.append(si);
		
		return si;
	}
	
	/**
	 * Factory method for creating a rich UI representation of a user.
	 */
	public final static ContactSummarizedItem createUserProfileItem(ProfileSummary ps, Form form)
	{
		return createUserProfileItem(ps, ContactSummarizedItem.NETWORK_ICON_MODE_ALL, ContactSummarizedItem.PRESENCE_MODE_ALL_STATES, form, null);
	}
	
	/**
	 * Factory method for creating a rich UI representation of a user.
	 */
	public final static ContactSummarizedItem createUserProfileItem(ProfileSummary ps, Form form, Style style)
	{
		return createUserProfileItem(ps, ContactSummarizedItem.NETWORK_ICON_MODE_ALL, ContactSummarizedItem.PRESENCE_MODE_ALL_STATES, form, style);
	}
	
	/**
	 * Factory method for creating a rich UI representation of a user.
	 */
	public final static ContactSummarizedItem createUserProfileItem(ProfileSummary ps, int networkIconMode, Form form)
	{
		return createUserProfileItem(ps, networkIconMode, ContactSummarizedItem.PRESENCE_MODE_ALL_STATES, form, null);
	}
	
	/**
	 * Factory method for creating a rich UI representation of a user.
	 */
	public final static ContactSummarizedItem createUserProfileItem(ProfileSummary ps, int networkIconMode, Form form, Style style)
	{
		return createUserProfileItem(ps, networkIconMode, ContactSummarizedItem.PRESENCE_MODE_ALL_STATES, form, style);
	}
	
	/**
	 * Factory method for creating a rich UI representation of a user.
	 */
	public final static ContactSummarizedItem createUserProfileItem(ProfileSummary ps, int networkIconMode, int presenceMode, Form form)
	{
		return createUserProfileItem(ps, networkIconMode, presenceMode, form, null);
	}
	
	/**
	 * Factory method for creating a rich UI representation of a user.
	 */
	public final static ContactSummarizedItem createUserProfileItem(ProfileSummary ps, int networkIconMode, int presenceMode, Form form, Style style)
	{
		ContactSummarizedItem csi = null;
		
		if(null != style)
			csi = new ContactSummarizedItem(ps, networkIconMode, presenceMode, style);
		else
			//#style ui_factory_profile_item
			csi = new ContactSummarizedItem(ps, networkIconMode, presenceMode);

		if(null != form)
			form.append(csi);
		
		return csi;
	}
	
	/**
	 * Factory method for creating a CommandItem.
	 */
	public final static CommandItem createCascadeItem(String text, Command cmd, Item parent, ItemCommandListener listener, Form form)
	{
		return createCascadeItem(text, cmd, parent, listener, form, null);
	}
	
	/**
	 * Factory method for creating a CommandItem.
	 */
	public final static CommandItem createCascadeItem(String text, Command cmd, Item parent, ItemCommandListener listener, Form form, Style style)
	{
		CommandItem ci = null;
		
		if(null != style)
			ci = new CascadeItem(cmd, parent, style);
		else
			//#style ui_factory_commanditem
			ci = new CascadeItem(cmd, parent);
		
		if(null != parent && parent instanceof CommandItem)
			((CommandItem)parent).addChild(ci);
			
		if(null != text && !text.equalsIgnoreCase(""))
			ci.setText(text);
		
		if(null != listener)
			ci.setItemCommandListener(listener);
		
		if(null != form)
			form.append(ci);
		
		return ci;
	}
	
	/**
	 * Factory method for creating a StringItem.
	 */
	public final static StringItem createStringItem(String label, String text, Command cmd, ItemCommandListener listener, Form form)
	{
		return createStringItem(label, text, cmd, listener, form, null);
	}
	
	/**
	 * Factory method for creating a StringItem.
	 */
	public final static StringItem createStringItem(String label, String text, Command cmd, ItemCommandListener listener, Form form, Style style)
	{
		StringItem si = null;
		
		if(null != style)
			si = new StringItem(label, text, style);
		else
			//#style ui_factory_stringitem
			si = new StringItem(label, text);
			
		if(null != cmd)
		{
			//#if polish.blackberry
				si.setDefaultCommand(cmd);
			//#else
				si.addCommand(cmd);
			//#endif
		}
		
		if(null != listener)
			si.setItemCommandListener(listener);
		
		if(null != form)
			form.append(si);
		
		return si;
	}
	
	/**
	 * Creates a network icon matching the parameter id.
	 * <p>
	 * The resulting icon will have both a focused and defocused state.
	 * 
	 * @param networkId
	 * @param isChecked true if the 'red' version of network image should overwrite the normal defoused version. 
	 * @return
	 */
	public static final IconItem createNetworkIcon(String networkId, boolean isChecked)
	{
		return createNetworkIcon(networkId, isChecked, null);
	}
	
	/**
	 * Creates a network icon matching the parameter id.
	 * <p>
	 * The resulting icon will have both a focused and defocused state.
	 * 
	 * @param networkId
	 * @param isChecked true if the 'red' version of network image should overwrite the normal defoused version. 
	 * @param style style to be used for this item
	 * @return
	 */
	public static final IconItem createNetworkIcon(String networkId, boolean isChecked, Style style)
	{
		networkId = ExternalNetwork.getStandardId(networkId);
		
		Style s = createNetworkStyle(networkId , isChecked, style);
		
		IconItem nwi = null;
		
		if(null != s)
			//create icon
			nwi = new IconItem(null,null, s);
		
		return nwi;
	}
	
	/**
	 * Creates a Style holding image references matching the parameter network id.
	 * <p>
	 * The resulting style will have both a focused and defocused state.
	 * 
	 * @param networkId
	 * @param isChecked true if the 'red' version of network image should overwrite the normal defoused version. 
	 * @return
	 */
	public static final Style createNetworkStyle(String networkId, boolean isChecked)
	{
		return createNetworkStyle(networkId, isChecked, null);
	}
	
	/**
	 * Creates a Style holding image references matching the parameter network id.
	 * <p>
	 * The resulting style will have both a focused and defocused state.
	 * 
	 * @param networkId
	 * @param isChecked true if the 'red' version of network image should overwrite the normal defoused version. 
	 * @param style style to be used for this item
	 * @return
	 */
	public static final Style createNetworkStyle(String networkId, boolean isChecked, Style style)
	{
		networkId = ExternalNetwork.getStandardId(networkId);
		
		//these values are found in css-attributes.xml in Polish build branch
		//TODO: Can possibly be fetches via preprocessing, ask Andre about this
		final int ICONIMAGE_KEY = 6;
		final int FOCUSSTYLE_KEY = 1;
		
		Style styleUnfocused = null, styleFocused = null;
		
		//#mdebug debug
		try 
		{
		//#enddebug
		
		if(null != networkId)
		{
			String unfocusedNetworkUrl = null;
				
			if(!isChecked)
				unfocusedNetworkUrl = getNetworkImageUrl(networkId, false, false);
			else
				unfocusedNetworkUrl = getNetworkImageUrl(networkId, false, true);
			
			if(null == unfocusedNetworkUrl)
				return null;
				
			if(null != style)
			{
				styleUnfocused = style.clone(true);
				styleFocused = ((Style)style.getObjectProperty(FOCUSSTYLE_KEY)).clone(true);
			}
			else
			{
				styleUnfocused = new Style(
						null, Item.LAYOUT_SHRINK | Item.LAYOUT_VCENTER,
						null, null, 
						null, null 
				);
				styleFocused = new Style(
						null, Item.LAYOUT_SHRINK | Item.LAYOUT_VCENTER,
						null, null, 
						null, null 
				);
			}
		
			//define style(s) for icon
			styleFocused.addAttribute(ICONIMAGE_KEY, getNetworkImageUrl(networkId, true, false));
			styleUnfocused.addAttribute(ICONIMAGE_KEY, unfocusedNetworkUrl );
			styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
			
			return styleUnfocused;
		}
		
		//#mdebug debug 
		} 
		catch (Throwable t)
		{
			System.out.println("Error while creating network icon style: "+t.getMessage());
		}
		//#enddebug
		
		return styleUnfocused;
	}
	
	/**
	 * Retrieves a network image url matching the parameter
	 * 
	 * @param networkId string id of the desired network
	 * @param isFocused true if focused version of network image should be return. False if defocused should be returned.
	 * @param isChecked true if the 'red' version of network image should be returned. Overwrites 'isFocused' parameter.
	 * @return the desired network image if found, otherwise default network image
	 */
	private final static String getNetworkImageUrl(String networkId, boolean isFocused, boolean isChecked)
	{
		networkId = ExternalNetwork.getStandardId(networkId);
		
		String url = null;
		
		if(null != networkId)
		{	
			/*if (networkId.equals("bebo"))
			{
				if(isChecked)
					url = "/bebo_15x15_checked.png";
				else
					if(isFocused)
						url = "/bebo_15x15_highlight.png";
					else
						url = "/bebo_15x15.png";
			}
			else*/
			if (ExternalNetwork.FACEBOOK.equals(networkId))
			{
				if(isChecked)
					url = "/facebook_15x15_checked.png";
				else
					if(isFocused)
						url = "/facebook_15x15_highlight.png";
					else
						url = "/facebook_15x15.png";
			}
			else
			/*if ("flickr".equals(networkId))
			{
				if(isChecked)
					url = "/flickr_15x15_checked.png";
				else
					if(isFocused)
						url = "/flickr_15x15_highlight.png";
					else
						url = "/flickr_15x15.png";
			}
			else*/
			if (ExternalNetwork.GOOGLE.equals(networkId))
			{
				if(isChecked)
					url = "/google_15x15_checked.png";
				else
					if(isFocused)
						url = "/google_15x15_highlight.png";
					else
						url = "/google_15x15.png";
			}
			else
			/*if (networkId.equals("hi5.com") || networkId.equals("hi5"))
			{
				if(isChecked)
					url = "/hi5_15x15_checked.png";
				else
					if(isFocused)
						url = "/hi5_15x15_highlight.png";
					else
						url = "/hi5_15x15.png";
			}
			else*/
			if (ExternalNetwork.HYVES.equals(networkId))
			{
				if(isChecked)
					url = "/hyves_15x15_checked.png";
				else
					if(isFocused)
						url = "/hyves_15x15_highlight.png";
					else
						url = "/hyves_15x15.png";
			}
			else
			/*if (networkId.equals("icq"))
			{
				if(isChecked)
					url = "/icq_15x15_checked.png";
				else
					if(isFocused)
						url = "/icq_15x15_highlight.png";
					else
						url = "/icq_15x15.png";
			}
			else
			if (networkId.equals("linkedin"))
			{
				if(isChecked)
					url = "/linkedin_15x15_checked.png";
				else
					if(isFocused)
						url = "/linkedin_15x15_highlight.png";
					else
						url = "/linkedin_15x15.png";
			}
			else
			if (networkId.equals("myspace.com") || networkId.equals("myspace"))
			{
				if(isChecked)
					url = "/myspace_15x15_checked.png";
				else
					if(isFocused)
						url = "/myspace_15x15_highlight.png";
					else
						url = "/myspace_15x15.png";
			}
			else
			if (networkId.equals("skype"))
			{
				if(isChecked)
					url = "/skype_15x15_checked.png";
				else
					if(isFocused)
						url = "/skype_15x15_highlight.png";
					else
						url = "/skype_15x15.png";
			}
			else*/
			if (ExternalNetwork.TWITTER.equals(networkId))
			{
				if(isChecked)
					url = "/twitter_15x15_checked.png";
				else
					if(isFocused)
						url = "/twitter_15x15_highlight.png";
					else
						url = "/twitter_15x15.png";
			}
			else
			/*if (networkId.equals("zing"))
			{
				if(isChecked)
					url = "/zing_15x15_checked.png";
				else
					if(isFocused)
						url = "/zing_15x15_highlight.png";
					else
						url = "/zing_15x15.png";
			}
			else*/
			if (networkId.equals("yahoo.com") || networkId.equals("yahoo"))
			{
				if(isChecked)
					url = "/yahoo_15x15_checked.png";
				else
					if(isFocused)
						url = "/yahoo_15x15_highlight.png";
					else
						url = "/yahoo_15x15.png";
			}
			else
			if (ExternalNetwork.VODAFONE_360.equals(networkId))
			{
				if(isChecked)
					url = "/vodafone_15x15_checked.png";
				else
					if(isFocused)
						url = "/vodafone_15x15_highlight.png";
					else
						url = "/vodafone_15x15.png";
			}
			else
			if (ExternalNetwork.WINDOWS_LIVE.equals(networkId))
			{
				if(isChecked)
					url = "/win_15x15_checked.png";
				else
					if(isFocused)
						url = "/win_15x15_highlight.png";
					else
						url = "/win_15x15.png";
			}
			else
			{
				//#ifdef testversion:defined
				System.out.println("Can't find image for " + networkId + ", use default.");
				//#endif

				if(isChecked)
					url = "/default_15x15_checked.png";
				else
					if(isFocused)
						url = "/default_15x15_highlight.png";
					else
						url = "/default_15x15.png";
			}	
		}
		
		return url;
	}
	
	public static final IconItem createFilterNetworkIcon(final String filterName, final int filterType, final String filterIconId)
	{
		return createFilterNetworkIcon(filterName, filterType, filterIconId, null);
	}
	
	/**
	 * Creates a network icon matching the parameter id.
	 * <p>
	 * Used in the PeoplePage 'filterbar'
	 *  
	 * @param filterIconId
	 * @return
	 */
	public static final IconSwapItem createFilterNetworkIcon(final String filterName, final int filterType, final String filterIconId, Style style)
	{
		//these values are found in css-attributes.xml in Polish build branch
		//TODO: Can possibly be fetches via preprocessing, ask Andre about this
		int ICONIMAGE_KEY = 6;
		int FOCUSSTYLE_KEY = 1;
		
		Style styleUnfocused, styleFocused;
		
		if(null != style)
		{
			styleUnfocused = style.clone(true);
			styleFocused = ((Style)style.getObjectProperty(FOCUSSTYLE_KEY)).clone(true);
		}
		else
		{
			styleUnfocused = new Style(
					null, Item.LAYOUT_SHRINK | Item.LAYOUT_VCENTER,
					null, null, 
					null, null 
			);
			styleFocused = new Style(
					null, Item.LAYOUT_SHRINK | Item.LAYOUT_VCENTER,
					null, null, 
					null, null 
			);
		}
		
		IconSwapItem icon = null;
		
		if( filterType > 0 && filterType <= 8)
		{
			switch(filterType)
			{
				case Filter.TYPE_ALL : 	
					
					//#if polish.blackberry
						styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_all_highlight_28x28.png");
						styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_all_28x28.png");
					//#else
						styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_all_24x24_focus.png");
						styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_all_24x24_oof.png");
					//#endif
					styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
					
					break;
					
				case Filter.TYPE_ONLINE:
					
					//#if polish.blackberry
						styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_online_highlight_28x28.png");
						styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_online_28x28.png");
					//#else
						styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_online_24x24_focus.png");
						styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_online_24x24_oof.png");
					//#endif
					styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
				
					break;
					
				case Filter.TYPE_CONNECTED: 	
					
					//#if polish.blackberry
						styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_connectedfriends_highlight_28x28.png");
						styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_connectedfriends_28x28.png");
					//#else
						styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_nowplus_24x24_focus.png");
						styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_nowplus_24x24_oof.png");
					//#endif
					styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
					
					break;
					
				case Filter.TYPE_NATIVE_PHONEBOOK:
					
					//#if polish.blackberry
						styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_phonebook_highlight_28x28.png");
						styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_phonebook_28x28.png");
					//#else
						styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_phonebook_24x24_focus.png");
						styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_phonebook_24x24_oof.png");
					//#endif
					styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
					
					break;
				
				case Filter.TYPE_STANDARD_GROUP: 	
					
					//#if polish.blackberry
						styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_family_highlight_28x28.png");
						styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_family_28x28.png");
					//#else
						styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_family_24x24_focus.png");
						styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_family_24x24_oof.png");
					//#endif
					styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
				
					break;
					
				case Filter.TYPE_CUSTOM_GROUP: 		
					
					//#if polish.blackberry
						styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_usergeneratedgroups_highlight_28x28.png");
						styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_usergeneratedgroups_28x28.png");
					//#else
						styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_user_generated_groups_24x24_focus.png");
						styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_user_generated_groups_24x24_oof.png");
					//#endif
					styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
				
					break;
					
				case Filter.TYPE_SOCIAL_NETWORK_GROUP:
					
					if(null != filterIconId)
					{
						if (ExternalNetwork.FACEBOOK.equals(filterIconId))
						{
							//#if polish.blackberry
							styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_facebook_highlight_28x28.png");
							styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_facebook_28x28.png");
							//#else
							styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_facebook_24x24_focus.png");
							styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_facebook_24x24_oof.png");
							//#endif
							styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
						}
						else
						if (ExternalNetwork.GOOGLE.equals(filterIconId))
						{
							//#if polish.blackberry
							styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_google_highlight_28x28.png");
							styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_google_28x28.png");
							//#else
							styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_google_24x24_focus.png");
							styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_google_24x24_oof.png");
							//#endif
							styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
						}
						else
						if (ExternalNetwork.TWITTER.equals(filterIconId))
						{	
							//#if polish.blackberry
							styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_twitter_highlight_28x28.png");
							styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_twitter_28x28.png");
							//#else
							styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_twitter_24x24_focus.png");
							styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_twitter_24x24_oof.png");				
							//#endif
							styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
						}			
						else
						if (ExternalNetwork.VODAFONE_360.equals(filterIconId))
						{
							//#if polish.blackberry
							styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_vodafone_highlight_28x28.png");
							styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_vodafone_28x28.png");
							//#else
							styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_nowplus_24x24_focus.png");
							styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_nowplus_24x24_oof.png");
							//#endif
							styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
						}
						else
						if (ExternalNetwork.WINDOWS_LIVE.equals(filterIconId))
						{	
							//#if polish.blackberry
							styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_windows_highlight_28x28.png");
							styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_windows_28x28.png");
							//#else
							styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_win_24x24_focus.png");
							styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_win_24x24_oof.png");
							//#endif
							styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
						}
						else
						if (ExternalNetwork.HYVES.equals(filterIconId))
						{	
							//#if polish.blackberry
							styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_hyves_highlight_28x28.png");
							styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_hyves_28x28.png");
							//#else
							styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_hyves_24x24_focus.png");
							styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_hyves_24x24_oof.png");
							//#endif
							styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);
						}
					}
					
					break;
					
				default:
					
					//#debug warning
					System.out.println("No icon found for network " + filterIconId);

					//#if polish.blackberry
					styleFocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_default_highlight_28x28.png");
					styleUnfocused.addAttribute(ICONIMAGE_KEY, "/bb_contacts_filter_default_28x28.png");
					//#else
					styleFocused.addAttribute(ICONIMAGE_KEY, "/p_filter_default_24x24_focus.png");
					styleUnfocused.addAttribute(ICONIMAGE_KEY, "/p_filter_default_24x24_oof.png");
					//#endif
					styleUnfocused.addAttribute(FOCUSSTYLE_KEY, styleFocused);	
					
					break;
			}

			icon = new IconSwapItem(filterName,null);
			icon.setStyle(styleUnfocused);
		}
		
		return icon;
	}
	
	/**
	 * Creates a presence icon matching the parameter id.
	 * 
	 * @param presence
	 * @return
	 */
	public final static IconItem createPresenceIcon(int presence)
	{
		return createPresenceIcon(presence, null);
	}
	
	/**
	 * Creates a presence icon matching the parameter id.
	 * 
	 * @param presence
	 * @param style
	 * @return
	 */
	public final static IconItem createPresenceIcon(int presence, Style style)
	{
		Style s = createPresenceIconStyle(presence, style);
		
		IconItem presenceIcon = null;
		
		if(null != s)
			//create icon
			presenceIcon = new IconItem(null,null, s);
		
		return presenceIcon;
	}
	
	/**
	 * Creates a Style holding image references matching the parameter precense mode.
	 * <p>
	 * The resulting style will have both a focused and defocused state.
	 * 
	 * @param presence
	 * @return
	 */
	public static final Style createPresenceIconStyle(int presence)
	{
		return createPresenceIconStyle(presence, null);
	}
	
	/**
	 * Creates a Style holding image references matching the parameter presence mode.
	 * 
	 * @param presence
	 * @param style style to be used for this item
	 * @return
	 */
	public static final Style createPresenceIconStyle(int presence, Style style)
	{
		//these values are found in css-attributes.xml in Polish build branch
		//TODO: Can possibly be fetches via preprocessing, ask Andre about this
		final int ICONIMAGE_KEY = 6;
		
		Style presenceStyle = null;
		
		//#mdebug debug
		try 
		{
		//#enddebug
		
		if(presence == Channel.PRESENCE_UNKNOWN)
			return presenceStyle;
		else
		if(	presence == Channel.PRESENCE_ONLINE || 
			presence == Channel.PRESENCE_INVISIBLE ||
			presence == Channel.PRESENCE_OFFLINE ||
			presence == Channel.PRESENCE_IDLE
				)
		{
			if(null != style)
			{
				presenceStyle = style.clone(true);
			}
			else
			{
				presenceStyle = new Style(
						null, Item.LAYOUT_SHRINK | Item.LAYOUT_VCENTER,
						null, null, 
						null, null 
				);
			}
			
			if(Channel.PRESENCE_ONLINE == presence)
				presenceStyle.addAttribute(ICONIMAGE_KEY, "/precense_available_13x13.png");
			else
			if(Channel.PRESENCE_OFFLINE == presence)
				presenceStyle.addAttribute(ICONIMAGE_KEY, "/precense_offline_13x13.png");
			else
			if(Channel.PRESENCE_INVISIBLE == presence)
				presenceStyle.addAttribute(ICONIMAGE_KEY, "/precense_invisible_13x13.png");
			else
			if(Channel.PRESENCE_IDLE == presence)
				presenceStyle.addAttribute(ICONIMAGE_KEY, "/precense_idle_13x13.png");
		}
		
		//#mdebug debug 
		} 
		catch (Throwable t)
		{
			System.out.println("Error while creating presence icon style: "+t.getMessage());
		}
		//#enddebug
		
		return presenceStyle;
	}
	

	/**
	 * Sets the complete style for an item. This must be used if a style is to be
	 * set manually
	 * @param item the item
	 * @param style the style
	 */
	public static void setCompleteStyle(Item item, Style style)
	{
		Style newStyle;
		
		if(null != item)
		{
			if(item.isFocused && null != style)
			{
				Style focusedStyle = (Style)style.getObjectProperty("focused-style");
				if(null != focusedStyle)
					newStyle = focusedStyle;
				else
					newStyle = style;
			} 
			else
				newStyle = style;
			
			if(null != newStyle)
			{
				item.setStyle( newStyle );
				item.setAttribute("os", newStyle);
			}
		}
	}
	
	public static Container createProfilePageDetailContainer(String type)
	{
		final int BEFORE_KEY = 190;
		final int LABEL_STYLE_KEY = 3;
		
		Container con = null;
		
		if(null != type)
		{
			con = new Container(false);
			
			//#style profilepage_generic_content_container
			Style containerStyle = new Style();
			containerStyle = containerStyle.clone(true);
			Style labelStyle = ((Style)containerStyle.getObjectProperty(LABEL_STYLE_KEY)).clone(true);
			containerStyle.removeAttribute(LABEL_STYLE_KEY);
			containerStyle.addAttribute(LABEL_STYLE_KEY,labelStyle);

			if(type.equalsIgnoreCase("name"))
			{
				labelStyle.addAttribute(BEFORE_KEY, "/profile_details_name_15x15.png");
			}
			else
			if(type.equalsIgnoreCase("phone") || type.equalsIgnoreCase("phones") )
			{
				labelStyle.addAttribute(BEFORE_KEY, "/profile_details_call_15x15.png");
			}
			else
			if(type.equalsIgnoreCase("email"))
			{
				labelStyle.addAttribute(BEFORE_KEY, "/profile_details_email_15x15.png");	
			}
			else
			if(type.equalsIgnoreCase("im") || type.equalsIgnoreCase("instant messenger"))
			{
				labelStyle.addAttribute(BEFORE_KEY, "/profile_details_IM_15x15.png");	
			}
			else
			if(type.equalsIgnoreCase("adress"))
			{
				labelStyle.addAttribute(BEFORE_KEY, "/profile_details_adress_15x15.png");
			}
			else
			if(type.equalsIgnoreCase("birthday"))
			{
				labelStyle.addAttribute(BEFORE_KEY, "/profile_details_bday_15x15.png");
			}
			else
			if(type.equalsIgnoreCase("url"))
			{
				labelStyle.addAttribute(BEFORE_KEY, "/profile_details_url_15x15.png");
			}
			else
			if(type.equalsIgnoreCase("sn") || type.equalsIgnoreCase("social network"))
			{
				labelStyle.addAttribute(BEFORE_KEY, "/profile_details_network_15x15.png");
			}
			else				
			if(type.equalsIgnoreCase("note") || type.equalsIgnoreCase("notes"))
			{
				labelStyle.addAttribute(BEFORE_KEY, "/profile_details_note_15x15.png");
			}
			else
			if(type.equalsIgnoreCase("group") || type.equalsIgnoreCase("groups"))
			{
				labelStyle.addAttribute(BEFORE_KEY, "/profile_details_groups_15x15.png");
			}
			else
			if(type.equalsIgnoreCase("work"))
			{
				labelStyle.addAttribute(BEFORE_KEY, "/profile_details_info_15x15.png");
			}
			else
				return null;
			
			con.setStyle(containerStyle);
		}
		
		return con;
		
	}
	
//<Mobica
//#if using.native.textfield:defined &&  ${lowercase(polish.vendor)}==samsung 

/**
 * Modifies native editor on Samsung devices. Used to resolve native mode caption problems.
 * 
 * @param textField - instance of textfield for which underlying native editor should be modified
 */
public static void fixSamsungNativeTextField(TextField textField){
	int constraints = textField.getConstraints();
	textField.setInitialInputMode("MIDP_LOWERCASE_LATIN");
	if ((constraints & TextField.INITIAL_CAPS_NEVER) == TextField.INITIAL_CAPS_NEVER){
		textField.setConstraints(constraints);
	}
}	
//#endif

//#if using.native.textfield:defined && polish.device.textField.requires.initialCapsNeverFix:defined 
/**
 * Modifies native editor on Nokia S40 devices. Used to resolve native mode caption problems 
 * (TextField.INITIAL_CAPS_NEVER makes impossible to enter uppercase letters into native text inputs on s40)  
 * 
 * @param textField - instance of textfield for which underlying native editor should be modified
 */
public static void fixS40NativeTextField(TextField textField){
	int constraints = textField.getConstraints();
	
	//if INITIAL_CAPS_NEVER set
	if ((constraints & TextField.INITIAL_CAPS_NEVER) == TextField.INITIAL_CAPS_NEVER){ 
		//unset INITIAL_CAPS_NEVER
		textField.setConstraints( constraints & (~TextField.INITIAL_CAPS_NEVER));
	}
}	
//#endif
//Mobica>
}
