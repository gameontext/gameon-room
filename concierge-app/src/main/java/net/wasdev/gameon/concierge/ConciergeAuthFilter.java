package net.wasdev.gameon.concierge;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebFilter(
		filterName = "registrationAuthFilter",
		urlPatterns = {"/*"}
		  )
public class ConciergeAuthFilter implements Filter{
	private static final String CHAR_SET = "UTF-8";
	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static long timeoutMS = 5000;		//timeout for requests, default to 5 seconds
	
	@Resource(lookup="registrationSecret")
	String registrationSecret;
	@Resource(lookup="querySecret")
	String querySecret;
	/**
	 * Used API Key (Eg, one that has been seen, and is being tracked to ensure no reuse)
	 * Equality / Hashcode is determined by apikey string alone.
	 * Sort order is provided by key timestamp.
	 */
	private final static class UsedKey implements Comparable<UsedKey> {
		private final String apiKey;
		private final Long time;
		public UsedKey(String a,Long t){
			this.apiKey=a; this.time=t;
		}
		@Override
		public int compareTo(UsedKey o) {
			return o.time.compareTo(time);
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((apiKey == null) ? 0 : apiKey.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UsedKey other = (UsedKey) obj;
			if (apiKey == null) {
				if (other.apiKey != null)
					return false;
			} else if (!apiKey.equals(other.apiKey))
				return false;
			return true;
		}
	}
	
	private static final Set<UsedKey> usedKeys = 
			Collections.synchronizedSet(new LinkedHashSet<UsedKey>());	//keys already received, prevent replay attacks
	
	//the authentication steps that are performed on an incoming request
	private enum AuthenticationState {
		hasQueryString,			//starting state
		hasAPIKeyParam,
		isAPIKeyValid,
		hasKeyExpired,
		checkReplay,
		PASSED,					//end state
		ACCESS_DENIED			//end state
	}
	
	//ensure consistent parameter names
	public enum Params {
		apikey,
		serviceID,
		stamp;
		
		public String toString() {
			return "&" + this.name() + "=";
		}		
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	private static boolean hasExpired(Long value){
		return (System.currentTimeMillis() - value) > timeoutMS;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		//we're a single filter, but we protect different paths with different keys.		
		HttpServletRequest http = (HttpServletRequest) request;		
		String requestUri = http.getRequestURI();
		//TODO: why did http.getServletPath() return empty string for me?
		String path = requestUri.substring(http.getContextPath().length());
						
		String sharedSecret;
		if("/registerRoom".equals(path)){
			sharedSecret = registrationSecret;
		}else{
			sharedSecret = querySecret;
		}
		System.out.println("Filter Path: "+path+" Using Key: "+sharedSecret);
		
		String queryString = null; String apikey = null;
		int pos = 0; long time = 0;
		AuthenticationState state = AuthenticationState.hasQueryString;		//default
		while(!state.equals(AuthenticationState.PASSED)) {
			switch(state) {
				case hasQueryString :	//check that there is a query string which will contain the service ID and api key
					queryString = ((HttpServletRequest) request).getQueryString();	//this is the raw version
					state = (queryString == null) ? AuthenticationState.ACCESS_DENIED : AuthenticationState.hasAPIKeyParam;
					break;
				case hasAPIKeyParam :	//check there is an apikey parameter
					pos = queryString.lastIndexOf(Params.apikey.toString());
					state = (pos == -1) ? AuthenticationState.ACCESS_DENIED : AuthenticationState.isAPIKeyValid;
					break;
				case isAPIKeyValid :	//validate API key against all parameters (except the API key itself)
					queryString = queryString.substring(0, pos);	//remove API key from end of query string
					String hmac = request.getParameter(Params.apikey.name());
					apikey = digest(queryString,sharedSecret);
					state = !apikey.equals(hmac) ? AuthenticationState.ACCESS_DENIED : AuthenticationState.hasKeyExpired;
					break;
				case hasKeyExpired :	//check that key has not timed out
					time = Long.parseLong(request.getParameter(Params.stamp.name()));
					state = hasExpired(time) ? AuthenticationState.ACCESS_DENIED : AuthenticationState.checkReplay;
					break;
				case checkReplay : //simple replay check - only allows the one time use of API keys, storing time allows expired keys to be purged
					boolean alreadyPresent = usedKeys.add(new UsedKey(apikey, time));
					//the set of keys is sorted with oldest (smallest) timestamp first so we can iterate from the oldest key, 
					//and remove all expired ones.
					synchronized(usedKeys){
						Iterator<UsedKey> i = usedKeys.iterator();
						while(i.hasNext()){
							UsedKey k = i.next();
							if(hasExpired(k.time)){
								i.remove();
							}else{
								break;
							}
						}
					}
					state = !alreadyPresent ? AuthenticationState.ACCESS_DENIED : AuthenticationState.PASSED;
					break;
				case ACCESS_DENIED :
				default :
					((HttpServletResponse)response).sendError(HttpServletResponse.SC_FORBIDDEN);
					return;
			}
		}
		//request has passed all validation checks, so allow it to proceed
		request.setAttribute(Params.serviceID.name(), request.getParameter(Params.serviceID.name()));
		chain.doFilter(request, response);		
	}
	
	/*
	 * Construct a HMAC for this request.
	 * It is then base 64 and URL encoded ready for transmission as a query parameter.
	 */
	private String digest(String message, String sharedSecret) throws IOException {
		try {
			byte[] data = message.getBytes(CHAR_SET);
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			SecretKeySpec key = new SecretKeySpec(sharedSecret.getBytes(CHAR_SET), HMAC_ALGORITHM);
			mac.init(key);
			return javax.xml.bind.DatatypeConverter.printBase64Binary(mac.doFinal(data));
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public void destroy() {
	}

}
