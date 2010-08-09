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
package com.zyb.nowplus.business;

import java.util.Hashtable;
import java.util.Vector;

import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.business.domain.Message;
import com.zyb.nowplus.business.domain.MyProfile;
import com.zyb.nowplus.business.domain.ExternalNetworkManager;
import com.zyb.nowplus.business.domain.Profile;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.types.ChatObject;
import com.zyb.nowplus.data.protocol.types.Presence;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.util.ArrayUtils;
import com.zyb.util.HashUtil;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.util.event.EventListener;

/**
 * Manages chat.
 */
class ChatManager implements ExternalNetworkManager, EventListener
{		
	private static final String TEMP_CONVERSATION_ID_PREFIX = "requestId=";
	
	private final ServiceBroker services;
	private final ChatAccountIndex index;
	
	private ExternalNetwork[] networks;
	private int networksLen;
	
	private boolean running;
	private ChatManagerAction[] actions;
	
	/**
	 * If my nowplus presence is set before my profile is received, 
	 * the required presence is saved, so when my profile
	 * comes in, my nowplus presence can be set. 
	 */
	private int tmpPresence;
	
	/**
	 * This indicates presences were received for unknown contacts. If
	 * new contacts are received, we request the activities.
	 */
	private boolean contactsNotFound;
	
	/**
	 * Creates a chat manager.
	 */
	public ChatManager(ServiceBroker services, EventDispatcher dispatcher)
	{
		//#debug debug
		System.out.println("Constructing chat manager.");
		
		this.services = services;
		this.index = new ChatAccountIndex();
		
		actions = new ChatManagerAction[20];
		
		networks = new ExternalNetwork[4];
		networksLen = 0;
		
		dispatcher.attach(this);
	}

	/**
	 * Starts the chat manager.
	 */
	public void start()
	{
		if (!running) 
		{
			//#debug info
			System.out.println("Starting chat manager.");
			
			ExternalNetwork[] storedNetworks = services.getSettings().getExternalNetworks();

			if (storedNetworks != null)
			{
				for (int i = 0; i < storedNetworks.length; i++)
				{
					addExternalNetwork(storedNetworks[i]);
				}
			}
			
			synchronized (actions)
			{
				ChatManagerAction action = createAction(ChatManagerAction.TYPE_GET_NETWORKS);
				requestNetworks(action);
			}
			
			running = true;
		}
	}
	
	/**
	 * Stops the chat manager.
	 */
	public void stop()
	{
		if (running)
		{
			running = false;
			
			//#debug info
			System.out.println("Chat manager stopped.");
		}
	}
	
	public byte getContext() 
	{
		return Event.Context.MODEL;
	}

	public void handleEvent(byte context, int id, Object data) 
	{
		if (context == Event.Context.MODEL) {
			//#debug debug
			System.out.println("Chat manager received event " + id);

			if (id == Event.Model.MY_IDENTITIES_CHANGED_IN_SAB) {
				requestMyIdentities(null);
			}
			else if (id == Event.Model.PRESENCES_CHANGED) {
				requestPresences();
			}
		}
		else if (context == Event.Context.APP) {
			switch (id) {
				case Event.App.CONNECTION_UP:
					requestPresences();
					break;

				case Event.App.CONNECTION_DOWN:
				case Event.App.SERVICE_UNAVAILABLE:
					invalidatePresences();
					break;
			}
		}
	}	
	
	public boolean hasNetworks()
	{
		return (networksLen != 0);
	}
	
	public boolean hasExternalNetworks()
	{
		boolean found = false;
		
		synchronized (networks) {
			for (int i = 0; !found && (i < networksLen); i++) {
				ExternalNetwork candidate = networks[i];

				if (!ExternalNetwork.VODAFONE_360.equals(candidate.getNetworkId())) {
					found = true;
					break;
				}
			}
		}

		return found;
	}
	
	public ExternalNetwork[] getAvailableExternalNetworks()
	{
		ExternalNetwork[] selection = null;
		int len = 0;
		
		synchronized (networks)
		{
			selection = new ExternalNetwork[networksLen];
		
			for (int i = 0; i < networksLen; i++)
			{
				ExternalNetwork candidate = networks[i];
				if (!ExternalNetwork.VODAFONE_360.equals(candidate.getNetworkId()))
				{
					selection[len++] = candidate;
				}
			}
		}
		return ExternalNetwork.trimArray(selection, len);
	}
	
	public ExternalNetwork findNetworkById(String networkId)
	{
		networkId = ExternalNetwork.getStandardId(networkId);
		
		ExternalNetwork network = null;
		synchronized (networks)
		{
			int index = getIndexOfNetwork(networkId);
		    if (index == -1) 
		    {
		    	network = new ExternalNetwork("", networkId, networkId, null);
		    }
		    else
		    {
		    	network = networks[index];
		    }
		}
		return network;
	}
	
	private void addExternalNetwork(ExternalNetwork network)
	{
		if (networksLen == networks.length)
		{
			networks = ExternalNetwork.extendArray(networks);
		}
		networks[networksLen++] = network;
	}	
	
	private int getIndexOfNetwork(String networkId)
	{
		int index = -1;
		for (int i = 0; (i < networksLen) && (index == -1); i++)
		{
			ExternalNetwork candidate = networks[i];
			if (HashUtil.equals(candidate.getNetworkId(), networkId))
			{
				index = i;
			}
		}
		return index;
	}
	
