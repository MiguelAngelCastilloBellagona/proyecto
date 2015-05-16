package es.ficonlan.web.api.test.userservice;

import static es.ficonlan.web.api.model.util.GlobalNames.SPRING_CONFIG_FILE;
import static es.ficonlan.web.api.test.util.GlobalNames.SPRING_CONFIG_TEST_FILE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import es.ficonlan.web.api.jersey.Main;
import es.ficonlan.web.api.model.emailtemplate.EmailTemplate;
import es.ficonlan.web.api.model.emailtemplate.EmailTemplateDao;
import es.ficonlan.web.api.model.session.Session;
import es.ficonlan.web.api.model.session.SessionDao;
import es.ficonlan.web.api.model.sessionService.SessionService;
import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.user.UserDao;
import es.ficonlan.web.api.model.userService.UserService;
import es.ficonlan.web.api.model.userService.UserServiceImpl;
import es.ficonlan.web.api.model.util.exceptions.InstanceException;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {SPRING_CONFIG_FILE, SPRING_CONFIG_TEST_FILE})
@Transactional
public class UserSessionServiceTest {
	
	private static Properties properties;

	static {
		try {
			properties = new Properties();
			InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("backend.properties");
			properties.load(inputStream);
		} catch (IOException e) {
			throw new RuntimeException("Could not read config file: " + e.getMessage());
		}
	}

	@SuppressWarnings("unused")
	private static final int SESSION_TIMEOUT = Math.abs(Integer.parseInt(properties.getProperty("session.timeout")));
	

	@Autowired
    private UserService userService;
    
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private SessionDao sessionDao;
	
	@Autowired
	private EmailTemplateDao emailTemplateDao;
	
	@Autowired
    private SessionService sessionService;
	
	@Before 
	public void initialize() throws ServiceException {
	     userService.initialize(); 
	}
	
	@Test
	public void correctTest() {
		
	}
	
	/*
	@Test
	public void closeOldSessionsTest() throws ServiceException, InstanceException {
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    Session s = sessionDao.find(sessionService.login("login", "password").getSessionId());
	    sessionService.closeOldSessions();
	    sessionDao.find(s.getSessionId());
	    
	    Calendar now = Calendar.getInstance();
	    now.add(Calendar.MINUTE, (SESSION_TIMEOUT*-1)-1);
	    s.setLastAccess(now);
	    sessionService.closeOldSessions();
	    try {
	    	sessionDao.find(s.getSessionId());
	    	Assert.fail();
	    } catch (InstanceException e) { }
	}
	*/
	
	@Test
	public void passwordRecoverTest() throws ServiceException {
		try {
			emailTemplateDao.remove(emailTemplateDao.findByName("passwordRecover").getEmailtemplateid());
		} catch (InstanceException e1) {} catch (NullPointerException e1) {}
		assertFalse(userService.passwordRecover("email@yopmail.com"));
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    EmailTemplate e = new EmailTemplate();
	    e.setName("passwordRecover");
	    e.setAsunto("Asunto");
	    e.setContenido(" ");
	    emailTemplateDao.save(e);
	    
		assertTrue(userService.passwordRecover("email@yopmail.com"));
		
		assertFalse(userService.passwordRecover("Noemail@yopmails.com"));
	}
	
