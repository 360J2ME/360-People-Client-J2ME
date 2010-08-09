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
//#condition !polish.remove_status_tab
package com.zyb.nowplus.business.domain;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.data.storage.DataRecord;

import de.enough.polish.io.Serializer;

/**
 * An activity represents an event in the Now+ network.
 */
public class Activity implements DataRecord
{
	public final static int TYPE_CALL_DIALED = 1;
	public final static int TYPE_CALL_RECEIVED = 2;
	public final static int TYPE_CALL_MISSED = 3;
	public final static int TYPE_CONTACT_SENT_STATUS_UPDATE = 4;
	public final static int TYPE_CONTACT_RECEIVED_STATUS_UPDATE = 5;
	public final static int TYPE_CONTACT_JOINED = 6;
	public final static int TYPE_CONTACT_FRIEND_INVITATION_SENT = 7;
	public final static int TYPE_CONTACT_FRIEND_INVITATION_RECEIVED = 8;
	public final static int TYPE_CONTACT_NEW_FRIENDS = 9;
	public final static int TYPE_CONTACT_WALL_POST_SENT = 10;
	public final static int TYPE_CONTACT_WALL_POST_RECEIVED = 11;
	public final static int TYPE_CONTACT_PROFILE_EMAIL_UPDATED = 12;
	public final static int TYPE_CONTACT_PROFILE_PHONE_UPDATED = 13;
	public final static int TYPE_CONTACT_PROFILE_ADDRESS_UPDATED = 14;
	public final static int TYPE_CONTACT_PROFILE_PICTURE_UPDATED = 15;
	public final static int TYPE_STORE_APPLICATION_PURCHASED = 16;
	public final static int TYPE_SN_ADDED = 17;
	public final static int TYPE_SN_ADDED_BY_FRIEND = 18;
	public final static int TYPE_SN_WALL_POST_SENT = 19;
	public final static int TYPE_SN_WALL_POST_RECEIVED = 20;
	public final static int TYPE_SN_MESSAGE_SENT = 21;
	public final static int TYPE_SN_MESSAGE_RECEIVED = 22;
	public final static int TYPE_SN_PHOTOS_POSTED = 23;
	public final static int TYPE_SN_VIDEOS_POSTED = 24;
	public final static int TYPE_SN_STATUS_SENT = 25;
	public final static int TYPE_SN_STATUS_RECEIVED = 26;
	public final static int TYPE_SN_CONTACT_PROFILE_EMAIL_UPDATED = 27;
	public final static int TYPE_SN_CONTACT_PROFILE_PHONE_UPDATED = 28;
	public final static int TYPE_SN_CONTACT_PROFILE_ADDRESS_UPDATED = 29;
	public final static int TYPE_MESSAGE_SMS_SENT = 30;
	public final static int TYPE_MESSAGE_SMS_RECEIVED = 31;
	public final static int TYPE_MESSAGE_MMS_SENT = 32;
	public final static int TYPE_MESSAGE_MMS_RECEIVED = 33;
	public final static int TYPE_MESSAGE_IM_CONVERSATION = 34;
	public final static int TYPE_MESSAGE_EMAIL_SENT = 35;
	public final static int TYPE_MESSAGE_EMAIL_RECEIVED = 36;
	public final static int TYPE_SHARE_ALBUM_SENT = 37;
	public final static int TYPE_SHARE_ALBUM_RECEIVED = 38;
	public final static int TYPE_SHARE_PHOTO_SENT = 39;
	public final static int TYPE_SHARE_PHOTO_RECEIVED = 40;
	public final static int TYPE_SHARE_VIDEO_SENT = 41;
	public final static int TYPE_SHARE_VIDEO_RECEIVED = 42;
	public final static int TYPE_SHARE_PHOTO_COMMENT_SENT = 43;
	public final static int TYPE_SHARE_PHOTO_COMMENT_RECEIVED = 44;
	public final static int TYPE_SHARE_PHOTO_MULTIPLE_SENT = 45;
	public final static int TYPE_SHARE_PHOTO_MULTIPLE_RECEIVED = 46;
	public final static int TYPE_SHARE_VIDEO_MULTIPLE_SENT = 47;
	public final static int TYPE_SHARE_VIDEO_MULTIPLE_RECEIVED = 48;
	public final static int TYPE_LOCATION_SENT = 49;
	public final static int TYPE_LOCATION_RECEIVED = 50;
	public final static int TYPE_LOCATION_SHARED_PLACEMARK_CREATED = 51;
	public final static int TYPE_LOCATION_SHARED_PLACEMARK_RECEIVED = 52;
	public final static int TYPE_LOCATION_PLACEMARK_CREATED = 53;
	public final static int TYPE_LOCATION_PLACEMARK_RECEIVED = 54;
	public final static int TYPE_MUSIC_PURCHASED_SONG = 55;
	public final static int TYPE_MUSIC_PURCHASED_ALBUM = 56;
	public final static int TYPE_MUSIC_DOWNLOADED_SONG = 57;
	public final static int TYPE_MUSIC_DOWNLOADED_ALBUM = 58;
	public final static int TYPE_MUSIC_DOWNLOADED_PLAYLIST = 59;
	public final static int TYPE_MUSIC_RATED_SONG = 60;
	public final static int TYPE_MUSIC_RATED_ALBUM = 61;
	public final static int TYPE_MUSIC_RECOMMENDATION_SENT_TRACK = 62;
	public final static int TYPE_MUSIC_RECOMMENDATION_SENT_ALBUM = 63;
	public final static int TYPE_MUSIC_RECOMMENDATION_SENT_PLAYLIST = 64;
	public final static int TYPE_MUSIC_RECOMMENDATION_SENT_TRACK_ANON = 65;
	public final static int TYPE_MUSIC_RECOMMENDATION_SENT_ALBUM_ANON = 66;
	public final static int TYPE_MUSIC_RECOMMENDATION_SENT_PLAYLIST_ANON = 67;
	public final static int TYPE_MUSIC_RECOMMENDATION_RECEIVED_TRACK = 68;
	public final static int TYPE_MUSIC_RECOMMENDATION_RECEIVED_ALBUM = 69;
	public final static int TYPE_MUSIC_RECOMMENDATION_RECEIVED_PLAYLIST = 70;
	public final static int TYPE_MUSIC_RECOMMENDATION_RECEIVED_TRACK_ANON = 71;
	public final static int TYPE_MUSIC_RECOMMENDATION_RECEIVED_ALBUM_ANON = 72;
	
