package es.ficonlan.web.api.model.email;

public interface EmailFIFO {

	public int mailQueueSize();
	
	public void adEmailToQueue(Email email);
	
	public void setEmailAdressData(String user, String password);
}