	@Test
	public void addUserTest() throws InstanceException, ServiceException {
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    User u1 = userDao.find(user.getUserId());
	    Assert.assertEquals("User", u1.getName());
	    User user2 = new User("", "User", "login", "password", "00000000B", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    try {
			userService.addUser(user2);
			Assert.fail();
		} catch (ServiceException e) {}
	    User user3 = new User("", "User", "login2", "password", "00000000A", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    try {
			userService.addUser(user3);
			Assert.fail();
		} catch (ServiceException e) {}
	    User user4 = new User("", "User", "login2", "password", "00000000B", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    try {
			userService.addUser(user4);
			Assert.fail();
		} catch (ServiceException e) {}  
	}
	
	@Test
	public void loginTest() throws ServiceException {
		try {
			sessionService.login("login", "password");
			Assert.fail();
		} catch (ServiceException e) {}
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    try {
	    	sessionService.login("login2", "password");
			Assert.fail();
		} catch (ServiceException e) {}
	    try {
	    	sessionService.login("login", "password2");
			Assert.fail();
		} catch (ServiceException e) {}
	    try {
	    	sessionService.login("login2", "password2");
			Assert.fail();
		} catch (ServiceException e) {}

	    sessionService.login("login", "password");   
	}
 
	@Test
	public void sessionExistsTest() throws InstanceException, ServiceException {
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    String sessionId = sessionService.login("login", "password").getSessionId();
	    Assert.assertTrue(sessionService.sessionExists(sessionId));
	    sessionDao.remove(sessionId);
	    Assert.assertFalse(sessionService.sessionExists(sessionId));
	}
	
	@Test
	public void getUserUSERTest() throws ServiceException {
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    String sessionId = sessionService.login("login", "password").getSessionId();
	    User user2 = sessionService.getUserUSER(sessionId);
	    Assert.assertEquals(user.getUserId(), user2.getUserId());
	}
	
	@Test
	public void removeUserUSERTest() throws ServiceException {
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    userService.removeUser(user.getUserId());
	    try {
			userService.removeUser(user.getUserId());
			Assert.fail();
		} catch (ServiceException e) {}
	}
	
	@Test
	public void changeUserDataUSERTest () throws ServiceException {
		User user1 = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user2 = new User("", "User2", "login2", "password2", "00000000B", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user2);
	    User userData = new User("", "User", "login", "password", "00000000C", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userData = new User("", "User", "login3", "password", "00000000C", "email3@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.changeUserData(user2.getUserId(), userData);
	}
	
	@Test
	public void changeUserPasswordUSERTest () throws ServiceException {
		User user1 = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user = userDao.findUserBylogin("login");
	    try {
			userService.changeUserPasswordUSER(user.getUserId(), "password2", "newPassword");
			Assert.fail();
		} catch (ServiceException e) {}
	    userService.changeUserPasswordUSER(user.getUserId(), "password", "newPassword");
	}
	
	@Test
	public void getUserPermissionsUSERTest () throws ServiceException {
		User user1 = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user2 = userDao.findUserByDni("00000000A");
	    user2.setPremissions("AU");
	    userDao.save(user2);
	    Assert.assertEquals("AU",userService.getUserPermissions(sessionService.login("login", "password").getUserId()));
	}
	
	@SuppressWarnings("unused")
	@Test
	public void closeAllUserSessionsTest () throws ServiceException {
		User user = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user);
		String sessionId1 = sessionService.login("login", "password").getSessionId();
		String sessionId2 = sessionService.login("login", "password").getSessionId();
		String sessionId3 = sessionService.login("login", "password").getSessionId();
		List<Session> ls = sessionDao.findSessionByUserId(user.getUserId());
		Assert.assertEquals(3, ls.size());
		sessionService.closeAllUserSessions(sessionId1);
		ls = sessionDao.findSessionByUserId(user.getUserId());
		Assert.assertEquals(0, ls.size());
	}
	
	@Test
	public void closeUserSessionTest () throws ServiceException {
		User user = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user);
		String sessionId1 = sessionService.login("login", "password").getSessionId();
		String sessionId2 = sessionService.login("login", "password").getSessionId();
		String sessionId3 = sessionService.login("login", "password").getSessionId();
		List<Session> ls = sessionDao.findSessionByUserId(user.getUserId());
		Assert.assertEquals(3, ls.size());
		sessionService.closeUserSession(sessionId1);
		ls = sessionDao.findSessionByUserId(user.getUserId());
		Assert.assertEquals(2, ls.size());
		sessionService.closeUserSession(sessionId2);
		ls = sessionDao.findSessionByUserId(user.getUserId());
		Assert.assertEquals(1, ls.size());
		sessionService.closeUserSession(sessionId3);
		ls = sessionDao.findSessionByUserId(user.getUserId());
		Assert.assertEquals(0, ls.size());
		try {
			sessionService.closeUserSession(sessionId1);
			Assert.fail();
		} catch (ServiceException e) {}
	}
	
	@SuppressWarnings("unused")
	@Test
	public void getAllUserSessionsADMINTest () throws ServiceException {
		
		String sesionIdAdmin = sessionService.login(UserServiceImpl.ADMIN_LOGIN, UserServiceImpl.INITIAL_ADMIN_PASS).getSessionId();
		
		User user = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user);
		String sessionId1 = sessionService.login("login", "password").getSessionId();
		String sessionId2 = sessionService.login("login", "password").getSessionId();
		String sessionId3 = sessionService.login("login", "password").getSessionId();
		List<Session> ls = sessionService.getAllUserSessionsADMIN(user.getUserId());
		Assert.assertEquals(3, ls.size());
	}
	
	@Test
	public void getUserADMINTest () throws ServiceException {
		
		User user = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user);
		User user2 = userService.getUser(user.getUserId());
		Assert.assertEquals(user.getUserId(), user2.getUserId());
	}
	
	@Test
	public void getAllUsersADMINTest () throws ServiceException {
		
		User user1 = new User("AU", "User", "login1", "password", "00000000A", "email1@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user1);
		User user2 = new User("AU", "User", "login2", "password", "00000000B", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user2);
		User user3 = new User("AU", "User", "login3", "password", "00000000C", "email3@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user3);
		List<User> list = userService.getAllUsers( 0, 10, "userId", false);
		Assert.assertEquals(4,list.size());
	}
	
	@Test
	public void getAllUsersTAMADMINTest () throws ServiceException {
		
		User user1 = new User("AU", "User", "login1", "password", "00000000A", "email1@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user1);
		User user2 = new User("AU", "User", "login2", "password", "00000000B", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user2);
		User user3 = new User("AU", "User", "login3", "password", "00000000C", "email3@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user3);
		
		Assert.assertEquals(4,userService.getAllUsersTAM());
	}
	
	@Test
	public void findUsersByNameADMINTest () throws ServiceException {
		
		User user1 = new User("AU", "User", "login1", "password", "00000000A", "email1@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user1);
		User user2 = new User("AU", "User", "login2", "password", "00000000B", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user2);
		User user3 = new User("AU", "User", "login3", "password", "00000000C", "email3@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user3);
		List<User> list = userService.findUsersByName( "User", 0, 10);
		Assert.assertEquals(3,list.size());
	}
	
	@Test
	public void removeUserADMINTest () throws ServiceException, InstanceException {
		
		User user = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user);
		
		userDao.find(user.getUserId());
		
		userService.removeUser(user.getUserId());
		
		try {
			userDao.find(user.getUserId());
			Assert.fail();
		} catch (InstanceException e) {}
	}
	
	@Test
	public void changeUserDataADMINTest () throws ServiceException {
		
		User user1 = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user2 = new User("", "User2", "login2", "password2", "00000000B", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user2);
	    User userData = new User("", "User", "login", "password", "00000000C", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    try {
			userService.changeUserData(user1.getUserId(), userData);
			Assert.fail();
		} catch (ServiceException e) {}
	    userData = new User("", "User", "login3", "password", "00000000C", "email3@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.changeUserData( user1.getUserId(), userData);
	}
	
	@Test
	public void changeUserPasswordADMINTest () throws ServiceException {
		
		User user1 = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    
	    userService.changeUserPasswordADMIN(user1.getUserId(), "newPassword");
	}
	
	@Test
	public void getUserPermissionsADMINest () throws ServiceException {
		
		User user1 = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user2 = userDao.findUserByDni("00000000A");
	    user2.setPremissions("AU");
	    userDao.save(user2);
	    Assert.assertEquals("AU",userService.getUserPermissions(user2.getUserId()));
	}
	
	@Test
	public void addUserPermissionsADMINTest () throws ServiceException {
		
		User user1 = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user2 = userDao.findUserByDni("00000000A");
	    user2.setPremissions("AU");
	    userDao.save(user2);
	    Assert.assertEquals("AU",userService.getUserPermissions(user2.getUserId()));
	    userService.addUserPermissions(user2.getUserId(), "");
	    Assert.assertEquals("AU",userService.getUserPermissions(user2.getUserId()));
	    userService.addUserPermissions(user2.getUserId(), "A");
	    Assert.assertEquals("AU",userService.getUserPermissions(user2.getUserId()));
	    userService.addUserPermissions( user2.getUserId(), "B");
	    Assert.assertEquals("AUB",userService.getUserPermissions(user2.getUserId()));
	    userService.addUserPermissions(user2.getUserId(), "CD");
	    Assert.assertEquals("AUBCD",userService.getUserPermissions(user2.getUserId()));
	}
	
	@Test
	public void removeUserPermissionsTest () throws ServiceException {
		
		User user1 = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user2 = userDao.findUserByDni("00000000A");
	    user2.setPremissions("AU");
	    userDao.save(user2);
	    Assert.assertEquals("AU",userService.getUserPermissions(user2.getUserId()));
	    userService.removeUserPermissions(user2.getUserId(), "");
	    Assert.assertEquals("AU",userService.getUserPermissions(user2.getUserId()));
	    userService.removeUserPermissions(user2.getUserId(), "U");
	    Assert.assertEquals("A",userService.getUserPermissions(user2.getUserId()));
	    userService.removeUserPermissions(user2.getUserId(), "B");
	    Assert.assertEquals("A",userService.getUserPermissions(user2.getUserId()));
	    userService.removeUserPermissions(user2.getUserId(), "A");
	    Assert.assertEquals("",userService.getUserPermissions(user2.getUserId()));  
	}
}
