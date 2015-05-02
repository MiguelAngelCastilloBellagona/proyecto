package es.ficonlan.web.api.model.sessionService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.MessagingException;

import es.ficonlan.web.api.model.sessionmanager.SessionManager;

@SuppressWarnings("rawtypes")
public class SessionManagerFactory {
	private final static String CLASS_NAME_PARAMETER = "SessionManager/className";
	private static final String CONFIGURATION_FILE = "factory.properties";

	private static SessionManager instance;

	static {

		Properties properties = new Properties();
		InputStream inputStream = SessionManagerFactory.class.getClassLoader().getResourceAsStream(CONFIGURATION_FILE);

		try {
			properties.load(inputStream);
		} catch (IOException e1) {
			System.out.println("File not found: " + CONFIGURATION_FILE);
		}
		try {
			String implClassName = properties.getProperty(CLASS_NAME_PARAMETER);
			Class implClass = Class.forName(implClassName);
			instance = (SessionManager) implClass.newInstance();
		} catch (Exception e) {
		}
	}

	private SessionManagerFactory() {
	}

	public static SessionManager getSessionManager() throws MessagingException {

		if (instance == null) {
			throw new MessagingException("Cant Create : " + CLASS_NAME_PARAMETER);
		} else {
			return instance;
		}

	}
}
