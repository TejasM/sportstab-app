package com.example.coachingtab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import android.os.AsyncTask;

// I will use this class to do authenticated server accesses
// Note we check the current http session before all requests (check if user is logged in)

public class HttpCommunication extends AsyncTask<String, Integer, String>{
	private static String domain = "http://sports-tab.com";
	protected String doInBackground(String... params) {

	    try {
	    	HttpClient httpClient = new DefaultHttpClient();
	    	HttpPost postRequest = null;
	    	MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

	    	if (params[0].equals("LOGIN")){
	    		postRequest = new HttpPost(domain + "/app_login");
	    		reqEntity.addPart("email", new StringBody(params[1]));
	    		reqEntity.addPart("password", new StringBody(params[2]));
	    		reqEntity.addPart("type", new StringBody("login"));
	    	} else if (params[0].equals("SIGNUP")){
	    		postRequest = new HttpPost(domain + "/app_login");
	    		reqEntity.addPart("email", new StringBody(params[1]));
	    		reqEntity.addPart("password", new StringBody(params[2]));
	    		reqEntity.addPart("type", new StringBody("signup"));
	    		reqEntity.addPart("first_name", new StringBody(params[3]));
	    		reqEntity.addPart("last_name", new StringBody(params[4]));
	    	} else if (params[0].equals("GETPLAY")){
	    		postRequest = new HttpPost(domain + "/plays/app_get_play/");
	    		reqEntity.addPart("play_name", new StringBody(params[1]));
	    	} else if (params[0].equals("GETTAG")){
	    		postRequest = new HttpPost(domain + "/plays/app_get_tags/");
	    	} else if (params[0].equals("SETTAG")){
	    		postRequest = new HttpPost(domain + "/plays/app_set_tags/");
	    		reqEntity.addPart("input_obj", new StringBody(params[1]));
	    	} else if (params[0].equals("CHECKUSERNAMEAVAIL")){
	    		postRequest = new HttpPost(domain + "/app_checkusername");
	    		reqEntity.addPart("email", new StringBody(params[1]));
	    	}

	    	if (postRequest == null){
	    		return "INVALID PARAM";
	    	}

	    	// Get the current HttpContext
	    	HttpContext local_context = HttpSession.getInstance().getContext();
	    	postRequest.setEntity(reqEntity);
	    	// Do the Http request
            HttpResponse response = httpClient.execute(postRequest, local_context);
            // Save the updated context
            HttpSession.getInstance().setContext(local_context);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder s = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }

	        return s.toString();
	    } catch (ClientProtocolException e) {
	        return null;
	    } catch (IOException e) {
	        return null;
	    }
	}

	protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPreExecute() {
    }

	protected void onPostExecute(Long result) {
    }
}