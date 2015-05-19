package es.ficonlan.web.api.model.userService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import es.ficonlan.web.api.jersey.Main;
import es.ficonlan.web.api.model.email.Email;
import es.ficonlan.web.api.model.email.EmailFIFOFactory;
import es.ficonlan.web.api.model.emailtemplate.EmailTemplate;
import es.ficonlan.web.api.model.emailtemplate.EmailTemplateDao;
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

	public static final String ADMIN_LOGIN = "Admin";
	public static final String INITIAL_ADMIN_PASS = "initialAdminPass";

	private Properties properties;

	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailTemplateDao emailTemplateDao;

	@Override
	@Transactional
	public void initialize() {

		try {
			properties = new Properties();
			InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("backend.properties");
			properties.load(inputStream);
		} catch (IOException e) {
			throw new RuntimeException("Could not read config file: " + e.getMessage());
		}

		User admin = userDao.findUserBylogin(ADMIN_LOGIN);
		if (admin == null)
			admin = new User("EMNRSU", "Administrador", ADMIN_LOGIN, PasswordManager.hashPassword(INITIAL_ADMIN_PASS), "0", "adminMail", "-", "-", Calendar.getInstance(), "ES");
		userDao.save(admin);

	}

	// ANONYMOUS

	@Override
	@Transactional
	public boolean passwordRecover(String email) throws ServiceException {

		int minutos = 30;

		User user = userDao.findUserByEmail(email);
		EmailTemplate template = emailTemplateDao.findByName("passwordRecover");

		if ((user != null) && (template != null)) {

			String pass = PasswordManager.generatePassword();

			user.setSecondPassword(PasswordManager.hashPassword(pass));
			Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			now.add(Calendar.MINUTE, minutos);
			user.setSecondPasswordExpDate(now);
			userDao.save(user);

			Hashtable<String, String> tabla = new Hashtable<String, String>();
			tabla.put("#loginusuario", user.getLogin());
			tabla.put("#nuevapas", pass);
			tabla.put("#tiemporestante", Integer.toString(minutos));

			Email e = emailTemplateDao.findByName("passwordRecover").generateEmail(user, tabla);

			try {
				EmailFIFOFactory.getEmailFIFO().adEmailToQueue(e);
			} catch (MessagingException e1) {
				throw new ServiceException(ServiceException.OTHER, "Error on EmailFIFO");
			}

			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public User addUser(User user) throws ServiceException {
		User u = userDao.findUserBylogin(user.getLogin());
		if (u != null)
			throw new ServiceException(ServiceException.DUPLICATED_FIELD, "login");
		u = userDao.findUserByDni(user.getDni());
		if (u != null)
			throw new ServiceException(ServiceException.DUPLICATED_FIELD, "dni");
		if (user.getLogin() == null)
			throw new ServiceException(ServiceException.MISSING_FIELD, "login");
		if (user.getPassword() == null)
			throw new ServiceException(ServiceException.MISSING_FIELD, "password");
		if (user.getName() == null)
			throw new ServiceException(ServiceException.MISSING_FIELD, "name");
		if (user.getDni() == null)
			throw new ServiceException(ServiceException.MISSING_FIELD, "dni");
		u = userDao.findUserByEmail(user.getEmail());
		if (u != null)
			throw new ServiceException(ServiceException.DUPLICATED_FIELD, "email");
		if (user.getEmail() == null)
			throw new ServiceException(ServiceException.MISSING_FIELD, "email");
		if (user.getDob() == null)
			throw new ServiceException(ServiceException.MISSING_FIELD, "dob");
		if (user.getLanguage() == null)
			throw new ServiceException(ServiceException.MISSING_FIELD, "language");
		user.setPremissions("");
		String pass = PasswordManager.hashPassword(user.getPassword());
		user.setPassword(pass);
		user.setSecondPassword(pass);
		userDao.save(user);
		return user;
	}

	// USER

	@Override
	@Transactional
	public void removeUser(int userId) throws ServiceException {
		try {
			userDao.remove(userId);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION, "Session");
		}
	}

	@Override
	@Transactional
	public void changeUserData(int userId, User userData) throws ServiceException {
		try {
			User user = userDao.find(userId);
			if (userData.getName() != null)
				user.setName(userData.getName());
			User u = userDao.findUserByEmail(userData.getEmail());
			if (u != null)
				if (!(u.getUserId() == user.getUserId()))
					throw new ServiceException(ServiceException.DUPLICATED_FIELD, "email");
			if (userData.getEmail() != null)
				user.setEmail(userData.getEmail());
			if (userData.getPhoneNumber() != null)
				user.setPhoneNumber(userData.getPhoneNumber());
			if (userData.getShirtSize() != null)
				user.setShirtSize(userData.getShirtSize());
			userDao.save(user);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND, "User");
		}
	}

	@Override
	@Transactional
	public void changeUserPasswordUSER(int userId, String oldPassword, String newPassword) throws ServiceException {
		try {
			User user = userDao.find(userId);

			if (!PasswordManager.hashPassword(oldPassword).contentEquals(user.getPassword()))
				if ((user.getSecondPasswordExpDate() == null) || (user.getSecondPasswordExpDate().before(Calendar.getInstance()))
						|| (!user.getSecondPassword().contentEquals(PasswordManager.hashPassword(oldPassword))))
					throw new ServiceException(ServiceException.INCORRECT_FIELD, "pass");
			user.setSecondPasswordExpDate(null);
			user.setPassword(PasswordManager.hashPassword(newPassword));
			user.setSecondPassword(user.getSecondPassword());
			userDao.save(user);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND, "User");
		}

	}

	@Override
	@Transactional(readOnly = true)
	public String getUserPermissions(int userId) throws ServiceException {
		try {
			return userDao.find(userId).getPremissions();
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND, "User");
		}
	}

	// ADMIN

	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public User getUser(int userId) throws ServiceException {
		try {
			return userDao.find(userId);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INVALID_SESSION, "User");
		}
	}

	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<User> getAllUsers(int startIndex, int maxResults, String orderBy, boolean desc) throws ServiceException {
		return userDao.getAllUsers(startIndex, maxResults, orderBy, desc);
	}

	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public long getAllUsersTAM() throws ServiceException {
		return userDao.getAllUsersTAM();
	}

	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<User> findUsersByName(String name, int startindex, int maxResults) throws ServiceException {
		return userDao.findUsersByName(name, startindex, maxResults);
	}

	@Override
	@Transactional
	public void changeUserPasswordADMIN(int userId, String newPassword) throws ServiceException {
		try {
			User user = userDao.find(userId);
			user.setSecondPasswordExpDate(null);
			user.setPassword(PasswordManager.hashPassword(newPassword));
			user.setSecondPassword(user.getSecondPassword());
			userDao.save(user);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND, "User");
		}
	}

	@Override
	@Transactional
	public String addUserPermissions(int userId, String permission) throws ServiceException {
		try {
			User user = userDao.find(userId);
			if (!user.getPremissions().contains(permission)) {
				user.setPremissions(user.getPremissions() + permission);
				userDao.save(user);
				return user.getPremissions();
			}
			return user.getPremissions();
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND, "User");
		}
	}

	@Override
	@Transactional
	public String removeUserPermissions(int userId, String permission) throws ServiceException {
		try {
			User user = userDao.find(userId);
			if (user.getPremissions().contains(permission)) {
				user.setPremissions(user.getPremissions().replace(permission, ""));
				userDao.save(user);
				return user.getPremissions();
			}
			return user.getPremissions();
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND, "User");
		}
	}

}
