package es.ficonlan.web.api.model.session;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.TimeZone;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.util.exceptions.InstanceException;
import es.ficonlan.web.api.model.util.session.SessionData;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@Entity
@Table(name = "Session")
public class Session {
	
	@Id
	@Column(name = "Session_id")
	private String sessionId;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "Session_user_id")
	private User user;
	
	@Column(name = "Session_lastAccess")
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private Calendar lastAccess;
	
	public Session() {}
	
	public Session(User user) {
		this.sessionId = generateSessionId(user);
		this.user = user;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Calendar getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(Calendar lastAccess) {
		this.lastAccess = lastAccess;
	}

	public SessionData createSessionData() throws InstanceException {
		return new SessionData(this.sessionId,this.user.getUserId(),false,
				this.user.getLogin(),this.user.getPremissions(),this.user.getLanguage());
	}

}
