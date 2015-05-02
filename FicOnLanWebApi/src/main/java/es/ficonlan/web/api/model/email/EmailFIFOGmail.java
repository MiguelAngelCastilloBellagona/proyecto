package es.ficonlan.web.api.model.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import es.ficonlan.web.api.jersey.Main;

public class EmailFIFOGmail implements EmailFIFO {

	private Queue<Email> emails = new LinkedList<Email>();
	
	private String emailAdressUser;
	
	private String emailAdressPassword;
	
	private Properties properties;

	public EmailFIFOGmail() {
		try {
			this.properties = new Properties();
			InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("backend.properties");
			properties.load(inputStream);
			emailAdressUser = properties.getProperty("gmail.user");
			emailAdressPassword = properties.getProperty("gmail.password");
		} catch (IOException e) {
			throw new RuntimeException("Could not read config file: " + e.getMessage());
		}
		this.startEmailQueueThread();
	}
	
	public void startEmailQueueThread() {
		Thread mailFIFO = new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					while (true) {
						//System.out.println("DENTRO EMAIL THREAD");
						Email email = emails.poll();
						if (email != null) {
							try {
								email.sendMail(emailAdressUser,emailAdressPassword);
								//System.out.println("Email Enviado " + email.getAsunto());
							}
							catch (Exception e1) {
								emails.add(email);
								//System.out.println("Error al enviar el Email " + email.getAsunto());
								Thread.sleep(30000);
							}
							Thread.sleep(100);
						}
						else {
							//System.out.println("No hay Emails");
							Thread.sleep(3000);
						}
					}
				}
				catch (Exception e2) {
					System.out.println("Error en el sistema de Emvío de Email");
					throw new RuntimeException("EmailFIFO thread error: " + e2.getMessage());
				}
			}
		};
		mailFIFO.start();
	}

	public int mailQueueSize() {
		return emails.size();
	}
	
	public void adEmailToQueue(Email email) {
		emails.add(email);
	}
	
	public void setEmailAdressData(String user, String password) {
		emailAdressUser = user;
		emailAdressPassword = password;
	}
}
