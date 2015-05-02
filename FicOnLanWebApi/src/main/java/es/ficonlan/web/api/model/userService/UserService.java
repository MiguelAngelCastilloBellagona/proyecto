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

	
	public void removeUserUSER(String sessionId) throws ServiceException;
	
    public void changeUserDataUSER(String sessionId, User userData)  throws ServiceException;
	
	public void changeUserPasswordUSER(String sessionId, String oldPassword, String newPassword)  throws ServiceException;
	
	public String getUserPermissionsUSER(String sessionId) throws ServiceException;
	
	
	//ADMIN
	
	
	public User getUserADMIN(String sessionId, int userId) throws ServiceException;
	
	public List<User> getAllUsersADMIN(String sessionId, int startIndex, int maxResults, String orderBy, boolean desc) throws ServiceException;
	
	public long getAllUsersTAMADMIN(String sessionId) throws ServiceException;
	
	public List<User> findUsersByNameADMIN(String sessionId, String name, int startindex, int maxResults) throws ServiceException;
	
	public void removeUserADMIN(String sessionId, int userId) throws ServiceException;
	
    public void changeUserDataADMIN(String sessionId, int userId, User userData)  throws ServiceException;
	
	public void changeUserPasswordADMIN(String sessionId, int userId, String newPassword)  throws ServiceException;
	
	public String getUserPermissionsADMIN(String sessionId, int userId) throws ServiceException;
	
	public String addUserPermissionsADMIN(String sessionId, int userId, String permission) throws ServiceException;
	
	public String removeUserPermissionsADMIN(String sessionId, int userId, String permission) throws ServiceException;
	
}
