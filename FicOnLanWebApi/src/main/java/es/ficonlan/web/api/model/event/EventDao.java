package es.ficonlan.web.api.model.event;

import java.util.List;

import es.ficonlan.web.api.model.util.dao.GenericDao;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
public interface EventDao extends GenericDao<Event, Integer> {

	public List<Event> getAllEvents();

	public List<Event> searchEventsByName(String name);

	public Event findEventByName(String name);

}
