package es.ficonlan.web.api.jersey.resources;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.Request;
import org.springframework.beans.factory.annotation.Autowired;

import es.ficonlan.web.api.jersey.util.ApplicationContextProvider;
import es.ficonlan.web.api.jersey.util.RequestControl;
import es.ficonlan.web.api.model.event.Event;
import es.ficonlan.web.api.model.eventService.EventService;
import es.ficonlan.web.api.model.sessionService.SessionService;
import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@Path("event")
public class EventResource {

	private String[] s = { "eventId", "name", "description", "numParticipants", "minimunAge", "price", "registrationOpenDate", "registrationCloseDate" };
	private ArrayList<String> l;

	@Autowired
	private EventService eventService;

	@Autowired
	private SessionService sessionService;

	private static final String ADMINPERMISSIONKEY = "U";

	public EventResource() {
		this.eventService = ApplicationContextProvider.getApplicationContext().getBean(EventService.class);
		this.sessionService = ApplicationContextProvider.getApplicationContext().getBean(SessionService.class);
		l = new ArrayList<String>();
		l.add(s[0]);
		l.add(s[1]);
		l.add(s[2]);
		l.add(s[3]);
		l.add(s[4]);
		l.add(s[5]);
		l.add(s[6]);
		l.add(s[7]);
	}
	
	// ANONYMOUS
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEvent(@Context Request request, @PathParam("userId") int eventId) {
		try {
			RequestControl.showContextData("getEvent", request);
			Event e = eventService.getEvent(eventId);
			if (request != null)
				System.out.println("event-name {" + e.getName() + "}");
			return Response.status(200).entity(e).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	
	// ADMIN

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response addEvent(@Context Request request, @HeaderParam("sessionId") String sessionId, Event event) {
		try {
			RequestControl.showContextData("addEvent", request);
			User commander = sessionService.getUserUSER(sessionId);
			if (commander.getPremissions().contains(ADMINPERMISSIONKEY)) {
				Event e = eventService.createEvent(event);
				if (request != null)
					System.out.println("event-name = " + event.getName());
				return Response.status(201).entity(e).build();
			} else {
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
			}
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	@DELETE
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response removeEvent(@Context Request request, @HeaderParam("sessionId") String sessionId,  @PathParam("userId") int eventId) {
		try {
			RequestControl.showContextData("removeEvent", request);

			User commander = sessionService.getUserUSER(sessionId);
			if (commander.getPremissions().contains(ADMINPERMISSIONKEY)) {
				String eventName = eventService.getEvent(eventId).getName();
				eventService.removeEvent(eventId);
				if (request != null)
					System.out.println("event-name = " + eventName);
				return Response.status(204).build();
			} else {
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
			}
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	//TODO //FIXME
}