	private void requestNetworks(ChatManagerAction action)
	{
		Hashtable filters = new Hashtable();
		Vector v = new Vector();
		v.addElement("get_own_status");
		v.addElement("chat");
		filters.put("capability", v);
		
		Hashtable params = new Hashtable();
		params.put("filterlist", filters);
		
		int requestId = services.getProtocol().sendRequest(ServerRequest.GET, ServerRequest.IDENTITIES, 
				null, params, ServerRequest.HIGH_PRIORITY, 2 * 60);	
		action.setRequestId(requestId);
		
		//#debug info
		System.out.println("Request " + requestId + ": get networks");
	}
	
	public void identitiesReceived(int requestId, ServiceObject[] serviceObjects)
	{
		if (running)
		{
			synchronized (actions)
			{
				int index = getIndexForAction(requestId);
				if (index == -1)
				{
					//#debug error
					System.out.println("Received response to stale request " + requestId + ", ignored.");
				}
				else
				{		
					destroyAction(index);
					
					synchronized (networks)
					{
						if (networksLen == 0)
						{
							for (int i = 0; i < serviceObjects.length; i++)
							{
								com.zyb.nowplus.data.protocol.types.Identity serviceObject = (com.zyb.nowplus.data.protocol.types.Identity) serviceObjects[i];
								
								String networkId = ExternalNetwork.getStandardId(serviceObject.getNetwork());
								
								addExternalNetwork(new ExternalNetwork(serviceObject.getPluginid(), networkId, serviceObject.getName(), serviceObject.getCapabilities())); 
							}
							addExternalNetwork(new ExternalNetwork(null, ExternalNetwork.VODAFONE_360, ExternalNetwork.VODAFONE_360_LABEL, null));
							services.networksReceived();
						}
						else
						{
							// add new external networks, overwrite existing ones and leave deleted ones, as they may still be referred to
							for (int i = 0; i < serviceObjects.length; i++)
							{
								com.zyb.nowplus.data.protocol.types.Identity serviceObject = (com.zyb.nowplus.data.protocol.types.Identity) serviceObjects[i];
				
								String networkId = ExternalNetwork.getStandardId(serviceObject.getNetwork());
								
								ExternalNetwork network = new ExternalNetwork(serviceObject.getPluginid(), networkId, serviceObject.getName(), serviceObject.getCapabilities()); 
				
								index = getIndexOfNetwork(network.getNetworkId());
								if (index == -1)
								{
									addExternalNetwork(network);
								}
								else
								{
									networks[index] = network;
								}
							}
						}
						services.getSettings().setExternalNetworks(ExternalNetwork.trimArray(networks, networksLen));
					}
				}
			}
		}
	}	
	
	/**
	 * Adds a social network account to my profile.
	 * @param importContacts If true, imports contacts.
	 */
	public void addSocialNetworkAccount(ExternalNetwork network, String name, String password, boolean importContacts)
	{
		if (running) 
		{
			synchronized (actions)
			{
				ChatManagerAction action = createAction(ChatManagerAction.TYPE_ADD_ACCOUNT);
				action.setNetwork(network);
				action.setName(name);
				
				com.zyb.nowplus.data.protocol.types.Identity serviceObject = 
					new com.zyb.nowplus.data.protocol.types.Identity(network.getPluginId(), network.getNetworkId(), 
							name, password, network.getCapabilities());
			
				int requestId = services.getProtocol().sendRequest(ServerRequest.VALIDATE, ServerRequest.IDENTITIES,
					new ServiceObject[] {serviceObject}, null, ServerRequest.HIGH_PRIORITY, 40);
				action.setRequestId(requestId);
				
				//#debug info
				System.out.println("Request " + requestId + ": add social network account " + network);
			}
		}
	}
	