	public static final int LOCAL_ID = -1;
	
	private long id;
	private int type;
	private String title;
	private String description;
	private ManagedProfile[] involvedContacts;	
	private String url;
	private long time;
	private ExternalNetwork source;
	
	// for activities of type message_im_conversation
	private String sourceName;
	private String conversationId;
	private Channel timeLineChannel;
	public Channel getTimeLineChannel()
	{
		return timeLineChannel;
	}

	
	public Activity()
	{
		// for serialisation
	}
	
	/**
	 * Creates an activity.
	 */
	public Activity(long id, int type, String title, String description, 
			ManagedProfile[] involvedContacts, String url, 
			long time, ExternalNetwork source)
	{
		this.id = id;
		this.type = type;
		this.title = title;		
		this.description = description;
		this.involvedContacts = involvedContacts;
		this.url = url;
		this.time = time;
		this.source = source;
	}
	
	/**
	 * Creates an activity of type message_im_conversation.
	 */
	public Activity(long id, String title, String description, 
			ManagedProfile chatBuddyContact, String url, 
			long time, ExternalNetwork source, 
			String sourceName, String conversationId,Channel timeLineChannel)
	{
		this(id, TYPE_MESSAGE_IM_CONVERSATION, title, description,
				new ManagedProfile[]{chatBuddyContact}, url, time, source);
		
		this.sourceName = sourceName;
		this.conversationId = conversationId;
		this.timeLineChannel=timeLineChannel;
	}
	
	
	public long getId()
	{
		return id;
	}
	
	public int getType() 
	{
		return type;
	}
	
	public String getTitle() 
	{
		return title;
	}
	
	public void setDescription(String description) 
	{
		this.description = description;
	}
	
	public String getDescription() 
	{
		return description;
	}
	
