package es.ficonlan.web.api.model.sessionService;

import java.util.List;

import es.ficonlan.web.api.jersey.resources.util.SessionData;
import es.ficonlan.web.api.model.session.Session;
import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
public interface SessionService {
	
	public void initialize();

	//ANONYMOUS
	
	public SessionData login(String login, String password)  throws ServiceException;
	
	
	//USER
	
	public boolean sessionExists(String sessionId) throws ServiceException;
	
	public User getUserUSER(String sessionId) throws ServiceException;
	
	public void closeAllUserSessions(String sessionId) throws ServiceException;
	
	public void closeAllUserOtherSessions(String sessionId) throws ServiceException;
	
	public void closeUserSession(String sessionId) throws ServiceException;
	
	
	//ADMIN
	
	
	public List<Session> getAllUserSessionsADMIN(String sessionId, int userId) throws ServiceException;
	
	public void closeAllUserSessionsADMIN(String sessionId, int userId) throws ServiceException;
	
	//OTHER

	public void closeOldSessions();

}
