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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import de.enough.polish.util.zip.GZipInputStream;

import com.zyb.nowplus.data.protocol.CommunicationManagerImpl;
import com.zyb.nowplus.data.protocol.apihelpers.Toolkit;
import com.zyb.nowplus.data.protocol.hessian.MicroHessianInput;
import com.zyb.nowplus.data.protocol.request.RequestQueue;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.types.APIEvent;
import com.zyb.nowplus.data.protocol.types.ChatObject;
import com.zyb.nowplus.data.protocol.types.Presence;
import com.zyb.nowplus.data.protocol.types.ServiceObject;

/**
 * The incoming connection receives responses from the backend and pushes them
 * to this decoder, which adds it to a queue and eventually decodes it.
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 */
public class ResponseDecoder extends Thread {
	private static final int 	PAYLOAD_LEN_OFFSET = 11,
								PAYLOAD_LENGTH_END = 4,
								PAYLOAD_OFFSET = 1,
								RPG_HEADER_LENGTH = 16;
	
	public static final byte TYPE_API_EVENT = 0, 
								TYPE_IM = 1, 
	 							TYPE_AVAILABILITY = 2;
	private static final int UNKNOWN = -1;
	private static final int END_OF_STREAM = -1;
	
	private final CommunicationManagerImpl cmMgr;
	private final ResponseListener respListener;
	private final ResponseQueue queue;
	private boolean isRunning;

	public ResponseDecoder(CommunicationManagerImpl cmMgr,
			ResponseListener respListener) {
		super("response decoder");
		this.cmMgr = cmMgr;
		this.respListener = respListener;
		this.queue = new ResponseQueue(this);
		isRunning = true;
	}
	
	/**
	 * Stops the decoder thread.
	 */
	public void stopDecoder() {
		isRunning = false;
	}	
	
	/**
	 * 
	 * Adds a response to the queue for decoding. The running thread of the
	 * decoder will eventually take the response from the queue and decode it by
	 * invoking decodeResponseData().
	 * 
	 * @param responseData The data of the response in bytes. If the response is null it
	 * is not added to the queue.
	 * @param doAddRPGHeader True if the responseData needs to be enriched with the RPG
	 * header. E.g. if it was fetched from a synchronous call.
	 * 
	 * @return True if adding succeeded, false otherwise.
	 * 
	 */
	public boolean addResponseForDecoding(byte[] responseData) {
		//#debug info
		System.out.println(".............. Added Response to decoder " + responseData.length);
		
		return queue.addResponse(responseData);
	}
	
	/**
	 * 
	 * Adds a response of a synchronous call to the queue for decoding. 
	 * The running thread of the decoder will eventually take the response 
	 * from the queue and decode it by invoking decodeResponseData().
	 * 
	 * @param responseData The data of the response in bytes. If the response is null it
	 * is not added to the queue.
	 * @param requestID The ID of the synchronous request.
	 * 
	 * @return True if adding succeeded, false otherwise.
	 * 
	 */
	public boolean addResponseForDecoding(byte[] responseData, int requestID) {
		if (null == responseData) {
			return false;
		}
		
		byte[] data = new byte[responseData.length + RPG_HEADER_LENGTH];
			
		byte[] payloadSize = 
				Toolkit.intToSignedBytes(responseData.length);
		byte[] splitRequestID =
			Toolkit.intToSignedBytes(requestID);
		
		System.arraycopy(responseData, 0, data, 16, responseData.length);
		data[0] = ((byte) 0xFF);
		data[1] = ((byte) 0xFF);
		data[2] = ServerRequest.RPG_INTERNAL_MSG_RESPONSE;
		data[3] = splitRequestID[0];
		data[4] = splitRequestID[1];
		data[5] = splitRequestID[2];
		data[6] = splitRequestID[3];
		data[11] = payloadSize[0];
		data[12] = payloadSize[1];
		data[13] = payloadSize[2];
		data[14] = payloadSize[3];
		responseData = null;
		
		return queue.addResponse(data);
	}

