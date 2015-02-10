package es.ficonlan.web.api.model.session;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;

import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.user.UserDao;
import es.ficonlan.web.api.model.util.exceptions.InstanceException;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
public class Session {
	
	@Autowired
	UserDao userDao;
	
	private String sessionId;
	private int userId;
	private Calendar lastAccess;
	
	public Session(User user) {
		this.sessionId = generateSessionId(user);
		this.userId = user.getUserId();
		this.lastAccess = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}
	
	private String hexEncode(byte[] barray) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < barray.length; i++) {
			String hex = Integer.toHexString(0xff & barray[i]);
			if (hex.length() == 1)
				sb.append('0');
			sb.append(hex);
		}
		return sb.toString();
	}

	private String generateSessionId(User user) {
		String s = user.getLogin() + user.getName() + user.getPassword()
				+ Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
		try {
			MessageDigest mdigest = MessageDigest.getInstance("SHA-256");
			return hexEncode(mdigest.digest(s.getBytes("UTF-8")));
		} catch (Exception e) {
			return null;
		}
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

	public Calendar getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(Calendar lastAccess) {
		this.lastAccess = lastAccess;
	}
	
	public void setLastAccessNow() {
		this.lastAccess = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}
	
	public SessionData createSessionData() throws InstanceException {
		User u = userDao.find(this.userId);
		return new SessionData(this.sessionId,this.userId,false,
				u.getLogin(),u.getPremissions(),u.getLanguage());
	}

}
