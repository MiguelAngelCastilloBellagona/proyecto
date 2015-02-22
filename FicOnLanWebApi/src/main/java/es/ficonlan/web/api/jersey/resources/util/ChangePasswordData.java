package es.ficonlan.web.api.jersey.resources.util;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
public class ChangePasswordData {
	
	private String oldPassword;
	private String newPassword;

	public ChangePasswordData() {
	}

	public ChangePasswordData(String oldPassword, String newPassword) {
		super();
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
