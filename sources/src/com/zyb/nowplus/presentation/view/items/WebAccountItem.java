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

import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.view.forms.WebAccountsForm;
import com.zyb.nowplus.presentation.view.items.AnimatedIconItem;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.util.ArrayList;
import de.enough.polish.util.Locale;

/**
 * Item for displaying a SN connection under 'web accounts' page.
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class WebAccountItem extends Container
{
	private final byte INDEX_NETWORK_ICON = 0;
	private final byte INDEX_NETWORK_NAME = 1;
	private final byte INDEX_USER_NAME = 2;
	private final byte INDEX_USER_CHECKMARK_PROGRESS = 3;
	
	protected ExternalNetwork nw;
	
	protected Identity iden;
	
	protected IconItem networkIcon;
	
	protected static IconItem dummy;
	
	protected IconItem checkMarkIcon;
	
	protected AnimatedIconItem processIcon;
	
	protected StringItem networkName, userName;
	
	public WebAccountItem(ExternalNetwork nw, Identity iden, boolean isAwaitingUpdate) 
	{
		//#style webaccount_item
		super(false);
		
		this.nw = nw;
		this.iden = iden;
		
		//insert dummy items
		networkIcon = new IconItem(null,null);
		networkIcon.setAppearanceMode(Item.PLAIN);
		this.add(networkIcon);
		
		networkName = new StringItem(null,null);
		networkName.setAppearanceMode(Item.PLAIN);
		this.add(networkName);
		
		userName = new StringItem(null,null);
		userName.setAppearanceMode(Item.PLAIN);
		this.add(userName);
		
		//#style webaccount_sn_item
		dummy = new IconItem(null,null);
		dummy.setAppearanceMode(Item.PLAIN);
		this.add(dummy);
		
		updateAccount(nw, iden, isAwaitingUpdate);
	}
	
	public ExternalNetwork getExternalNetwork()
	{
		return nw;
	}
	
	public void updateAccount(ExternalNetwork nw, Identity iden, boolean isAwaitingUpdate)
	{
		this.nw = nw;
		this.iden = iden;
		
		/*
		if(isAwaitingUpdate)
		{
			//create network icon
			networkIcon = UiFactory.createNetworkIcon(nw.getNetworkId(),false);
			this.set(INDEX_NETWORK_ICON, networkIcon);

			//#style webaccount_sn_item
			networkName = new StringItem(null,nw.getName());
			this.set(INDEX_NETWORK_NAME, networkName);
			
			//#style webaccount_sn_item
			userName = new StringItem(null,Locale.get("nowplus.client.java.webaccounts.item.updating"));
			this.set(INDEX_USER_NAME, userName);

			if(null == processIcon)
			{
				//#style webaccount_sn_item
				processIcon = new AnimatedIconItem(
						null, 
						new String[]{"/sync_01_now6_75_21x21.png","/sync_02_now6_75_21x21.png","/sync_03_now6_75_21x21.png","/sync_04_now6_75_21x21.png"},
						new String[]{"/sync_01_now6_0_21x21.png","/sync_02_now6_0_21x21.png","/sync_03_now6_0_21x21.png","/sync_04_now6_0_21x21.png"},
						0,
						null
				);
			}
			this.set(INDEX_USER_CHECKMARK_PROGRESS, processIcon);
			
			//NO COMMAND
		}
		else
		*/
		if(null != nw)
		{
			//make sure previous commands are cleared
			clearCommands();
			
			if(null != iden)
			{
				if(iden.isLoggedIn())
				{
					//create network icon
					networkIcon = UiFactory.createNetworkIcon(nw.getNetworkId(),false);
					this.set(INDEX_NETWORK_ICON, networkIcon);

					//#style webaccount_sn_item_online
					networkName = new StringItem(null,nw.getName());
					this.set(INDEX_NETWORK_NAME, networkName);

					//#style webaccount_sn_item_online
					userName = new StringItem(null,iden.getName());
					
					this.set(INDEX_USER_NAME, userName);

					if(null == checkMarkIcon)
					{
						//#style webaccount_checkmark
						checkMarkIcon = new IconItem(null, null);
					}
					this.set(INDEX_USER_CHECKMARK_PROGRESS, checkMarkIcon);
					
					//#if polish.blackberry
						this.setDefaultCommand(WebAccountsForm.cmdLogout);
					//#else
						this.addCommand(WebAccountsForm.cmdLogout);
					//#endif
				}
				else
				//only check if user has an account!
				if(nw.removingFromMyProfile())
				{
					//create network icon
					networkIcon = UiFactory.createNetworkIcon(nw.getNetworkId(),false);
					this.set(INDEX_NETWORK_ICON, networkIcon);

					//#style webaccount_sn_item
					networkName = new StringItem(null,nw.getName());
					this.set(INDEX_NETWORK_NAME, networkName);
					
					//#style webaccount_sn_item
					userName = new StringItem(null,Locale.get("nowplus.client.java.webaccounts.item.deleting"));
					this.set(INDEX_USER_NAME, userName);

					if(null == processIcon)
					{
						//#style webaccount_sn_item
						processIcon = new AnimatedIconItem(
								100,
								new String[]{"/sync_01_now6_75_21x21.png","/sync_02_now6_75_21x21.png","/sync_03_now6_75_21x21.png","/sync_04_now6_75_21x21.png"},
								new String[]{"/sync_01_now6_0_21x21.png","/sync_02_now6_0_21x21.png","/sync_03_now6_0_21x21.png","/sync_04_now6_0_21x21.png"}
								);
					}
					this.set(INDEX_USER_CHECKMARK_PROGRESS, processIcon);
					
					//NO COMMAND
				}
				else
				{
					//create network icon
					networkIcon = UiFactory.createNetworkIcon(nw.getNetworkId(),false);
					this.set(INDEX_NETWORK_ICON, networkIcon);

					//#style webaccount_sn_item
					networkName = new StringItem(null,nw.getName());
					this.set(INDEX_NETWORK_NAME, networkName);

					//#style webaccount_sn_item
					userName = new StringItem(null,iden.getName());
					this.set(INDEX_USER_NAME, userName);

					this.set(INDEX_USER_CHECKMARK_PROGRESS, dummy);
					
					//#if polish.blackberry
						this.setDefaultCommand(WebAccountsForm.cmdLogin);
					//#else
						this.addCommand(WebAccountsForm.cmdLogin);
					//#endif
				}
			}
			else
			//only check if user has no account!
			if(nw.addingToMyProfile())
			{
				//create network icon
				networkIcon = UiFactory.createNetworkIcon(nw.getNetworkId(),false);
				this.set(INDEX_NETWORK_ICON, networkIcon);

				//#style webaccount_sn_item
				networkName = new StringItem(null,nw.getName());
				this.set(INDEX_NETWORK_NAME, networkName);
				
				//#style webaccount_sn_item
				userName = new StringItem(null,Locale.get("nowplus.client.java.webaccounts.item.adding"));
				this.set(INDEX_USER_NAME, userName);

				if(null == processIcon)
				{
					//#style webaccount_sn_item
					processIcon = new AnimatedIconItem(
							100, 
							new String[]{"/sync_01_now6_75_21x21.png","/sync_02_now6_75_21x21.png","/sync_03_now6_75_21x21.png","/sync_04_now6_75_21x21.png"},
							new String[]{"/sync_01_now6_0_21x21.png","/sync_02_now6_0_21x21.png","/sync_03_now6_0_21x21.png","/sync_04_now6_0_21x21.png"}
							);
				}
				this.set(INDEX_USER_CHECKMARK_PROGRESS, processIcon);
				
				//NO COMMAND
			}
			else
			{
				//create network icon
				networkIcon = UiFactory.createNetworkIcon(nw.getNetworkId(),false);
				this.set(INDEX_NETWORK_ICON, networkIcon);

				//#style webaccount_sn_item
				networkName = new StringItem(null,nw.getName());
				this.set(INDEX_NETWORK_NAME, networkName);

				//#style webaccount_sn_item
				userName = new StringItem(null,"");
				this.set(INDEX_USER_NAME, userName);

				this.set(INDEX_USER_CHECKMARK_PROGRESS, dummy);
				
				//#if polish.blackberry
					this.setDefaultCommand(WebAccountsForm.cmdAdd);
				//#else
					this.addCommand(WebAccountsForm.cmdAdd);
				//#endif
			}
			
			//making absolutely sure that all children are PLAIN as some might have switched state
			networkIcon.setAppearanceMode(Item.PLAIN);
			networkName.setAppearanceMode(Item.PLAIN);
			userName.setAppearanceMode(Item.PLAIN);
			if(null != processIcon)
				processIcon.setAppearanceMode(Item.PLAIN);
			if(null != checkMarkIcon)
				checkMarkIcon.setAppearanceMode(Item.PLAIN);
			
		}
	}
	
	private void clearCommands()
	{
		ArrayList cmds = this.getItemCommands();
		while(null != cmds && cmds.size()>0)
			this.removeCommand((Command)cmds.get(0));
	}
}
