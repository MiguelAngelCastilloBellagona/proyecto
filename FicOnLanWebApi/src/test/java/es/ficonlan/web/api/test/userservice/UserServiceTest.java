package es.ficonlan.web.api.test.userservice;

import static es.ficonlan.web.api.model.util.GlobalNames.SPRING_CONFIG_FILE;
import static es.ficonlan.web.api.test.util.GlobalNames.SPRING_CONFIG_TEST_FILE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {SPRING_CONFIG_FILE, SPRING_CONFIG_TEST_FILE})
@Transactional
public class UserServiceTest {

	@Test
	public void correctTest() {
		
	}
  
}
