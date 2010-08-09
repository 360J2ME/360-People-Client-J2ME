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

import java.util.Date;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Address;
import com.zyb.nowplus.business.domain.Group;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.InvalidValueException;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;
import com.zyb.util.TextUtilities;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Choice;
import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.ChoiceItem;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.DateField;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextField;
import de.enough.polish.util.ArrayList;
import de.enough.polish.util.Locale;

//#if polish.blackberry
import java.util.Calendar;
import com.zyb.util.DateHelper;
import net.rim.device.api.ui.Keypad;
//#endif

/**
 * Common form for editing all types of contact details.
 * <p>
 * Layout and input handling adapts to the desired edit type, therefore
 * only one class is needed for all edit modes.
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class EditDetailForm extends BaseFramedForm
{
	/**
	 * Commands
	 */
	private static final Command cmdSave = new Command(Locale.get("nowplus.client.java.edit.contact.command.save"),Command.SCREEN,0);

	private static final Object KEY_GROUP = "K";
	
	/**
	 * Title item displaying both screen name and current time
	 */
	protected TitleBarItem titleitem;
	
	/**
	 * item for displaying which edit mode is being used
	 */
	protected StringItem modeItem;
	
	private ArrayList textFields, choiceGroups, dateFields;
	private Event event;
	
	public EditDetailForm(final Model model, final Controller controller, Event event) 
	{
		this(model,controller,event,null);
	}
	
	public EditDetailForm(final Model model, final Controller controller, Event event, Style style) 
	{
		super(model,controller,null,style);
		
		this.event = event;
		
		//add mode item
		modeItem = getModeItem(event.getId());
		this.append(modeItem);
		
		//#if polish.blackberry
			modeItem.setAppearanceMode(Item.PLAIN);
		//#endif
		
		textFields = new ArrayList();
		choiceGroups = new ArrayList();
		dateFields = new ArrayList();
		
		//construct layout for this edit mode
		createEditMode(event.getId());
		
		//#if polish.blackberry
			  //#style ui_factory_button_item
			  StringItem saveButton = UiFactory.createButtonItem(null, cmdSave.getLabel(), (de.enough.polish.ui.Command) cmdSave, null, null);
			  append(saveButton);
		//#else
			this.addCommand(cmdSave);
			this.addCommand(cmdBack);
		//#endif
	}
	
	protected void createEditMode(final int mode)
	{
		Identity iden = null;
		boolean isPrimary;
		ChoiceItem[] preferred = null;
		ChoiceItem[] subtypes = null;
		int selectedIndex, sub;
		TextField t1,t2,t3,t4,t5;
		
		switch(mode)
		{
			case Event.EditProfile.EDIT_NAME:
			case Event.EditProfile.NEW_NAME:
				
				String[] names = null;
				
				//use existing name if any
				if(null != event.getData())
					names = (String[]) event.getData();
				
				t1 = UiFactory.createTextField(null,null, 256, TextField.INITIAL_CAPS_WORD | TextField.ANY, this);
				t1.setHelpText(Locale.get("nowplus.client.java.edit.contact.name.first"));
				
				//#if polish.blackberry
					//#style ui_factory_textfield_helpfont
					t1.setHelpStyle();
				//#endif
				
		        //#if using.native.textfield:defined
					t1.setTitle(Locale.get("nowplus.client.java.edit.contact.name.first"));
				//#endif
				//#if using.native.textfield:defined && (${lowercase(polish.vendor)}==samsung)
					UiFactory.fixSamsungNativeTextField(t1);
				//#endif
				
				t2 = UiFactory.createTextField(null,null, 256, TextField.INITIAL_CAPS_WORD | TextField.ANY, this);
				t2.setHelpText(Locale.get("nowplus.client.java.edit.contact.name.middle"));
				
				//#if polish.blackberry
					//#style ui_factory_textfield_helpfont
					t2.setHelpStyle();
				//#endif
				
		        //#if using.native.textfield:defined
				t2.setTitle(Locale.get("nowplus.client.java.edit.contact.name.middle"));
					//#if (${lowercase(polish.vendor)}==samsung)
					UiFactory.fixSamsungNativeTextField(t2);
					//#endif
				//#endif
				
				t3 = UiFactory.createTextField(null,null, 256, TextField.INITIAL_CAPS_WORD | TextField.ANY, this);
				t3.setHelpText(Locale.get("nowplus.client.java.edit.contact.name.last"));
				
				//#if polish.blackberry
					//#style ui_factory_textfield_helpfont
					t3.setHelpStyle();
				//#endif
				
		        //#if using.native.textfield:defined
				t3.setTitle(Locale.get("nowplus.client.java.edit.contact.name.last"));
					//#if (${lowercase(polish.vendor)}==samsung)
				    UiFactory.fixSamsungNativeTextField(t3);
					//#endif
				//#endif
				
				
				//check for presence of name elements or set help text
				if(null != names[0] && !names[0].toString().trim().equalsIgnoreCase(""))
					t1.setString(names[0]);
				
				if(null != names[1] && !names[1].toString().trim().equalsIgnoreCase(""))
					t2.setString(names[1]);
				
				if(null != names[2] && !names[2].toString().trim().equalsIgnoreCase(""))
					t3.setString(names[2]);
				
				//create and add text fields
				this.textFields.add(t1);
				this.textFields.add(t2);
				this.textFields.add(t3);
				break;
				
			case Event.EditProfile.EDIT_PHONE:
			case Event.EditProfile.NEW_PHONE:
				
				if(null != event.getData())
					iden = (Identity) event.getData();
				
					t1 = UiFactory.createTextField(null,null, 256, TextField.PHONENUMBER, this);
					t1.setHelpText(Locale.get("nowplus.client.java.edit.contact.phone.number"));
					
					//#if polish.blackberry
						//#style ui_factory_textfield_helpfont
						t1.setHelpStyle();
					//#endif
					
			        //#if using.native.textfield:defined
					t1.setTitle(Locale.get("nowplus.client.java.edit.contact.phone.number"));
						//#if (${lowercase(polish.vendor)}==samsung)
						UiFactory.fixSamsungNativeTextField(t1);
						//#endif
					//#endif
				
				//set from existing phone identity if any or set help text
				if(null != iden)
				{
					isPrimary = iden.isPreferred();
					
					selectedIndex = 0;
					
					//map subtype to choicegroup index
					sub = iden.getSubtype();
					if(sub == Identity.SUBTYPE_HOME)
						selectedIndex = 0;
					else
					if(sub == Identity.SUBTYPE_MOBILE)
						selectedIndex = 1;
					else
					if(sub == Identity.SUBTYPE_WORK)
						selectedIndex = 2;
					else
					if(sub == Identity.SUBTYPE_FAX)
						selectedIndex = 3;
					else
					if(sub == Identity.SUBTYPE_OTHER)
						selectedIndex = 4;					
					//Cleaning invalid characters before editing
					t1.setString(TextUtilities.stripNonValidChars(TextUtilities.VALID_PHONE_NUMBER_CHARS, iden.getName()));
				}
				else
				{
					isPrimary = false;
					selectedIndex = 0;
				}
				
				//create and add text fields
				this.textFields.add(t1);
				
				//create and add 'preferred' checkbox
				preferred = new ChoiceItem[1];
				preferred[0] = UiFactory.createChoiceCheckBoxItem(Locale.get("nowplus.client.java.edit.contact.phone.preffered"),Choice.MULTIPLE);
				choiceGroups.add(
						UiFactory.createChoiceGroup(null, ChoiceGroup.MULTIPLE, preferred, 0, isPrimary, this, this)
						);
				
				//create and add subtype choicegroup
				subtypes = new ChoiceItem[]{
						UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.profilepage.subtype.home"),Choice.EXCLUSIVE),
						UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.profilepage.subtype.mobile"),Choice.EXCLUSIVE),
						UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.profilepage.subtype.work"),Choice.EXCLUSIVE),
						UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.profilepage.subtype.fax"),Choice.EXCLUSIVE),
						UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.profilepage.subtype.other"),Choice.EXCLUSIVE)
					};
				
				//set selected
				subtypes[selectedIndex].isSelected = true;
				
				choiceGroups.add(
						UiFactory.createChoiceGroup(Locale.get("nowplus.client.java.edit.contact.subtype.label"), ChoiceGroup.EXCLUSIVE, subtypes, selectedIndex, true, this,this)
						);
				
				break;
			
			case Event.EditProfile.EDIT_EMAIL:
			case Event.EditProfile.NEW_EMAIL:
				
				if(null != event.getData())
					iden = (Identity) event.getData();
				
					/* previously had the type TextField.EMAILADDR which cause more
					 * problems than solving it 
					 * see http://mantis.next-vodafone.com/mantis/view.php?id=4679
					 */
					t1 = UiFactory.createTextField(null,null, 256, TextField.ANY | TextField.INITIAL_CAPS_NEVER,this);
					t1.setHelpText(Locale.get("nowplus.client.java.edit.contact.email.address"));
					
					//#if polish.blackberry
						//#style ui_factory_textfield_helpfont
						t1.setHelpStyle();
					//#endif
						
			        //#if using.native.textfield:defined
					t1.setTitle(Locale.get("nowplus.client.java.edit.contact.email.address"));
						//#if (${lowercase(polish.vendor)}==samsung)
						UiFactory.fixSamsungNativeTextField(t1);
						//#endif
					//#endif
				
				//set from existing email identity if any or set help text
				if(null != iden)
				{
					isPrimary = iden.isPreferred();
					
					sub = iden.getSubtype();
					
					selectedIndex = 0;
					
					//map subtype to choicegroup index
					if(sub == Identity.SUBTYPE_HOME)
						selectedIndex = 0;
					else
					if(sub == Identity.SUBTYPE_WORK)
						selectedIndex = 1;
					else
					if(sub == Identity.SUBTYPE_OTHER)
						selectedIndex = 2;
					
					t1.setString(iden.getName());
				}
				else
				{
					isPrimary = false;
					selectedIndex = 0;
				}
				
				//create and add text fields
				this.textFields.add(t1);
				
				//create and add 'preferred' checkbox
				preferred = new ChoiceItem[]{
						UiFactory.createChoiceCheckBoxItem(Locale.get("nowplus.client.java.edit.contact.phone.preffered"),Choice.MULTIPLE)
						};
				choiceGroups.add(
						UiFactory.createChoiceGroup(null, ChoiceGroup.MULTIPLE, preferred, 0, isPrimary, this,this) 
						);
				
				//create and add subtype choicegroup
				subtypes = new ChoiceItem[]{
						UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.profilepage.subtype.home"),Choice.EXCLUSIVE),
						UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.profilepage.subtype.work"),Choice.EXCLUSIVE),
						UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.profilepage.subtype.other"),Choice.EXCLUSIVE)
						};
				
				//set selected
				subtypes[selectedIndex].isSelected = true;
				
				choiceGroups.add(
						UiFactory.createChoiceGroup(Locale.get("nowplus.client.java.edit.contact.subtype.label"), ChoiceGroup.EXCLUSIVE, subtypes, selectedIndex, true, this, this)
						);
				
				break;
				
			case Event.EditProfile.EDIT_ADRESS:
			case Event.EditProfile.NEW_ADRESS:
				
				Address address = null;
				
				if(null != event.getData())
					address = (Address) event.getData();
				
					t1 = UiFactory.createTextField(null,null, 256, TextField.INITIAL_CAPS_WORD | TextField.ANY,this);
					t1.setHelpText(Locale.get("nowplus.client.java.edit.contact.address.street"));
					
					//#if polish.blackberry
						//#style ui_factory_textfield_helpfont
						t1.setHelpStyle();
					//#endif
					
			        //#if using.native.textfield:defined
					t1.setTitle(Locale.get("nowplus.client.java.edit.contact.address.street"));
						//#if (${lowercase(polish.vendor)}==samsung)
						UiFactory.fixSamsungNativeTextField(t1);
						//#endif
					//#endif
						
						t2 = UiFactory.createTextField(null,null, 256, TextField.INITIAL_CAPS_WORD | TextField.ANY,this);
						t2.setHelpText(Locale.get("nowplus.client.java.edit.contact.address.region"));
						
						//#if polish.blackberry
							//#style ui_factory_textfield_helpfont
							t2.setHelpStyle();
						//#endif
							
				        //#if using.native.textfield:defined
						t2.setTitle(Locale.get("nowplus.client.java.edit.contact.address.region"));
							//#if (${lowercase(polish.vendor)}==samsung)
							UiFactory.fixSamsungNativeTextField(t2);
							//#endif
						//#endif
				
						
						t3 = UiFactory.createTextField(null,null, 256, TextField.INITIAL_CAPS_WORD | TextField.ANY,this);
						t3.setHelpText(Locale.get("nowplus.client.java.edit.contact.address.town"));
						
						//#if polish.blackberry
							//#style ui_factory_textfield_helpfont
							t3.setHelpStyle();
						//#endif
						
				        //#if using.native.textfield:defined
						t3.setTitle(Locale.get("nowplus.client.java.edit.contact.address.town"));
							//#if (${lowercase(polish.vendor)}==samsung)
							UiFactory.fixSamsungNativeTextField(t3);
							//#endif
						//#endif
				
						t4 = UiFactory.createTextField(null,null, 256, TextField.ANY,this);
						t4.setHelpText(Locale.get("nowplus.client.java.edit.contact.address.postcode"));
						
						//#if polish.blackberry
							//#style ui_factory_textfield_helpfont
							t4.setHelpStyle();
						//#endif
						
				        //#if using.native.textfield:defined
						t4.setTitle(Locale.get("nowplus.client.java.edit.contact.address.postcode"));
							//#if (${lowercase(polish.vendor)}==samsung)
							UiFactory.fixSamsungNativeTextField(t4);
							//#endif
						//#endif
						
						t5 = UiFactory.createTextField(null,null, 256, TextField.INITIAL_CAPS_WORD | TextField.ANY,this);
						t5.setHelpText(Locale.get("nowplus.client.java.edit.contact.address.country"));
						
						//#if polish.blackberry
							//#style ui_factory_textfield_helpfont
							t5.setHelpStyle();
						//#endif
						
				        //#if using.native.textfield:defined
						t5.setTitle(Locale.get("nowplus.client.java.edit.contact.address.country"));
							//#if (${lowercase(polish.vendor)}==samsung)
							UiFactory.fixSamsungNativeTextField(t5);
							//#endif
						//#endif
				
					
				
				//set address data or set help text
				if(null != address && address.getStreet() != null && !address.getStreet().toString().trim().equalsIgnoreCase(""))
					t1.setString(address.getStreet());
				
				if(null != address && address.getRegion() != null && !address.getRegion().toString().trim().equalsIgnoreCase(""))
					t2.setString(address.getRegion());
				
				if(null != address && address.getTown() != null && !address.getTown().toString().trim().equalsIgnoreCase(""))
					t3.setString(address.getTown());
				
				if(null != address && address.getPostcode() != null && !address.getPostcode().toString().trim().equalsIgnoreCase(""))
					t4.setString(address.getPostcode());
				
				if(null != address && address.getCountry() != null && !address.getCountry().toString().trim().equalsIgnoreCase(""))
					t5.setString(address.getCountry());
				
				//create and add text fields
				this.textFields.add(t1);
				this.textFields.add(t2);
				this.textFields.add(t3);
				this.textFields.add(t4);
				this.textFields.add(t5);		
				
				//set type index if possible
				if(null != address)
				{
					sub = address.getType();
					
					selectedIndex = 0;
					
					//map subtype to choicegroup index
					if(sub == Address.TYPE_HOME)
						selectedIndex = 0;
					else
					if(sub == Address.TYPE_WORK)
						selectedIndex = 1;
					else
					if(sub == Address.TYPE_OTHER)
						selectedIndex = 2;
				}
				else
					selectedIndex = 0;
				
				//create and add subtype choicegroup
				subtypes = new ChoiceItem[]{
						UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.profilepage.subtype.home"),Choice.EXCLUSIVE),
						UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.profilepage.subtype.work"),Choice.EXCLUSIVE),
						UiFactory.createChoiceRadioItem(Locale.get("nowplus.client.java.profilepage.subtype.other"),Choice.EXCLUSIVE)
						};
				
				//set selected
				subtypes[selectedIndex].isSelected = true;
				
				choiceGroups.add(
						UiFactory.createChoiceGroup(Locale.get("nowplus.client.java.edit.contact.subtype.label"), ChoiceGroup.EXCLUSIVE, subtypes, selectedIndex, true, this, this)
						);
				
				break;
				
			case Event.EditProfile.EDIT_GROUP:
			case Event.EditProfile.NEW_GROUP:
				
				Group[] groups = null;
				
				if(null != event.getData())
					groups = (Group[]) event.getData();
				
				//fetch all available groups
				Group[] allGroups = getModel().getAvailableGroups();
				
				//init choice items
				subtypes = new ChoiceItem[allGroups.length];
				
				int firstSelected = -1;
				
				for(int i = 0, length = allGroups.length; i < length; ++i)
				{
					//create choice group item
					subtypes[i] = UiFactory.createChoiceCheckBoxItem(allGroups[i].getName(), Choice.MULTIPLE);
					subtypes[i].setAttribute(KEY_GROUP, allGroups[i]);
					
					if(null != groups)
					{
						//check if choice group item should be selected
						for(int j = 0, leng = groups.length; j < leng; ++j)
						{
							if(allGroups[i].equals(groups[j]))
							{
								subtypes[i].isSelected = true;
								
								//init selection index
								if(firstSelected < 0)
									firstSelected = i;
								
								break;
							}
						}
					}
				}
				
				/*
				//set selection index to default if not already set
				if(firstSelected < 0)
					firstSelected = 1;
				*/

				//create groups choicegroup
				choiceGroups.add(
						UiFactory.createChoiceGroup(Locale.get("nowplus.client.java.edit.contact.group.label"), ChoiceGroup.MULTIPLE, subtypes, firstSelected, true, this, this)
						);
				
				break;
			case Event.EditProfile.EDIT_URL:
			case Event.EditProfile.NEW_URL:
				
				if(null != event.getData())
					iden = (Identity) event.getData();
				
					t1 = UiFactory.createTextField(null,null, 256, TextField.URL | TextField.INITIAL_CAPS_NEVER, this);
					t1.setHelpText(Locale.get("nowplus.client.java.edit.contact.url.address"));
					
					//#if polish.blackberry
						//#style ui_factory_textfield_helpfont
						t1.setHelpStyle();
					//#endif
					
			        //#if using.native.textfield:defined
					t1.setTitle(Locale.get("nowplus.client.java.edit.contact.url.address"));
						//#if (${lowercase(polish.vendor)}==samsung)
						UiFactory.fixSamsungNativeTextField(t1);
						//#endif
					//#endif
				
				//set from existing url identity if any or set help text
				if(null != iden)
					t1.setString(iden.getUrl());
				
				//create and add text fields
				this.textFields.add(t1);
				
				break;
				
			case Event.EditProfile.EDIT_BIRTHDAY:
			case Event.EditProfile.NEW_BIRTHDAY:
				
				Date bday = null;
				
				if(null != event.getData())
					bday = (Date) event.getData();
				
				//#if polish.blackberry
				if(null == event.getData()){
					bday = new Date();
				}
				//#endif
				
				//create and add date fields
				this.dateFields.add( 
						UiFactory.createDateField(null, bday, DateField.DATE, this)
						);
				
				break;
				
			case Event.EditProfile.EDIT_NOTE:
			case Event.EditProfile.NEW_NOTE:
				
				
					t1 = UiFactory.createTextField(null,null, 1024, TextField.INITIAL_CAPS_SENTENCE | TextField.ANY,this);
					
					//use existing note or set help text
					if(null != event.getData())
						t1.setString((String) event.getData());
					else
					{
						t1.setHelpText(Locale.get("nowplus.client.java.edit.contact.note.enter"));
						
						//#if polish.blackberry
							//#style ui_factory_textfield_helpfont
							t1.setHelpStyle();
						//#endif
					}
					
					 //#if using.native.textfield:defined
					t1.setTitle(Locale.get("nowplus.client.java.edit.contact.note.enter"));
						//#if (${lowercase(polish.vendor)}==samsung)
						UiFactory.fixSamsungNativeTextField(t1);
						//#endif
					//#endif
					
				//create and add text fields
				this.textFields.add(t1);
				
				break;
				
			case Event.EditProfile.EDIT_WORK:
			case Event.EditProfile.NEW_WORK:
				
				String[] work = null;
				
				if(null != event.getData())
					work = (String[]) event.getData();
				
				t1 = UiFactory.createTextField(null,null, 256, TextField.INITIAL_CAPS_WORD | TextField.ANY,this);
				t1.setHelpText(Locale.get("nowplus.client.java.profilepage.content.work.company"));
				
				//#if polish.blackberry
					//#style ui_factory_textfield_helpfont
					t1.setHelpStyle();
				//#endif
				
                //#if using.native.textfield:defined
				t1.setTitle(Locale.get("nowplus.client.java.profilepage.content.work.company"));
					//#if (${lowercase(polish.vendor)}==samsung)
					UiFactory.fixSamsungNativeTextField(t1);
					//#endif
				//#endif
					
				t2 = UiFactory.createTextField(null,null, 256, TextField.INITIAL_CAPS_WORD | TextField.ANY,this);
				t2.setHelpText(Locale.get("nowplus.client.java.profilepage.content.work.department"));
				
				//#if polish.blackberry
					//#style ui_factory_textfield_helpfont
					t2.setHelpStyle();
				//#endif
				
                //#if using.native.textfield:defined
				t2.setTitle(Locale.get("nowplus.client.java.profilepage.content.work.department"));
					//#if (${lowercase(polish.vendor)}==samsung)
					UiFactory.fixSamsungNativeTextField(t2);
					//#endif
				//#endif
					
				t3 = UiFactory.createTextField(null,null, 256, TextField.INITIAL_CAPS_WORD | TextField.ANY,this);
				t3.setHelpText(Locale.get("nowplus.client.java.profilepage.content.work.title"));
				
				//#if polish.blackberry
					//#style ui_factory_textfield_helpfont
					t3.setHelpStyle();
				//#endif
				
                //#if using.native.textfield:defined
				t3.setTitle(Locale.get("nowplus.client.java.profilepage.content.work.title"));
					//#if (${lowercase(polish.vendor)}==samsung)
					UiFactory.fixSamsungNativeTextField(t3);
					//#endif
				//#endif
				
				//set work data or set help text
				if(work != null && work[0] != null && !work[0].toString().trim().equalsIgnoreCase(""))
					t1.setString(work[0]);
				
				if(work != null && work[1] != null && !work[1].toString().trim().equalsIgnoreCase(""))
					t2.setString(work[1]);
				
				if(work != null && work[2] != null && !work[2].toString().trim().equalsIgnoreCase(""))
					t3.setString(work[2]);
				
				//create and add text fields
				this.textFields.add(t1);
				this.textFields.add(t2);
				this.textFields.add(t3);
				
				break;

			default:
				break;
		}
	}
	
	protected StringItem getModeItem(final int mode)
	{
		String s;
		switch(mode)
		{
			case Event.EditProfile.EDIT_NAME:
			case Event.EditProfile.NEW_NAME:
				s = Locale.get("nowplus.client.java.edit.contact.edit") + " " + Locale.get("nowplus.client.java.edit.contact.name");
				break;
			case Event.EditProfile.EDIT_PHONE:
			case Event.EditProfile.NEW_PHONE:
				s = Locale.get("nowplus.client.java.edit.contact.edit") + " " + Locale.get("nowplus.client.java.edit.contact.phone");
				break;
			case Event.EditProfile.EDIT_EMAIL:
			case Event.EditProfile.NEW_EMAIL:
				s = Locale.get("nowplus.client.java.edit.contact.edit") + " " + Locale.get("nowplus.client.java.edit.contact.email");
				break;
			case Event.EditProfile.EDIT_ADRESS:
			case Event.EditProfile.NEW_ADRESS:
				s = Locale.get("nowplus.client.java.edit.contact.edit") + " " + Locale.get("nowplus.client.java.edit.contact.address");
				break;
			case Event.EditProfile.EDIT_GROUP:
			case Event.EditProfile.NEW_GROUP:
				s = Locale.get("nowplus.client.java.edit.contact.edit") + " " + Locale.get("nowplus.client.java.edit.contact.group");
				break;
			case Event.EditProfile.EDIT_URL:
			case Event.EditProfile.NEW_URL:
				s = Locale.get("nowplus.client.java.edit.contact.edit") + " " + Locale.get("nowplus.client.java.edit.contact.url");
				break;
			case Event.EditProfile.EDIT_BIRTHDAY:
			case Event.EditProfile.NEW_BIRTHDAY:
				s = Locale.get("nowplus.client.java.edit.contact.edit") + " " + Locale.get("nowplus.client.java.edit.contact.birthday");
				break;
			case Event.EditProfile.EDIT_NOTE:
			case Event.EditProfile.NEW_NOTE:
				s = Locale.get("nowplus.client.java.edit.contact.edit") + " " + Locale.get("nowplus.client.java.edit.contact.note");
				break;
			case Event.EditProfile.EDIT_WORK:
			case Event.EditProfile.NEW_WORK:
				s = Locale.get("nowplus.client.java.edit.contact.edit") + " " + Locale.get("nowplus.client.java.edit.contact.work");
				break;
			default:
				s = "";
		}
		
		//#style editdetail_editbar
		return new StringItem(null,s);
	}

	public byte getContext() 
	{
		return -1;
	}
	
	public void commandAction(Command c, Displayable d)
	{
		//#debug debug
		System.out.println(c+"/"+d);

		if(c == cmdBack)
		{
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK_CHECKPOINT);			
		}
		else
		if(c == cmdSave)
		{
			
			//#debug debug
			System.out.println("cmdSave:"+event.getId());
			
			Identity iden = null;
			int subType, selectedIndex;
			boolean preferred;
			
			switch( event.getId() )
			{
				case Event.EditProfile.EDIT_NAME:
				case Event.EditProfile.NEW_NAME:
					
					String first = ((TextField)textFields.get(0)).getString();
					String middle = ((TextField)textFields.get(1)).getString();
					String last = ((TextField)textFields.get(2)).getString();
					
					//check that textfield strings are valid, else set to null
					if(null != first && first.toString().trim().equalsIgnoreCase(""))
						first = null;
					if(null != middle && middle.toString().trim().equalsIgnoreCase(""))
						middle = null;
					if(null != last && last.toString().trim().equalsIgnoreCase(""))
						last = null;
					
					if(first != null || middle != null || last != null)
					{
						//set new name
						String[] name = new String[]
						                           {
								first,
								middle,
								last
						                           };
						
						//pass event,  model takes care of validity evaluation
						event.setData(name);
					}
					else
					{
						//envoke missing name confirmation screen 
						Command ok = new Command(Locale.get("nowplus.client.java.edit.contact.missing.name.notify.ok"), Command.OK, 0);
						
						//#style notification_form_base
						NotificationForm nf = new NotificationForm(
								model, controller, 
								Locale.get("nowplus.client.java.edit.contact.missing.name.title"),
								Locale.get("nowplus.client.java.edit.contact.missing.name.text"),
								ok,
								-1
								);
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, nf);
						
						return;
					}

					break;
					
				case Event.EditProfile.EDIT_PHONE:
				case Event.EditProfile.NEW_PHONE:
					
					Identity oldPhone = (Identity) event.getData();
					
					//set number and subtype
					String num = ((TextField)this.textFields.get(0)).getString();
					
					//no validity check in UI!
//					if(null != num && !num.trim().equalsIgnoreCase(""))
//					{
						selectedIndex = ((ChoiceGroup)this.choiceGroups.get(1)).getSelectedIndex();
						
						subType = 0;
						
						//map choicegroup index to subtype
						if(selectedIndex == 0)
							subType = Identity.SUBTYPE_HOME;
						else
						if(selectedIndex == 1)
							subType = Identity.SUBTYPE_MOBILE;
						else
						if(selectedIndex == 2)
							subType = Identity.SUBTYPE_WORK;
						else
						if(selectedIndex == 3)
							subType = Identity.SUBTYPE_FAX;
						else
						if(selectedIndex == 4)
							subType = Identity.SUBTYPE_OTHER;
						
						//check preferred
						preferred = ((ChoiceGroup)this.choiceGroups.get(0)).isSelected(0);
						
						//create new number
						if (oldPhone == null)
						{
							iden = Identity.createPhoneNumber(subType, num, preferred);
						}
						else
						{
							iden = Identity.createPhoneNumber(subType, oldPhone.getNabSubtypes(), num, preferred, oldPhone.getSabDetailId());
						}
//					}

					//pass event,  model takes care of validity evaluation
					event.setData( new Identity[] {oldPhone, iden});
					
					break;
				
				case Event.EditProfile.EDIT_EMAIL:
				case Event.EditProfile.NEW_EMAIL:
					
					try
					{
						Identity oldEmail = (Identity) event.getData();
						
						//get mail
						String mail = ((TextField)this.textFields.get(0)).getString();
						
						//no validity check in UI!
//						if(null != mail && !mail.trim().equalsIgnoreCase(""))
//						{
							//get subtype
							selectedIndex = ((ChoiceGroup)this.choiceGroups.get(1)).getSelectedIndex();
							
							subType = 0;
							
							//map choicegroup index to subtype
							if(selectedIndex == 0)
								subType = Identity.SUBTYPE_HOME;
							else
							if(selectedIndex == 1)
								subType = Identity.SUBTYPE_WORK;
							else
							if(selectedIndex == 2)
								subType = Identity.SUBTYPE_OTHER;
							
							//check preferred
							preferred = ((ChoiceGroup)this.choiceGroups.get(0)).isSelected(0);
							
							//create new mail
							if (oldEmail == null)
							{
								iden = Identity.createEmail(subType, mail, preferred);
							}
							else
							{
								iden = Identity.createEmail(subType, oldEmail.getNabSubtypes(), mail, preferred, oldEmail.getSabDetailId());
							}
//						}
						
						//pass event,  model takes care of validity evaluation
						this.event.setData( new Identity[] {(Identity) event.getData(), iden} );
					}
					catch (InvalidValueException e)
					{
						//envoke invalid email notification screen 
						Command ok = new Command(Locale.get("polish.command.ok"), Command.OK, 0);
						
						//#style notification_form_base
						NotificationForm nf = new NotificationForm(
								getModel(), getController(), 
								Locale.get("nowplus.client.java.edit.contact.invalid.email.title"),
								Locale.get("nowplus.client.java.edit.contact.invalid.email.text"),
								ok,
								-1
								);
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, nf);
						
						return;
					}

					break;
					
				case Event.EditProfile.EDIT_ADRESS:
				case Event.EditProfile.NEW_ADRESS:
					
					Address oldAddress = (Address) event.getData();
					
					String street = ((TextField)textFields.get(0)).getString();
					String region = ((TextField)textFields.get(1)).getString();
					String town = ((TextField)textFields.get(2)).getString();
					String postcode = ((TextField)textFields.get(3)).getString();
					String country = ((TextField)textFields.get(4)).getString();
					
					//check that textfield strings are valid, else set to null
					if(null != street && street.toString().trim().equalsIgnoreCase(""))
						street = null;
					if(null != region && region.toString().trim().equalsIgnoreCase(""))
						region = null;
					if(null != town && town.toString().trim().equalsIgnoreCase(""))
						town = null;
					if(null != postcode && postcode.toString().trim().equalsIgnoreCase(""))
						postcode = null;
					if(null != country && country.toString().trim().equalsIgnoreCase(""))
						country = null;
					//get subtype
					selectedIndex = ((ChoiceGroup)this.choiceGroups.get(0)).getSelectedIndex();
					
					subType = 0;
					
					//map subtype to choicegroup index
					if(selectedIndex == 0)
						subType = Address.TYPE_HOME;
					else
					if(selectedIndex == 1)
						subType = Address.TYPE_WORK;
					else
					if(selectedIndex == 2)
						subType = Address.TYPE_OTHER;
					
					//create new address
					Address address = null;
					if (oldAddress == null)
					{
						address = Address.createAddress(subType, null,street, null, town, postcode, region, country);
					}
					else
					{
						address = Address.createAddress(subType, oldAddress.getNabSubtypes(), null, street, null, town, postcode, region, country, oldAddress.getSabDetailId());
					}
					
					//pass event,  model takes care of validity evaluation
					this.event.setData( new Address[] {oldAddress, address} );
					
					break;
					
				case Event.EditProfile.EDIT_GROUP:
				case Event.EditProfile.NEW_GROUP:
					
					ChoiceGroup cg = ((ChoiceGroup)this.choiceGroups.get(0));
					
					boolean[] selected = new boolean[cg.size()];
					
					cg.getSelectedFlags(selected);
					
					ArrayList al = new ArrayList();
					
					for(int i = 0, length = cg.size(); i < length; ++i)
					{
						//add item if selected in selection array
						if(selected[i])
							al.add( cg.getItem(i).getAttribute(KEY_GROUP));
					}
					
					//fetch new groups
					Group[] newGroups = new Group[0];
					if(al.size() > 0)
					{
						newGroups = new Group[al.size()];
						if(newGroups.length > 0)
							al.toArray(newGroups);
					}	
					
					//pass event,  model takes care of validity evaluation
					event.setData(newGroups);
					
					break;
					
				case Event.EditProfile.EDIT_URL:
				case Event.EditProfile.NEW_URL:
					
					String url = ((TextField)this.textFields.get(0)).getString();
					
					//no validity check in UI!
