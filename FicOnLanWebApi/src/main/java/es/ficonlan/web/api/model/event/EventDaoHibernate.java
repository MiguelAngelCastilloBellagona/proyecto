package es.ficonlan.web.api.model.event;

import java.util.List;

import es.ficonlan.web.api.model.util.dao.GenericDaoHibernate;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@Repository("eventDao")
public class EventDaoHibernate extends GenericDaoHibernate<Event, Integer>
		implements EventDao {

	@SuppressWarnings("unchecked")
	public List<Event> getAllEvents() {
		return getSession().createQuery("SELECT e " + "FROM Event e ").list();
	}

	@SuppressWarnings("unchecked")
	public List<Event> searchEventsByName(String name) {
		return getSession()
				.createQuery(
						"SELECT e "
								+ "FROM Event e "
								+ "WHERE LOWER(e.name) LIKE '%'||LOWER(:name)||'%' ")
				.setString("name", name).list();
	}

	public Event findEventByName(String name) {
		return (Event) getSession().createCriteria(Event.class)
				.add(Restrictions.eq("name", name)).uniqueResult();
	}
}
