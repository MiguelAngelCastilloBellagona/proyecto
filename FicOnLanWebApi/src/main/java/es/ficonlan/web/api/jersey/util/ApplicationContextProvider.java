package es.ficonlan.web.api.jersey.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
public class ApplicationContextProvider implements ApplicationContextAware {

	private static ApplicationContext context = null;

	public static ApplicationContext getApplicationContext() {
		return context;
	}

	@SuppressWarnings("static-access")
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}
}