	public ManagedProfile[] getInvolvedContacts()
	{
		return involvedContacts;
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public void setTime(long time)
	{
		this.time = time;
	}
	
	public long getTime()
	{
		return this.time;
	}
	
	public ExternalNetwork getSource()
	{
		return source;
	}
	
	/**
	 * Gets the name of the contact on the external network.
	 */	
	public String getSourceName()
	{
		return sourceName;
	}
	
	public String getConversationId() 
	{
		return conversationId;
	}
	
	public void release() 
	{
		// no simple/full records, so this does nothing
	}
	
	public void read(DataInputStream in) throws IOException 
	{
		id = in.readLong();
		type = in.readInt();
		title = (String) Serializer.deserialize(in);	
		description = (String) Serializer.deserialize(in);
		long cabId = in.readLong();
		if (cabId == 0) 
		{
			involvedContacts = new ManagedProfile[0];
		}
		else 
		{
			involvedContacts = new ManagedProfile[] {Profile.manager.getProfileByCabId(cabId)};
		}
		url = (String) Serializer.deserialize(in);
		in.readLong();
		in.readLong();
		time = in.readLong();
		String networkId = (String) Serializer.deserialize(in);
		if (networkId != null)
		{
			source = ExternalNetwork.manager.findNetworkById(networkId);
		}
		if (type == TYPE_MESSAGE_IM_CONVERSATION) 
		{
			sourceName = (String) Serializer.deserialize(in);
			conversationId = (String) Serializer.deserialize(in);
		}
	}
	
	public void write(DataOutputStream out) throws IOException 
	{
		out.writeLong(id);
		out.writeInt(type);
		Serializer.serialize(title, out);
		Serializer.serialize(description, out);
		if (involvedContacts.length == 0)
		{
			out.writeLong(0);
		}
		else 
		{
			out.writeLong(involvedContacts[0].getCabId());
		}
		Serializer.serialize(url, out);
		out.writeLong(0L);
		out.writeLong(0L);
		out.writeLong(time);
		Serializer.serialize((source == null) ? null : source.getNetworkId(), out);
		if (type == TYPE_MESSAGE_IM_CONVERSATION) 
		{
			Serializer.serialize(sourceName, out);
			Serializer.serialize(conversationId, out);
		}
	}
	
	public boolean equals(Object obj) 
	{
		Activity activity = (Activity) obj;
		return getId() == activity.getId();
	}	
	
	//#mdebug error
	public String toString()
	{
		return "Activity[id=" + id
			+ ",type=" + type
			+ ",title=" + title
			+ ",description=" + description
			+ ",url=" + url
			+ ",time=" + time
			+ ",source=" + source
			+ ",sourceName=" + sourceName
			+ ",conversationId=" + conversationId
			+ "]";
	}
	//#enddebug
	
	/**
	 * Gives the type for the given type label.
	 */
	public static int toType(String typeLabel)
	{
		int type = 0;
		if("call_dialed".equals(typeLabel)){
			type=TYPE_CALL_DIALED;
		} else
		if("call_received".equals(typeLabel)){
			type=TYPE_CALL_RECEIVED;
		} else
		if("call_missed".equals(typeLabel)){
			type=TYPE_CALL_MISSED;
		} else
		if("contact_sent_status_update".equals(typeLabel)){
			type=TYPE_CONTACT_SENT_STATUS_UPDATE;
		} else
		if("contact_received_status_update".equals(typeLabel)){
			type=TYPE_CONTACT_RECEIVED_STATUS_UPDATE;
		} else
		if("contact_joined".equals(typeLabel)){
			type=TYPE_CONTACT_JOINED;
		} else
		if("contact_friend_invitation_sent".equals(typeLabel)){
			type=TYPE_CONTACT_FRIEND_INVITATION_SENT;
		} else
		if("contact_friend_invitation_received".equals(typeLabel)){
			type=TYPE_CONTACT_FRIEND_INVITATION_RECEIVED;
		} else
		if("contact_new_friends".equals(typeLabel)){
			type=TYPE_CONTACT_NEW_FRIENDS;
		} else
		if("contact_wall_post_sent".equals(typeLabel)){
			type=TYPE_CONTACT_WALL_POST_SENT;
		} else
		if("contact_wall_post_received".equals(typeLabel)){
			type=TYPE_CONTACT_WALL_POST_RECEIVED;
		} else
		if("contact_profile_email_updated".equals(typeLabel)){
			type=TYPE_CONTACT_PROFILE_EMAIL_UPDATED;
		} else
		if("contact_profile_phone_updated".equals(typeLabel)){
			type=TYPE_CONTACT_PROFILE_PHONE_UPDATED;
		} else
		if("contact_profile_address_updated".equals(typeLabel)){
			type=TYPE_CONTACT_PROFILE_ADDRESS_UPDATED;
		} else
		if("contact_profile_picture_updated".equals(typeLabel)){
			type=TYPE_CONTACT_PROFILE_PICTURE_UPDATED;
		} else
		if("store_application_purchased".equals(typeLabel)){
			type=TYPE_STORE_APPLICATION_PURCHASED;
		} else
		if("sn_added".equals(typeLabel)){
			type=TYPE_SN_ADDED;
		} else
		if("sn_added_by_friend".equals(typeLabel)){
			type=TYPE_SN_ADDED_BY_FRIEND;
		} else
		if("sn_wall_post_sent".equals(typeLabel)){
			type=TYPE_SN_WALL_POST_SENT;
		} else
		if("sn_wall_post_received".equals(typeLabel)){
			type=TYPE_SN_WALL_POST_RECEIVED;
		} else
		if("sn_message_sent".equals(typeLabel)){
			type=TYPE_SN_MESSAGE_SENT;
		} else
		if("sn_message_received".equals(typeLabel)){
			type=TYPE_SN_MESSAGE_RECEIVED;
		} else
		if("sn_photos_posted".equals(typeLabel)){
			type=TYPE_SN_PHOTOS_POSTED;
		} else
		if("sn_videos_posted".equals(typeLabel)){
			type=TYPE_SN_VIDEOS_POSTED;
		} else
		if("sn_status_sent".equals(typeLabel)){
			type=TYPE_SN_STATUS_SENT;
		} else
		if("sn_status_received".equals(typeLabel)){
			type=TYPE_SN_STATUS_RECEIVED;
		} else
		if("sn_contact_profile_email_updated".equals(typeLabel)){
			type=TYPE_SN_CONTACT_PROFILE_EMAIL_UPDATED;
		} else
		if("sn_contact_profile_phone_updated".equals(typeLabel)){
			type=TYPE_SN_CONTACT_PROFILE_PHONE_UPDATED;
		} else
		if("sn_contact_profile_address_updated".equals(typeLabel)){
			type=TYPE_SN_CONTACT_PROFILE_ADDRESS_UPDATED;
		} else
		if("message_sms_sent".equals(typeLabel)){
			type=TYPE_MESSAGE_SMS_SENT;
		} else
		if("message_sms_received".equals(typeLabel)){
			type=TYPE_MESSAGE_SMS_RECEIVED;
		} else
		if("message_mms_sent".equals(typeLabel)){
			type=TYPE_MESSAGE_MMS_SENT;
		} else
		if("message_mms_received".equals(typeLabel)){
			type=TYPE_MESSAGE_MMS_RECEIVED;
		} else
		if("message_im_conversation".equals(typeLabel)){
			type=TYPE_MESSAGE_IM_CONVERSATION;
		} else
		if("message_email_sent".equals(typeLabel)){
			type=TYPE_MESSAGE_EMAIL_SENT;
		} else
		if("message_email_received".equals(typeLabel)){
			type=TYPE_MESSAGE_EMAIL_RECEIVED;
		} else
		if("share_album_sent".equals(typeLabel)){
			type=TYPE_SHARE_ALBUM_SENT;
		} else
		if("share_album_received".equals(typeLabel)){
			type=TYPE_SHARE_ALBUM_RECEIVED;
		} else
		if("share_photo_sent".equals(typeLabel)){
			type=TYPE_SHARE_PHOTO_SENT;
		} else
		if("share_photo_received".equals(typeLabel)){
			type=TYPE_SHARE_PHOTO_RECEIVED;
		} else
		if("share_video_sent".equals(typeLabel)){
			type=TYPE_SHARE_VIDEO_SENT;
		} else
		if("share_video_received".equals(typeLabel)){
			type=TYPE_SHARE_VIDEO_RECEIVED;
		} else
		if("share_photo_comment_sent".equals(typeLabel)){
			type=TYPE_SHARE_PHOTO_COMMENT_SENT;
		} else
		if("share_photo_comment_received".equals(typeLabel)){
			type=TYPE_SHARE_PHOTO_COMMENT_RECEIVED;
		} else
		if("share_photo_multiple_sent".equals(typeLabel)){
			type=TYPE_SHARE_PHOTO_MULTIPLE_SENT;
		} else
		if("share_photo_multiple_received".equals(typeLabel)){
			type=TYPE_SHARE_PHOTO_MULTIPLE_RECEIVED;
		} else
		if("share_video_multiple_sent".equals(typeLabel)){
			type=TYPE_SHARE_VIDEO_MULTIPLE_SENT;
		} else
		if("share_video_multiple_received".equals(typeLabel)){
			type=TYPE_SHARE_VIDEO_MULTIPLE_RECEIVED;
		} else
		if("location_sent".equals(typeLabel)){
			type=TYPE_LOCATION_SENT;
		} else
		if("location_received".equals(typeLabel)){
			type=TYPE_LOCATION_RECEIVED;
		} else
		if("location_shared_placemark_created".equals(typeLabel)){
			type=TYPE_LOCATION_SHARED_PLACEMARK_CREATED;
		} else
		if("location_shared_placemark_received".equals(typeLabel)){
			type=TYPE_LOCATION_SHARED_PLACEMARK_RECEIVED;
		} else
		if("location_placemark_created".equals(typeLabel)){
			type=TYPE_LOCATION_PLACEMARK_CREATED;
		} else
		if("location_placemark_received".equals(typeLabel)){
			type=TYPE_LOCATION_PLACEMARK_RECEIVED;
		} else
		if("music_purchased_song".equals(typeLabel)){
			type=TYPE_MUSIC_PURCHASED_SONG;
		} else
		if("music_purchased_album".equals(typeLabel)){
			type=TYPE_MUSIC_PURCHASED_ALBUM;
		} else
		if("music_downloaded_song".equals(typeLabel)){
			type=TYPE_MUSIC_DOWNLOADED_SONG;
		} else
		if("music_downloaded_album".equals(typeLabel)){
			type=TYPE_MUSIC_DOWNLOADED_ALBUM;
		} else
		if("music_downloaded_playlist".equals(typeLabel)){
			type=TYPE_MUSIC_DOWNLOADED_PLAYLIST;
		} else
		if("music_rated_song".equals(typeLabel)){
			type=TYPE_MUSIC_RATED_SONG;
		} else
		if("music_rated_album".equals(typeLabel)){
			type=TYPE_MUSIC_RATED_ALBUM;
		} else
		if("music_recommendation_sent_track".equals(typeLabel)){
			type=TYPE_MUSIC_RECOMMENDATION_SENT_TRACK;
		} else
		if("music_recommendation_sent_album".equals(typeLabel)){
			type=TYPE_MUSIC_RECOMMENDATION_SENT_ALBUM;
		} else
		if("music_recommendation_sent_playlist".equals(typeLabel)){
			type=TYPE_MUSIC_RECOMMENDATION_SENT_PLAYLIST;
		} else
		if("music_recommendation_sent_track_anon".equals(typeLabel)){
			type=TYPE_MUSIC_RECOMMENDATION_SENT_TRACK_ANON;
		} else
		if("music_recommendation_sent_album_anon".equals(typeLabel)){
			type=TYPE_MUSIC_RECOMMENDATION_SENT_ALBUM_ANON;
		} else
		if("music_recommendation_sent_playlist_anon".equals(typeLabel)){
			type=TYPE_MUSIC_RECOMMENDATION_SENT_PLAYLIST_ANON;
		} else
		if("music_recommendation_received_track".equals(typeLabel)){
			type=TYPE_MUSIC_RECOMMENDATION_RECEIVED_TRACK;
		} else
		if("music_recommendation_received_album".equals(typeLabel)){
			type=TYPE_MUSIC_RECOMMENDATION_RECEIVED_ALBUM;
		} else
		if("music_recommendation_received_playlist".equals(typeLabel)){
			type=TYPE_MUSIC_RECOMMENDATION_RECEIVED_PLAYLIST;
		} else
		if("music_recommendation_received_track_anon".equals(typeLabel)){
			type=TYPE_MUSIC_RECOMMENDATION_RECEIVED_TRACK_ANON;
		} else
		if("music_recommendation_received_album_anon".equals(typeLabel)){
			type=TYPE_MUSIC_RECOMMENDATION_RECEIVED_ALBUM_ANON;
		} 
		return type;
	}	
	
	/**
	 * Extends an array.
	 */
	public static Activity[] extendArray(Activity[] src)
	{
		Activity[] dst = new Activity[src.length * 3 / 2 + 1];
		System.arraycopy(src, 0, dst, 0, src.length);
		return dst;
	}
}
