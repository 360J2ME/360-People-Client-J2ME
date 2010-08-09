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
package com.zyb.nowplus.business.domain;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.zyb.util.Collator;
import com.zyb.util.HashUtil;
import com.zyb.util.TextUtilities;

import de.enough.polish.io.Serializer;

/**
 * Represents an identity in a {@link com.zyb.nowplus.business.domain.Profile profile}
 * on an external network.
 */
public class Identity extends ContactDetail
{
	public static final int TYPE_PHONE = 1;
	public static final int TYPE_EMAIL = 2;
	public static final int TYPE_URL = 3;
	public static final int TYPE_IM_ACCOUNT = 4;
	public static final int TYPE_SN_ACCOUNT = 5;
	
	public static final int SUBTYPE_HOME = 1;  // phone, email
	public static final int SUBTYPE_MOBILE = 2; // phone
	public static final int SUBTYPE_WORK = 3; // phone, email
	public static final int SUBTYPE_FAX = 4; // phone
	public static final int SUBTYPE_OTHER = 5; // phone, email

	public static final String SUBTYPE_HOME_LABEL = "home"; 
	public static final String SUBTYPE_MOBILE_LABEL = "cell"; 
	public static final String SUBTYPE_WORK_LABEL = "work"; 
	public static final String SUBTYPE_FAX_LABEL = "fax"; 
	public static final String SUBTYPE_OTHER_LABEL = "other"; 
	
	public static final long NOWPLUS_ACCOUNT_SABDETAILID = -2;  // -1 was used for url
	public static final String NOWPLUS_ACCOUNT_LABEL = "Vodafone 360";
	
	private Profile profile;
	private int type;
	private int subtype;
	private int nabSubtypes;
	private ExternalNetwork network;
	private String name;
	private String url;
	private Channel[] channels;
	private boolean preferred;
	
	public static final int URL_HELP = 1;
	public static final int URL_TNC = 2;
	public static final int URL_PRIVACY = 3;
	
	
	public static Identity createPhoneNumber(int subtype, String phoneNumber, boolean preferred)
	{
		return createPhoneNumber(subtype, -1, phoneNumber, preferred, 0);
	}
	
	public static Identity createPhoneNumber(int subtype, String phoneNumber, boolean preferred, long sabDetailId)
	{
		return createPhoneNumber(subtype, -1, phoneNumber, preferred, sabDetailId);
	}
	
	public static Identity createPhoneNumber(int subtype, int nabSubtypes, String phoneNumber, boolean preferred)
	{
		return createPhoneNumber(subtype, nabSubtypes, phoneNumber, preferred, 0);
	}
	
	public static Identity createPhoneNumber(int subtype, int nabSubtypes, String phoneNumber, boolean preferred, long sabDetailId)
	{
		Identity id = new Identity(sabDetailId, TYPE_PHONE);
		id.subtype = subtype;
		id.nabSubtypes = nabSubtypes;
		id.name = phoneNumber;
		id.url = null;
		id.channels = new Channel[] {
				new Channel(id, Channel.TYPE_CALL), 
				new Channel(id, Channel.TYPE_SMS),
				new Channel(id, Channel.TYPE_MMS)};
		id.preferred = preferred;
		return id;
	}
	
	public static Identity createEmail(int subtype, String email, boolean preferred) throws InvalidValueException
	{
		return createEmail(subtype, -1, email, preferred, 0);
	}
	
	public static Identity createEmail(int subtype, String email, boolean preferred, long sabDetailId) throws InvalidValueException
	{
		return createEmail(subtype, -1, email, preferred, sabDetailId);
	}
	
	public static Identity createEmail(int subtype, int nabSubtypes, String email, boolean preferred) throws InvalidValueException
	{
		return createEmail(subtype, nabSubtypes, email, preferred, 0);
	}
	
	public static Identity createEmail(int subtype, int nabSubtypes, String email, boolean preferred, long sabDetailId) throws InvalidValueException
	{
		if (!Collator.isEmpty(email) && !TextUtilities.isValidEmail(email))
		{
			throw new InvalidValueException(InvalidValueException.TYPE_INVALID_EMAIL);
		}
		 
		Identity id = new Identity(sabDetailId, TYPE_EMAIL);
		id.subtype = subtype;
		id.nabSubtypes = nabSubtypes;
		id.name = email;
		id.url = null;
		id.channels = new Channel[] {
				new Channel(id, Channel.TYPE_EMAIL)};
		id.preferred = preferred;
		return id;
	}
	
	public static Identity createUrl(String url)
	{
		Identity id = new Identity(0, TYPE_URL);
		if (url.startsWith("https://")) {
            id.name = url.substring(8);
            id.url = url;
        } else if (url.startsWith("http://")) {
            id.name = url.substring(7);
            id.url = url;
        } else{
            id.name = url;
            id.url = "http://" + url;
        }
		id.channels = new Channel[] {new Channel(id, Channel.TYPE_BROWSE)};
		return id;
	}
	
