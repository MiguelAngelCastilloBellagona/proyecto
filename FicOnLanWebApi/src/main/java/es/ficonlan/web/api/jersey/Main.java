package es.ficonlan.web.api.jersey;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson1.Jackson1Feature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import es.ficonlan.web.api.jersey.resources.UserResource;
import es.ficonlan.web.api.jersey.util.CORSResponseFilter;
import es.ficonlan.web.api.jersey.util.CustomExceptionMapper;
import es.ficonlan.web.api.model.email.EmailFIFOGmail;
import es.ficonlan.web.api.model.userService.UserService;

/**
 * @author Miguel Ángel Castillo
 * @version 2.0
 */
public class Main {

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

	private static final URI SERVER_URI = URI.create(properties.getProperty("server.baseUri"));
	private static final String KEYSTORE_FILE = properties.getProperty("server.keystoreFile");
	private static final String KEYSTORE_PASS = properties.getProperty("server.keystorePass");
	private static final String TRUSTSTORE_FILE = properties.getProperty("server.truststoreFile");
	private static final String TRUSTSTORE_PASS = (properties.getProperty("server.truststorePass"));
	private static final boolean IS_SECURE = Boolean.parseBoolean(properties.getProperty("server.isSecure"));

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	public static HttpServer startServer() {

		final ResourceConfig rc = new ResourceConfig();
		
		rc.register(Jackson1Feature.class);
		
		rc.register(UserResource.class);
		
		rc.register(CORSResponseFilter.class);
		rc.register(CustomExceptionMapper.class);
		
		SSLContextConfigurator sslContext = new SSLContextConfigurator();
		sslContext.setKeyStoreFile(KEYSTORE_FILE);
		sslContext.setKeyStorePass(KEYSTORE_PASS);
		sslContext.setTrustStoreFile(TRUSTSTORE_FILE);
		sslContext.setTrustStorePass(TRUSTSTORE_PASS);

		return GrizzlyHttpServerFactory.createHttpServer(SERVER_URI, rc, IS_SECURE, new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(true));
	}

	public static void main(String[] args) {
		
		// Spring context initialization
		@SuppressWarnings("resource")
		ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-config.xml");

		// UserService Initialization
		UserService userService = ctx.getBean(UserService.class);
		userService.initialize();

		EmailFIFOGmail.startEmailQueueThread();
		
		// Server Start
		final HttpServer server = startServer();   
		
		System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl\nHit enter to stop it...", properties.getProperty("server.baseUri")));
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.shutdownNow();
	}
}
