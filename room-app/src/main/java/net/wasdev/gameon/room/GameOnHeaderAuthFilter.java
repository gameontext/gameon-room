package net.wasdev.gameon.room;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.logging.Level;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

public class GameOnHeaderAuthFilter extends GameOnHeaderAuth implements ClientRequestFilter {
    
    public GameOnHeaderAuthFilter(String userId, String secret) {
        super(secret,userId);
        if (secret == null)       
            throw new IllegalStateException("NULL secret");
    }
    
    @Override
    public void filter(ClientRequestContext context) throws IOException {
        try {
            // create the timestamp
            Instant now = Instant.now();
            String dateValue = now.toString();

            // create the signature
            String hmac = buildHmac(Arrays.asList(new String[] { userId, dateValue }), secret);

            MultivaluedMap<String, Object> headers = context.getHeaders();
            headers.add("gameon-id", userId);
            headers.add("gameon-date", dateValue);
            headers.add("gameon-signature", hmac);

        } catch (Exception e) {
            Log.log(Level.WARNING, this, "Error during auth filter", e);
            throw new IOException(e);
        }
    }
}