	public static Identity createUrl(String url, String name){
		Identity id = new Identity(0, TYPE_URL);
		id.name = name;
		id.url = url;
		id.channels = new Channel[] {new Channel(id, Channel.TYPE_BROWSE)};
		return id;
	}
	
	public static Identity createImAccount(ExternalNetwork network, String name, boolean preferred, long sabDetailId)
	{
		Identity id = new Identity(sabDetailId, TYPE_IM_ACCOUNT);
		id.network = network;
		id.name = name;
		id.channels = new Channel[] {new Channel(id, Channel.TYPE_CHAT)};
		id.preferred = preferred;
		return id;
	}
	
	public static Identity createSnAccount(ExternalNetwork network, String name, String profileUrl, long sabDetailId)
	{
		Identity id = new Identity(sabDetailId, TYPE_SN_ACCOUNT);
		id.network = network;
		id.name = name;
		id.url = profileUrl;
		id.channels = new Channel[] {new Channel(id, Channel.TYPE_BROWSE)};
		return id;
	}
	
	public static Identity createEmptyIdentity(int type, int nabSubtypes, long sabDetailId)
	{
		Identity id = new Identity(sabDetailId, type);
		id.nabSubtypes = nabSubtypes;
		id.channels = new Channel[0];
		return id;
	}	
	
	/**
	 * Constructor for serialisation.
	 */
	public Identity()
	{
	}
	
	private Identity(long sabDetailId, int type)
	{
		this.sabDetailId = sabDetailId;
		this.type = type;
	}

	public void setProfile(Profile profile)
	{
		this.profile = profile;		
	}
		
	public Profile getProfile()
	{
		return profile;
	}
	
	public int getType() 
	{
		return type;
	}
	
	public boolean isWebAccount()
	{
		return (type == TYPE_SN_ACCOUNT) || (type == TYPE_IM_ACCOUNT);
	}
	
	public void setPresence(int presence)
	{
		if (type == Identity.TYPE_SN_ACCOUNT)
		{
			getChannel(Channel.TYPE_BROWSE).setPresence(presence);
		}
		else
		if (type == Identity.TYPE_IM_ACCOUNT)
		{
			getChannel(Channel.TYPE_CHAT).setPresence(presence);
		}
	}
	
	public int getPresence()
	{
		if (type == TYPE_SN_ACCOUNT)
		{
			return getChannel(Channel.TYPE_BROWSE).getPresence();
		}
		else
		if (type == TYPE_IM_ACCOUNT)
		{
			return getChannel(Channel.TYPE_CHAT).getPresence();
		}
		else
		{
			return Channel.PRESENCE_UNKNOWN;
		}
	}
	
	public boolean isLoggedIn()
	{
		int presence = getPresence();
		return (presence == Channel.PRESENCE_ONLINE) || (presence == Channel.PRESENCE_IDLE) || (presence == Channel.PRESENCE_INVISIBLE);
	}
	
	public boolean hasCap(String capability)
	{
		return (network != null) && (network.hasCap(capability));
	}
	
	public int getSubtype() 
	{
		return subtype;
	}
	
	public void setNabSubtypes(int nabSubtypes)
	{
		this.nabSubtypes = nabSubtypes;
	}
	
	public int getNabSubtypes()
	{
		return nabSubtypes;
	}
	
	public String getNetworkId()
	{
		return (network == null) ? null : network.getNetworkId();
	}

