package com.cube.simple.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.cube.simple.enums.FirebaseCode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FirebaseUtil
{
    static final String CHARSET = "UTF-8";
	static final String FIREBASE_SERVER = "https://fcm.googleapis.com/fcm/send";
	static final String FIREBASE_SERVER_KEY = "FIREBASE-SERVER-KEY";
	
    public static FirebaseCode send (String token, String title, String body)
    {
    	FirebaseCode status = FirebaseCode.FAILURE;
    	
        try
        {
        	String response = exec (token, title, body, null);
			Map <String, Object> json = (new ObjectMapper ()).readValue (response, Map.class);
        	int check = 1;
        	int success = Integer.valueOf (json.get ("success").toString ());
            if (check != success) status = FirebaseCode.FAILURE;
            if (check == success) status = FirebaseCode.SUCCESS;
        } catch (Exception e) { e.printStackTrace (); }
    	
    	return status;
    }
    
    public static FirebaseCode send (String token, String title, String body, String link)
    {
    	FirebaseCode status = FirebaseCode.FAILURE;
    	
        try
        {
        	String response = exec (token, title, body, link);
			Map <String, Object> json = (new ObjectMapper ()).readValue (response, Map.class);
        	int check = 1;
        	int success = Integer.valueOf (json.get ("success").toString ());
            if (check != success) status = FirebaseCode.FAILURE;
            if (check == success) status = FirebaseCode.SUCCESS;
        } catch (Exception e) { e.printStackTrace (); }
    	
    	return status;
    }

    public static FirebaseCode send (List <String> token, String title, String body)
    {
    	FirebaseCode status = FirebaseCode.FAILURE;
    	
        try
        {
        	String response = exec (token, title, body, null);
			Map <String, Object> json = (new ObjectMapper ()).readValue (response, Map.class);
        	int check = token.size ();
        	int success = Integer.valueOf (json.get ("success").toString ());
            if (0 == success) status = FirebaseCode.FAILURE;
            if (check == success) status = FirebaseCode.SUCCESS;
            if (check > success && 0 < success) status = FirebaseCode.PARTIAL;
        } catch (Exception e) { e.printStackTrace (); }

        return status;
    }    
    
    public static FirebaseCode send (List <String> token, String title, String body, String link)
    {
    	FirebaseCode status = FirebaseCode.FAILURE;
    	
        try
        {
        	String response = exec (token, title, body, link);
			Map <String, Object> json = (new ObjectMapper ()).readValue (response, Map.class);
        	int check = token.size ();
        	int success = Integer.valueOf (json.get ("success").toString ());
            if (0 == success) status = FirebaseCode.FAILURE;
            if (check == success) status = FirebaseCode.SUCCESS;
            if (check > success && 0 < success) status = FirebaseCode.PARTIAL;
        } catch (Exception e) { e.printStackTrace (); }

        return status;
    }    

    public static String exec (String token, String title, String body)
    {
        String response = "";
        
        try
        {
        	response = exec (token, title, body, null);
        } catch (Exception e) { e.printStackTrace (); }
        
        return response;
    }        
    
    public static String exec (String token, String title, String body, String link)
    {
        String response = "";
    	
        try
        {
        	response = exec ((Object) token, title, body, link);
        } catch (Exception e) { e.printStackTrace (); }

        return response;
    }
    
    public static String exec (List <String> token, String title, String body)
    {
        String response = "";
    	
        try
        {
        	response = exec (token, title, body, null);
        } catch (Exception e) { e.printStackTrace (); }

        return response;
    }    
        
    public static String exec (List <String> token, String title, String body, String link)
    {
        String response = "";
    	
        try
        {
        	response = exec ((Object) token, title, body, link);
        } catch (Exception e) { e.printStackTrace (); }

        return response;
    }    
    
    public static String exec (Object token, String title, String body, String link)
    {
        String response = "";
    	
        try
        {
        	Map <String, Object> notification = new HashMap <> ();
        	notification.put ("title", title);
        	notification.put ("body", body);
        	notification.put ("icon", "default");
        	notification.put ("sound", "default");
        	
        	Map <String, Object> request = new HashMap <> ();
        	if (List.class.isInstance (token)) { request.put ("registration_ids", token); } else { request.put ("to", token); }
        	request.put ("notification", notification);
        	if (!Objects.isNull (link))
        	{
            	Map <String, Object> data = new HashMap <> ();
            	data.put ("link", link);
            	request.put ("data", data);
        	}
        	
        	String json = (new ObjectMapper ()).writeValueAsString (request);
            response = post (json);
        } catch (Exception e) { e.printStackTrace (); }

        return response;
    }
    
    private static String post (String data)
    {
        URL request = null;
        HttpURLConnection client = null;
        String response = "";
        
        try
        {
            request = new URL (FIREBASE_SERVER);
            client = (HttpURLConnection) request.openConnection ();
            client.setUseCaches (false);
            client.setReadTimeout (12000);
            client.setConnectTimeout (12000);
            client.setDoInput (true);
            client.setDoOutput (true);
            client.setRequestMethod ("POST");
            client.setRequestProperty ("Accept-Charset", CHARSET);
            client.setRequestProperty ("Connection", "keep-alive");
            client.setRequestProperty ("Cache-control", "no-cache");
            client.setRequestProperty ("Authorization", "key=" + FIREBASE_SERVER_KEY);
            client.setRequestProperty ("Content-Type", "application/json; Charset=" + CHARSET);
        	
            DataOutputStream stream = new DataOutputStream (client.getOutputStream ());
            stream.write (data.getBytes ("UTF-8"));
            stream.flush (); stream.close ();
            
            InputStream in = new BufferedInputStream (client.getInputStream ());
            BufferedReader reader = new BufferedReader (new InputStreamReader (in));
            StringBuilder out = new StringBuilder();
            String line;
            while (null != (line = reader.readLine ())) out.append (line);
            reader.close ();
            response = out.toString ();
        } catch (Exception e) { e.printStackTrace (); } finally { if (null != client) client.disconnect (); client = null; }
        
        return (response);
    }    
}
