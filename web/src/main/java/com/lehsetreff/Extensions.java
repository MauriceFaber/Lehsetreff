package com.lehsetreff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;

import com.google.gson.Gson;
import com.lehsetreff.controllers.Database;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Extensions {
	private static Database db = Database.getInstance();

    /**
     * Sendet Antwort im Json Format zurueck.
     * @param response
     * Antwort.
     * @param object
     * Objekt, welches in Json konvertiert werden soll.
     * @throws IOException
     */
    public static void sendJsonResponse(HttpServletResponse response, Object object ) throws IOException{
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String objecString = new Gson().toJson(object); 
        out.print(objecString);
        out.flush();
    }

    /**
     * Prueft ob Benutzer authentifiziert ist.
     * @param request
     * Https Request.
     * @param response
     * Antwort.
     * @return
     * Benutzer authentifiziert.
     * @throws IOException
     */
	public static boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) throws IOException{
		if(!db.getUserController().isAuthenticated(request)){
			response.sendError(401, "not authenticated");
			return false;
		}
		return true;
	}

	public static boolean isAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException{
		if(!db.getUserController().isAdmin(request)){
			response.sendError(401, "no admin rights");
			return false;
		}
		return true;
	}
	
	public static boolean isModerator(HttpServletRequest request, HttpServletResponse response) throws IOException{
		if(!db.getUserController().isModerator(request)){
			response.sendError(401, "no moderator rights");
			return false;
		}
		return true;
	}
	
	public static boolean isModOrThreadOwner(HttpServletRequest request, HttpServletResponse response, int threadId) throws IOException{
		if(!db.getUserController().isModerator(request) && !db.getUserController().isThreadOwner(threadId, request)){
			response.sendError(401, "no moderator or thradOwner");
			return false;
		}
		return true;
	}

	public static boolean isSender(HttpServletRequest request, HttpServletResponse response, int messageID) throws IOException{
		if(!db.getUserController().isMessageSender(messageID, request)){
			response.sendError(401, "not the sender");
			return false;
		}
		return true;
	}

	public static boolean isUser(HttpServletRequest request, HttpServletResponse response) throws IOException{
		if(!db.getUserController().isUser(request)){
			response.sendError(401, "not a user");
			return false;
		}
		return true;
	}
/**
 * 
 * @param req
 * HashMap request.
 * @return
 * Schluessel und Wert.
 */
	public static HashMap<String, String> getParameterMap(HttpServletRequest req){
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			BufferedReader br = null;
			InputStreamReader reader = new InputStreamReader(req.getInputStream());
			br = new BufferedReader(reader);

			String data = br.readLine();
			String[] parameterPairs = data.split("&");
			for (String tmp : parameterPairs) {
				String[] pair = tmp.split("=");
				if(pair.length == 2){
					map.put(pair[0], pair[1]);
				}
			}
			System.out.println(data);
			} catch(Exception e){
		}
		return map;
	}
/**
 * Schluessel und Wert von HashMap loeschen.
 */
	private static HashMap<HttpServletRequest, HashMap<String, String>> values = new HashMap<HttpServletRequest, HashMap<String, String>>();

	public static void removeHashmap(HttpServletRequest req){
		if(values.containsKey(req)){
			values.remove(req);
		}
	}
/**
 * 
 * @param req
 * Request bestimmten Schluessel und Wert.
 * @param key
 * Schluessel der ausgelesen wird.
 * @return
 * null.
 */
	public static String getParameterFromMap(HttpServletRequest req, String key){
		HashMap<String, String> parameters;
		if(!values.containsKey(req)){
			parameters = Extensions.getParameterMap(req);
			values.put(req, parameters);
		}else {
			parameters = values.get(req);
		}
		if(parameters.containsKey(key)){
			return parameters.get(key);
		}
		return null;
	}
}