//					if(null != url && !url.toString().trim().equalsIgnoreCase(""))
//					{
						//set new url
						iden = Identity.createUrl(url);
//					}
					
					//pass event,  model takes care of validity evaluation
					event.setData( new Identity[] {(Identity) event.getData(), iden} );
					
					break;
					
				case Event.EditProfile.EDIT_BIRTHDAY:
				case Event.EditProfile.NEW_BIRTHDAY:
					
					Date newDate = ((DateField)this.dateFields.get(0)).getDate();
					
					//pass event,  model takes care of validity evaluation
					//#if polish.blackberry
					Calendar newCal = Calendar.getInstance();
					newCal.setTime(newDate);
					if(DateHelper.isToday(Calendar.getInstance(), newCal)){
						newDate = null;
					}
					//#endif
					event.setData(newDate);
					
					break;
					
				case Event.EditProfile.EDIT_NOTE:
				case Event.EditProfile.NEW_NOTE:
					
					//set new note
					String note = ((TextField)this.textFields.get(0)).getString();
					
					//check that textfield strings are valid
					if(null != note && note.toString().trim().equalsIgnoreCase(""))
						note = null;
					
					//pass event,  model takes care of validity evaluation
					event.setData( new String[]{note} );
					
					break;
					
				case Event.EditProfile.EDIT_WORK:
				case Event.EditProfile.NEW_WORK:
					
					//extract new work info
					String company = ((TextField)textFields.get(0)).getString();
					String department = ((TextField)textFields.get(1)).getString();
					String title = ((TextField)textFields.get(2)).getString();
					
					//check that textfield strings are valid, else set to null
					if(company != null && company.toString().trim().equalsIgnoreCase(""))
						company = null;
					if(department != null && department.toString().trim().equalsIgnoreCase(""))
						department = null;
					if(title != null && title.toString().trim().equalsIgnoreCase(""))
						title = null;

					String[] work = new String[]{company, department, title}; 
					
					//pass event,  model takes care of validity evaluation
					event.setData(work);

					break;
					
				default:
					break;
			}
			
			//#debug debug
			System.out.println("Passing event:"+event.toString());

			//pass new data to controller
			getController().notifyEvent(Event.Context.EDIT_PROFILE, Event.EditProfile.SAVE, event);
		}
		else
			super.commandAction(c, d);
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
		return new Item[]{ this.titleitem = new TitleBarItem( Locale.get("nowplus.client.java.edit.contact.edit"),getModel() ) };
	}

	protected Item createTopItem() 
	{
		return null;
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
			getCommandListener().commandAction(cmdBack, this);
		else
			super.keyPressed(keyCode);
	}
	protected boolean handleKeyReleased(int keyCode, int gameAction)
	{
		//prevent context menu shown, when radio choice group does not handle FIRE
		if(gameAction == FIRE){
			if(getCurrentItem() instanceof ChoiceGroup){
				ChoiceGroup group = (ChoiceGroup) getCurrentItem();
				if(group.size() > 0 && group.getSelectedIndex() != -1){
					ChoiceItem item = group.getItem(group.getSelectedIndex());
					if(item.isSelected && group.getFocusedIndex() == group.getSelectedIndex()){
						return true;
					}
				}
			}
		}
		return super.handleKeyReleased(keyCode, gameAction);
	}
	//#endif
}
