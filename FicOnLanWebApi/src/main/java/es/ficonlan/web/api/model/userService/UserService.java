package es.ficonlan.web.api.model.userService;

import java.util.List;

import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;


/**
 * @author Miguel Ángel Castillo Bellagona
 */
public interface UserService {
	
	public void initialize();

	//ANONYMOUS
	
	public boolean passwordRecover(String email) throws ServiceException;
    
	public User addUser(User user) throws ServiceException;
	
	
	//USER

	
	public void removeUser(int userId) throws ServiceException;
	
    public void changeUserData(int userId, User userData)  throws ServiceException;
	
	public void changeUserPasswordUSER(int userId, String oldPassword, String newPassword)  throws ServiceException;
	
	public String getUserPermissions(int userId) throws ServiceException;
	
	
	//ADMIN
	
	public User getUser(int userId) throws ServiceException;
	
	public List<User> getAllUsers(int startIndex, int maxResults, String orderBy, boolean desc) throws ServiceException;
	
	public long getAllUsersTAM() throws ServiceException;
	
	public List<User> findUsersByName(String name, int startindex, int maxResults) throws ServiceException;
	
	public void changeUserPasswordADMIN(int userId, String newPassword) throws ServiceException;
	
	public String addUserPermissions(int userId, String permission) throws ServiceException;
	
	public String removeUserPermissions(int userId, String permission) throws ServiceException;
	
}
