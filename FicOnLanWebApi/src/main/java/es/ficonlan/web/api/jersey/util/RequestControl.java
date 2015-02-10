package es.ficonlan.web.api.jersey.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.glassfish.grizzly.http.server.Request;

public class RequestControl {
	
	private static SimpleDateFormat dateFormat;
	
	public static void showContextData(String name, Request request) {
		if(request!=null) { 
			dateFormat = new SimpleDateFormat("dd-MM-yyyy/HH:mm:ss");
			String ip = request.getRemoteAddr();
			Calendar now = Calendar.getInstance();
			System.out.format("%30s%30s%30s", "[" + name + "]", "from: [" + ip + "]", " at: [" + dateFormat.format(now.getTime()) + "]\t");
		}
	}
}
