package es.ficonlan.web.api.test.userservice;

import static es.ficonlan.web.api.model.util.GlobalNames.SPRING_CONFIG_FILE;
import static es.ficonlan.web.api.test.util.GlobalNames.SPRING_CONFIG_TEST_FILE;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.junit.After;
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
public class UserServiceTest {
	
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

	private static final int SESSION_TIMEOUT = Math.abs(Integer.parseInt(properties.getProperty("session.timeout")));
	

	@Autowired
    private UserService userService;
    
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private SessionDao sessionDao;
	
	@Autowired
	private EmailTemplateDao emailTemplateDao;
	
	@Before 
	public void initialize() throws ServiceException {
	     userService.initialize(); 
	}
	
	@After
	public void close() throws ServiceException, InstanceException {
	}
	
	@Test
	public void correctTest() {
		
	}
	
	
	@Test
	public void closeOldSessionsTest() throws ServiceException, InstanceException {
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    Session s = sessionDao.find(userService.login("login", "password").getSessionId());
	    userService.closeOldSessions();
	    sessionDao.find(s.getSessionId());
	    
	    Calendar now = Calendar.getInstance();
	    now.add(Calendar.MINUTE, (SESSION_TIMEOUT*-1)-1);
	    s.setLastAccess(now);
	    userService.closeOldSessions();
	    try {
	    	sessionDao.find(s.getSessionId());
	    	Assert.fail();
	    } catch (InstanceException e) { }
	}
	
	@Test
	public void passwordRecoverTest() throws ServiceException {
		assertFalse(userService.passwordRecover("email@yopmail.com"));
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    System.out.println(emailTemplateDao.findByName("passwordRecover"));
	    System.out.println(userService.passwordRecover("email@yopmail.com"));
	    assertFalse(userService.passwordRecover("email@yopmail.com"));
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
			userService.login("login", "password");
			Assert.fail();
		} catch (ServiceException e) {}
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    try {
			userService.login("login2", "password");
			Assert.fail();
		} catch (ServiceException e) {}
	    try {
			userService.login("login", "password2");
			Assert.fail();
		} catch (ServiceException e) {}
	    try {
			userService.login("login2", "password2");
			Assert.fail();
		} catch (ServiceException e) {}

