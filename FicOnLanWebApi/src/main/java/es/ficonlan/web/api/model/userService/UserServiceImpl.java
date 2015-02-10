package es.ficonlan.web.api.model.userService;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ficonlan.web.api.model.email.Email;
import es.ficonlan.web.api.model.email.EmailFIFOGmail;
import es.ficonlan.web.api.model.emailtemplate.EmailTemplate;
import es.ficonlan.web.api.model.emailtemplate.EmailTemplateDao;
import es.ficonlan.web.api.model.session.Session;
import es.ficonlan.web.api.model.session.SessionData;
import es.ficonlan.web.api.model.session.SessionManager;
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
	
	private static final String ADMIN_LOGIN = "Admin";
	private static final String INITIAL_ADMIN_PASS = "initialAdminPass";

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private EmailTemplateDao emailTemplateDao;

	@Override
	public void initialize() {

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
	@Transactional(readOnly=true)
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
		SessionManager.addSession(session);
		return new SessionData(session.getSessionId(),user.getUserId(),secondPass,user.getLogin(), user.getPremissions(), user.getLanguage());
	}

	// USER

	@Override
	@Transactional(readOnly=true)
	public User getCurrenUserUSER(String sessionId) throws ServiceException {
		try {
			return userDao.find(SessionManager.getSession(sessionId).getUserId());
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"User");
		}
	}

	@Override
	@Transactional
	public void removeUserUSER(String sessionId) throws ServiceException {
		try {
			userDao.remove(SessionManager.getSession(sessionId).getUserId());
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"User");
		}
	}

	@Override
	@Transactional
	public void changeUserDataUSER(String sessionId, User userData) throws ServiceException {
		Session session = SessionManager.getSession(sessionId);
		try {
			User user = userDao.find(session.getUserId());
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
			throw new  ServiceException(ServiceException.INSTANCE_NOT_FOUND,"User");
		}
	}

	@Override
	@Transactional
	public void changeUserPasswordUSER(String sessionId, String oldPassword, String newPassword) throws ServiceException {
		Session session = SessionManager.getSession(sessionId);
		try {
			User user = userDao.find(session.getUserId());
			if(!PasswordManager.hashPassword(oldPassword).contentEquals(user.getPassword())) 
					if ((user.getSecondPasswordExpDate()==null) || (user.getSecondPasswordExpDate().before(Calendar.getInstance())) || (!user.getSecondPassword().contentEquals(PasswordManager.hashPassword(oldPassword)))) 
						throw new ServiceException(ServiceException.INCORRECT_FIELD,"pass"); 
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
	public String getUserPermissionsUSER(String sessionId)
			throws ServiceException {
		Session session = SessionManager.getSession(sessionId);
		try {
			return userDao.find(session.getUserId()).getPremissions();
		} catch (InstanceException e) {
			throw new  ServiceException(ServiceException.INSTANCE_NOT_FOUND,"User");
		}
	}

	// ADMIN

	@Override
	@Transactional(readOnly = true)
	public List<Session> getAllUserSessionsADMIN(String sessionId, int userId)
			throws ServiceException {
		try { 
			if(!checkPermissions(userDao.find(SessionManager.getSession(sessionId).getUserId()), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"user");
		}
		return SessionManager.getAllUserSessions(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public void closeAllUserSessionsADMIN(String sessionId, int userId)
			throws ServiceException {
		try { 
			if(!checkPermissions(userDao.find(SessionManager.getSession(sessionId).getUserId()), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"user");
		}
		SessionManager.closeAllUserSessions(userId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public User getUserADMIN(String sessionId, int userId) throws ServiceException {
		try { 
			if(!checkPermissions(userDao.find(SessionManager.getSession(sessionId).getUserId()), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
			return userDao.find(userId);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"user");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<User> getAllUsersADMIN(String sessionId, int startIndex,
			int maxResults, String orderBy, boolean desc)
			throws ServiceException {
		try { 
			if(!checkPermissions(userDao.find(SessionManager.getSession(sessionId).getUserId()), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"user");
		}
		return userDao.getAllUsers(startIndex, maxResults, orderBy, desc);
	}

	@Override
	@Transactional(readOnly = true)
	public long getAllUsersTAMADMIN(String sessionId) throws ServiceException {
		try { 
			if(!checkPermissions(userDao.find(SessionManager.getSession(sessionId).getUserId()), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"user");
		}
		return userDao.getAllUsersTAM();
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> findUsersByNameADMIN(String sessionId, String name, int startindex, int maxResults) 
			throws ServiceException {
		try { 
			if(!checkPermissions(userDao.find(SessionManager.getSession(sessionId).getUserId()), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"user");
		}
		return userDao.findUsersByName(name,startindex,maxResults);
	}

	@Override
	@Transactional
	public void removeUserADMIN(String sessionId, int userId)
			throws ServiceException {
		try { 
			if(!checkPermissions(userDao.find(SessionManager.getSession(sessionId).getUserId()), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"user");
		}
		try {
			userDao.remove(userId);
		} catch (InstanceException e) {}

	}

	@Override
	@Transactional
	public void changeUserDataADMIN(String sessionId, int userId, User userData)
			throws ServiceException {
		try { 
			if(!checkPermissions(userDao.find(SessionManager.getSession(sessionId).getUserId()), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"user");
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
	public void changeUserPasswordADMIN(String sessionId, int userId,
			String oldPassword, String newPassword) throws ServiceException {
		try { 
			if(!checkPermissions(userDao.find(SessionManager.getSession(sessionId).getUserId()), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"user");
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
			if(!checkPermissions(userDao.find(SessionManager.getSession(sessionId).getUserId()), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"user");
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
			if(!checkPermissions(userDao.find(SessionManager.getSession(sessionId).getUserId()), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"user");
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
	public String removeUserPermissionsADMIN(String sessionId, int userId,
			String permission) throws ServiceException {
		try { 
			if(!checkPermissions(userDao.find(SessionManager.getSession(sessionId).getUserId()), USERSERVICEPERMISIONLEVEL))
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND,"user");
		}
		try {
			User user = userDao.find(userId);
			if(!user.getPremissions().contains(permission)) 
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
