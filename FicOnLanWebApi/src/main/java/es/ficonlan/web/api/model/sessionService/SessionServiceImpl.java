package es.ficonlan.web.api.model.sessionService;

import java.util.Calendar;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import es.ficonlan.web.api.jersey.resources.util.SessionData;
import es.ficonlan.web.api.model.session.Session;
import es.ficonlan.web.api.model.session.SessionDao;
import es.ficonlan.web.api.model.sessionmanager.SessionManagerFactory;
import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.user.UserDao;
import es.ficonlan.web.api.model.util.PasswordManager;
import es.ficonlan.web.api.model.util.exceptions.InstanceException;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;

@Service("SessionManager")
@Transactional
public class SessionServiceImpl implements SessionService {
	
	private static final String SESSIONSERVICEPERMISIONLEVEL = "S";

	private int SESSION_TIMEOUT;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private SessionDao sessionDao;
	
	@Override
	public void initialize() {
		try {
			SessionManagerFactory.getSessionManager().startSessionManager();
		} catch (MessagingException e) {
			throw new RuntimeException("Cant initialize Sessionmanager");
		}

	}
	
	@Transactional(readOnly=true, isolation=Isolation.READ_COMMITTED)
	public boolean checkPermissions(User user, String permisionLevelRequired) {
		try {
			return userDao.find(user.getUserId()).getPremissions().contains(permisionLevelRequired);
		} catch (InstanceException e) {
			return false;
		}
	}

	@Transactional(readOnly=true, propagation=Propagation.REQUIRED, isolation=Isolation.READ_UNCOMMITTED)
	private void sessionAcceded(String sessionId) throws InstanceException {
			sessionDao.find(sessionId).setLastAccess(Calendar.getInstance());
	}
	
	// ANONYMOUS
	
	@Override
	@Transactional
	public SessionData login(String login, String password) throws ServiceException {
		if(login==null) throw new ServiceException(ServiceException.MISSING_FIELD,"Login");
		if(password ==null) throw new ServiceException(ServiceException.MISSING_FIELD,"Password");
		User user = userDao.findUserBylogin(login);
		boolean secondPass = false;
		if (user == null) throw new ServiceException(ServiceException.INCORRECT_FIELD,"User/Password");
		if (!user.getPassword().contentEquals(PasswordManager.hashPassword(password))) {
			if ((user.getSecondPasswordExpDate()==null) || (user.getSecondPasswordExpDate().before(Calendar.getInstance())) || (!user.getSecondPassword().contentEquals(PasswordManager.hashPassword(password)))) {
				throw new ServiceException(ServiceException.INCORRECT_FIELD,"User/Password"); 
			}
			else secondPass = true;
		}
		Session session = new Session(user);
		while(sessionDao.exists(session.getSessionId())) {
			session = new Session(user);
		}
		sessionDao.save(session);
		return new SessionData(session.getSessionId(),user.getUserId(),secondPass,user.getLogin(), user.getPremissions(), user.getLanguage());
	}

	//USER
	
	@Override
	@Transactional(isolation=Isolation.READ_COMMITTED)
	public boolean sessionExists(String sessionId) throws ServiceException {
		try {
			Session s = sessionDao.find(sessionId);
			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, SESSION_TIMEOUT * -1);
			if(s.getLastAccess().before(c)) {
				sessionDao.remove(sessionId);
				return false;
			}
			else {
				sessionAcceded(sessionId);
				return true;
			}
		} catch (InstanceException e) {
			return false;
		}
	}

	@Override
	@Transactional(isolation=Isolation.READ_COMMITTED)
	public User getUserUSER(String sessionId) throws ServiceException {
		try {
			sessionAcceded(sessionId);
			return sessionDao.find(sessionId).getUser();
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
	}

	@Override
	@Transactional
	public void closeAllUserSessions(String sessionId) throws ServiceException {
		try {
			List<Session> list = sessionDao.findSessionByUserId(sessionDao.find(sessionId).getUser().getUserId());
			for(Session s : list) {
				sessionDao.remove(s.getSessionId());
			}
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
	}
	
	@Override
	@Transactional
	public void closeAllUserOtherSessions(String sessionId) throws ServiceException {
		try {
			sessionAcceded(sessionId);
			List<Session> list = sessionDao.findSessionByUserId(sessionDao.find(sessionId).getUser().getUserId());
			for(Session s : list) {
				if(s.getSessionId()!=sessionId) sessionDao.remove(s.getSessionId());
			}
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
	}

	@Override
	@Transactional
	public void closeUserSession(String sessionId) throws ServiceException {
		try {
			sessionDao.remove(sessionId);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
	}
	
	// ADMIN

	@Override
	@Transactional(isolation=Isolation.READ_UNCOMMITTED)
	public List<Session> getAllUserSessionsADMIN(String sessionId, int userId) throws ServiceException {
		try { 
			sessionAcceded(sessionId);
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), SESSIONSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
		return sessionDao.findSessionByUserId(userId);
	}

	@Override
	@Transactional
	public void closeAllUserSessionsADMIN(String sessionId, int userId) throws ServiceException {
		try { 
			sessionAcceded(sessionId);
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), SESSIONSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
		List<Session> list = sessionDao.findSessionByUserId(userId);
		
		for(Session s : list) {
			try {
				sessionDao.remove(s.getSessionId());
			} catch (InstanceException e) {}
		}

	}

	@Override
	@Transactional
	public void closeOldSessions() {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, SESSION_TIMEOUT * -1);
		List<Session> list = sessionDao.findOlderThanDate(c);
		for(Session session : list) {
			try {
				sessionDao.remove(session.getSessionId());
			} catch (InstanceException e) { }
		}

	}

}