	/**
	 * Takes a response and checks whether it contains multiple responses
	 * and whether each of the responses is GZIPped or not. If it is the
	 * response is decompressed. Each response is then decoded using the 
	 * MicroHessianInput-class.
	 * 
	 * The MicroHessianInput-class does a callback containing the decoded
	 * ServiceObjects.
	 * 
	 * @param responseData The data in bytes to decode from Hessian.
	 * 
	 */
	private void decodeResponse(byte[] responseData) {
		if (null == responseData) {
			return;
		}
		
		//#debug info
		Toolkit.printHessian(new java.io.ByteArrayInputStream(responseData));

		//#debug info
		System.out.println("Decoding response data...");
				
		int offset = 0;
		
		// decode responses
		while ((offset + PAYLOAD_LEN_OFFSET + PAYLOAD_LENGTH_END) 
												< responseData.length) {
			try {				
				offset += PAYLOAD_LEN_OFFSET + PAYLOAD_LENGTH_END;
				int payloadLen = Toolkit.signedBytesToInt(responseData[offset - 4], 
														  responseData[offset - 3],
														  responseData[offset - 2],
														  responseData[offset - 1]);
				//#debug info
				System.out.println("Decoding from " + (offset - PAYLOAD_LEN_OFFSET - PAYLOAD_LENGTH_END));

				// the payload length larger than the total response data? -> malformed response, let's break out!
				if (payloadLen > responseData.length) {
					//#debug error
					System.out.println("MALFORMED RESPONSE DATA, SKIPPING RESPONSE(S)!!!");
					responseData = null;
					return;
				}
				
				ByteArrayInputStream bais = new ByteArrayInputStream(responseData,
						(offset - PAYLOAD_LEN_OFFSET - PAYLOAD_LENGTH_END), 
						(payloadLen + RPG_HEADER_LENGTH));
				
				decodeSplitResponse(bais);
				
				offset += PAYLOAD_OFFSET + payloadLen;
			} catch (Exception ee) {
				//#debug error
				System.out.println("Exception " + ee);
			} 
		}		

		responseData = null;
	}
	