	/**
	 * Handles the result of adding a social network.
	 */
	public void socialNetworkAccountAddedResultReceived(int requestId, ServiceObject[] serviceObjects)
	{
		if (running) {
			if (serviceObjects.length == 1) {
				com.zyb.nowplus.data.protocol.types.Identity identity = 
					(com.zyb.nowplus.data.protocol.types.Identity) serviceObjects[0];

				synchronized (actions) {
					int index = getIndexForAction(requestId);

					if (index == -1) {
						//#debug error
						System.out.println("Received response to stale request " + requestId + ", ignored.");
					}
					else {								
						if (identity.isOperationSuccessful()) {
							actions[index].getNetwork().setAddRemoveFlag(ExternalNetwork.FLAG_ADDING);
							
							String[] networkIds = {actions[index].getNetwork().getNetworkId()};
							String[] names = {actions[index].getNetwork().getNetworkId() + "::" + actions[index].getName()};
							int[] presences = {Channel.PRESENCE_ONLINE};
							
							setPresence(presences, networkIds, names);	
							
							// wait for change event instead of requesting
							// services.fireEvent(Event.Context.MODEL, Event.Model.ME_CHANGED_IN_SAB, null);
							
							services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_SUCCEEDED, null);
						}
						else {
							services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_FAILED, null);
						}
					}
					
					destroyAction(index);
				}
			}
			else {
				//#debug error
				System.out.println("Result of add social network contains " + serviceObjects.length + " items, ignored.");		
			}	
		}
	}	
	
	/**
	 * Removes a social network account from my profile.
	 */
	public void removeSocialNetworkAccount(Identity accountToRemove)
	{
		if (running && (accountToRemove != null)) {
			synchronized (actions) {
				ChatManagerAction action = createAction(ChatManagerAction.TYPE_REMOVE_ACCOUNT);
				action.setNetwork(accountToRemove.getNetwork());
			
				requestMyIdentities(action);
			
				//#debug info
				System.out.println("Request " + action.getRequestId() + ": get social network accounts to remove ");
			}
		}
	}
	
	private void removeSocialNetworkAccount2(ChatManagerAction action)
	{
		com.zyb.nowplus.data.protocol.types.Identity serviceObject = 
			new com.zyb.nowplus.data.protocol.types.Identity(action.getNetwork().getPluginId(), action.getNetwork().getNetworkId(), action.getIdentityId());
	
		int requestId = services.getProtocol().sendRequest(ServerRequest.DELETE, ServerRequest.IDENTITIES,
			new ServiceObject[] {serviceObject}, null, ServerRequest.HIGH_PRIORITY, 40);
		action.setRequestId(requestId);
		
		//#debug info
		System.out.println("Request " + requestId + ": remove social network account " + action.getNetwork());	
	}
	
	/**
	 * Handles the result of removing a social network account.
	 */
	public void socialNetworkAccountRemovedResultReceived(int requestId, ServiceObject[] serviceObjects)
	{
		if (running) {
			if (serviceObjects.length == 1) {
				com.zyb.nowplus.data.protocol.types.Identity identity = 
					(com.zyb.nowplus.data.protocol.types.Identity) serviceObjects[0];
		
				synchronized (actions) {
					int index = getIndexForAction(requestId);

					if (index == -1) {
						//#debug error
						System.out.println("Received response to stale request " + requestId + ", ignored.");
					}
					else {
						if (identity.isOperationSuccessful()) {
							actions[index].getNetwork().setAddRemoveFlag(ExternalNetwork.FLAG_REMOVING);
							
							destroyAction(index);
							services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_SUCCEEDED, null);
						}
						else {
							destroyAction(index);
							services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_FAILED, null);
						}
					}
				}
			}
			else {
				//#debug error
				System.out.println("Result of remove social network contains " + serviceObjects.length + " items, ignored.");		
			}
		}
	}		
	
	/**
	 * Logs in a social network.
	 */
	public void loginSocialNetworkAccount(Identity accountToLogin)
	{
		if (running && (accountToLogin != null)) {
			synchronized (actions) {
				ChatManagerAction action = null;

				if (accountToLogin.getType() == Identity.TYPE_SN_ACCOUNT) {
					action = createAction(ChatManagerAction.TYPE_LOGIN_SN_ACCOUNT);
				}
				else {
					action = createAction(ChatManagerAction.TYPE_LOGIN_IM_ACCOUNT);
				}

				action.setNetwork(accountToLogin.getNetwork());
				requestMyIdentities(action);
				
				//#debug info
				System.out.println("Request " + action.getRequestId() + ": get social network accounts to login ");
			}
		}
	}
	
	private void loginSocialNetworkAccount2(ChatManagerAction action)
	{
		com.zyb.nowplus.data.protocol.types.Identity serviceObject = 
			new com.zyb.nowplus.data.protocol.types.Identity(action.getNetwork().getPluginId(), action.getNetwork().getNetworkId(), action.getIdentityId(),
				com.zyb.nowplus.data.protocol.types.Identity.T_ENABLE, null);
		int requestId = services.getProtocol().sendRequest(ServerRequest.SET, ServerRequest.IDENTITIES,
			new ServiceObject[] {serviceObject}, null, ServerRequest.HIGH_PRIORITY, 40);
		action.setRequestId(requestId);
		
		//#debug info
		System.out.println("Request " + requestId + ": login social network account " + action.getNetwork());	
	}

	private void loginIMAccount2(ChatManagerAction action)
	{
		//#debug info
		System.out.println("Login im account " + action.getNetwork());
		
		String[] networkIds = {action.getNetwork().getNetworkId()};
		String[] names = {action.getIdentityId()};
		int[] presences = {Channel.PRESENCE_ONLINE};
		
		setPresence(presences, networkIds, names);							

		services.getMe().setPresences(networkIds, presences);
		services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_SUCCEEDED, null);
	}
	
	/**
	 * Logs out from a social network.
	 */	
	public void logoutSocialNetworkAccount(Identity accountToLogout)
	{
		if (running && (accountToLogout != null)) {
			synchronized (actions) {
				ChatManagerAction action = null;

				if (accountToLogout.getType() == Identity.TYPE_SN_ACCOUNT) {
					action = createAction(ChatManagerAction.TYPE_LOGOUT_SN_ACCOUNT);
				}
				else {
					action = createAction(ChatManagerAction.TYPE_LOGOUT_IM_ACCOUNT);
				}

				action.setNetwork(accountToLogout.getNetwork());
				requestMyIdentities(action);
				
				//#debug info
				System.out.println("Request " + action.getRequestId() + ": get social network accounts to logout ");
			}
		}
	}	
	
	private void logoutSocialNetworkAccount2(ChatManagerAction action)
	{
		com.zyb.nowplus.data.protocol.types.Identity serviceObject = 
			new com.zyb.nowplus.data.protocol.types.Identity(action.getNetwork().getPluginId(), action.getNetwork().getNetworkId(), action.getIdentityId(),
				com.zyb.nowplus.data.protocol.types.Identity.T_DISABLE, null);
		int requestId = services.getProtocol().sendRequest(ServerRequest.SET, ServerRequest.IDENTITIES,
			new ServiceObject[] {serviceObject}, null, ServerRequest.HIGH_PRIORITY, 40);
		action.setRequestId(requestId);
		
		//#debug info
		System.out.println("Request " + requestId + ": logout social network account " + action.getNetwork());	
	}

	private void logoutIMAccount2(ChatManagerAction action)
	{
		//#debug info
		System.out.println("Logout im account " + action.getNetwork());	
		
		String[] networkIds = {action.getNetwork().getNetworkId()};
		String[] names = {action.getIdentityId()};
		int[] presences = {Channel.PRESENCE_OFFLINE};
		
		setPresence(presences, networkIds, names);							
		
		services.getMe().setPresences(networkIds, presences);
		services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_SUCCEEDED, null);		
	}
	
	/**
	 * Handles the result of logging in or out.
	 */
	public void socialNetworkLoginLogoutResultReceived(int requestId, ServiceObject[] serviceObjects)
	{
		if (running) {
			if (serviceObjects.length == 1) {
				com.zyb.nowplus.data.protocol.types.Identity identity = 
					(com.zyb.nowplus.data.protocol.types.Identity) serviceObjects[0];

				synchronized (actions) {
					int index = getIndexForAction(requestId);

					if (index == - 1) {
						//#debug error
						System.out.println("Received response to stale request " + requestId + ", ignored.");
					}
					else {
						if (actions[index].getType() == ChatManagerAction.TYPE_LOGIN_SN_ACCOUNT) {							
							if (identity.isOperationSuccessful()) {			
								services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_SUCCEEDED, null);
							}
							else {
								services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_FAILED, null);
							}

							destroyAction(index);
						}
						else if (actions[index].getType() == ChatManagerAction.TYPE_LOGOUT_SN_ACCOUNT) {
							if (identity.isOperationSuccessful()) {								
								services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_SUCCEEDED, null);
							}
							else {
								services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_FAILED, null);
							}

							destroyAction(index);
						}				
					}
				}
			}
			else {
				//#debug error
				System.out.println("Result of login/logout social network contains " + serviceObjects.length + " items, ignored.");		
			}
		}
	}	
	
	public void meChangesReceived(boolean newMe)
	{
		if (running && newMe && tmpPresence != 0) {
			setMyPresences(tmpPresence);
		}
	}
		
	private void requestMyIdentities(ChatManagerAction action)
	{
		Hashtable filters = new Hashtable();
		Vector v = new Vector();
		v.addElement("get_own_status");
		v.addElement("chat");
		filters.put("capability", v);
		
		Hashtable params = new Hashtable();
		params.put("filterlist", filters);
		
		int requestId = services.getProtocol().sendRequest(ServerRequest.GET, ServerRequest.MY_IDENTITIES,
			null, params, ServerRequest.HIGH_PRIORITY, 40);	
		
		if (action == null) {
			//#debug info
			System.out.println("Request " + requestId + ": get my identities");
		}
		else {
			action.setRequestId(requestId);
		}
	}
	
	public void myIdentitiesReceived(int requestId, ServiceObject[] serviceObjects)
	{
		if (running) {
			synchronized (actions) {
				int index = getIndexForAction(requestId);

				if (index == -1) {
					myIdentitiesReceived2(requestId, serviceObjects);
				}
				else {
					if (actions[index].getType() == ChatManagerAction.TYPE_SET_PRESENCES) {
						MyProfile me = services.getMe();
						me.load(true);
						
						int presence = me.getNowPlusPresence();
						String[] networkIds = new String[serviceObjects.length + 1];
						String[] names = new String[serviceObjects.length + 1];
						int[] presences = new int[serviceObjects.length + 1];
						int len = 0;
						
						networkIds[len] = null;
						names[len] = Long.toString(me.getUserId());
						presences[len] = presence;
						len++;
						
						for (int i = 0; i < serviceObjects.length; i++) {
							com.zyb.nowplus.data.protocol.types.Identity serviceObject = 
								(com.zyb.nowplus.data.protocol.types.Identity) serviceObjects[i];
							String[] capabilities = serviceObject.getCapabilities();

							if (capabilities != null) {
								for (int j = 0; j < capabilities.length; j++) {
									if ("chat".equals(capabilities[j])) {
//										Identity chatAccount = me.getAccount(serviceObject.getNetwork());
//										
//										if ((chatAccount != null) && chatAccount.isOnline())
//										{
											networkIds[len] = serviceObject.getNetwork();
											names[len] = serviceObject.getIdentityID();
											presences[len] = presence;
											len++;
//										}
//										else {
//											//#debug info
//											System.out.println("Don't set " + serviceObject.getNetwork() + " to online: " + chatAccount);
//										}
										break;
									}
								} 
							}
						}

						if (len > 0) {
							networkIds = ArrayUtils.trimArray(networkIds, len);
							presences = ArrayUtils.trimArray(presences, len);
							names = ArrayUtils.trimArray(names, len);
							setPresence(presences, networkIds, names);							
							me.setMyPresences(networkIds, presences);
						}
						
						me.unload();
					}
//					else if (actions[index].getType() == ChatManagerAction.TYPE_SET_PRESENCE) {
//						MyProfile me = services.getMe();
//						me.load(true);
//						int presence = me.getNowPlusPresence();
//						String[] networkIds = new String[1];
//						String[] names = new String[1];
//						int[] presences = new int[1];
//						int len = 0;
//						networkIds[len] = null;
//						names[len] = Long.toString(me.getUserId());
//						presences[len] = presence;
//						len++;
//						
//						if (len > 0) {
//							networkIds = ArrayUtils.trimArray(networkIds, len);
//							presences = ArrayUtils.trimArray(presences, len);
//							names = ArrayUtils.trimArray(names, len);
//							services.getProtocol().setPresence(presences, networkIds, names);							
//							me.setMyPresences(networkIds, presences);
//						}
//					
//						me.unload();
//					}						
					else {
						for (int i = 0; (i < serviceObjects.length) && (actions[index].getIdentityId() == null); i++) {
							com.zyb.nowplus.data.protocol.types.Identity serviceObject = 
								(com.zyb.nowplus.data.protocol.types.Identity) serviceObjects[i];
							String networkId = ExternalNetwork.getStandardId(serviceObject.getNetwork());
							
							if (HashUtil.equals(networkId, actions[index].getNetwork().getNetworkId())) {
								actions[index].setIdentityId(serviceObject.getIdentityID());
							}
						}
						
						if (actions[index].getType() == ChatManagerAction.TYPE_REMOVE_ACCOUNT) {
							removeSocialNetworkAccount2(actions[index]);	
						}					
						else if (actions[index].getType() == ChatManagerAction.TYPE_LOGIN_SN_ACCOUNT) {
							loginSocialNetworkAccount2(actions[index]);	
						}
						else
						if (actions[index].getType() == ChatManagerAction.TYPE_LOGIN_IM_ACCOUNT)
						{
							loginIMAccount2(actions[index]);	
						}			
						else
						if (actions[index].getType() == ChatManagerAction.TYPE_LOGOUT_SN_ACCOUNT)
						{
							logoutSocialNetworkAccount2(actions[index]);	
						}
						else
						if (actions[index].getType() == ChatManagerAction.TYPE_LOGOUT_IM_ACCOUNT)
						{
							logoutIMAccount2(actions[index]);	
						}
					}
					
					destroyAction(index);
				}
			}					
		}
	}
	
	private void myIdentitiesReceived2(int requestId, ServiceObject[] serviceObjects)
	{
		MyProfile me = services.getMe();
		
		if (me != null)
		{
			me.load(true);
			
			Identity[] newIdentities = new Identity[serviceObjects.length];
			for (int i = 0; i < serviceObjects.length; i++)
			{
				com.zyb.nowplus.data.protocol.types.Identity serviceObject = (com.zyb.nowplus.data.protocol.types.Identity) serviceObjects[i];
				
				ExternalNetwork network = ExternalNetwork.manager.findNetworkById(serviceObject.getNetwork());
								
				if (network.hasCap("chat"))
				{
					newIdentities[i] = Identity.createImAccount(network, serviceObject.getUsername(), false, 0);
					
					Identity oldIdentity = me.getAccount(newIdentities[i].getNetwork());
					if (oldIdentity == null)
					{
						//#debug debug
						System.out.println("Set presence for new im account " + me.getFullName() + " " + newIdentities[i].getNetworkId() + " " + Channel.PRESENCE_ONLINE);
						
						newIdentities[i].setPresence(Channel.PRESENCE_ONLINE);
					}
					else
					{
						//#debug debug
						System.out.println("Copied presence for existing im account " + me.getFullName() + " " + newIdentities[i].getNetworkId() + " " + oldIdentity.getPresence());
						
						newIdentities[i].setPresence(oldIdentity.getPresence());
					}
				}
				else
				{
					newIdentities[i] = Identity.createSnAccount(network, serviceObject.getUsername(), serviceObject.getNetworkUrl(), 0);
					
					if (serviceObject.isActive())
					{
						//#debug debug
						System.out.println("Set presence for sn account " + me.getFullName() + " " + newIdentities[i].getNetworkId() + " " + Channel.PRESENCE_ONLINE);
						
						newIdentities[i].setPresence(Channel.PRESENCE_ONLINE);
					}
					else
					{
						//#debug debug
						System.out.println("Set presence for sn account " + me.getFullName() + " " + newIdentities[i].getNetworkId() + " " + Channel.PRESENCE_OFFLINE);
						
						newIdentities[i].setPresence(Channel.PRESENCE_OFFLINE);
					}
				}
			}
		
			me.setWebaccounts(newIdentities);
			me.unload();
		}
	}
	
	/**
	 * Sets the chat presence on the RPG.
	 * 
	 * @param presenceStates The states of the presence. States can be found in
	 * {@link com.zyb.nowplus.business.domain.Channel Channel}.
	 * @param networks The networks (e.g. facebook.com, google, etc.) to set the presence
	 * in. Defined in
	 * @param names The identifiers for a IM network. E.g. rudynorff@gmail.com. Ignored
	 * at the moment as only single identities of the same IM network are allowed per
	 * contact.
	 */
	private void setPresence(int[] presenceStates, String[] networks, String[] names) {
			
		ServiceObject[] serviceObjects = new ServiceObject[1];		
		serviceObjects[0] = new Presence(networks, presenceStates);
		
		int requestId = services.getProtocol().sendRequest(ServerRequest.SET, 
				ServerRequest.PRESENCE, serviceObjects, null, ServerRequest.LOW_PRIORITY);
		
		//#debug info
		System.out.println("Request " + requestId + ": set presences.");
	}
	
	public synchronized boolean errorReceived(int requestId, byte errorCode)
	{
		boolean handled = false;
		
		if (running)
		{
			synchronized (actions)
			{
				int index = getIndexForAction(requestId);

				if (index != -1) {
					if (actions[index].getType() == ChatManagerAction.TYPE_ADD_ACCOUNT)
					{
						destroyAction(index);
						
						services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_FAILED, null);		
						handled = true;
					}
					else
					if (actions[index].getType() == ChatManagerAction.TYPE_REMOVE_ACCOUNT)
					{
						destroyAction(index);
						
						services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_FAILED, null);		
						handled = true;
					}	
					else
					if ((actions[index].getType() == ChatManagerAction.TYPE_LOGIN_SN_ACCOUNT)
							|| (actions[index].getType() == ChatManagerAction.TYPE_LOGIN_IM_ACCOUNT))
					{
						destroyAction(index);

						services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_FAILED, null);		
						handled = true;
					}
					else
					if ((actions[index].getType() == ChatManagerAction.TYPE_LOGOUT_SN_ACCOUNT)
							|| (actions[index].getType() == ChatManagerAction.TYPE_LOGOUT_IM_ACCOUNT))
					{
						destroyAction(index);

						services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_FAILED, null);		
						handled = true;
					}					
				}
			}
		}

		return handled;
	}
	
	public synchronized boolean timeOutReceived(int requestId)
	{
		boolean handled = false;

		if (running)
		{
			synchronized (actions)
			{
				int index = getIndexForAction(requestId);

				if (index != -1) {
					if (actions[index].getType() == ChatManagerAction.TYPE_GET_NETWORKS)
					{
						//#debug info
						System.out.println("Request " + requestId + " for networks timed out, requested again.");
						
						requestNetworks(actions[index]);
						handled = true;
					}
					else
					if (actions[index].getType() == ChatManagerAction.TYPE_ADD_ACCOUNT)
					{
						destroyAction(index);
						
						services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.ADD_TIMED_OUT, null);		
						handled = true;
					}
					else
					if (actions[index].getType() == ChatManagerAction.TYPE_REMOVE_ACCOUNT)
					{
						destroyAction(index);
						
						services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE_TIMED_OUT, null);		
						handled = true;
					}	
					else
					if ((actions[index].getType() == ChatManagerAction.TYPE_LOGIN_SN_ACCOUNT)
						|| (actions[index].getType() == ChatManagerAction.TYPE_LOGIN_IM_ACCOUNT))
					{
						destroyAction(index);

						services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN_TIMED_OUT, null);		
						handled = true;
					}
					else
					if ((actions[index].getType() == ChatManagerAction.TYPE_LOGOUT_SN_ACCOUNT)
						|| (actions[index].getType() == ChatManagerAction.TYPE_LOGOUT_IM_ACCOUNT))
					{
						destroyAction(index);

						services.fireEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT_TIMED_OUT, null);		
						handled = true;
					}					
				}
			}
		}
		return handled;
	}
		
	/**
	 * Adds the chat accounts of the given profile to the chat account index.
	 */
	public void index(Profile profile)
	{		
		Identity[] imAccounts = profile.getIdentities(Identity.TYPE_IM_ACCOUNT);

		for (int i = 0; i < imAccounts.length; i++)
		{
			index(profile, imAccounts[i]);
		}
	}

	public void index(Profile profile, Identity imAccount)
	{
		if (imAccount.getNetworkId() != null && imAccount.getName() != null) {
			index.index(profile, imAccount.getNetworkId(), imAccount.getName());
		}
	}
	
	/**
	 * Sets my presences.
	 */
	public void setMyPresences(int presence)
	{
		if (running)
		{
			MyProfile me = services.getMe();
			if (me == null)
			{
				tmpPresence = presence;
			}
			else
			{
				me.setPresences(new String[] {"mobile"}, new int[] {presence});
			
				synchronized (actions)
				{
					ChatManagerAction action = createAction(ChatManagerAction.TYPE_SET_PRESENCES);
		
					int requestId = services.getProtocol().sendRequest(ServerRequest.GET, ServerRequest.MY_IDENTITIES, null, null, ServerRequest.HIGH_PRIORITY);	
					action.setRequestId(requestId);
					
					//#debug info
					System.out.println("Request " + requestId + ": get social network accounts to set me online ");
				}
				}
			}
		}
	
	private void requestPresences()
	{		
		int requestId = services.getProtocol().getPresences();
		
		//#debug info
		System.out.println("Request " + requestId + ": get presence");
	}
	
	private void invalidatePresences()
	{
		//#debug info
		System.out.println("Invalidating all online presences");

		services.invalidateAllOnlinePresences();
	}

	/**
	 * Handles changes of presence in my profile and contact profiles.
	 */
	public void presencesChanged(Presence presencesList)
	{	
		if (running) {
			String[] names = presencesList.getNames();
			
			if (names != null) {
				for (int i = 0; i < names.length; i++) {
					String[] networks = presencesList.getNetworks(i);
					int[] presences = presencesList.getPresences(i);

					if (networks != null && presences != null) {
						Profile profile = findProfile(networks[0], names[i]);

						if (profile == null) {
							contactsNotFound = true;
							
							//#debug error
							System.out.println("Presence received for unknown contact " + networks[0] + " / " + names[i]);
						}
						else {
							profile.setPresences(networks, presences);
						}
					}
				}
			}
		}
	}

	public void newContactsReceived()
	{
		if (contactsNotFound)
		{
			contactsNotFound = false;
			
			//#debug info
			System.out.println("Request presences again, because new contacts have arrived.");
			
			services.fireEvent(Event.Context.MODEL, Event.Model.PRESENCES_CHANGED, null);
		}
	}
	
	/**
	 * Starts a chat over the given channel.
	 */
	public void startConversation(Channel channel) throws LaunchException
	{
		if (running)
		{
//			if (channel.getPresence() != Channel.PRESENCE_ONLINE)
//			{
//				throw new LaunchException(LaunchException.TYPE_CHANNEL_NOT_ONLINE);
//			}
			if (channel.isOpen())
			{
				throw new LaunchException(LaunchException.TYPE_CHANNEL_ALREADY_OPEN);
			}
			String networkId = null;
			String name = null;
			
			if (ExternalNetwork.VODAFONE_360.equals(channel.getNetworkId()))
			{
				// Now+ Chat
				name = Long.toString(channel.getProfile().getUserId());
			}
			else
			{
				// third party chat
				networkId = channel.getNetworkId();
				name = channel.getName();
			}
			int requestId = services.getProtocol().sendCreateConversationRequest(networkId, name);
			
			//#debug info
			System.out.println("Request " + requestId + ": Start conversation over " + channel + " requested");

			channel.getProfile().setConversationId(channel.getNetworkId(), channel.getName(), null, TEMP_CONVERSATION_ID_PREFIX + requestId);
		}
	}
	
	/**
	 * Handles the result of starting a conversation.
	 */
	public void conversationStarted(int requestId, ServiceObject[] serviceObjects)
	{
		if (running)
		{
			if (serviceObjects.length == 1)
			{		
				ChatObject serviceObject = (ChatObject) serviceObjects[0];
				
				String network = serviceObject.getNetworks()[0];
				String name = serviceObject.getNames()[0];

				Profile profile = findProfile(network, name);
				if (profile == null)
				{
					//#debug error
					System.out.println("Conversation id received for unknown contact " + network + " / " + name);					
				}
				else
				{				
					Channel channel = profile.setConversationId(network, name, TEMP_CONVERSATION_ID_PREFIX + requestId, serviceObject.getConversationID());
					if (channel != null)
					{
						Message[] queuedMessages = channel.getQueuedMessages();
						
						int msgSize=(queuedMessages==null)?0:queuedMessages.length;//size is 0 or length 
						
							//#if !polish.remove_status_tab
							for (int i = 0; i < msgSize; i++)
								if(queuedMessages[i]!=null)
								{//created timeline item when message is waiting for be sent out to server
									services.getActivityManager().addTimelineActivity((ManagedProfile) profile, "", network, name, serviceObject.getConversationID(),channel);
									
									break;
								}
							//#endif
						for (int i = 0; i < msgSize; i++)
						{
							try
							{
								sendMessage(channel, queuedMessages[i]);
							}
							catch (LaunchException e)
							{
								//#debug error
								System.out.println("Failed to send queued message" + e);//TODO fail to send, delete timeline too?! or add comments to timeline
							}
						}
						
						//#debug info
						System.out.println("Started conversation over " + channel);						
					}
				}
			}
			else
			{
				//#debug error
				System.out.println("Conversation started contains " + serviceObjects.length + " items, ignored.");		
			}
		}
	}
	
	/**
	 * Sends a message over the given channel.
	 */
	public void sendMessage(Channel channel, Message message)
			throws LaunchException
	{
		if (running)
		{
			String conversationId = channel.getConversationId();
			
			if (conversationId == null)
				throw new LaunchException(LaunchException.TYPE_LAUNCH_FAILED);

			Profile profile = channel.getProfile();
			
			String name =  channel.getName();
			
			if (conversationId.startsWith(TEMP_CONVERSATION_ID_PREFIX))
			{
				message.setQueued(true);
				
				profile.addMessage(channel.getNetworkId(), channel.getName(),
						conversationId, message);

				//#debug info
				System.out.println("Queued " + message + " over " + channel);
			}
			else
			{
				message.setQueued(false);
				
				profile.addMessage(channel.getNetworkId(), channel.getName(),
						conversationId, message);

				String networkId = null;
				
				if (ExternalNetwork.VODAFONE_360.equals(channel.getNetworkId()))
				{
					// Now+ Chat
					name = Long.toString(channel.getProfile().getUserId());
				}
				else
				{
					// third party chat
					networkId = channel.getNetworkId();
					
					name = channel.getName();
				}

				int requestId = services.getProtocol().sendChatMessage(
						networkId, name, conversationId, message.getText());

				//#debug info
				System.out.println("Request " + requestId + ": Send " + message+ " over " + channel);
			}
			
			//#if !polish.remove_status_tab
			//update timeline chat log
			com.zyb.nowplus.business.domain.Activity macthedTimeLineChat=services.getActivityManager().updateTimelineActivity(channel,name,message.getTime(), message.getText());
			
			if(macthedTimeLineChat==null)//(ManagedProfile profile, String description, String networkId, String name, String conversationId)
				services.getActivityManager().addTimelineActivity((ManagedProfile) profile, message.getText(), channel.getNetworkId(), name, conversationId,channel);
			//#endif
			
			//#debug info
			System.out.println("update Chat Activity of TimeLine: [conversationId: " + conversationId + "; message: " + message.getText()+ " after sending a message over the given channel " + channel);
			
		}
	}
	
	/**
	 * Handles an incoming message.
	 */
	public void messageReceived(int requestId, ServiceObject[] serviceObjects)
	{
		if (running)
		{
			for (int i = 0; i < serviceObjects.length; i++)
			{
				ChatObject serviceObject = (ChatObject) serviceObjects[i];

				String network = serviceObject.getFromNetwork();
				
				String name = serviceObject.getFromName();

				Profile profile = findProfile(network, name);
				
				if (profile == null)
				{
					//#debug error
					System.out.println("Message received for unknown contact "+ network + " / " + name);
				}
				else
				{
					Message message = new Message(serviceObject.getBody(),System.currentTimeMillis(), false);

					Channel channel = profile.addMessage(network, name, serviceObject.getConversationID(), message);
					
					if (channel != null)
					{
						//#debug info
						System.out.println("Received " + message + " over "+ channel);

						String conversationId=channel.getConversationId();
						
						if (conversationId == null)
						{
							conversationId=serviceObject.getConversationID();
							
							profile.setConversationId(network, name, null,serviceObject.getConversationID());
							//#if !polish.remove_status_tab
							services.getActivityManager().addTimelineActivity((ManagedProfile) profile, "", network, name, conversationId,channel);
							//#endif
						}

						services.fireEvent(Event.Context.CHAT, Event.Chat.RECEIVED_MESSAGE, channel);
						
						//#if !polish.remove_status_tab
						//update timeline chat log
						services.getActivityManager().updateTimelineActivity(channel,name,message.getTime(), message.getText());
						//#endif
						
						//#debug info
						System.out.println("update Chat Activity of TimeLine: [conversationId: " + conversationId + "; message: " + message.getText()+ " from incoming message over the given channel " + channel);
					}
				}
			}
		}
	}
	
	/**
	 * Stops conversation over a given channel.
	 */
	public void stopConversation(Channel channel)
	{
		if (running)
		{
			if (channel.isOpen())
			{
				//#debug debug
				System.out.println("Stop conversation over " + channel);
				
				String networkId = null;
				String name = null;
				
				if (ExternalNetwork.VODAFONE_360.equals(channel.getNetworkId()))
				{
					// Now+ Chat			
					name = Long.toString(channel.getProfile().getUserId());
				}
				else
				{
					// third party chat
					networkId = channel.getNetworkId();
					name = channel.getName();
				}
	
				int requestId = services.getProtocol().sendStopConversationRequest(networkId, name, channel.getConversationId());
				
				//#debug info
				System.out.println("Request " + requestId + ": Stop conversation over " + channel + " requested");
				
				channel.getProfile().setConversationId(channel.getNetworkId(), channel.getName(), channel.getConversationId(), null);
			}
		}
	}
	
	private Profile findProfile(String network, String name)
	{
		Profile profile = null;
		long userId = -1;

		try {
			userId = Long.parseLong(name);
		} 
		catch (Exception e) {
			// Ignore.
		}
		
		if (userId == -1) {
			// third party chat
			if (network != null) {
				profile = index.get(network, name);
			}
		}
		else {
			// Now+ chat
			profile = Profile.manager.getProfileByUserId(userId);
		}		

		return profile;
	}
	
	private ChatManagerAction createAction(int type)
	{
		int index = actions.length - 1;

		while (index > -1) {
			if (actions[index] == null) {
				break;
			}

			index--;
		}

		actions[index] = new ChatManagerAction(type);
		return actions[index];
	}
	
	protected void destroyAction(int index)
	{
		this.actions[index] = null;
	}
	
	private int getIndexForAction(int requestId)
	{
		int index = actions.length - 1;
		while (index > -1)
		{
			if ((actions[index] != null) && (actions[index].getRequestId() == requestId))
			{
				break;
			}
			index--;
		}
		return index;
	}
	
//	private int getIndexForActionType(int type)
//	{
//		int i = actions.length - 1;
//		while (i > -1)
//		{
//			if ((actions[i] != null) && (actions[i].getType() == type))
//			{
//				break;
//			}
//			i--;
//		}
//		return i;
//	}
	
	//#mdebug error
	public String toString()
	{
		return "ChatManager[running=" + running 
			+ ",actions=" + ArrayUtils.toString(actions)
			+ "]";
	}	
	//#enddebug
}
