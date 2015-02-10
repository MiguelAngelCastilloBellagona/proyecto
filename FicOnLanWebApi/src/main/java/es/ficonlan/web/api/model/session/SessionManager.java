package es.ficonlan.web.api.model.session;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import es.ficonlan.web.api.jersey.Main;


public class SessionManager {
	
	private static ConcurrentHashMap<String, Session> openSessions = new ConcurrentHashMap<String, Session>();
	
	private static Properties properties;

	static {
		try {
			properties = new Properties();
			InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("backend.properties");
			properties.load(inputStream);
		} catch (IOException e) {
			throw new RuntimeException("Could not read config file: " + e.getMessage());
		}
	}
	
	private static final int SESSION_TIMEOUT = Math.abs(Integer.parseInt(properties.getProperty("session.timeout")));
	private static final int SESSION_CLEAN_FREQUENCY = Math.abs(Integer.parseInt(properties.getProperty("session.cleanFrequency")));
	
	public static void startSessionManager() {
		Thread scth = new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					while (true) {
						Thread.sleep(SESSION_CLEAN_FREQUENCY);
						SessionManager.cleanOldSessions(SESSION_TIMEOUT);
					}
				} catch (Exception e) {
					throw new RuntimeException("Session clean thread error: " + e.getMessage());
				}
			}
		};
		scth.start();
	}
	
	public static boolean exists(String sessionId){
		Session s = openSessions.get(sessionId);
		if(s!=null)  {
			s.setLastAccessNow();
			return true;
		}
		else return false;
	}
	
	public static void addSession(Session s){
		openSessions.put(s.getSessionId(), s);
	}
	
	public static Session getSession(String sessionId){
		Session s = openSessions.get(sessionId);
		s.setLastAccess(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
		return s;
	}
	
	public static void removeSession(String sessionId){
		openSessions.remove(sessionId);
	}
	
	public static void cleanOldSessions(int timeout){
		Calendar limitTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		limitTime.add(Calendar.SECOND, -timeout);
		for(Map.Entry<String,Session> e:openSessions.entrySet()){
			if(e.getValue().getLastAccess().before(limitTime))
				openSessions.remove(e.getKey());
		}
	}
	
	public static List<Session> getAllUserSessions(int userId) {
		List<Session> keys = new ArrayList<Session>();
		for (Entry<String, Session> e : openSessions.entrySet()) {
            Session value = e.getValue();
            if(value.getUserId()==userId) keys.add(value);
        }
		return keys;
	}
	
	public static void closeAllUserSessions(int userId) {
		List<String> keys = new ArrayList<String>();
		for (Entry<String, Session> e : openSessions.entrySet()) {
            String key = e.getKey();
            Session value = e.getValue();
            if(value.getUserId()==userId) keys.add(key);
        }
		
		for(String s : keys) {
			removeSession(s);
		}
	}
}
