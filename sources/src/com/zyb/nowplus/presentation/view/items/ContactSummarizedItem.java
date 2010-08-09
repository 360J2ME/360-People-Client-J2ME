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

import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.MyProfile;
import com.zyb.nowplus.business.domain.ProfileSummary;
import com.zyb.nowplus.business.sync.util.CRC32;
import com.zyb.nowplus.presentation.UiFactory;

import de.enough.polish.ui.Container;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;

/**
 * Visual representation of summarized contact in 'contact' list.
 * see http://wiki.zyb.local/index.php?title=ContactSummaryItemStyle for correct style properties 
 * 
 * @author Jens Vesti
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class ContactSummarizedItem extends Container
{
	private static final byte PRIORITY_STATUS = 0;
	private static final byte PRIORITY_NONE = 1;
	
	public static final byte NETWORK_ICON_MODE_NONE = 1;
	public static final byte NETWORK_ICON_MODE_ALL = 2;
	public static final byte NETWORK_ICON_MODE_NOWPLUS = 3;
	
	public static final byte PRESENCE_MODE_NONE = 4;
	public static final byte PRESENCE_MODE_ALL_STATES = 7;
	public static final byte PRESENCE_MODE_ONLINE_ONLY = 5;
	public static final byte PRESENCE_MODE_ONLINE_GRAY = 6;
	public static final byte PRESENCE_MODE_OFFLINE_ONLY = 8;
	
	private final static byte INNER_CONTAINER_INDEX_NAME = 0;
	private final static byte INNER_CONTAINER_INDEX_PRESENCE = 1;
	private final static byte INNER_CONTAINER_INDEX_STATUS_TEXT = 2;
	private final static byte INNER_CONTAINER_INDEX_STATUS_ICON = 3;
	
	private final static int PROFILE_CHECKSUM_SALT = 37;
	
	protected Style nameStyle,presenceStyle,statusStyle,networkStyle,leftSideStyle,rightSideStyle,rightInnerSideStyle;
	
	private Container  right;
	protected Container innerContainer,left;
	
	//Displayed icons
	protected CachedImageItem contactAvatar;
	protected StringItem name;
	protected IconItem presenceIcon;
	
	protected StringItem status;
	protected IconItem networkIcon;
	
	protected static StringItem dummyString;
	protected static IconItem dummyIcon;

	int profileChecksum = 0;
	
	boolean calculateChecksum = false;
	
	static
	{
		dummyString = new StringItem(null, " ");
		dummyIcon = new IconItem( null, null);
		
		if(null != dummyString)
		{
			dummyString.setAppearanceMode(Item.PLAIN);
		}
		
		if(null != dummyIcon)
			dummyIcon.setAppearanceMode(Item.PLAIN);
	}
	
	private int contentPriority, networkIconMode, presenceMode;
	protected ProfileSummary contact;

	/**
	 * 
	 * @param contact
	 */
	public ContactSummarizedItem(final ProfileSummary contact, final int netWorkIconMode, final int presenceMode)
	{
		this(contact, netWorkIconMode, presenceMode, null);
	}
	
	Style originalStyle;
	
	/**
	 * 
	 * @param contact
	 * @param style
	 */
	public ContactSummarizedItem(final ProfileSummary contact, final int netWorkIconMode, final int presenceMode, final Style style) 
	{
		super(false,style);
		
		this.originalStyle = style;
		
		updateInternalStyles();
		
		left = new Container(false, leftSideStyle);
		left.setAppearanceMode(Item.PLAIN);
		this.add(left);
		right = new Container(false, rightSideStyle);
		right.setAppearanceMode(Item.PLAIN);
		this.add(right);
		innerContainer = new Container(false, rightInnerSideStyle);
		innerContainer.setAppearanceMode(Item.PLAIN);
		this.right.add(innerContainer);
		
		this.networkIconMode = netWorkIconMode;
		this.presenceMode = presenceMode;
		
		/*
		 * Sets item members according to contact
		 */
		updateMemberItems(contact);
	}
	
	/**
	 * 
	 * @param contact
	 */
	public ContactSummarizedItem(final ProfileSummary contact, final int netWorkIconMode, final int presenceMode, boolean calculateChecksum)
	{
		this(contact, netWorkIconMode, presenceMode, calculateChecksum, null);
	}
	
	/**
	 * 
	 * @param contact
	 * @param style
	 */
	public ContactSummarizedItem(final ProfileSummary contact, final int netWorkIconMode, final int presenceMode, boolean calculateChecksum, final Style style) 
	{
		super(false,style);
		
		this.originalStyle = style;
		
		updateInternalStyles();
		
		left = new Container(false, leftSideStyle);
		left.setAppearanceMode(Item.PLAIN);
		this.add(left);
		right = new Container(false, rightSideStyle);
		right.setAppearanceMode(Item.PLAIN);
		this.add(right);
		innerContainer = new Container(false, rightInnerSideStyle);
		innerContainer.setAppearanceMode(Item.PLAIN);
		this.right.add(innerContainer);
		
		this.networkIconMode = netWorkIconMode;
		this.presenceMode = presenceMode;
		this.calculateChecksum = calculateChecksum;
		
		/*
		 * Sets item members according to contact
		 */
		updateMemberItems(contact);
	}
	
	/**
	 * Handles import of custom CSS attributes for unfocused and focused styles.
	 */
	private void updateInternalStyles() 
	{
		if( null != this.getStyle() && null != this.getFocusedStyle() )
		{
			updateCssStyleAttributes(this.getStyle(), false);
			updateCssStyleAttributes(this.getFocusedStyle(), true);
		}
	}
	
	/**
	 * Fetches custom CSS attributes and manages how these are applied to member objects
	 * depending on focus state.
	 *	
	 * @param style the style to extract custom css objects from
	 * @param isFocused if the style is the focused style or not
	 */
	private void updateCssStyleAttributes(Style style, boolean isFocused )
	{
		//these values are found in css-attributes.xml in Polish build branch
		//TODO: Can possibly be fetches via preprocessing, ask Andre about this
		final int FOCUSSTYLE_KEY = 1;
		
		Style name;
		//#ifdef polish.css.name-style
		if(style.getObjectProperty("name-style") != null)
			name = (Style)style.getObjectProperty("name-style");
		else
		//#endif
			name = style;
		
		//set style or add focused style attribute
		if(!isFocused)
			this.nameStyle = name;
		else
		if(null != nameStyle)
			this.nameStyle.addAttribute(FOCUSSTYLE_KEY,name);

		//set dummy item same as name to ensure size constraits from css
		dummyString.setStyle(this.nameStyle);
		
		Style presence;
		//#ifdef polish.css.presence-style
		if(style.getObjectProperty("presence-style") != null)
			presence = (Style)style.getObjectProperty("presence-style");
		else
		//#endif
			presence = style;
		
		//set style or add focused style attribute
		if(!isFocused)
			this.presenceStyle = presence;
		else
		if(null != presenceStyle)
			this.presenceStyle.addAttribute(FOCUSSTYLE_KEY,presence);
		
		//set dummy icon same as presence to ensure size constraits from css
		dummyIcon.setStyle(this.presenceStyle);
		
		Style status;
		//#ifdef polish.css.status-style
		if(style.getObjectProperty("status-style") != null)
			status = (Style)style.getObjectProperty("status-style");
		else
		//#endif
			status = style;
		
		//set style or add focused style attribute
		if(!isFocused)
			this.statusStyle = status;
		else
		if(null != statusStyle)
			this.statusStyle.addAttribute(FOCUSSTYLE_KEY,status);
		
		Style network;
		//#ifdef polish.css.network-style
		if(style.getObjectProperty("network-style") != null)
			network = (Style)style.getObjectProperty("network-style");
		else
		//#endif
			network = style;
		
		//set style or add focused style attribute
		if(!isFocused)
			this.networkStyle = network;
		else
		if(null != networkStyle)
			this.networkStyle.addAttribute(FOCUSSTYLE_KEY,network);
		
		Style leftSide;
		//#ifdef polish.css.left-side-style
		if(style.getObjectProperty("left-side-style") != null)
			leftSide = (Style)style.getObjectProperty("left-side-style");
		else
		//#endif
			leftSide = style;
		
		//set style or add focused style attribute
		if(!isFocused)
			this.leftSideStyle = leftSide;
		else
		if(null != leftSideStyle)
			this.leftSideStyle.addAttribute(FOCUSSTYLE_KEY,leftSide);

		Style rightSide;
		//#ifdef polish.css.right-side-style
		if(style.getObjectProperty("right-side-style") != null)
			rightSide = (Style)style.getObjectProperty("right-side-style");
		else
		//#endif
			rightSide = style;
		
		//set style or add focused style attribute
		if(!isFocused)
			this.rightSideStyle = rightSide;
		else
		if(null != rightSideStyle)
			this.rightSideStyle.addAttribute(FOCUSSTYLE_KEY,rightSide);

		Style rightInnerSide;
		//#ifdef polish.css.right-inner-side-style
		if(style.getObjectProperty("right-inner-side-style") != null)
			rightInnerSide = (Style)style.getObjectProperty("right-inner-side-style");
		else
		//#endif
			rightInnerSide = style;
		
		//set style or add focused style attribute
		if(!isFocused)
			this.rightInnerSideStyle = rightInnerSide;
		else
		if(null != rightInnerSideStyle)
			this.rightInnerSideStyle.addAttribute(FOCUSSTYLE_KEY,rightInnerSide);

		if(!isFocused)
		{
			String priorityString;
			//#ifdef polish.css.content-priority
			if(style.getProperty("content-priority") != null)
				priorityString = style.getProperty("content-priority");
			else
				//#endif
				priorityString = "status";
			
			if(null != priorityString)
			{
				priorityString = priorityString.toLowerCase();
				
				//#debug debug
				System.out.println("priorityString:"+priorityString);
				
				if(priorityString.equals("status"))
					contentPriority = PRIORITY_STATUS;
				else
				if(priorityString.equals("empty"))
					contentPriority = PRIORITY_NONE;
			}
			else
				contentPriority = PRIORITY_NONE;
		}
	}
	
	static int calculateSummaryChecksum(ProfileSummary summary) {
		int checksum = PROFILE_CHECKSUM_SALT;
		
		if(summary.getFullName() != null) {
			checksum = CRC32.update(summary.getFullName(), checksum);
		}
		
		checksum = CRC32.update(summary.getNowPlusPresence(), checksum);
		
		if(summary.getStatus() != null) {
			checksum = CRC32.update(summary.getStatus(), checksum);
		}
		
		return checksum;
	}

	/**
	 * Updates UI members of item based on passed Contact.
	 * 
	 * @param contact the new contact
	 */	
	protected synchronized void updateMemberItems(ProfileSummary contact)
	{
		if(this.calculateChecksum) {
			int newProfileChecksum = calculateSummaryChecksum(contact);
			if(this.profileChecksum == newProfileChecksum) {
				// no update needed as contact hasn't changed
				return;
			} 
			
			this.profileChecksum = newProfileChecksum;
		}
		//#debug debug
		System.out.println("initMemberItems("+contact+")");
		
		// reset style
		setStyle(this.originalStyle);
		updateInternalStyles();
		
		if(null != contact)
		{
			//#mdebug debug
			try 
			{
			//#enddebug
				
				StringItem si  = null;
				IconItem ii = null;
				
				/*
				 * TODO: For better performance, check if contact is different for the one currently represented and check if contact data has changed 
				 * e.g. if(contact.hashCode() != previousHashCode)
				 */
				
				this.contact = contact;
				
				//add/update contact avatar
				if(contactAvatar == null)
				{
					//#style .ui_factory_profile_avatar
					contactAvatar = new CachedImageItem( null, contact.getProfileImage(), 0, null);
					contactAvatar.setAppearanceMode(Item.PLAIN);
					
					if(null != contactAvatar)
						if(this.left.size() >= 1)
							left.set(0, contactAvatar);
						else
							left.add(contactAvatar);
				}
				else
					contactAvatar.setImageRef(contact.getProfileImage());
				
				//#debug debug
				System.out.println("Setting name to: "+contact.getFullName());
				
				//add/update contact name
				si = getName();
				
				if(this.innerContainer.size() >= INNER_CONTAINER_INDEX_NAME + 1)
					innerContainer.set(INNER_CONTAINER_INDEX_NAME, si);
				else
					innerContainer.add(si);
				
				//#debug debug
				System.out.println("Nowplus presens is: "+contact.getNowPlusPresence());
				
				//add/update contact presence
				ii = getPresenceIcon();
				
				if(this.innerContainer.size() >= INNER_CONTAINER_INDEX_PRESENCE + 1)
					innerContainer.set(INNER_CONTAINER_INDEX_PRESENCE, ii);
				else
					innerContainer.add(ii);

				//add/update contact status & status icon
                si  = getStatus();
				ii = getNetworkIcon();
				
				if(this.innerContainer.size() >= INNER_CONTAINER_INDEX_STATUS_TEXT + 1)
					innerContainer.set(INNER_CONTAINER_INDEX_STATUS_TEXT, si);
				else
					innerContainer.add(si);

				if(this.innerContainer.size() >= INNER_CONTAINER_INDEX_STATUS_ICON + 1)
					innerContainer.set(INNER_CONTAINER_INDEX_STATUS_ICON, ii);
				else
					innerContainer.add(ii);
				
			//#mdebug debug
			} 
			catch (Throwable t)
			{
				System.out.println("Failed initialising ContactSummarizeditem, error: "+t.getMessage());
				t.printStackTrace();
			}
			//#enddebug
		}
	}
	
	/**
	 * Returns current status StringItem based on current ProfileSummary member
	 * and current contentPrioruty.
	 * <p>
	 * Steps are taken to prevent processing overhead of Object creation
	 * 
	 * @return StringItem of status if available, otherwise empty StringItem dummyString
	 */
	protected StringItem getStatus() 
	{
		StringItem result = null;
		
		if(null != this.contact)
		{
			if(PRIORITY_STATUS == contentPriority)
			{
				//NOTE: contact must be loaded for ProfileSummary.getStatus() to work
				String statusString = contact.getStatus();
				
				//#debug debug
				System.out.println("statusString:"+statusString);
				
				if(statusString != null)
				{
					if(status == null)
					{
						status = new StringItem(null, statusString, statusStyle);
						status.setAppearanceMode(Item.PLAIN);
					}
					else
						status.setText(statusString);
					
					result = status;
				}
				else
					result = dummyString;
			}
			else
				result = dummyString;
		}
		else
			result = dummyString;
		
		return result;
	}

	/**
	 * Resets the content of this UI item. Used to reuse/respawn the object.
	 * 
	 * @param contact
	 */		
	public void setContact(ProfileSummary contact) 
	{
		updateMemberItems(contact);
		repaint();
	}
	
	/**
	 * Return held contact reference if any
	 */
	public ProfileSummary getContact() 
	{
		return contact;
	}
	
	//#mdebug error
	public String toString() {
		return "ContactSummarizedItem [name=" + this.name.getText() + "]";
	}
    //#enddebug
	
	/**
	 * Sets the network Icon of Item.
	 * <p>
	 * The new network icon is forced into place even if no status is present.
	 * 
	 * @param networkId
	 */
	public synchronized void setNetworkIcon(String networkId)
	{
		if(null != networkId)
		{
			IconItem ii;
			
			//ensure that networkicon is not null before calling getNetworkIcon
			//fixes borderline case bug 0010445, that happens when forcing setting
			//a network icon under chat view
			if(null == networkIcon)
			{
				this.networkIcon = UiFactory.createNetworkIcon(networkId, false, networkStyle);
				this.networkIcon.setAppearanceMode(Item.PLAIN);
			}
			
			ii = getNetworkIcon(networkId);
			
			if(null != ii)
				if(this.innerContainer.size() >= INNER_CONTAINER_INDEX_STATUS_ICON + 1)
					innerContainer.set(INNER_CONTAINER_INDEX_STATUS_ICON, ii);
				else
					innerContainer.add(ii);
		}
	}
	
	/**
	 * Change the network icon mode of this item and updates network icon accordingly
	 * 
	 * @param mode
	 */
	public synchronized void setNetworkIconMode(byte mode)
	{
		if(networkIconMode != mode && 
				(
					mode == NETWORK_ICON_MODE_ALL ||
					mode == NETWORK_ICON_MODE_NONE ||
					mode == NETWORK_ICON_MODE_NOWPLUS 
				)
				)
		{
			networkIconMode = mode;
			
			//update network icon
			IconItem ii = getNetworkIcon();
			
			if(null != ii)
				if(this.innerContainer.size() >= INNER_CONTAINER_INDEX_STATUS_ICON + 1)
					innerContainer.set(INNER_CONTAINER_INDEX_STATUS_ICON, ii);
				else
					innerContainer.add(ii);
		}
	}
	
	/**
	 * Change the network icon mode of this item.
	 * 
	 * @param mode
	 */
	public synchronized void setPresenceMode(byte mode)
	{
		if(presenceMode != mode && 
				(
					mode == PRESENCE_MODE_ALL_STATES ||
					mode == PRESENCE_MODE_NONE ||
					mode == PRESENCE_MODE_ONLINE_GRAY ||
					mode == PRESENCE_MODE_ONLINE_ONLY
				)
				)
		{
			presenceMode = mode;
			
			//update network icon
			IconItem ii = getPresenceIcon();
			
			if(this.innerContainer.size() >= INNER_CONTAINER_INDEX_PRESENCE + 1)
				innerContainer.set(INNER_CONTAINER_INDEX_PRESENCE, ii);
			else
				innerContainer.add(ii);
		}
	}
	
	/**
	 * Returns current network IconItem based on current ProfileSummary member
	 * and current networkIconMode.
	 * <p>
	 * Steps are taken to prevent processing overhead of Object creation
	 * 
	 * @return IconItem of network if available, otherwise empty IconItem dummyIcon
	 */
	protected IconItem getNetworkIcon()
	{
		String networkId = null;
		
		if(null != contact)
		{
			//#mdebug debug
            System.out.println("network for " + contact.getFullName() + ": "  );
            if (contact.getStatusSource()!=null)
            	System.out.println(contact.getStatusSource().getNetworkId());
            //#enddebug
			
			//Set network icon and make sure that current network icon mode is respected
			ExternalNetwork ex;
			if( null != (ex = contact.getStatusSource()) )
				networkId = ex.getNetworkId();
		}
		
		//sometimes getNetworkIcon(networkId) returns null 
		IconItem iconTemp = getNetworkIcon(networkId); 
		if (iconTemp==null)	
			return dummyIcon;
		return iconTemp;
	}
	
	/**
	 * Returns network IconItem based on current networkIconMode and parameter networkId.
	 * <p>
	 * Steps are taken to prevent processing overhead of Object creation
	 * 
	 * @param networkId
	 * @return IconItem of network if available, otherwise empty IconItem dummyIcon
	 */
	private IconItem getNetworkIcon(String networkId)
	{
		IconItem result;
		Style style, focusedStyle = null;

		if(null != networkId)
		{
			if(networkIconMode != NETWORK_ICON_MODE_NONE )
			{
				if(networkIconMode == NETWORK_ICON_MODE_ALL)
				{
					if(networkIcon == null)
					{
						networkIcon = UiFactory.createNetworkIcon(networkId, false, networkStyle);
						if(networkIcon != null)
							networkIcon.setAppearanceMode(Item.PLAIN);
						else
							result = dummyIcon;
					}
					else
					{
						//force network icon focus state if appearance mode is PLAIN
						if(Item.PLAIN == this.appearanceMode)
						{
							style = UiFactory.createNetworkStyle( networkId, false, networkStyle);
							if(null != style)
								focusedStyle = (Style)style.getObjectProperty("focused-style");
							if(null != focusedStyle)
								style = focusedStyle;
						}
						else
							style = UiFactory.createNetworkStyle( networkId, false, networkStyle);
						
						UiFactory.setCompleteStyle(networkIcon, style);
					}
					
					result = networkIcon;
				}
				else
				if(networkIconMode == NETWORK_ICON_MODE_NOWPLUS )
				{
					if("nowplus".equals(networkId))
					{
						if(networkIcon == null)
						{
							networkIcon = UiFactory.createNetworkIcon(networkId, false, networkStyle);
							if(networkIcon != null)
								networkIcon.setAppearanceMode(Item.PLAIN);
							else
								result = dummyIcon;
						}
						else
						{
							//force network icon focus state if appearance mode is PLAIN
							if(Item.PLAIN == this.appearanceMode)
							{
								style = UiFactory.createNetworkStyle( networkId, false, networkStyle);
								if(null != style)
									focusedStyle = (Style)style.getObjectProperty("focused-style");
								if(null != focusedStyle)
									style = focusedStyle;
							}
							else
								style = UiFactory.createNetworkStyle( networkId, false, networkStyle);
							
							UiFactory.setCompleteStyle(networkIcon, style);
						}
						
						result = networkIcon;
					}
					else
						result = dummyIcon;
				}
				else
					result =  dummyIcon;
			}
			else
				result =  dummyIcon;
		}
		else
			result =  dummyIcon;			
        
		return result;
	}
	
	
	/**
	 * Returns current presence IconItem based on current ProfileSummary member
	 * and current presenceMode.
	 * <p>
	 * Steps are taken to prevent processing overhead of Object creation
	 * 
	 * @return IconItem of presence if available, otherwise empty IconItem dummyIcon
	 */
	/*protected*/public IconItem getPresenceIcon()
	{
		int presence = this.contact.getNowPlusPresence();
		
		if(PRESENCE_MODE_NONE != presenceMode && 
				(
					Channel.PRESENCE_UNKNOWN == presence ||
					Channel.PRESENCE_IDLE == presence ||
					Channel.PRESENCE_INVISIBLE == presence ||
					Channel.PRESENCE_OFFLINE == presence ||
					Channel.PRESENCE_ONLINE == presence
					)
		)
		{
			if(PRESENCE_MODE_ALL_STATES == presenceMode)
			{
				return createPresenceIcon(presenceIcon, presence, presenceStyle);
			}
			else
			if(PRESENCE_MODE_ONLINE_ONLY == presenceMode)
			{
				if(Channel.PRESENCE_ONLINE == presence)
					return createPresenceIcon(presenceIcon, presence, presenceStyle);
				else if(Channel.PRESENCE_IDLE == presence)
					return createPresenceIcon(presenceIcon, Channel.PRESENCE_IDLE, presenceStyle);
				else
					return dummyIcon;
			}
			else
			if(PRESENCE_MODE_ONLINE_GRAY == presenceMode)
			{
				if(Channel.PRESENCE_ONLINE == presence || Channel.PRESENCE_UNKNOWN == presence)
					return createPresenceIcon(presenceIcon, presence, presenceStyle);
				else if(Channel.PRESENCE_IDLE == presence)
					return createPresenceIcon(presenceIcon, Channel.PRESENCE_IDLE, presenceStyle);
				else					
					return createPresenceIcon(presenceIcon, Channel.PRESENCE_INVISIBLE, presenceStyle);
			}
			else 
			if(PRESENCE_MODE_OFFLINE_ONLY == presenceMode){
				return createPresenceIcon(presenceIcon, Channel.PRESENCE_INVISIBLE, presenceStyle);
			}
			else
				return dummyIcon;
		}
		else
			return dummyIcon;
	}
	
	private IconItem createPresenceIcon(IconItem icon, int presence, Style iconStyle)
	{
		if(null == icon)
		{
			icon = UiFactory.createPresenceIcon(presence, iconStyle);
			if(null != icon)
			{
				icon.setAppearanceMode(Item.PLAIN);		
				return icon;
			}
		}
		else 
		{
			Style style = UiFactory.createPresenceIconStyle(presence, iconStyle);
			if(null != style)
			{
				UiFactory.setCompleteStyle(icon,style);
				return icon;
			}
		}
		
		return dummyIcon;
	}
	
	public synchronized CachedImageItem getAvatar()
	{
		return this.contactAvatar;
	}
	
	public void requestInit()
	{
		if (isInitialized()) {
			setInitialized(false);
		
			if (this.isShown) {
                repaint();
            }
		}
	}
	
	/**
	 * Returns name of current ProfileSummary member
	 * <p>
	 * Steps are taken to prevent processing overhead of Object creation
	 * 
	 * @return StringItem of name if available, otherwise empty StringItem dummyString
	 */
	protected StringItem getName()
	{
		String cName = this.contact.getFullName();

		if (cName != null && !"".equals(cName)) {
			if (this.name == null) {
				this.name = new StringItem(null, cName, nameStyle);
				this.name.setAppearanceMode(Item.PLAIN);
			}
			else {
				this.name.setText(cName);
			}

			return this.name;
		}

		Channel channel;
		if(cName != null && cName.equals("")){
			channel = contact.getPrimaryEmailChannel();
			if(channel!=null)
				cName = channel.getName();
		}
		
		if(cName != null && cName.equals("")){
			channel = contact.getPrimaryCallChannel();
			if(channel!=null)
				cName = channel.getName();
		}
		if (cName != null) {
			if (this.name == null) {
				this.name = new StringItem(null, cName, nameStyle);
				this.name.setAppearanceMode(Item.PLAIN);
			}
			else {
				this.name.setText(cName);
			}

			return this.name;
		}
		
		return dummyString;
	}

	public void setAppearanceMode(int appearanceMode) 
	{
		super.setAppearanceMode(appearanceMode);
		
		updateMemberItems(this.contact);
	}
}
