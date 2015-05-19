package es.ficonlan.web.api.model.eventService;

import java.util.List;

import es.ficonlan.web.api.model.event.Event;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;


/**
 * @author Miguel Ángel Castillo Bellagona
 */
public interface EventService {
	
	public Event createEvent(Event event) throws ServiceException;

	public void removeEvent(int eventId) throws ServiceException;

	public Event getEvent(int eventId) throws ServiceException;

	public Event changeEventData(int eventId, Event eventData) throws ServiceException;

	public List<Event> getAllEvents() throws ServiceException;

	public List<Event> findEventByName(String name) throws ServiceException;

	public String getEventData(int eventId, String data) throws ServiceException;

}
