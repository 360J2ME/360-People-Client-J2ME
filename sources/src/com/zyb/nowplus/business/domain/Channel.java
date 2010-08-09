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

import de.enough.polish.io.Serializer;
import de.enough.polish.util.Comparator;

/**
 * A channel for communication using a certain identity.
 */
public class Channel 
{
	public static final int TYPE_CALL = 1;
	public static final int TYPE_SMS = 2;
	public static final int TYPE_MMS = 3;
	public static final int TYPE_CHAT = 4;
	public static final int TYPE_EMAIL = 5;
	public static final int TYPE_BROWSE = 6;
	
	// the presences are ordered
	public static final int PRESENCE_UNKNOWN = 0;
	public static final int PRESENCE_OFFLINE = 1;
	public static final int PRESENCE_INVISIBLE = 2;
	public static final int PRESENCE_IDLE = 3;
	public static final int PRESENCE_ONLINE = 4;

	/**
	 * Sorts messages from old to new.
	 */
	private static final Comparator COMPARATOR = new Comparator()
	{
		public int compare(Object o1, Object o2)
		{
			Message m1 = (Message) o1;
			Message m2 = (Message) o2;
			// m2.getTime() - m1.getTime() may not fit in an int  
			if (m1.getTime() == m2.getTime())
			{
				return 0;
			}
			else
			{
				if (m1.getTime() < m2.getTime())
				{
					return -1;
				}
				else
				{
					return 1;
				}
			}
		}
	};

	public static final int MAX_NUMBER_OF_MESSAGES = 18;	
	
	private Identity identity;
	private int type;
	private int presence;
	private String conversationId;
	private Message[] messages;
	private int messagesLen;
		
	Channel(Identity identity, int type)
	{
		this.identity = identity;
		this.type = type;
		
		this.messages = new Message[8];
		this.messagesLen = 0;
	}

	public Profile getProfile()
	{
		return identity.getProfile();
	}
	
	public String getNetworkId()
	{
		return identity.getNetworkId();
	}
	
	public String getName()
	{
		return identity.getName();
	}
	
	public String getUrl()
	{
		return identity.getUrl();
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setPresence(int presence)
	{
		this.presence = presence;
	}
	
	public int getPresence()
	{
		return presence;
	}
	
	public void setConversationId(String conversationId)
	{
		this.conversationId = conversationId;
	}
	
	public String getConversationId()
	{
		return conversationId;
	}

	public boolean isLoggedIn()
	{
		return this.identity.isLoggedIn();
	}

	public boolean isOpen()
	{
		return (conversationId != null);
	}
	
	public synchronized void addMessage(Message message)
	{
		for (int i = 0; i < messagesLen; i++)
		{
			if (messages[i].equals(message))
			{
				messages[i] = message;
				return;
			}
		}
		addMessage0(message);
	}
	
	private void addMessage0(Message message)
	{	
		if (messagesLen == MAX_NUMBER_OF_MESSAGES) 
		{
			messagesLen--;
			for (int i = 0; i < messagesLen; i++) 
			{
				messages[i] = messages[i + 1];
			}
		}
		if (messagesLen == messages.length)
		{
			messages = Message.extendArray(messages);
		}
		messages[messagesLen++] = message;
	}
	
	public synchronized Message[] getMessages()
	{
		Message[] selection = new Message[messagesLen];
		System.arraycopy(messages, 0, selection, 0, messagesLen);
		
		ArrayUtils.shellSort(selection, messagesLen, COMPARATOR);
		return selection;
	}
	
	public synchronized Message[] getQueuedMessages()
	{
		Message[] selection = new Message[messagesLen];
		int len = 0;
		for (int i = 0; i < messagesLen; i++)
		{
			if (messages[i].isQueued())
			{
				selection[len++] = messages[i];
			}
		}
		return Message.trimArray(selection, len);
	}
	
	synchronized void deserializeMessages(DataInputStream in) throws IOException
	{
		int len = in.readInt();
		for (int j = 0; j < len; j++)
		{
			Message message = new Message((String) Serializer.deserialize(in), in.readLong(), in.readBoolean());
			message.setQueued(in.readBoolean());
			
			addMessage0(message);
		}
	}
	
	synchronized void serializeMessages(DataOutputStream out) throws IOException
	{
		out.writeInt(messagesLen);
		for (int j = 0; j < messagesLen; j++)
		{
			Serializer.serialize(messages[j].getText(), out);
			out.writeLong(messages[j].getTime());
			out.writeBoolean(messages[j].isFromMe());
			out.writeBoolean(messages[j].isQueued());
		}
	}
	
	public boolean equals(Object o)
	{
		if (!(o instanceof Channel)) {
			return false;
		}

		Channel that = (Channel) o;
		return (this.identity.equals(that.identity) 
				&& (this.type == that.type) 
				&& (this.presence == that.presence)
				&& HashUtil.equals(this.conversationId, that.conversationId));
	}
	
	//#mdebug error
	public String toString()
	{
		return "Channel[identity=" + identity
			+ ",type=" + type
			+ ",presence=" + presence
			+ ",conversationId=" + conversationId
			+ ",messages=" + ArrayUtils.toString(messages)
			+ "]";
	}	
	//#enddebug
	
	/**
	 * Creates a new array of len elements. Elements 0..len-1 will be
	 * filled with src[0]..src[len-1].
	 */
	public static Channel[] trimArray(Channel[] src, int len)
	{
		Channel[] dst = new Channel[len];
		System.arraycopy(src, 0, dst, 0, len);
		return dst;
	}		
}
