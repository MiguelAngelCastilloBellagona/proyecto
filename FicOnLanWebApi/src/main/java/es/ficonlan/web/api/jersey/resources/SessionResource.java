package es.ficonlan.web.api.jersey.resources;

import java.util.List;

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

import es.ficonlan.web.api.jersey.resources.util.LoginData;
import es.ficonlan.web.api.jersey.resources.util.SessionData;
import es.ficonlan.web.api.jersey.util.ApplicationContextProvider;
import es.ficonlan.web.api.jersey.util.RequestControl;
import es.ficonlan.web.api.model.session.Session;
import es.ficonlan.web.api.model.sessionService.SessionService;
import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.userService.UserService;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@Path("session")
public class SessionResource {

	private static final String ADMINPERMISSIONKEY = "U";

	@Autowired
	private UserService userService;

	@Autowired
	private SessionService sessionService;

	public SessionResource() {
		this.userService = ApplicationContextProvider.getApplicationContext().getBean(UserService.class);
		this.sessionService = ApplicationContextProvider.getApplicationContext().getBean(SessionService.class);
	}

	// ANONYMOUS

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@Context Request request, LoginData loginData) {
		try {
			RequestControl.showContextData("login", request);
			SessionData u = sessionService.login(loginData.getLogin(), loginData.getPassword());
			if (request != null)
				System.out.println("login {" + loginData.getLogin() + "}\t");
			return Response.status(200).entity(u).build();
		} catch (ServiceException e) {
			System.out.println("login {" + loginData.getLogin() + "}\t" + e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	@Path("/passwordrecover/")
	@POST
	@Consumes("text/plain")
	public Response passwordRecover(@Context Request request, String email) {
		try {
			RequestControl.showContextData("passwordRecover", request);
			boolean b = userService.passwordRecover(email);
			if (request != null)
				System.out.println("email {" + b + "} = " + email);
			return Response.status(200).entity(b).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	// USER

	@DELETE
	public Response closeSessionUSER(@Context Request request, @HeaderParam("sessionId") String sessionId) {
		RequestControl.showContextData("closeSessionUSER", request);
		if (request != null)
			try {
				String login = sessionService.getUserUSER(sessionId).getLogin();
				sessionService.closeUserSession(sessionId);
				System.out.println("login {" + login + "}");
			} catch (ServiceException e) {
				System.out.println(e.toString());
				return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
			}
		return Response.status(204).build();
	}

	// ADMIN

	@Path("/admin/allUserSession/{userId}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllUserSessionsADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId) {
		try {
			RequestControl.showContextData("getSllUserSessionsADMIN", request);
			User commander = sessionService.getUserUSER(sessionId);
			User target = userService.getUser(userId);
			if (commander.getPremissions().contains(ADMINPERMISSIONKEY)) {
				List<Session> l = sessionService.getAllUserSessionsADMIN(userId);
				if (request != null)
					System.out.println("login {" + commander.getLogin() + "}\t" + "target {" + target.getLogin() + "}");
				return Response.status(200).entity(l).build();
			} else {
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
			}
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	@Path("/admin/allUserSession/{userId}")
	@DELETE
	public Response closeAllUserSessionsADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN", request);
			User commander = sessionService.getUserUSER(sessionId);
			User target = userService.getUser(userId);
			if (commander.getPremissions().contains(ADMINPERMISSIONKEY)) {
				sessionService.closeAllUserSessionsADMIN(userId);
				if (request != null)
					System.out.println("login {" + commander.getLogin() + "}\t" + "target {" + target.getLogin() + "}");
				return Response.status(203).build();
			} else {
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
			}
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
}
