/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
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

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;

/*
 * Class specifically designed to authenticate the headers presented when establishing a web socket.
 */
public class GameOnWSHandshakeAuth extends GameOnHeaderAuth {
    private final HandshakeRequest request;
    private final HandshakeResponse response;
    private boolean validated = false;

    public GameOnWSHandshakeAuth(String secret, HandshakeRequest request, HandshakeResponse response) {
        super(secret, null);
        this.request = request;
        this.response = response;
    }

    //modify the handshake response if required e.g. in the event of a validation failure
    public void validate() {
        if(validated) {
            return;
        }
        validated = true;       //only need to validate this once
        Log.log(Level.INFO, this, "Validating WS handshake");
        if((secret == null) || secret.isEmpty()) {
            //no security, so no modification of the request
            Log.log(Level.INFO, this, "No token set for room, skipping validation");
            return;
        }
        String dateValue = getSingletonHeader("gameon-date");
        String hmac = getSingletonHeader("gameon-signature");
        if((dateValue == null) || (hmac == null)) {
            return;     //something wrong with one of the headers
        }
        try {
            Log.log(Level.INFO, this, "Validating HMAC supplied for WS");
            String hmac2 = buildHmac(Arrays.asList(new String[] {dateValue}), secret);
            if(hmac.equals(hmac2)) {
                //generate a new HMAC to allow the mediator to validate who we are
                dateValue = Instant.now().toString();
                response.getHeaders().put("gameon-date", Arrays.asList(new String[] {dateValue}));
                hmac2 = buildHmac(Arrays.asList(new String[] {dateValue}), secret);
                response.getHeaders().put("gameon-signature", Arrays.asList(new String[] {hmac2}));
                return;
            }
            Log.log(Level.WARNING, this, "Failed to validate HMAC, unable to establish connection");
        } catch (Exception e) {
            Log.log(Level.WARNING, this, "Failed to validate HMAC, unable to establish connection", e);
        }
        response.getHeaders().replace(HandshakeResponse.SEC_WEBSOCKET_ACCEPT, Collections.emptyList());
    }
    
    //gets a header that must only have a single value, returns null if it is missing or has multiple values
    private String getSingletonHeader(String name) {
        List<String> values = request.getHeaders().get(name);
        if((values == null) || values.isEmpty() || (values.size() != 1)) {
            //removing the accept header will cause the connection not to be made. Depending on the implementation
            //the initiator may get either OnError() or OnClose() invoked.
            Log.log(Level.WARNING, this, "Missing or invalid " + name + " header in request, connection will be rejected", values);
            response.getHeaders().replace(HandshakeResponse.SEC_WEBSOCKET_ACCEPT, Collections.emptyList());
            return null;
        }
        return values.get(0);
    }
}