	private void decodeSplitResponse(InputStream is) {

		int tag = 0;
					
		try {
			// we read as long as we do not reach -1
			// inside we read 16 bytes for the jibe header and then pass
			// the input stream to the MicroHessianInput to parse
			// if availabe we then read the next jibe header etc.
			while ((tag = is.read()) != -1) {
				
				// ********** read 2 byte delimiter
				if (tag != 0xFF) {
					break;
				}
				tag = is.read();
				if (tag != 0xFF) {
					break;
				} // ********** end: read 2 byte delimiter
				
				
				// ********** read response type and ignore
				int responseType = is.read();
				if (responseType == END_OF_STREAM) {
					break;
				} // ********** end: read response type and ignore
				
				// ********** read request ID
				byte id1, id2, id3, id4;
				
				if ((tag = is.read()) == END_OF_STREAM) {
					break;
				} else {
					id1 = (byte) tag;
				}
				
				if ((tag = is.read()) == END_OF_STREAM) {
					break;
				} else {
					id2 = (byte) tag;
				}
				
				if ((tag = is.read()) == END_OF_STREAM) {
					break;
				} else {
					id3 = (byte) tag;
				}
				
				if ((tag = is.read()) == END_OF_STREAM) {
					break;
				} else {
					id4 = (byte) tag;
				}
				
				int requestID = Toolkit.signedBytesToInt(id1, id2, id3, id4);
				// ********** end read request ID
				
				// check if we have request, if not it timed out and we exit
				RequestQueue reqQueue = RequestQueue.getInstance();

				if (reqQueue != null
					&& !reqQueue.hasRequest(requestID)
					&& responseType != ServerRequest.RPG_MSG_PUSH) {
					//#debug error
					System.out.println("TIMEOUT!!! Request " + requestID + " (resp type: " + responseType + ") no longer in request queue as it was removed already by the timer.");
					
					try {
						is.close();
					}
					catch (Exception e) {
						//#debug error
						System.out.println("Could not close input stream;");
					}
					
					continue;	// continue with next request
				}		
				
				if (is.read() == END_OF_STREAM) {
					break;
				}

				if (is.read() == END_OF_STREAM) {
					break;
				}

				if (is.read() == END_OF_STREAM) {
					break;
				}

				if (is.read() == END_OF_STREAM) {
					break;
				}
				
				// ********** read payload size
				if ((tag = is.read()) == END_OF_STREAM) {
					break;
				} else {
					id1 = (byte) tag;
				}
				
				if ((tag = is.read()) == END_OF_STREAM) {
					break;
				} else {
					id2 = (byte) tag;
				}
				
				if ((tag = is.read()) == END_OF_STREAM) {
					break;
				} else {
					id3 = (byte) tag;
				}
				
				if ((tag = is.read()) == END_OF_STREAM) {
					break;
				} else {
					id4 = (byte) tag;
				}
				int payloadSize = Toolkit.signedBytesToInt(id1, id2, id3, id4);
				
				// ********** read GZIPped flag
				boolean isGzipped = false;
				
				if ((tag = is.read()) == END_OF_STREAM) {
					break;
				}
				
				if (tag == 1) {	// gzipped
					//#debug info
					System.out.println("Content is GZIPped!");
					isGzipped = true;
				}
				// ********** end: read GZIPped flag
										
				// get request queue, find message type (we need for
				// choosing decoding) and remove request
				RequestQueue requestQueue = RequestQueue.getInstance();
				int messageType = ServerRequest.UNKNOWN;
				byte noun = ServerRequest.UNKNOWN;
				byte verb = ServerRequest.UNKNOWN;

				if (null != requestQueue) {
					ServerRequest request = requestQueue.getRequest(requestID);
					
					if (null != request) {
						messageType = request.getMessageType();
						noun = request.getNoun();
						verb = request.getVerb();	
					}
					
					requestQueue.removeRequest(requestID);
					request = null;
				}	

				//#mdebug info
				StringBuffer sb = new StringBuffer("\n==================== Decoding RequestID = " + requestID + " ====================\n");

				switch (verb) {
					case ServerRequest.GET:
						sb.append("GET ");
						break;
					case ServerRequest.SET:
						sb.append("SET ");
						break;
					case ServerRequest.SEARCH:
						sb.append("SEARCH ");
						break;
					case ServerRequest.ADD:
						sb.append("ADD ");
						break;
					case ServerRequest.DELETE:
						sb.append("DELETE ");
						break;
					case ServerRequest.INVITE:
						sb.append("INVITE ");
						break;
					case ServerRequest.START:
						sb.append("START ");
						break;
					case ServerRequest.STOP:
						sb.append("STOP ");
						break;
					case ServerRequest.BULK_UPDATE:
						sb.append("BULK UPDATE ");
						break;
					default:
						sb.append("UNKNOWN (").append(verb).append(") ");
						break;
				}

				switch (noun) {
					case ServerRequest.CONTACTS:
						sb.append("CONTACTS");
						break;
					case ServerRequest.CONTACTS_CHANGES:
						sb.append("CONTACTS CHANGES");
						break;
					case ServerRequest.ACTIVITIES:
						sb.append("ACTIVITIES");
						break;
					case ServerRequest.MESSAGES:
						sb.append("MESSAGES");
						break;
					case ServerRequest.USER_PROFILES:
						sb.append("USER PROFILES");
						break;
					case ServerRequest.CONVERSATION:
						sb.append("CONVERSATION");
						break;
					case ServerRequest.CHAT_MESSAGES:
						sb.append("CHAT MESSAGES");
						break;
					case ServerRequest.BUDDY:
						sb.append("BUDDIES");
						break;
					case ServerRequest.FRIENDS_OF_FRIENDS:
						sb.append("FRIENDS OF FRIENDS");
						break;
					case ServerRequest.IDENTITIES:
						sb.append("IDENTITIES");
						break;
					case ServerRequest.MY_IDENTITIES:
						sb.append("MY IDENTITIES");
						break;
					case ServerRequest.IMAGES:
						sb.append("IMAGES");
						break;
					case ServerRequest.PRESENCE:
						sb.append("PRESENCE");
						break;
					case ServerRequest.CONTACT_GROUP_RELATIONS:
						sb.append("CONTACT GROUP RELATIONS");
						break;
					case ServerRequest.IDENTITY_STATUS:
						sb.append("IDENTITY STATUS");
						break;
					case ServerRequest.GROUPS:
						sb.append("GROUPS");
						break;
					case ServerRequest.ME:
						sb.append("ME");
						break;
					case ServerRequest.MY_CHANGES:
						sb.append("MY CHANGES");
						break;
					default:
						sb.append("UNKNOWN (").append(noun).append(")");
						break;
				}

				sb.append("\n");
				sb.append("RPG Message Request Type: " + messageType + " (1=ext req; 4=int req; 7=set avail; 8=send im; 9=strt conv; 10=stop conv; 11=get pres)\n");
				sb.append("RPG Message Response Type: " + responseType + " (2=ext resp; 6=int resp; 12=pres resp)");

				System.out.println(sb.toString());
				//#enddebug
		
				// if response type is an internal RPG request, a push or 
				// contacts request we hessian decode it
				switch (responseType) {
					case ServerRequest.RPG_MSG_PUSH:
						decodeMessagePush(isGzipped, is, requestID, payloadSize);
						break;
					case ServerRequest.RPG_INTERNAL_MSG_RESPONSE:
						// internal: activities, contacts, messages, etc.
						decodeInternalMessageResponse(isGzipped, is, requestID,
								verb, noun, payloadSize);
						break;
					case ServerRequest.RPG_MSG_RESPONSE:
						// normal message response, thumbnails, audio etc.
						decodeMessageResponse(isGzipped, is, requestID,
								verb, noun, payloadSize);
						break;
					case ServerRequest.RPG_CHAT_GET_PRESENCE_RESPONSE:
						// presence response
						decodePresenceResponse(isGzipped, is, requestID,
								verb, noun, payloadSize);
						break;
					case ServerRequest.RPG_TCP_SEND_TEST_RESPONSE:
						// tcp test response
						decodeTcpTestResponse(is, payloadSize, requestID);
						break;
					default:
						//#debug error
						System.out.println("ServerRequest Type UNKNOWN");
						
						respListener.errorReceived(requestID, ResponseListener.REQUEST_FAILED_UNKNOWN);
						break;
				}
				
				//#debug info
				System.out.println("\n===================== END DECODE " + requestID + " =====================");
			}
		} catch (Exception e) {
			//#debug error
			System.out.println("Parsing the response failed!" + e);

			//#debug info
			System.out.println("\n===================== END DECODE =====================");
		}
			
		try {
			is.close();
		}
		catch (IOException ioe) {
			//#debug error
			System.out.println("Could not close InputStream");

			//#debug info
			System.out.println("\n===================== END DECODE =====================");
		}
		finally {
			is = null;
		}
	}

