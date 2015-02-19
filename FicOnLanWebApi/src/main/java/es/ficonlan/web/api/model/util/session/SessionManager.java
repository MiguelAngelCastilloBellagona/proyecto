package es.ficonlan.web.api.model.util.session;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import es.ficonlan.web.api.jersey.Main;
import es.ficonlan.web.api.model.userService.UserService;

public class SessionManager {
	
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

	private static final int SESSION_CLEAN_FREQUENCY = Math.abs(Integer.parseInt(properties.getProperty("session.cleanFrequency")));
	
	public static void startSessionManager() {
		
		@SuppressWarnings("resource")
		ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-config.xml");
		UserService userService = ctx.getBean(UserService.class);
		
		Thread scth = new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					while (true) {
						Thread.sleep(SESSION_CLEAN_FREQUENCY*1000*60);
						userService.closeOldSessions();
					}
				} catch (Exception e) {
					throw new RuntimeException("Session clean thread error: " + e.getMessage());
				}
			}
		};
		scth.start();
	}
}
