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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FirebaseUtil
{
	@Getter
	@Setter
    private static String serverKey;

    private static final String CHARSET = "UTF-8";
    private static final String FIREBASE_SERVER = "https://fcm.googleapis.com/fcm/send";
	
    /**
     * 외부에서 객체 생성을 방지하기 위한 private 생성자
     */
    private FirebaseUtil() {
        throw new IllegalStateException("Utility class");
    }

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
        } catch (Exception ex) { ex.printStackTrace (); }
    	
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
        } catch (Exception ex) { ex.printStackTrace (); }
    	
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
        } catch (Exception ex) { ex.printStackTrace (); }

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
        } catch (Exception ex) { ex.printStackTrace (); }

        return status;
    }    

    public static String exec (String token, String title, String body)
    {
        String response = "";
        
        try
        {
        	response = exec (token, title, body, null);
        } catch (Exception ex) { ex.printStackTrace (); }
        
        return response;
    }        
    
    public static String exec (String token, String title, String body, String link)
    {
        String response = "";
    	
        try
        {
        	response = exec ((Object) token, title, body, link);
        } catch (Exception ex) { ex.printStackTrace (); }

        return response;
    }
    
    public static String exec (List <String> token, String title, String body)
    {
        String response = "";
    	
        try
        {
        	response = exec (token, title, body, null);
        } catch (Exception ex) { ex.printStackTrace (); }

        return response;
    }    
        
    public static String exec (List <String> token, String title, String body, String link)
    {
        String response = "";
    	
        try
        {
        	response = exec ((Object) token, title, body, link);
        } catch (Exception ex) { ex.printStackTrace (); }

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
        } catch (Exception ex) { ex.printStackTrace (); }

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
            client.setRequestProperty ("Authorization", String.format("key=%s", getServerKey ()));
            client.setRequestProperty ("Content-Type", String.format("application/json; Charset=%s", CHARSET));
        	
            DataOutputStream stream = new DataOutputStream (client.getOutputStream ());
            stream.write (data.getBytes ("UTF-8"));
            stream.flush (); stream.close ();
            
            InputStream in = new BufferedInputStream (client.getInputStream ());
            BufferedReader reader = new BufferedReader (new InputStreamReader (in));
            StringBuilder out = new StringBuilder();
            String line;
            while (Objects.nonNull(line = reader.readLine ())) out.append (line);
            reader.close ();
            response = out.toString ();
        } catch (Exception ex) { ex.printStackTrace (); } finally { if (Objects.nonNull(client)) client.disconnect (); client = null; }
        
        return (response);
    }    
}
