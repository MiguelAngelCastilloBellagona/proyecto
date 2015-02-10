package es.ficonlan.web.api.model.session;


public class SessionData {
	
	private String sessionId;
	private int userId;
	private boolean secondpass;
	private String loginName;
	private String permissions;
	private String language;
	
	public SessionData(String sessionId, int userId, boolean secondpass,
			String loginName, String permissions, String language) {
		super();
		this.sessionId = sessionId;
		this.userId = userId;
		this.secondpass = secondpass;
		this.loginName = loginName;
		this.permissions = permissions;
		this.language = language;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public boolean isSecondpass() {
		return secondpass;
	}

	public void setSecondpass(boolean secondpass) {
		this.secondpass = secondpass;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
