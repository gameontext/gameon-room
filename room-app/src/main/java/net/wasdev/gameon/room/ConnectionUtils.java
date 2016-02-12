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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

/**
 */
@ApplicationScoped
public class ConnectionUtils {

    /**
     * Simple text based broadcast.
     *
     * @param session
     *            Target session (used to find all related sessions)
     * @param message
     *            {@link RoutedMessage} to send
     * @see #sendMessage(Session, RoutedMessage)
     */
    public static void broadcast(Collection<Session> activeSessions, String message, String msgDesc) {
        for (Session s : activeSessions) {
            Log.log(Level.INFO, s, "ROOM({0}): sending to session {1} message:{2}",msgDesc,s.getId(),message);
            sendMessage(s, message);
        }
    }

    /**
     * Try sending the {@link RoutedMessage} using
     * {@link Session#getBasicRemote()}, {@link Basic#sendObject(Object)}.
     *
     * @param session
     *            Session to send the message on
     * @param message
     *            {@link RoutedMessage} to send
     * @return true if send was successful, or false if it failed
     */
    public static boolean sendMessage(Session session, String message) {
        if (session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                return true;
            } catch (IOException ioe) {
                // An IOException, on the other hand, suggests the connection is
                // in a bad state
                Log.log(Level.WARNING, session, "Unexpected condition writing message", ioe);
                tryToClose(session, new CloseReason(CloseCodes.UNEXPECTED_CONDITION, trimReason(ioe.toString())));
            }
        }
        return false;
    }

    private static String trimReason(String message) {
        return message.length() > 124 ? message.substring(0, 124) : message;
    }

    /**
     * Try to close the WebSocket session and give a reason for doing so.
     *
     * @param s
     *            Session to close
     * @param reason
     *            {@link CloseReason} the WebSocket is closing.
     */
    public static void tryToClose(Session s, CloseReason reason) {
        try {
            s.close(reason);
        } catch (IOException e) {
            tryToClose(s);
        }
    }

    /**
     * Try to close a session (usually once an error has already occurred).
     *
     * @param c
     */
    public static void tryToClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e1) {
            }
        }
    }
}
