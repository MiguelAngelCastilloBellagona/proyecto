package es.ficonlan.web.api.model.eventService;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import es.ficonlan.web.api.model.event.Event;
import es.ficonlan.web.api.model.event.EventDao;
import es.ficonlan.web.api.model.util.exceptions.InstanceException;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;

/**
 * @author Miguel Ángel Castillo Bellagona
 */

@Service("EventService")
@Transactional
public class EventServiceImpl implements EventService {

	@Autowired
	private EventDao eventDao;

	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public Event createEvent(Event event) throws ServiceException {
		if (event.getName() == null)
			throw new ServiceException(ServiceException.MISSING_FIELD, "name");
		if (event.getRegistrationOpenDate() == null)
			throw new ServiceException(ServiceException.MISSING_FIELD, "registrationOpenDate");
		if (event.getRegistrationCloseDate() == null)
			throw new ServiceException(ServiceException.MISSING_FIELD, "registrationCloseDate");
		if (event.getNormas() == null)
			throw new ServiceException(ServiceException.MISSING_FIELD, "rules");
		if (event.getDescription() == null)
			throw new ServiceException(ServiceException.MISSING_FIELD, "description");
		if (eventDao.findEventByName(event.getName()) != null)
			throw new ServiceException(ServiceException.DUPLICATED_FIELD, "name");
		eventDao.save(event);

		return event;
	}

	@Override
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void removeEvent(int eventId) throws ServiceException {
		try {
			eventDao.remove(eventId);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND, "Event");
		}
	}

	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public Event getEvent(int eventId) throws ServiceException {
		try {
			return eventDao.find(eventId);
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND, "Event");
		}
	}

	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public Event changeEventData(int eventId, Event eventData) throws ServiceException {
		try {
			Event event = eventDao.find(eventId);
			event.setEventId(eventId);
			if (eventData.getName() != null)
				event.setName(eventData.getName());
			if (eventData.getDescription() != null)
				event.setDescription(eventData.getDescription());
			if (eventData.getNumParticipants() > 0)
				event.setNumParticipants(eventData.getNumParticipants());
			if (eventData.getMinimunAge() >= 0)
				event.setMinimunAge(eventData.getMinimunAge());
			if (eventData.getPrice() >= 0)
				event.setPrice(eventData.getPrice());
			if (eventData.getRegistrationOpenDate() != null)
				event.setRegistrationOpenDate(eventData.getRegistrationOpenDate());
			if (eventData.getRegistrationCloseDate() != null)
				event.setRegistrationCloseDate(eventData.getRegistrationCloseDate());
			eventDao.save(event);
			if (eventData.getNormas() != null)
				event.setNormas(eventData.getNormas());
			return event;
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND, "Event");
		}
	}

	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<Event> getAllEvents() throws ServiceException {
		return eventDao.getAllEvents();
	}

	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<Event> findEventByName(String name) throws ServiceException {
		return eventDao.searchEventsByName(name);
	}

	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public String getEventData(int eventId, String data) throws ServiceException {
		try {
			Event event = eventDao.find(eventId);
			switch (data) {
			case "rules":
				return event.getNormas();
			case "description":
				return event.getDescription();
			case "name":
				return event.getName();
			case "numparticipants":
				return Integer.toString(event.getNumParticipants());
			case "minimunage":
				return Integer.toString(event.getMinimunAge());
			case "price":
				return Integer.toString(event.getPrice());
			case "isopen":
				Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				if (now.after(event.getRegistrationOpenDate()) && now.before(event.getRegistrationCloseDate()))
					return "true";
				else
					return "false";
			default:
				throw new ServiceException(ServiceException.INCORRECT_FIELD, "Data");
			}
		} catch (InstanceException e) {
			throw new ServiceException(ServiceException.INSTANCE_NOT_FOUND, "Event");
		}
	}
}
