package es.ficonlan.web.api.model.userService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ficonlan.web.api.jersey.Main;
import es.ficonlan.web.api.jersey.resources.util.SessionData;
import es.ficonlan.web.api.model.email.Email;
import es.ficonlan.web.api.model.email.EmailFIFOGmail;
import es.ficonlan.web.api.model.emailtemplate.EmailTemplate;
import es.ficonlan.web.api.model.emailtemplate.EmailTemplateDao;
import es.ficonlan.web.api.model.session.Session;
import es.ficonlan.web.api.model.session.SessionDao;
import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.user.UserDao;
import es.ficonlan.web.api.model.util.PasswordManager;
import es.ficonlan.web.api.model.util.exceptions.InstanceException;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;


/**
 * @author Miguel Ángel Castillo Bellagona
 */
@Service("UserService")
@Transactional
public class UserServiceImpl implements UserService {

	private static final String USERSERVICEPERMISIONLEVEL = "U";
	
	public static final String ADMIN_LOGIN = "Admin";
	public static final String INITIAL_ADMIN_PASS = "initialAdminPass";
	
	private Properties properties;
	
	private int SESSION_TIMEOUT;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private SessionDao sessionDao;
	
	@Autowired
	private EmailTemplateDao emailTemplateDao;

	@Override
	@Transactional
	public void initialize() {
		
		try {
			properties = new Properties();
			InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("backend.properties");
			properties.load(inputStream);
			SESSION_TIMEOUT = Math.abs(Integer.parseInt(properties.getProperty("session.timeout")));
		} catch (IOException e) {
			throw new RuntimeException("Could not read config file: " + e.getMessage());
		}

		try {
			properties = new Properties();
			InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("backend.properties");
			properties.load(inputStream);
		} catch (IOException e) {
			throw new RuntimeException("Could not read config file: " + e.getMessage());
		}

		User admin = userDao.findUserBylogin(ADMIN_LOGIN);
		if (admin == null)
			admin = new User("EMNRSU", "Administrador", ADMIN_LOGIN,
					PasswordManager.hashPassword(INITIAL_ADMIN_PASS), "0", "adminMail", "-",
					"-", Calendar.getInstance(),"ES");
		userDao.save(admin);

	}

	@Transactional(readOnly=true)
	public boolean checkPermissions(User user, String permisionLevelRequired) {
		try {
			return userDao.find(user.getUserId()).getPremissions().contains(permisionLevelRequired);
		} catch (InstanceException e) {
			return false;
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


	// ANONYMOUS

	@Override
	@Transactional
	public boolean passwordRecover(String email) throws ServiceException {
		
		int minutos = 30;
		
		User user = userDao.findUserByEmail(email);
		EmailTemplate template = emailTemplateDao.findByName("passwordRecover");
		
		if((user!=null) && (template!=null)) { 
			
			String pass = PasswordManager.generatePassword();
			
			user.setSecondPassword(PasswordManager.hashPassword(pass));
			Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			now.add(Calendar.MINUTE,minutos); 
			user.setSecondPasswordExpDate(Calendar.getInstance());
			
			Hashtable<String,String> tabla = new Hashtable<String,String>();
    		tabla.put("#loginusuario", user.getLogin());
    		tabla.put("#nuevapas", pass);
    		tabla.put("#tiemporestante",Integer.toString(minutos));
			 		
    		Email e = emailTemplateDao.findByName("passwordRecover").generateEmail(user, tabla);
    		
    		EmailFIFOGmail.adEmailToQueue(e);
    		
    		return true;
		}
		return false;
	}

	@Override
	@Transactional
	public User addUser(User user) throws ServiceException {
		User u = userDao.findUserBylogin(user.getLogin());
		if (u != null) throw new ServiceException(ServiceException.DUPLICATED_FIELD,"login");
		u = userDao.findUserByDni(user.getDni());
		if (u != null) throw new ServiceException(ServiceException.DUPLICATED_FIELD,"dni");
		if(user.getLogin()==null) throw new ServiceException(ServiceException.MISSING_FIELD,"login");
		if(user.getPassword()==null) throw new ServiceException(ServiceException.MISSING_FIELD,"password");
		if(user.getName()==null) throw new ServiceException(ServiceException.MISSING_FIELD,"name");
		if(user.getDni()==null) throw new ServiceException(ServiceException.MISSING_FIELD,"dni");
		u = userDao.findUserByEmail(user.getEmail());
		if (u != null) throw new ServiceException(ServiceException.DUPLICATED_FIELD,"email");
		if(user.getEmail()==null) throw new ServiceException(ServiceException.MISSING_FIELD,"email");
		if(user.getDob()==null) throw new ServiceException(ServiceException.MISSING_FIELD,"dob");
		if(user.getLanguage()==null) throw new ServiceException(ServiceException.MISSING_FIELD,"language");
		user.setPremissions("");
		String pass = PasswordManager.hashPassword(user.getPassword());
		user.setPassword(pass);
		user.setSecondPassword(pass);
		userDao.save(user);
		return user;	
	}
	
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
		Session session = null;
		boolean uniqueSessionId = true;
		while(uniqueSessionId) {
			try {
				session = new Session(user);
				sessionDao.find(session.getSessionId());
			} catch (InstanceException e) {
				uniqueSessionId = false;
			}
			
		}
		sessionDao.save(session);
		return new SessionData(session.getSessionId(),user.getUserId(),secondPass,user.getLogin(), user.getPremissions(), user.getLanguage());
	}

	// USER

	@Override
	@Transactional
	public void setSessionLastAccessNow(String sessionId) throws ServiceException {
		try {
			Session s = sessionDao.find(sessionId);
			s.setLastAccess(Calendar.getInstance());
			sessionDao.save(s);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}		
	}
	
	@Override
	@Transactional(readOnly=true)
	public boolean sessionExists(String sessionId) throws ServiceException {
		try {
			Session s = sessionDao.find(sessionId);
			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, SESSION_TIMEOUT * -1);
			if(s.getLastAccess().before(c)) {
				sessionDao.remove(sessionId);
				return false;
			}
			else return true;
		} catch (InstanceException e) {
			return false;
		}
	}

