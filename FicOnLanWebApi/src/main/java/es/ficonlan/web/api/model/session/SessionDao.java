package es.ficonlan.web.api.model.session;


import java.util.Calendar;
import java.util.List;

import es.ficonlan.web.api.model.util.dao.GenericDao;

public interface SessionDao extends GenericDao<Session,String>{

	public List<Session> getAllSessions();
	
	public List<Session> findSessionByUserId(int userId);
	
	public List<Session> findOlderThanDate(Calendar date);
	

}
