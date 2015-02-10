package es.ficonlan.web.api.model.user;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import es.ficonlan.web.api.model.util.dao.GenericDaoHibernate;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@Repository("userDao")
public class UserDaoHibernate extends GenericDaoHibernate<User,Integer> implements UserDao {
	
	@SuppressWarnings("unchecked")
	public List<User> getAllUsers(int startindex, int cont, String orderBy, boolean desc){
		String aux = " ";
		if(desc) aux=" DESC";
		Query query =  getSession().createQuery(
	        	"SELECT u " +
		        "FROM User u " +
	        	"ORDER BY u." + orderBy +  aux 
		        );
		if(cont<1) return query.list();
		else return query.setFirstResult(startindex).setMaxResults(cont).list();
	}
	
	public long getAllUsersTAM() {
		return (long) getSession().createQuery(
	        	"SELECT count(u) " +
		        "FROM User u " +
	        	"ORDER BY u.login").uniqueResult();
	}
	
	public User findUserBylogin(String login) {
		return (User) getSession()
				.createQuery("SELECT u FROM User u WHERE User_login = :login ")
				.setParameter("login", login).uniqueResult();
	}
	
	public User findUserByEmail(String email) {
		return (User) getSession()
				.createQuery("SELECT u FROM User u Where User_email = :email ")
				.setParameter("email", email).uniqueResult();
	}

	public User findUserByDni(String dni) {
		return (User) getSession()
				.createQuery("SELECT u FROM User u Where User_dni = :dni ")
				.setParameter("dni", dni).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findUsersByName(String name, int startindex, int maxResults) {
		return getSession().createQuery(
	        	"SELECT u " +
		        "FROM User u " +
		        "WHERE ( LOWER(u.name) LIKE '%'||LOWER(:name)||'%' " +
		           "OR  LOWER(u.login) LIKE '%'||LOWER(:name)||'%' ) " +
	        	"ORDER BY u.login").setParameter("name",name).setFirstResult(startindex).setMaxResults(maxResults).list();
	}

}
