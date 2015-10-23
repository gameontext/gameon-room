/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package net.wasdev.gameon.room;

import javax.websocket.Session;
/*
 * Wraps a session object with useful things
 */
public class SessionWrapper {
	private final Session session;
	private final boolean newUser;
	private String name = null;
	private String id = null;
	
	public SessionWrapper(Session session) {
		this.session = session;
		newUser = session.getUserProperties().containsKey(Constants.USERNAME);
		if(!newUser) {
			name = session.getUserProperties().get(Constants.USERNAME).toString();
			id = session.getUserProperties().get(Constants.USERID).toString();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isNewUser() {
		return newUser;
	}
	
	
	
}
