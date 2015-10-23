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

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.wasdev.gameon.room.common.Room;

/**
 * Manges the registration of all room providers with the concierge service
 */
@HandlesTypes(RoomProvider.class)
public class LifecycleManager implements ServletContainerInitializer {
	private static final String ENV_CONCIERGE_SVC = "service.concierge";
	private String conciergeLocation = null;
	
	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		getConfig();
		registerRooms(c);
	}
	
	private void getConfig() throws ServletException {
		conciergeLocation = System.getProperty(ENV_CONCIERGE_SVC, System.getenv(ENV_CONCIERGE_SVC));
		if(conciergeLocation == null) {
			throw new ServletException("The location for the concierge service cold not be "
					+ "found in a system property or environment variable named : " + ENV_CONCIERGE_SVC);
		}
	}

	private void registerRooms(Set<Class<?>> rooms) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(conciergeLocation);
		for(Class<?> room : rooms) {
			if(room.isInterface()) {
				continue;
			}
			try {
				Object obj = room.newInstance();
				if(obj instanceof RoomProvider) {
					RoomProvider provider = (RoomProvider) obj;
					Room instance = provider.getRoom();
					System.out.println("Registering room " + instance.getRoomName());
					Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
					Response response = builder.post(Entity.json(provider.getRoom()));
					try {
						if(Status.OK.getStatusCode()== response.getStatus()) {
							String resp = response.readEntity(String.class);
							System.out.println("Registered with UUID " + resp);
						} else {
							System.out.println("Error registering room provider : " + room.getName() + " : status code " + response.getStatus());
						}
					} finally {
						response.close();
					}
				}
			} catch (Exception e) {
				System.out.println("Error registering room provider : " + room.getName() + ", " + e.getMessage());
			}
		}
	}
	
}