	public ExternalNetwork getNetwork()
	{
		return network;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public String getUrl()
	{
		return url;
	}
	
	/**
	 * Gets the channel of a given type.
	 */
	public Channel getChannel(int type)
	{
		Channel channel = null;
		for (int i = 0; i < channels.length; i++)
		{
			if (channels[i].getType() == type)
			{
				channel = channels[i];
				break;
			}
		}
		return channel; 
	}

	void setPreferred(boolean preferred)
	{
		this.preferred = preferred;
	}
	
	public boolean isPreferred()
	{
		return preferred;
	}

	public boolean isEmpty()
	{
		return Collator.isEmpty(name);
	}
	
	public void read(DataInputStream in) throws IOException 
	{
		sabDetailId = in.readLong();
		type = in.readInt();
		subtype = in.readInt();
		nabSubtypes = in.readInt();
		String networkId = (String) Serializer.deserialize(in);

		if (networkId != null) {
			network = ExternalNetwork.manager.findNetworkById(networkId);
		}

		name = (String) Serializer.deserialize(in);

		if (type == TYPE_URL || type == TYPE_SN_ACCOUNT) {
			url = (String) Serializer.deserialize(in);
		}

		preferred = in.readBoolean();
		cri = in.readLong();
		deserializeChannels(in);
	}

	private void deserializeChannels(DataInputStream in) throws IOException
	{
		channels = new Channel[in.readInt()];

		for (int i = 0; i < channels.length; i++) {
			channels[i] = new Channel(this, in.readInt());
			channels[i].setPresence(in.readInt());
			channels[i].setConversationId((String) Serializer.deserialize(in));
			channels[i].deserializeMessages(in);
		}
	}
	
	public void write(DataOutputStream out) throws IOException 
	{
		out.writeLong(sabDetailId);
		out.writeInt(type);
		out.writeInt(subtype);
		out.writeInt(nabSubtypes);
		Serializer.serialize((network == null) ? null : network.getNetworkId(), out);
		Serializer.serialize(name, out);

		if (type == TYPE_URL || type == TYPE_SN_ACCOUNT)
		{
			Serializer.serialize(url, out);
		}		

		out.writeBoolean(preferred);
		out.writeLong(cri);
		serializeChannels(out);
	}
	
	private void serializeChannels(DataOutputStream out) throws IOException
	{
		out.writeInt(channels.length);

		for (int i = 0; i < channels.length; i++) {
			out.writeInt(channels[i].getType());
			out.writeInt(channels[i].getPresence());
			Serializer.serialize(channels[i].getConversationId(), out);
			channels[i].serializeMessages(out);
		}
	}
	
	public boolean equals(Object o)
	{
		Identity that = (Identity) o;
		return (this.sabDetailId == that.sabDetailId) 
			&& (this.type == that.type)
			&& (this.subtype == that.subtype)
			&& (this.nabSubtypes == that.nabSubtypes)
			&& HashUtil.equals(this.network, that.network)
			&& HashUtil.equals(this.name, that.name)
			&& HashUtil.equals(this.url, that.url)
			&& (this.preferred == that.preferred);
	}
	
	//#mdebug error
	public String toString()
	{
		return "Identity[sabDetailId=" + sabDetailId
			+ ",type=" + type
			+ ",subtype=" + subtype
			+ ",nabSubtypes=" + nabSubtypes
			+ ",network=" + network
			+ ",name=" + name
			+ ",url=" + url
			+ ",preferred=" + preferred
			+ ",cri=" + cri
			+ "]";
	}	
	//#enddebug
	
	/**
	 * Creates a new array of len elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1].
	 */
	public static Identity[] trimArray(Identity[] src, int len)
	{
		Identity[] dst = new Identity[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}		
	
	/**
	 * Extends an array.
	 */
	public static Identity[] extendArray(Identity[] src)
	{
		Identity[] dst = new Identity[src.length * 3 / 2 + 1];
		System.arraycopy(src, 0, dst, 0, src.length);
		return dst;
	}	
	
	/**
	 * Converts a subtype label to a subtype.
	 */	
	public static int toSubtype(String subtypeLabel)
	{
		int subtype = 0;
		if (null == subtypeLabel)
		{
			return SUBTYPE_OTHER;
		}
		
		if (subtypeLabel.indexOf(SUBTYPE_HOME_LABEL) >= 0)
		{
			subtype = SUBTYPE_HOME;
		}
		else
		if (subtypeLabel.indexOf(SUBTYPE_WORK_LABEL) >= 0)
		{
			subtype = SUBTYPE_WORK;	
		}	

		if (subtypeLabel.indexOf(SUBTYPE_MOBILE_LABEL) >= 0)
		{
			subtype = SUBTYPE_MOBILE;	
		}						
		else
		if (subtypeLabel.indexOf(SUBTYPE_FAX_LABEL) >= 0)
		{
			subtype = SUBTYPE_FAX;	
		}		
		else
		if (subtypeLabel.indexOf(SUBTYPE_OTHER_LABEL) >= 0)
		{
			subtype = SUBTYPE_OTHER;	
		}
		return subtype;
	}
	
	/**
	 * Converts a subtype to a subtype label.
	 */	
	public static String toSubtypeLabel(int subtype)
	{
		String subtypeLabel = null;
		if (subtype == SUBTYPE_HOME)
		{
			subtypeLabel = SUBTYPE_HOME_LABEL;
		}
		else
		if (subtype == SUBTYPE_MOBILE)
		{
			subtypeLabel = SUBTYPE_MOBILE_LABEL;
		}
		else	
		if (subtype == SUBTYPE_WORK)
		{
			subtypeLabel = SUBTYPE_WORK_LABEL;
		}
		else
		if (subtype == SUBTYPE_FAX)
		{
			subtypeLabel = SUBTYPE_FAX_LABEL;
		}
		else
		if (subtype == SUBTYPE_OTHER)
		{
			subtypeLabel = SUBTYPE_OTHER_LABEL;
		}
		return subtypeLabel;
	}
}
