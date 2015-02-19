package es.ficonlan.web.api.test.userservice;

import static es.ficonlan.web.api.model.util.GlobalNames.SPRING_CONFIG_FILE;
import static es.ficonlan.web.api.test.util.GlobalNames.SPRING_CONFIG_TEST_FILE;
import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import es.ficonlan.web.api.model.emailtemplate.EmailTemplate;
import es.ficonlan.web.api.model.emailtemplate.EmailTemplateDao;
import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.user.UserDao;
import es.ficonlan.web.api.model.userService.UserService;
import es.ficonlan.web.api.model.util.exceptions.InstanceException;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {SPRING_CONFIG_FILE, SPRING_CONFIG_TEST_FILE})
@Transactional
public class UserServiceTest {

	@Autowired
    private UserService userService;
    
	@Autowired
	private UserDao userDao;
	
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
	public void passwordRecoverTest() throws ServiceException {
		assertFalse(userService.passwordRecover("email@yopmail.com"));
		User user = new User("", "User", "login", "password", "00000000A", "email@yopmail.com", "666666666", "XL", Calendar.getInstance(), "EN");
	    userService.addUser(user);
	    assertFalse(userService.passwordRecover("email@yopmail.com"));
	    EmailTemplate e = new EmailTemplate();
	    e.setName("passwordrecover");
	    e.setAsunto("Asunto");
	    e.setContenido(" ");
	    emailTemplateDao.save(e);
	    
		assertTrue(userService.passwordRecover("email@yopmail.com"));
		
		assertFalse(userService.passwordRecover("Noemail@yopmails.com"));
	}
	
  
}
