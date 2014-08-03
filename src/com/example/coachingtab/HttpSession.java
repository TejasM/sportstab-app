package com.example.coachingtab;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

//The global singleton class to store the localcontext (the cookies). 
//This is needed to maintain the current "logged in" context (Django session ID, etc)

public class HttpSession {
	
	private CookieStore cookieStore;
    private HttpContext localContext;
	private static HttpSession instance; // We will have ONE instance of this class
	private boolean currentlyLoggedIn;
	// Use this function to get the singleton instance
	public static synchronized HttpSession getInstance(){
		if(instance==null){
			instance=new HttpSession();
		}
		return instance;
	}
	
	public HttpContext getContext(){
		return this.localContext;
	}
	public void setContext(HttpContext contxt){
		this.localContext = contxt;
	}
	
	public boolean isCurrentlyLoggedIn(){
		
		return currentlyLoggedIn;
	}
	public void setCurrentlyLoggedIn(boolean status){
		currentlyLoggedIn = status;
	}
	
    public HttpSession(){
    	cookieStore = new BasicCookieStore();
    	localContext = new BasicHttpContext();
    	localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    	currentlyLoggedIn = false;
    }
    
	
}