	@Override
	@Transactional(readOnly=true)
	public User getCurrenUserUSER(String sessionId) throws ServiceException {
		try {
			return sessionDao.find(sessionId).getUser();
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
	}

	@Override
	@Transactional
	public void removeUserUSER(String sessionId) throws ServiceException {
		try {
			userDao.remove(sessionDao.find(sessionId).getUser().getUserId());
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
	}

	@Override
	@Transactional
	public void changeUserDataUSER(String sessionId, User userData) throws ServiceException {
		try {
			User user = sessionDao.find(sessionId).getUser();
			if(userData.getName()!=null) user.setName(userData.getName());
			User u = userDao.findUserByEmail(userData.getEmail());
			if(u!=null) 
				if(!(u.getUserId()==user.getUserId())) 
					throw new ServiceException(ServiceException.DUPLICATED_FIELD,"email");
			if(userData.getEmail()!=null) user.setEmail(userData.getEmail());
			if(userData.getPhoneNumber()!=null )user.setPhoneNumber(userData.getPhoneNumber());
			if(userData.getShirtSize()!=null) user.setShirtSize(userData.getShirtSize());
			userDao.save(user);
		} catch (InstanceException e) {
			throw new  ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
	}

	@Override
	@Transactional
	public void changeUserPasswordUSER(String sessionId, String oldPassword, String newPassword) throws ServiceException {
		try {
			User user = sessionDao.find(sessionId).getUser();
			if(!PasswordManager.hashPassword(oldPassword).contentEquals(user.getPassword())) 
					if ((user.getSecondPasswordExpDate()==null) || (user.getSecondPasswordExpDate().before(Calendar.getInstance())) || (!user.getSecondPassword().contentEquals(PasswordManager.hashPassword(oldPassword)))) 
						throw new ServiceException(ServiceException.INCORRECT_FIELD,"pass"); 
			user.setSecondPasswordExpDate(null);
			user.setPassword(PasswordManager.hashPassword(newPassword));
			user.setSecondPassword(user.getSecondPassword());
			userDao.save(user);
			List<Session> list = sessionDao.findSessionByUserId(user.getUserId());
			for(Session s : list) {
				if(s.getSessionId()!=sessionId) sessionDao.remove(s.getSessionId());
			}
		} catch (InstanceException e) {
			throw new  ServiceException(ServiceException.INVALID_SESSION,"Session");
		}	
	}

	@Override
	@Transactional(readOnly = true)
	public String getUserPermissionsUSER(String sessionId)
			throws ServiceException {
		try {
			return sessionDao.find(sessionId).getUser().getPremissions();
		} catch (InstanceException e) {
			throw new  ServiceException(ServiceException.INVALID_SESSION,"Session");
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
	public void closeUserSession(String sessionId) throws ServiceException {
		try {
			sessionDao.remove(sessionId);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
	}

	// ADMIN

	@Override
	@Transactional(readOnly = true)
	public List<Session> getAllUserSessionsADMIN(String sessionId, int userId) throws ServiceException {
		try { 
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
		return sessionDao.findSessionByUserId(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public void closeAllUserSessionsADMIN(String sessionId, int userId)
			throws ServiceException {
		try { 
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), USERSERVICEPERMISIONLEVEL))
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
	@Transactional(readOnly = true)
	public User getUserADMIN(String sessionId, int userId) throws ServiceException {
		try { 
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
			return userDao.find(userId);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<User> getAllUsersADMIN(String sessionId, int startIndex, int maxResults, String orderBy, boolean desc) throws ServiceException {
		try { 
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
		return userDao.getAllUsers(startIndex, maxResults, orderBy, desc);
	}

	@Override
	@Transactional(readOnly = true)
	public long getAllUsersTAMADMIN(String sessionId) throws ServiceException {
		try { 
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
		return userDao.getAllUsersTAM();
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> findUsersByNameADMIN(String sessionId, String name, int startindex, int maxResults) throws ServiceException {
		try { 
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"user");
		}
		return userDao.findUsersByName(name,startindex,maxResults);
	}

	@Override
	@Transactional
	public void removeUserADMIN(String sessionId, int userId) throws ServiceException {
		try { 
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
		try {
			userDao.remove(userId);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"User");
		}

	}

	@Override
	@Transactional
	public void changeUserDataADMIN(String sessionId, int userId, User userData) throws ServiceException {
		try { 
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
		try {
			User user = userDao.find(userId);
			if(userData.getName()!=null) user.setName(userData.getName());
			User u = userDao.findUserByEmail(userData.getEmail());
			if(u!=null) 
				if(!(u.getUserId()==user.getUserId())) 
					throw new ServiceException(ServiceException.DUPLICATED_FIELD,"email");
			if(userData.getEmail()!=null) user.setEmail(userData.getEmail());
			if(userData.getPhoneNumber()!=null )user.setPhoneNumber(userData.getPhoneNumber());
			if(userData.getShirtSize()!=null) user.setShirtSize(userData.getShirtSize());
			if(userData.getDni()!=null )user.setDni(userData.getDni());
			if(userData.getDob()!=null )user.setDob(userData.getDob());
			userDao.save(user);
		} catch (InstanceException e) {
			throw new  ServiceException(ServiceException.INSTANCE_NOT_FOUND,"User");
		}
	}

	@Override
	@Transactional
	public void changeUserPasswordADMIN(String sessionId, int userId, String newPassword) throws ServiceException {
		try { 
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
		try {
			User user = userDao.find(userId);
			user.setSecondPasswordExpDate(null);
			user.setPassword(PasswordManager.hashPassword(newPassword));
			user.setSecondPassword(user.getSecondPassword());
			userDao.save(user);	
		} catch (InstanceException e) {
			throw new  ServiceException(ServiceException.INSTANCE_NOT_FOUND,"User");
		}	
	}

	@Override
	@Transactional(readOnly = true)
	public String getUserPermissionsADMIN(String sessionId, int userId)
			throws ServiceException {
		try { 
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
		try {
			return userDao.find(userId).getPremissions();
		} catch (InstanceException e) {
			throw new  ServiceException(ServiceException.INSTANCE_NOT_FOUND,"User");
		}
	}

	@Override
	@Transactional
	public String addUserPermissionsADMIN(String sessionId, int userId,
			String permission) throws ServiceException {
		try { 
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
		try {
			User user = userDao.find(userId);
			if(!user.getPremissions().contains(permission)) 
			{
				user.setPremissions(user.getPremissions() + permission);
				userDao.save(user);
				return user.getPremissions();
			}
			return user.getPremissions();
		} catch (InstanceException e) {
			throw new  ServiceException(ServiceException.INSTANCE_NOT_FOUND,"User");
		}
	}

	@Override
	@Transactional
	public String removeUserPermissionsADMIN(String sessionId, int userId,
			String permission) throws ServiceException {
		try { 
			if(!checkPermissions(sessionDao.find(sessionId).getUser(), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION,"Session");
		}
		try {
			User user = userDao.find(userId);
			if(user.getPremissions().contains(permission)) 
			{
				user.setPremissions(user.getPremissions().replace(permission,""));
				userDao.save(user);
				return user.getPremissions();
			}
			return user.getPremissions();
		} catch (InstanceException e) {
			throw new  ServiceException(ServiceException.INSTANCE_NOT_FOUND,"User");
		}
	}

}