	/**
	 * 
	 * Decodes a presence response. It contains presence updates for one
	 * or multiple users.
	 * 
	 * @param isGzipped True if the content is gzipped, false otherwise.
	 * @param is The InputStream to read from.
	 * @param requestID The request ID the response is coming from.
	 * @param verb The verb of the request. Used for the hessian parser.
	 * @param noun The noun of the request. Used for the hessian parser.
	 * @param payloadSize The size of the Hessian payload.
	 * 
	 */
	private void decodePresenceResponse(boolean isGzipped, 
									InputStream is, int requestID,
									byte verb, byte noun, int payloadSize) {
		MicroHessianInput mhi = null;
		
		//#debug debug
		System.out.println("Looking at Internal or Contact Request");
		
		InputStream usedIS = getResponseInputStream(is, isGzipped);
		
		if(null != usedIS) {	// not gzipped
			//#debug debug
			System.out.println("Init Hessian");
			
			mhi = new MicroHessianInput(usedIS, cmMgr, respListener, 
									requestID, true, verb, noun);
		}
	
		if (null != mhi) {
			//#debug debug
			System.out.println("Starting Hessian");

			Hashtable ht = null;
			
			try {
				if (!isGzipped) {
					mhi.setPayloadSize(payloadSize);
				}
				mhi.startReply();
				ht = (Hashtable) mhi.readObject();
				mhi.completeReply();
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not read Presence.");
				
				respListener.errorReceived(requestID, 
						ResponseListener.REQUEST_PARSING_FAILED);
			}
			
			if (null != ht) {
				APIEvent apiEvt = new APIEvent();
				apiEvt.parseAPIEvent(ht);
				Object o = apiEvt.getServiceObject();
				
				if (null != o) {
					Presence p = (Presence) o;
					
					if (null != p) {
						respListener.presenceChangeReceived(requestID, p);
					} else {
						respListener.errorReceived(requestID, 
								ResponseListener.REQUEST_PARSING_FAILED);
					}
				} else {
					respListener.errorReceived(requestID, 
							ResponseListener.REQUEST_PARSING_FAILED);
				}
			} else {
				respListener.errorReceived(requestID, 
						ResponseListener.REQUEST_PARSING_FAILED);
			}
		} else {
			respListener.errorReceived(requestID, 
					ResponseListener.REQUEST_PARSING_FAILED);
		}
	}
	
	
	/**
	 * Decodes an internal message response. Internal message responses are responses
	 * that are a result of an internal message request or in the case of contacts
	 * contact requests. Internal message requests are sent through the RPG to the 
	 * VF APIs directly.
	 * 
	 * @param isGzipped True if the content is gzipped, false otherwise.
	 * @param is The InputStream to read from.
	 * @param requestID The request ID the response is coming from.
	 * @param verb The verb of the request. Used for the hessian parser.
	 * @param noun The noun of the request. Used for the hessian parser.
	 * @param payloadSize The size of the Hessian payload.
	 * 
	 */
	private void decodeInternalMessageResponse(boolean isGzipped, 
									InputStream is, int requestID,
									byte verb, byte noun, int payloadSize) {
		MicroHessianInput mhi = null;
		
		//#debug debug
		System.out.println("Looking at Internal or Contact Request");
		
		InputStream usedIS = getResponseInputStream(is, isGzipped);
		
		if(null != usedIS) {	// not gzipped
			//#debug debug
			System.out.println("Init Hessian");	
			
			// #mdebug
			/*Toolkit.printHessian(usedIS);
			try {
				usedIS.reset();
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}*/
			// #enddebug
			
			mhi = new MicroHessianInput(usedIS, cmMgr, respListener, 
					requestID, false, verb, noun);
		}
	
		if (null != mhi) {
			//#debug debug
			System.out.println("Starting Hessian");			
			try {
				if (!isGzipped) {
					mhi.setPayloadSize(payloadSize);
				}
				mhi.startReply();
				mhi.readObject();
			} catch (Exception e) {
				//#debug error
				System.out.println("Failed parsing request" + e);
				
				respListener.errorReceived(requestID, 
						ResponseListener.REQUEST_PARSING_FAILED);
			}
		} else {
			respListener.errorReceived(requestID, 
					ResponseListener.REQUEST_PARSING_FAILED);
		}
	}
	
