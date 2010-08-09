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

import com.zyb.util.ArrayUtils;
import com.zyb.util.HashUtil;

import de.enough.polish.io.Externalizable;
import de.enough.polish.io.Serializer;
import de.enough.polish.util.Locale;

/**
 * An external network, can be a social network, or an chat network.
 */
public class ExternalNetwork implements Externalizable
{
	public static ExternalNetworkManager manager;
	
	/**
	 * Network identifier for Vodafone 360
	 */
	public static final String VODAFONE_360 = "nowplus";

	/**
	 * Network name for Vodafone 360
	 */
	public static final String VODAFONE_360_LABEL = "Vodafone 360";
	
	/**
	 * Network identifiers for social networks.
	 * This are used to map inconsistent ids from the servers with
	 * hardcoded resources in the client.
	 */
	public static final String FACEBOOK = "facebook.com";
	public static final String GOOGLE = "google";
	public static final String HYVES = "hyves.nl";
	public static final String TWITTER = "twitter.com";
	public static final String WINDOWS_LIVE = "microsoft";
	
	public static final int CREDENTIALS_USERNAME = 1;
	public static final int CREDENTIALS_USERNAME_OR_EMAIL = 2;
	
	private String pluginId;
	private String networkId;
	private String name;
	private String[] capabilities;
	
	public static final int FLAG_ADDING = 1;
	public static final int FLAG_REMOVING = 2;
	
	/**
	 * This flag is used to indicate if the user tried to 
	 * add/remove an account to/from this external network.
	 */
	private int addRemoveFlag;
	
	/**
	 * Constructor for serialization.
	 */
	public ExternalNetwork()
	{
	}
	
	public ExternalNetwork(String pluginId, String networkId, String name, String[] capabilities)
	{
		this.pluginId = pluginId;
		this.networkId = networkId;
		this.name = name;
		this.capabilities = capabilities;
	}
	
	public String getPluginId()
	{
		return pluginId;
	}
	
	public String getNetworkId()
	{
		return networkId;
	}
	
	public String getName()
	{
		return name;
	}

	public int getCredentialsType()
	{
		if (networkId == WINDOWS_LIVE) 
		{
			return CREDENTIALS_USERNAME_OR_EMAIL;
		}
		else
		{
			return CREDENTIALS_USERNAME;
		}
	}
	
	public String[] getCapabilities()
	{
		return capabilities;
	}
	
	public boolean hasCap(String capability)
	{
		if (capabilities != null)
		{
			for (int i = 0; i < capabilities.length; i++)
			{
				if (capabilities[i].equalsIgnoreCase(capability))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	// TODO: get from the server
	public String[] getDisclaimer()
	{
		if (networkId == ExternalNetwork.FACEBOOK)
		{
			return new String[]
			{
					Locale.get("nowplus.client.java.webaccounts.facebook.disclaimer.header"),
					Locale.get("nowplus.client.java.webaccounts.facebook.disclaimer.text1"),
					Locale.get("nowplus.client.java.webaccounts.facebook.disclaimer.text2"),
					Locale.get("nowplus.client.java.webaccounts.facebook.disclaimer.text3")
			};
		}
		else
		if (networkId == ExternalNetwork.WINDOWS_LIVE)
		{
			return new String[]
			{
					null,
					Locale.get("nowplus.client.java.webaccounts.windowslive.disclaimer.text")
			};
		}
		else
		{
			return new String[]
			{
					null
			};
		}
	}
	
	public void setAddRemoveFlag(int flag)
	{
		this.addRemoveFlag = flag;
	}
	
	public boolean addingToMyProfile()
	{
		return (addRemoveFlag == FLAG_ADDING);
	}
	
	public boolean removingFromMyProfile()
	{
		return (addRemoveFlag == FLAG_REMOVING);
	}
	
	public void read(DataInputStream in) throws IOException 
	{
		pluginId = (String) Serializer.deserialize(in);
		networkId = (String) Serializer.deserialize(in);
		name = (String) Serializer.deserialize(in);
		int len = in.readInt();
		if (len == -1)
		{
			capabilities = null;
		}
		else
		{
			capabilities = new String[len];
			for (int i = 0; i < len; i++)
			{
				capabilities[i] = (String) Serializer.deserialize(in);
			}
		}
	}

	public void write(DataOutputStream out) throws IOException 
	{
		Serializer.serialize(pluginId, out);
		Serializer.serialize(networkId, out);
		Serializer.serialize(name, out);
		if (capabilities == null)
		{
			out.writeInt(-1);
		}
		else
		{
			out.writeInt(capabilities.length);
			for (int i = 0; i < capabilities.length; i++)
			{
				Serializer.serialize(capabilities[i], out);
			}
		}
	}
	
	public boolean equals(Object o)
	{
		ExternalNetwork that = (ExternalNetwork) o;
		return HashUtil.equals(this.pluginId, that.pluginId) 
			&& HashUtil.equals(this.networkId, that.networkId)
			&& HashUtil.equals(this.name, that.name);
	}
	
	//#mdebug error
	public String toString()
	{
		return "ExternalNetwork[pluginId=" + pluginId
			+ ",networkId=" + networkId
			+ ",name=" + name
			+ ",capabilities=" + ArrayUtils.toString(capabilities)
			+ "]";
	}	
	//#enddebug
	
	/**
	 * Creates a new array of len elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1].
	 */
	public static ExternalNetwork[] trimArray(ExternalNetwork[] src, int len)
	{
		ExternalNetwork[] dst = new ExternalNetwork[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}		
	
	/**
	 * Extends an array.
	 */
	public static ExternalNetwork[] extendArray(ExternalNetwork[] src)
	{
		ExternalNetwork[] dst = new ExternalNetwork[src.length * 3 / 2 + 1];
		System.arraycopy(src, 0, dst, 0, src.length);
		return dst;
	}		
	
	/**
	 * Deserializes an array of ExternalNetworks.
	 */
	public static ExternalNetwork[] deserializeExternalNetworkArray(DataInputStream in) throws IOException 
	{
		int len = in.readInt();
		if (len == -1)
		{
			return null;
		}
		ExternalNetwork[] array = new ExternalNetwork[len];
		for (int i = 0; i < len; i++)
		{
			array[i] = (ExternalNetwork) Serializer.deserialize(in);
		}
		return array;		
	}	
	
	public static String getStandardId(String networkId)
	{
		if (networkId == null)
		{
			networkId = ExternalNetwork.VODAFONE_360;
		}
		else
		{
			networkId = networkId.toLowerCase();

			if ("zyb".equals(networkId))
			{
				networkId = ExternalNetwork.VODAFONE_360;
			}
			else
			if ("facebook".equals(networkId) || "facebook.com".equals(networkId))
			{
				networkId = ExternalNetwork.FACEBOOK;
			}
			else
			if ("google".equals(networkId))
			{
				networkId = ExternalNetwork.GOOGLE;
			}
			else
			if ("hyves".equals(networkId) || "hyves.nl".equals(networkId))
			{
				networkId = ExternalNetwork.HYVES;
			}			
			else
			if ("twitter".equals(networkId) || "twitter.com".equals(networkId))
			{
				networkId = ExternalNetwork.TWITTER;
			}
			else				
			if ("winlive".equals(networkId) || "win".equals(networkId) || "windows live".equals(networkId) 
					|| "microsoft".equals(networkId) || "msnmessenger".equals(networkId))
			{
				networkId = ExternalNetwork.WINDOWS_LIVE;
			}
		}
		return networkId;
	}
}
