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
package com.zyb.nowplus.data.email.commands;

import com.zyb.nowplus.data.email.response.EmailLoginResponse;
import com.zyb.nowplus.data.email.response.EmailResponse;

/**
 * 
 * @author Jens Vesti
 */
public class EmailLoginCommand extends EmailCommand {
	
	private final String host;
	private final int port;
	private final boolean useSSL;
	private final String username;
	private final String password;
	
	public EmailLoginCommand(final String host, final int port, final boolean useSSL,
			final String username, final String password) {

		this.host = host;
		this.port = port;
		this.useSSL = useSSL;
		this.username = username;
		this.password = password;
	}

	public String getUrl() {
		String protocol = (useSSL ? "ssl" : "socket");	
		return protocol + "://" + host + ":" + port;
	}
	
	public byte[][] getSerialisedCommand() {
		String command = this.getCurrentId()+" LOGIN "+username+" "+password;
		return new byte[][]{command.getBytes()};
	}

	public EmailResponse createResponse(String response) {
		return new EmailLoginResponse(response);
	}
	
	//#mdebug error
	public String toString() {
		return "EmailLoginCommand[host=" + host
		+ ";port=" + port
		+ ";useSSL=" + useSSL
		+ ";username=" + username
		+ ";password=" + password
		+ "]";
	}
	//#enddebug
}