	/**
	 * Decodes a TCP test response.
	 * 
	 * @param is The InputStream to read from.
	 * @param payloadSize The size of the Hessian payload.
	 * @param requestID The request ID the response is coming from.
	 * 
	 */
	private void decodeTcpTestResponse(InputStream is, 
										int payloadSize, int requestID) {
		//#debug debug
		System.out.println("Looking at Tcp Test Request");
		
		if (payloadSize == 8) {
			respListener.itemsReceived(requestID, new ServiceObject[1], 
										ServiceObject.TCP_TEST_PACKET);
		} else {
			respListener.errorReceived(requestID, ResponseListener.TYPE_UNKNOWN);
		}
	}
	
	
	/**
	 * 
	 * Decodes a message push used for generic updates on the API, chat
	 * and availability updates.
	 * 
	 * @param isGzipped True if the content is gzipped, false otherwise.
	 * @param is The InputStream to read from.
	 * @param payloadSize The size of the Hessian payload.
	 * 
	 */
	private void decodeMessagePush(boolean isGzipped, InputStream is, int requestID,
									int payloadSize) {
		//#debug debug
		System.out.println("Message Push");
	
		//byte itemType = ResponseListener.TYPE_UNKNOWN;
		InputStream usedIS = getResponseInputStream(is, isGzipped);
		
		if (null != usedIS) {			
			MicroHessianInput mhi = null;
			
			try {
				// instantiate with no callbacks
				mhi = new MicroHessianInput(usedIS, cmMgr, null, -1, true,
												ServerRequest.UNKNOWN, 
												ServerRequest.UNKNOWN);
			} catch (Exception be) {
				//#debug error
				System.out.println("Failed reading object bytes");
				
				// we don't send back any error msg as it would confuse the user
				// in a push message
			}
				
			Hashtable htPush = null;
			
			// int: http status; string: mime-type; hessian bytes: payload
			try {
				if (!isGzipped) {
					mhi.setPayloadSize(payloadSize);
				}
				mhi.startReply();
				htPush = (Hashtable) mhi.readObject();
				mhi.completeReply();
			} catch (Exception e) {
				//#debug error
				System.out.println("Failed parsing Hessian response");
			}
		
			if (null != htPush) {
				//#debug info
				System.out.println("Parsing API Event");
				APIEvent apiEvt = new APIEvent();
				apiEvt.parseAPIEvent(htPush);
				ServiceObject so = apiEvt.getServiceObject();
				int apiEventType = apiEvt.getType();
			
				if (null != so) {
					switch (apiEventType) {
						case APIEvent.CHAT_MESSAGE:
							//#debug info
							System.out.println("Parsing Chat Object");
							
							ChatObject co = null;
							try {
								co = (ChatObject) so;
							} catch (Exception e) {
								//#debug error
								System.out.println("Could not get ChatObject...");
							}
						
							if (null != co) {						
								//#debug info
								System.out.println("Finished parsing IM. ConvID"
											+ co.getConversationID() + " body " + co.getBody());
							
								//#debug info
								System.out.println("Calling back response " +
														"listener with IM.");
								
								respListener.itemsReceived(requestID, new ServiceObject[] {co}, 
										ServiceObject.CHAT_MESSAGE);
							}
							break;
						case APIEvent.PRESENCE:
							//#debug info
							System.out.println("Parsing Presence Push");
							
							Presence presence = null;
							try {
								presence = (Presence) so;
							} catch (Exception e) {
								//#debug error
								System.out.println("Could not get Presence Push...");
							}
							
							if (null != presence) {
								// the model expects a 0 as a presence push. ServerRequest.UNKNOWN is 0
								respListener.presenceChangeReceived(UNKNOWN, presence);
							}
							break;
						default:
							//#debug info
							System.out.println("Calling back default push! Type: " + apiEvt.getType());
							respListener.pushReceived(apiEvt);
							break;
					}
				} else {
					//#debug info
					System.out.println("Calling back default push! Type: " + apiEvt.getType());
					respListener.pushReceived(apiEvt);
				}
			}
		}
	}
	
