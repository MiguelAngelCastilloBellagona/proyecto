package es.ficonlan.web.api.model.session;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import es.ficonlan.web.api.model.util.dao.GenericDaoHibernate;

@Repository("SessionDao")
public class SessionDaoHibernate extends GenericDaoHibernate<Session,String> implements SessionDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Session> getAllSessions() {
		Query query =  getSession().createQuery(
	        	"SELECT s " +
		        "FROM Session s "
		        );
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Session> findSessionByUserId(int userId) {
		Query query =  getSession().createQuery(
	        	"SELECT s " +
		        "FROM Session s " +
	        	"WHERE s.user.userId = :userId "
		        ).setParameter("userId", userId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Session> findOlderThanDate(Calendar date) {
		Query query =  getSession().createQuery(
	        	"SELECT s " +
		        "FROM Session s " +
	        	"WHERE s.lastAccess < :date "
		        ).setParameter("date", date);
		return query.list();
	}

}
