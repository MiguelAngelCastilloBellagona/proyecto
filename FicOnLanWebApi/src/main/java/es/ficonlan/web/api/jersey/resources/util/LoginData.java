package es.ficonlan.web.api.jersey.resources.util;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
public class LoginData {

	private String login;
	private String password;

	public LoginData() {
	}

	public LoginData(String login, String password) {
		this.login = login;
		this.password = password;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}