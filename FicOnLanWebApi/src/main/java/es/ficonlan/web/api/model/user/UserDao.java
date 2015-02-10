package es.ficonlan.web.api.model.user;

import java.util.List;

import es.ficonlan.web.api.model.util.dao.GenericDao;


/**
 * @author Miguel Ángel Castillo Bellagona
 */
public interface UserDao extends GenericDao<User,Integer> {
	
	public List<User> getAllUsers(int startindex, int maxResults, String orderBy, boolean desc);
	
	public long getAllUsersTAM();
	
	public User findUserBylogin(String login);
	
	public User findUserByDni(String dni);
	
	public User findUserByEmail(String email);

	public List<User> findUsersByName(String name, int startindex, int maxResults);


}
