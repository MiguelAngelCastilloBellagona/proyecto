package es.ficonlan.web.api.model.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.MessagingException;

@SuppressWarnings("rawtypes")
public class EmailFIFOFactory {
	private final static String CLASS_NAME_PARAMETER = "EmailFIFO/className";
	private static final String CONFIGURATION_FILE = "factory.properties";

	private static EmailFIFO instance;

	static {

		Properties properties = new Properties();
		InputStream inputStream = EmailFIFOFactory.class.getClassLoader().getResourceAsStream(CONFIGURATION_FILE);

		try {
			properties.load(inputStream);
		} catch (IOException e1) {
			System.out.println("File not found: " + CONFIGURATION_FILE);
		}
		try {
			String implClassName = properties.getProperty(CLASS_NAME_PARAMETER);
			Class implClass = Class.forName(implClassName);
			instance = (EmailFIFO) implClass.newInstance();
		} catch (Exception e) {
		}
	}

	private EmailFIFOFactory() {
	}

	public static EmailFIFO getEmailFIFO() throws MessagingException {

		if (instance == null) {
			throw new MessagingException("Cant Create : " + CLASS_NAME_PARAMETER);
		} else {
			return instance;
		}

	}
}