	    userService.login("login", "password");   
	}
 
	@Test
	public void setSessionLastAccessNowTest() throws InstanceException, ServiceException {
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    Session s = sessionDao.find(userService.login("login", "password").getSessionId());
	    Calendar n = Calendar.getInstance();
	    n.add(Calendar.MINUTE, 14);
	    s.setLastAccess(n);
	    Assert.assertEquals(n.getTime(), s.getLastAccess().getTime());
	    sessionDao.save(s);
	    userService.setSessionLastAccessNow(s.getSessionId());
	    //Assert.assertEquals(Calendar.getInstance().getTime(), s.getLastAccess().getTime());
	}
	
	@Test
	public void sessionExistsTest() throws InstanceException, ServiceException {
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    String sessionId = userService.login("login", "password").getSessionId();
	    Assert.assertTrue(userService.sessionExists(sessionId));
	    sessionDao.remove(sessionId);
	    Assert.assertFalse(userService.sessionExists(sessionId));
	}
	
	@Test
	public void getCurrenUserUSERTest() throws ServiceException {
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    String sessionId = userService.login("login", "password").getSessionId();
	    User user2 = userService.getCurrenUserUSER(sessionId);
	    Assert.assertEquals(user.getUserId(), user2.getUserId());
	}
	
	@Test
	public void removeUserUSERTest() throws ServiceException {
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    String sessionId = userService.login("login", "password").getSessionId();
	    userService.removeUserUSER(sessionId);
	    try {
			userService.removeUserUSER(sessionId);
			Assert.fail();
		} catch (ServiceException e) {}
	}
	
	@Test
	public void changeUserDataUSERTest () throws ServiceException {
		User user1 = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user2 = new User("", "User2", "login2", "password2", "00000000B", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user2);
	    String sessionId = userService.login("login2", "password2").getSessionId();
	    User userData = new User("", "User", "login", "password", "00000000C", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    try {
			userService.changeUserDataUSER(sessionId, userData);
			Assert.fail();
		} catch (ServiceException e) {}
	    userData = new User("", "User", "login3", "password", "00000000C", "email3@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.changeUserDataUSER(sessionId, userData);
	}
	
	@Test
	public void changeUserPasswordUSERTest () throws ServiceException {
		User user1 = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    String sessionId = userService.login("login", "password").getSessionId();
	    try {
			userService.changeUserPasswordUSER(sessionId, "password2", "newPassword");
			Assert.fail();
		} catch (ServiceException e) {}
	    userService.changeUserPasswordUSER(sessionId, "password", "newPassword");
	}
	
	@Test
	public void getUserPermissionsUSERTest () throws ServiceException {
		User user1 = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user2 = userDao.findUserByDni("00000000A");
	    user2.setPremissions("AU");
	    userDao.save(user2);
	    String sessionId = userService.login("login", "password").getSessionId();
	    try {
			userService.getUserPermissionsUSER(sessionId + "asd");
			Assert.fail();
		} catch (ServiceException e) {}
	    Assert.assertEquals("AU",userService.getUserPermissionsUSER(sessionId));
	}
	
	@SuppressWarnings("unused")
	@Test
	public void closeAllUserSessionsTest () throws ServiceException {
		User user = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user);
		String sessionId1 = userService.login("login", "password").getSessionId();
		String sessionId2 = userService.login("login", "password").getSessionId();
		String sessionId3 = userService.login("login", "password").getSessionId();
		List<Session> ls = sessionDao.findSessionByUserId(user.getUserId());
		Assert.assertEquals(3, ls.size());
		userService.closeAllUserSessions(sessionId1);
		ls = sessionDao.findSessionByUserId(user.getUserId());
		Assert.assertEquals(0, ls.size());
	}
	
	@Test
	public void closeUserSessionTest () throws ServiceException {
		User user = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user);
		String sessionId1 = userService.login("login", "password").getSessionId();
		String sessionId2 = userService.login("login", "password").getSessionId();
		String sessionId3 = userService.login("login", "password").getSessionId();
		List<Session> ls = sessionDao.findSessionByUserId(user.getUserId());
		Assert.assertEquals(3, ls.size());
		userService.closeUserSession(sessionId1);
		ls = sessionDao.findSessionByUserId(user.getUserId());
		Assert.assertEquals(2, ls.size());
		userService.closeUserSession(sessionId2);
		ls = sessionDao.findSessionByUserId(user.getUserId());
		Assert.assertEquals(1, ls.size());
		userService.closeUserSession(sessionId3);
		ls = sessionDao.findSessionByUserId(user.getUserId());
		Assert.assertEquals(0, ls.size());
		try {
			userService.closeUserSession(sessionId1);
			Assert.fail();
		} catch (ServiceException e) {}
	}
	
	@SuppressWarnings("unused")
	@Test
	public void getAllUserSessionsADMINTest () throws ServiceException {
		
		String sesionIdAdmin = userService.login(UserServiceImpl.ADMIN_LOGIN, UserServiceImpl.INITIAL_ADMIN_PASS).getSessionId();
		
		User user = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user);
		String sessionId1 = userService.login("login", "password").getSessionId();
		String sessionId2 = userService.login("login", "password").getSessionId();
		String sessionId3 = userService.login("login", "password").getSessionId();
		List<Session> ls = userService.getAllUserSessionsADMIN(sesionIdAdmin,user.getUserId());
		Assert.assertEquals(3, ls.size());
	}
	
	@Test
	public void getUserADMINTest () throws ServiceException {
		
		String sesionIdAdmin = userService.login(UserServiceImpl.ADMIN_LOGIN, UserServiceImpl.INITIAL_ADMIN_PASS).getSessionId();
		
		User user = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user);
		User user2 = userService.getUserADMIN(sesionIdAdmin,user.getUserId());
		Assert.assertEquals(user.getUserId(), user2.getUserId());
	}
	
	@Test
	public void getAllUsersADMINTest () throws ServiceException {
		
		String sesionIdAdmin = userService.login(UserServiceImpl.ADMIN_LOGIN, UserServiceImpl.INITIAL_ADMIN_PASS).getSessionId();
		
		User user1 = new User("AU", "User", "login1", "password", "00000000A", "email1@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user1);
		User user2 = new User("AU", "User", "login2", "password", "00000000B", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user2);
		User user3 = new User("AU", "User", "login3", "password", "00000000C", "email3@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user3);
		List<User> list = userService.getAllUsersADMIN(sesionIdAdmin, 0, 10, "userId", false);
		Assert.assertEquals(4,list.size());
	}
	
	@Test
	public void getAllUsersTAMADMINTest () throws ServiceException {
		
		String sesionIdAdmin = userService.login(UserServiceImpl.ADMIN_LOGIN, UserServiceImpl.INITIAL_ADMIN_PASS).getSessionId();
		
		User user1 = new User("AU", "User", "login1", "password", "00000000A", "email1@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user1);
		User user2 = new User("AU", "User", "login2", "password", "00000000B", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user2);
		User user3 = new User("AU", "User", "login3", "password", "00000000C", "email3@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user3);
		
		Assert.assertEquals(4,userService.getAllUsersTAMADMIN(sesionIdAdmin));
	}
	
	@Test
	public void findUsersByNameADMINTest () throws ServiceException {
		
		String sesionIdAdmin = userService.login(UserServiceImpl.ADMIN_LOGIN, UserServiceImpl.INITIAL_ADMIN_PASS).getSessionId();
		
		User user1 = new User("AU", "User", "login1", "password", "00000000A", "email1@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user1);
		User user2 = new User("AU", "User", "login2", "password", "00000000B", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user2);
		User user3 = new User("AU", "User", "login3", "password", "00000000C", "email3@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user3);
		List<User> list = userService.findUsersByNameADMIN(sesionIdAdmin, "User", 0, 10);
		Assert.assertEquals(3,list.size());
	}
	
	@Test
	public void removeUserADMINTest () throws ServiceException, InstanceException {
		
		String sesionIdAdmin = userService.login(UserServiceImpl.ADMIN_LOGIN, UserServiceImpl.INITIAL_ADMIN_PASS).getSessionId();
		
		User user = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
		userService.addUser(user);
		
		userDao.find(user.getUserId());
		
		userService.removeUserADMIN(sesionIdAdmin, user.getUserId());
		
		try {
			userDao.find(user.getUserId());
			Assert.fail();
		} catch (InstanceException e) {}
	}
	
	@Test
	public void changeUserDataADMINTest () throws ServiceException {
		
		String sesionIdAdmin = userService.login(UserServiceImpl.ADMIN_LOGIN, UserServiceImpl.INITIAL_ADMIN_PASS).getSessionId();
		
		User user1 = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user2 = new User("", "User2", "login2", "password2", "00000000B", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user2);
	    String sessionId = userService.login("login2", "password2").getSessionId();
	    User userData = new User("", "User", "login", "password", "00000000C", "email2@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    try {
			userService.changeUserDataADMIN(sesionIdAdmin, user1.getUserId(), userData);
			Assert.fail();
		} catch (ServiceException e) {}
	    userData = new User("", "User", "login3", "password", "00000000C", "email3@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    try {
			userService.changeUserDataADMIN(sessionId, user1.getUserId(), userData);
			Assert.fail();
		} catch (ServiceException e) {}
	    userService.changeUserDataADMIN(sesionIdAdmin, user1.getUserId(), userData);
	}
	
	@Test
	public void changeUserPasswordADMINTest () throws ServiceException {
		
		String sesionIdAdmin = userService.login(UserServiceImpl.ADMIN_LOGIN, UserServiceImpl.INITIAL_ADMIN_PASS).getSessionId();
		
		User user1 = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    
	    userService.changeUserPasswordADMIN(sesionIdAdmin, user1.getUserId(), "newPassword");
	}
	
	@Test
	public void getUserPermissionsADMINest () throws ServiceException {
		
		String sesionIdAdmin = userService.login(UserServiceImpl.ADMIN_LOGIN, UserServiceImpl.INITIAL_ADMIN_PASS).getSessionId();
		
		User user1 = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user2 = userDao.findUserByDni("00000000A");
	    user2.setPremissions("AU");
	    userDao.save(user2);
	    String sessionId = userService.login("login", "password").getSessionId();
	    try {
			userService.getUserPermissionsADMIN(sessionId + "asd",user2.getUserId());
			Assert.fail();
		} catch (ServiceException e) {}
	    Assert.assertEquals("AU",userService.getUserPermissionsADMIN(sesionIdAdmin,user2.getUserId()));
	}
	
	@Test
	public void addUserPermissionsADMINTest () throws ServiceException {
		
		String sesionIdAdmin = userService.login(UserServiceImpl.ADMIN_LOGIN, UserServiceImpl.INITIAL_ADMIN_PASS).getSessionId();
		
		User user1 = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user2 = userDao.findUserByDni("00000000A");
	    user2.setPremissions("AU");
	    userDao.save(user2);
	    String sessionId = userService.login("login", "password").getSessionId();
	    try {
			userService.getUserPermissionsADMIN(sessionId + "asd",user2.getUserId());
			Assert.fail();
		} catch (ServiceException e) {}
	    Assert.assertEquals("AU",userService.getUserPermissionsADMIN(sesionIdAdmin,user2.getUserId()));
	    userService.addUserPermissionsADMIN(sesionIdAdmin, user2.getUserId(), "");
	    Assert.assertEquals("AU",userService.getUserPermissionsADMIN(sesionIdAdmin,user2.getUserId()));
	    userService.addUserPermissionsADMIN(sesionIdAdmin, user2.getUserId(), "A");
	    Assert.assertEquals("AU",userService.getUserPermissionsADMIN(sesionIdAdmin,user2.getUserId()));
	    userService.addUserPermissionsADMIN(sesionIdAdmin, user2.getUserId(), "B");
	    Assert.assertEquals("AUB",userService.getUserPermissionsADMIN(sesionIdAdmin,user2.getUserId()));
	    userService.addUserPermissionsADMIN(sesionIdAdmin, user2.getUserId(), "CD");
	    Assert.assertEquals("AUBCD",userService.getUserPermissionsADMIN(sesionIdAdmin,user2.getUserId()));
	}
	
	@Test
	public void removeUserPermissionsADMINTest () throws ServiceException {
		
		String sesionIdAdmin = userService.login(UserServiceImpl.ADMIN_LOGIN, UserServiceImpl.INITIAL_ADMIN_PASS).getSessionId();
		
		User user1 = new User("AU", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user1);
	    User user2 = userDao.findUserByDni("00000000A");
	    user2.setPremissions("AU");
	    userDao.save(user2);
	    String sessionId = userService.login("login", "password").getSessionId();
	    try {
			userService.removeUserPermissionsADMIN(sessionId + "asd",user2.getUserId(),"A");
			Assert.fail();
		} catch (ServiceException e) {}
	    Assert.assertEquals("AU",userService.getUserPermissionsADMIN(sesionIdAdmin,user2.getUserId()));
	    userService.removeUserPermissionsADMIN(sesionIdAdmin, user2.getUserId(), "");
	    Assert.assertEquals("AU",userService.getUserPermissionsADMIN(sesionIdAdmin,user2.getUserId()));
	    userService.removeUserPermissionsADMIN(sesionIdAdmin, user2.getUserId(), "U");
	    Assert.assertEquals("A",userService.getUserPermissionsADMIN(sesionIdAdmin,user2.getUserId()));
	    userService.removeUserPermissionsADMIN(sesionIdAdmin, user2.getUserId(), "B");
	    Assert.assertEquals("A",userService.getUserPermissionsADMIN(sesionIdAdmin,user2.getUserId()));
	    userService.removeUserPermissionsADMIN(sesionIdAdmin, user2.getUserId(), "A");
	    Assert.assertEquals("",userService.getUserPermissionsADMIN(sesionIdAdmin,user2.getUserId()));  
	}
}
