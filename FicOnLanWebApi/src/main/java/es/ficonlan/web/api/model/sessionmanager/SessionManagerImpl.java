package es.ficonlan.web.api.model.sessionmanager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;

import es.ficonlan.web.api.jersey.Main;
import es.ficonlan.web.api.model.sessionService.SessionService;

public class SessionManagerImpl implements SessionManager{

	private Properties properties;

	private int sessionc_clean_frecuency;

	@Autowired
	private SessionService sessionService;

	public SessionManagerImpl() {
		try {
			properties = new Properties();
			InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("backend.properties");
			properties.load(inputStream);
			sessionc_clean_frecuency = Math.abs(Integer.parseInt(properties.getProperty("session.cleanFrequency")));
		} catch (IOException e) {
			throw new RuntimeException("Could not read config file: " + e.getMessage());
		}
	}

	public void startSessionManager() {
		Thread scth = new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					while (true) {
						Thread.sleep(sessionc_clean_frecuency * 1000 * 60);
						sessionService.closeOldSessions();
					}
				} catch (Exception e) {
					throw new RuntimeException("Session clean thread error: " + e.getMessage());
				}
			}
		};
		scth.start();
	}
}