	/**
	 * Decodes a normal message response used for thumbnails, audio and
	 * other binary data.
	 * 
	 * @param isGzipped True if the content is gzipped, false otherwise.
	 * @param is The InputStream to read from.
	 * @param requestID The request ID the response is coming from.
	 * @param verb The verb of the request. Used for the hessian parser.
	 * @param noun The noun of the request. Used for the hessian parser.
	 * @param payloadSize The size of the Hessian payload.
	 * 
	 */
	private void decodeMessageResponse(boolean isGzipped, 
									InputStream is, int requestID,
									byte verb, byte noun, int payloadSize) {
		//#debug info
		System.out.println("Thumbnail, Audio, etc. Request");
	
		byte itemType = ResponseListener.TYPE_UNKNOWN;
		// IS to be used after decompression
		InputStream usedIS = getResponseInputStream(is, isGzipped);

		if (null != usedIS) {
			String mimeType = null;
			byte[] payload = null;
			int responseCode = -1;
			MicroHessianInput mhi = null;
			
			try {
				//#debug debug
				System.out.println("Reading Hessian.");
				
				// instantiate with no callbacks
				mhi = new MicroHessianInput(usedIS, cmMgr, null, -1, true,
												verb, noun);
				if (!isGzipped) {
					mhi.setPayloadSize(payloadSize);
				}
			} catch (Exception be) {
				//#debug error
				System.out.println("Failed reading object bytes");
				
				respListener.errorReceived(requestID, 
								ResponseListener.REQUEST_PARSING_FAILED);
			}
				
			// int: http status; string: mime-type; hessian bytes: payload
			try {
				responseCode = mhi.readInt();
			} catch (Exception e) {
				//#debug error
				System.out.println("Failed parsing response code.");
			}
			try {
				mimeType = mhi.readString();
			} catch (Exception e) {
				//#debug error
				System.out.println("Failed parsing mime type.");
			}
			try {
				payload = mhi.readBytes();
			} catch (Exception e) {
				//#debug error
				System.out.println("Failed parsing payload bytes.");
			}
		
			//#debug debug
			System.out.println("ResponseCode: " + responseCode);
			
			if ((responseCode == 200) || (responseCode == 302)) {
				if (null != mimeType) {
					//#debug info
					System.out.println("mime type:" + mimeType);
					
					if (mimeType.equals(ResponseListener.MIME_JPEG)) {
						itemType = ResponseListener.TYPE_JPEG;
					} else if (mimeType.equals(
								ResponseListener.MIME_PNG)) {
						itemType = ResponseListener.TYPE_PNG;
					} else if (mimeType.equals(
								ResponseListener.MIME_GIF)) {
						itemType = ResponseListener.TYPE_GIF;
					} else if (mimeType.equals(
								ResponseListener.MIME_MP3)) {
						itemType = ResponseListener.TYPE_MP3;
					} else if (mimeType.equals(
								ResponseListener.MIME_WAV)) {
						itemType = ResponseListener.TYPE_WAV;
					} else if (mimeType.equals(
								ResponseListener.MIME_MPEG4)) {
						itemType = ResponseListener.TYPE_MPEG4;
					}
				}
				
				mimeType = null;
				
				respListener.itemsReceived(requestID, 
								payload, itemType);
			} else {	// wrong response code, send error
				byte errorCode = ResponseListener.REQUEST_HTTP_OTHER_ERROR;
				
				if (responseCode == 404) {
					errorCode = ResponseListener.REQUEST_HTTP_NOT_FOUND;
				} else if (responseCode == 403) {
					errorCode = ResponseListener.REQUEST_HTTP_FORBIDDEN;
				} else {
					errorCode = ResponseListener.REQUEST_HTTP_OTHER_ERROR;
				}
				
				respListener.errorReceived(requestID, errorCode);
			}
		}
	}
	
