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
package com.zyb.nowplus.data.protocol.response;

import com.zyb.nowplus.data.protocol.types.APIEvent;
import com.zyb.nowplus.data.protocol.types.Presence;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.nowplus.data.protocol.types.Update;


public interface ResponseListener {
	public static final String  MIME_JPEG = "image/jpeg",
								MIME_PNG = "image/png",
								MIME_GIF = "image/gif",
								MIME_MP3 = "audio/mpeg",
								MIME_WAV = "audio/x-wav",
								MIME_MPEG4 = "video/mpeg";
	
	public static final byte 	TYPE_JPEG = 0,
							 	TYPE_PNG = 1,
							 	TYPE_GIF = 2,
							 	TYPE_MP3 = 3,
							 	TYPE_WAV = 4,
							 	TYPE_MPEG4 = 5,
							 	TYPE_UNKNOWN = -1;	

	/**
	 * 
	 * Called whenever there is a change of a user's availability state or
	 * when the multiple users change to the same availability state at once.
	 * Can be a result of a get presence-request or a presence push.
	 * 
	 * @param requestID The ID of the request or 0 if it is a push message
	 * with no request.
	 * @param presence The presence object associated.
	 * 
	 */
	public void presenceChangeReceived(int requestID, Presence presence);	
	
	/**
	 * Called back when new items (contacts, messages, etc.) are received.
	 * 
	 * @param requestID The ID of the request that was sent off to the server.
	 * @param serviceObjects The objects: contacts, messages, activities, etc.
	 * @param type The type defined in @link{types.ServiceObject ServiceObject}
	 */
	public void itemsReceived(int requestID, ServiceObject[] serviceObjects, byte type);
	
	/**
	 * Called back when a new thumbnail is received.
	 * 
	 * @param requestID The ID of the request that was sent off to the server.
	 * @param data The data in bytes, e.g. an image or a sound.
	 * @param itemType The type of the item described in @link{ResponseListener ResponseListener}.
	 * 
	 */
	public void itemsReceived(int requestID, byte[] data, byte itemType);
	
	// error codes for the errorReceived message.
	public static final byte 	REQUEST_FAILED_UNKNOWN = 1,
								REQUEST_PARSING_FAILED = 2,
								REQUEST_FAILED_SENDING = 3,
								REQUEST_FAILED_INTERNAL_ERROR = 4,
								REQUEST_FAILED_TEMP_ERROR = 5,
								REQUEST_FAILED_NOT_IMPLEMENTED = 6,
								REQUEST_FAILED_INVALID_REQUEST = 7,
								REQUEST_FAILED_INVALID_PARAMETER = 8,
								REQUEST_HTTP_NOT_FOUND = 9,	// 404
								REQUEST_HTTP_FORBIDDEN = 10,	// 403
								REQUEST_TIMED_OUT = 11,	// time out
								REQUEST_SERVER_UNREACHABLE = 14,
								REQUEST_HTTP_OTHER_ERROR = 13;	// all others
								
	
	/**
	 * Called back when an error happened executing a request on the backend. Every 
	 * request is retried 3 times until this message is sent off.
	 *
	 * @param requestID  The ID of the request that was sent off to the server.
	 * @param errorCode The error code sent back.
	 * 
	 */
	public void errorReceived(int requestID, byte errorCode);	

	/**
	 * 
	 * Sends a push message to the response listener which can be e.g. a contacts 
	 * change, profile change, timeline change or a friendstream change, etc.
	 * 
	 * @param apiEvent The APIEvent sent by the backend.
	 */
	public void pushReceived(APIEvent apiEvent);
	
	/**
	 * 
	 * Callback to the model signaling that an update of the client is 
	 * ready on the server.
	 * 
	 * @param update The update object.
	 * 
	 */
	public void clientUpdateAvailable(Update update);
	
	/**
	 * 
	 * Called back when the client is up-to-date.
	 * 
	 */
	public void clientIsUpToDate();

    /**
	 * Called back when client is initialized.
	 */
	public void clientInitialized();

	/**
	 * 
	 * Called back from the connection as soon as the response comes
	 * in from the server.
	 * 
	 * @param msisdn A string containing the MSISDN or null if the 
	 * MSISDN was not retrieved.
	 * 
	 */
	public void msisdnReceived(String msisdn);
	
	/**
	 * 
	 * Called back whenever a call is carried out with the amount
	 * of data transmitted (send/receive).
	 * 
	 * @param dataCounter The amount of data sent and received.
	 */
	public void dataTransmitted(int dataCounter);
	
	/**
	 * 
	 * If isBusy returns true the ResponseListener's UI is
	 * busy and the connection must not go into low power 
	 * mode.
	 * 
	 * @return True if the UI of the ResponseListener is 
	 * busy and therefore the connection must not go into 
	 * low power mode.
	 * 
	 */
	public boolean isBusy();
}
