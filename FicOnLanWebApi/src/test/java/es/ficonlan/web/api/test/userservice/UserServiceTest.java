package es.ficonlan.web.api.test.userservice;

import static es.ficonlan.web.api.model.util.GlobalNames.SPRING_CONFIG_FILE;
import static es.ficonlan.web.api.test.util.GlobalNames.SPRING_CONFIG_TEST_FILE;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.user.UserDao;
import es.ficonlan.web.api.model.userService.UserService;
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
	
	@Before 
	public void initialize() {
	     userService.initialize();
	}
	
	@Test
	public void correctTest() {
		
	}
	
	@Test
	public void loginTest() {
		try {
			String sessionId = userService.login("Admin", "initialAdminPass").getSessionId();
			System.out.println(sessionId);
			User u = userService.getCurrenUserUSER(sessionId);
			System.out.println(u.getLogin());
			
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
  
}