	/**
	 * Returns the correct input stream depending on whether isGzipped is
	 * true or not. If true, the GZipInputStream will be used.
	 * 
	 * @param is The InputStream. Must not be null.
	 * @param isGzipped True if the InputStream is Zipped. False otherwise.
	 * 
	 * @return The InputStream or null if an error occured.
	 */
	private final InputStream getResponseInputStream(InputStream is, boolean isGzipped)
	{
		if (null == is) {
			return null;
		}
		
		if (!isGzipped) {
			//#debug debug
			System.out.println("Using common InputStream.");
			
			return is;
		}
		else {			// gzipped
			//#debug debug
			System.out.println("Using GZipInputStream.");
			
			GZipInputStream gis = null;
			
			try {
				gis = new GZipInputStream(is, 
									GZipInputStream.TYPE_GZIP, true);
			}
			catch (Exception e) {
				//#debug error
				System.out.println("Could not create GZIPInputStream...");
			}

			return gis;
		}
	}

	/**
	 * Returns number of elements in response queue.
	 * 
	 * @return The number of elements in the queue.
	 */
	public int getNumberOfElementsInQueue()
	{
		return queue.getNumberOfElementsInQueue();
	}
	
	/**
	 * 
	 * Gets the response queue that the decoder holds
	 * for getting fresh responses.
	 * 
	 * @return The response queue.
	 */
	public ResponseQueue getResponseQueue()
	{
		return queue;
	}
	
	public void run()
	{
		//#debug info
		System.out.println("Starting response decoder...");
		
		byte[] responseData = null;
		isRunning = true;

		while (isRunning) {			
			try {
				responseData = queue.getNextResponse();
	
				// if we still have responses to decode
				if (null != responseData) {
					decodeResponse(responseData);
					responseData = null;
				}
				else { // no further responses to decode at the moment					
					try {
						synchronized (this) {
							wait();
						}
					}
					catch (InterruptedException e) {
						//#debug error
						System.out.println("Could not wait in ResponseDecoder...");

						try {
							Thread.sleep(500);
						}
						catch (Exception e2) {
							//#debug error
							System.out.println("Exception " + e2);
						}
					}
					/*try { // sleep for 500ms and poll again
						Thread.sleep(500);
					} catch (Exception e) {
					}*/
				}
			}
			catch(Throwable t) {
				//#debug error
				System.out.println("Caught an error: " + t);
				
				try {
					Thread.sleep(500);
				}
				catch (Exception e) {
					//#debug error
					System.out.println("Exception " + e);
				}
			}
		}
	}
	
	public void notifyOfResponse()
	{
		synchronized (this) {
			notify();
		}
	}
}
