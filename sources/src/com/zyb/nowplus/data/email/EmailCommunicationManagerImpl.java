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
package com.zyb.nowplus.data.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.zyb.nowplus.data.email.commands.EmailAppendMessageCommand;
import com.zyb.nowplus.data.email.commands.EmailCloseCommand;
import com.zyb.nowplus.data.email.commands.EmailCommand;
import com.zyb.nowplus.data.email.commands.EmailFetchFoldersCommand;
import com.zyb.nowplus.data.email.commands.EmailFetchMessagesCommand;
import com.zyb.nowplus.data.email.commands.EmailLoginCommand;
import com.zyb.nowplus.data.email.commands.EmailUseFolderCommand;
import com.zyb.nowplus.data.email.response.EmailResponse;
import com.zyb.nowplus.data.email.types.EmailMessage;
import com.zyb.util.Queue;
import com.zyb.util.SafeRunnable;
import com.zyb.util.SafeThread;

public class EmailCommunicationManagerImpl 
 	implements EmailCommunicationManager, SafeRunnable {
	
	private static final int INPUT_BUFFER_LEN = 256;
	private static final byte LF = 0x0A;
	private static final byte CR = 0x0D;
    public static final byte[] ENDLINE = new byte[] {LF, CR};
 
	private final SafeThread thread;
	private final Queue eventQueue;
   	
    private StreamConnection connection;
	private InputStream inbound;
	private OutputStream outbound;

    private final byte[] inputBuffer;
    private final StringBuffer inputBuffer2;
    private final StringBuffer inputBuffer3;

	private final Vector listeners;
	
	public EmailCommunicationManagerImpl() {
		thread = new SafeThread(this);
		eventQueue = new Queue();
		
		inputBuffer = new byte[INPUT_BUFFER_LEN];
		inputBuffer2 = new StringBuffer();
		inputBuffer3 = new StringBuffer();
		
		listeners = new Vector();
	}

	public void start()	{
		thread.start("email");
	}

	public void stop() {
		thread.stop();
	}
	
	public void openConnection(final String userName, final String passWord, 
			final String host, final int port, final boolean useSSL) {
		eventQueue.push(new EmailLoginCommand(host, port, useSSL, userName, passWord));
	}

	public void closeConnection() {
		eventQueue.push(new EmailCloseCommand());
	}

	public void fetchFolders() {
		eventQueue.push(new EmailFetchFoldersCommand());
	}

	public void useFolder(final String folder) {
		eventQueue.push(new EmailUseFolderCommand(folder));
	}

	public void fetchMessages(final String emailNumbers, final boolean useUids) {
		eventQueue.push(new EmailFetchMessagesCommand(emailNumbers,"all", useUids));
	}
	
	public void fetchMessage(final String emailNumber, final boolean useUids) {
		eventQueue.push(new EmailFetchMessagesCommand(emailNumber, "body[1]", useUids));
	}
	
	public void appendMessage(final String mailbox, final EmailMessage message) {
		eventQueue.push(new EmailAppendMessageCommand(mailbox, message));
	}
	
	public void init() {
	}

	public void work() {
		
		try	{			
			EmailCommand command = (EmailCommand) eventQueue.pop();
			if (command == null) {
				// ignore
			}
			else {
				//#debug debug
				System.out.println("Process " + command);
				
				try {
					if (command instanceof EmailLoginCommand) {
						try {
							connection = (StreamConnection)Connector.open(((EmailLoginCommand) command).getUrl(), Connector.READ_WRITE, true);
							inbound = connection.openInputStream();
							outbound = connection.openOutputStream();
						}
						catch (IOException e) {
							//#debug error
							System.out.println("Failed to open connection to email server." + e);
						}
						process(command);
					}
					else
					if (command instanceof EmailCloseCommand) {
						try	{
							connection.close();
						}
						catch (IOException e) {
							//#debug error
							System.out.println("Failed to close connection to email server." + e);
						}
						connection = null;
						inbound = null;
						outbound = null;
					}
					else {
						process(command);
					}
				}
				catch(IOException e) {
					//#debug error
					System.out.println("Failed sending/receiving command to/from server." + e);
					
					passResponseToListeners(command.createResponse("IOE"));
				}
			}
		}
		catch (InterruptedException e) {
			thread.stop();
		}
	}
	
	public void releaseMemory() {
	}

	public void cleanUp() {
	}
	
	private void process(EmailCommand command) throws IOException {
		
		if (connection == null) {
			passResponseToListeners(command.createResponse("NOCON"));
		}
		else {
			inputBuffer3.delete(0, inputBuffer3.length());
			
			byte[][] requests = command.getSerialisedCommand();
			String response = null;
			
			for (int i = 0; i < requests.length; i++)
			{
			    send(requests[i]);
			    
			    response = read();
			}
			
		    while (!response.startsWith(command.getCurrentId())) {
		    	inputBuffer3.append(response);
		    	inputBuffer3.append(" \n");
				response = read();
		    }
		    inputBuffer3.append(response);
		    
			passResponseToListeners(command.createResponse(inputBuffer3.toString()));
		}
	}

	private void send(final byte[] bytes) throws IOException {
		
		//#debug debug
		System.out.println("SENDING: " + new String(bytes));

        int length = bytes.length;

        if (0 == length) {
			outbound.write(ENDLINE, 0, 2); //Finished command
        }
        else {
            int totalSentBytes = 0;
            while (totalSentBytes<length) {
                int tmpSentbytes = totalSentBytes;
                while ((tmpSentbytes<length) && (bytes[tmpSentbytes]!=LF) && (bytes[tmpSentbytes]!=CR)) 
                    tmpSentbytes++;

                outbound.write(bytes, totalSentBytes, tmpSentbytes-totalSentBytes);
                outbound.write(ENDLINE, 0, 2);

                if ((tmpSentbytes<length-1) && (bytes[tmpSentbytes+1]==LF) && (bytes[tmpSentbytes]==CR)) 
                    tmpSentbytes++;

                totalSentBytes=tmpSentbytes+1;
            }
        }
    }
    
    private String read() throws IOException {
    	
    	inputBuffer2.delete(0, inputBuffer2.length());
    		
        boolean running = true;
        while (running) {
            int count = 0;
	        while (running && (count < INPUT_BUFFER_LEN)) {
	        	
                int actual = inbound.read(inputBuffer, count, 1);
	            if (actual == -1) {
	                throw new IOException("Connection disconnected");
	            }                
	            else 
	            if (actual == 0) {
	                try {
	                    Thread.yield();
	                }
	                catch (Exception e) {
	                	// ignore
	                }
	            }
	            else {
                    byte b = inputBuffer[count];
                    
                    if (b == CR) {
                    	// ignore
                    }
                    else 
                    if (b == LF) {
                        running = false;
                    }
                    else {
                        count++;
                    }
	            }
	        }
            inputBuffer2.append(new String(inputBuffer, 0, count));
        }
        
		//#debug debug
		System.out.println("RECEIVED: " + inputBuffer2);
		
        return inputBuffer2.toString();
    }
    
	public void addListener(EmailListener listener) {
		listeners.addElement(listener);
	}
	
	private void passResponseToListeners(final EmailResponse resp) {
		
		Enumeration elements = this.listeners.elements();
		while(elements.hasMoreElements())
		{
			EmailListener listener = (EmailListener) elements.nextElement();
			listener.emailResponseReceived(resp);
		}
	}
}
