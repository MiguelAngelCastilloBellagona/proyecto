package es.ficonlan.web.api.jersey.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.Request;
import org.springframework.beans.factory.annotation.Autowired;

import es.ficonlan.web.api.jersey.resources.util.ChangePasswordData;
import es.ficonlan.web.api.jersey.util.ApplicationContextProvider;
import es.ficonlan.web.api.jersey.util.RequestControl;
import es.ficonlan.web.api.model.sessionService.SessionService;
import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.userService.UserService;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@Path("user")
public class UserResource {

	private static final String ADMINPERMISSIONKEY = "U";

	private String[] s = { "userId", "name", "login", "dni", "email", "phoneNumber", "shirtSize", "dob" };
	private ArrayList<String> l;

	@Autowired
	private UserService userService;

	@Autowired
	private SessionService sessionService;

	public UserResource() {
		this.userService = ApplicationContextProvider.getApplicationContext().getBean(UserService.class);
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

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response addUser(@Context Request request, User user) {
		try {
			RequestControl.showContextData("adduser", request);
			User u = userService.addUser(user);
			if (request != null)
				System.out.println("login = " + user.getLogin());
			return Response.status(201).entity(u).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	// USER

	@DELETE
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response removeUserUSER(@Context Request request, @HeaderParam("sessionId") String sessionId) {
		try {
			RequestControl.showContextData("removeUserUSER", request);
			User target = sessionService.getUserUSER(sessionId);
			userService.removeUser(target.getUserId());
			if (request != null)
				System.out.println("login {" + target.getLogin() + "}");
			return Response.status(204).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response changeDataUSER(@Context Request request, @HeaderParam("sessionId") String sessionId, User user) {
		try {
			RequestControl.showContextData("changeDataUSER", request);
			User target = sessionService.getUserUSER(sessionId);
			userService.changeUserData(target.getUserId(), user);
			if (request != null)
				System.out.println("login {" + target.getLogin() + "}");
			return Response.status(204).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	@Path("/changePassword")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response changePasswordUSER(@Context Request request, @HeaderParam("sessionId") String sessionId, ChangePasswordData data) {
		try {
			RequestControl.showContextData("changePasswordUSER", request);
			User target = sessionService.getUserUSER(sessionId);
			userService.changeUserPasswordUSER(target.getUserId(), data.getOldPassword(), data.getNewPassword());
			if (request != null)
				System.out.println("login {" + target.getLogin() + "}");
			sessionService.closeAllUserOtherSessions(sessionId);
			return Response.status(204).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response currentUserUSER(@Context Request request, @HeaderParam("sessionId") String sessionId) {
		try {
			RequestControl.showContextData("currentUserUSER", request);
			User u = sessionService.getUserUSER(sessionId);
			if (request != null)
				System.out.println("login {" + u.getLogin() + "}");
			return Response.status(200).entity(u).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	// ADMIN

	@Path("/admin/allUser/query")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllUsersADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("eventId") int eventId,
			@DefaultValue("1") @QueryParam("page") int page, @DefaultValue("0") @QueryParam("pageTam") int pageTam, @DefaultValue("login") @QueryParam("orderBy") String orderBy,
			@DefaultValue("1") @QueryParam("desc") int desc) {
		RequestControl.showContextData("closeAllUserSessionsADMIN", request);
		int startIndex = page * pageTam - pageTam;
		int cont = pageTam;
		boolean b = true;
		if (desc == 0)
			b = false;
		try {
			if (l.indexOf(orderBy) < 0)
				throw new ServiceException(ServiceException.INCORRECT_FIELD, "orderBy");
			User commander = sessionService.getUserUSER(sessionId);
			if (commander.getPremissions().contains(ADMINPERMISSIONKEY)) {
				List<User> l = userService.getAllUsers(startIndex, cont, orderBy, b);
				if (request != null)
					System.out.println("login {" + commander.getLogin() + "}");
				return Response.status(200).entity(l).build();
			} else {
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
			}
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	@Path("/admin/allUserTAM/")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAllUsersTAMADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN", request);
			User commander = sessionService.getUserUSER(sessionId);
			if (commander.getPremissions().contains(ADMINPERMISSIONKEY)) {
				long l = userService.getAllUsersTAM();
				if (request != null)
					System.out.println("login {" + commander.getLogin() + "}");
				return Response.status(200).entity(l).build();
			} else {
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
			}
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	@Path("/admin/{userId}")
	@DELETE
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response removeUserADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN", request);
			User commander = sessionService.getUserUSER(sessionId);
			User target = userService.getUser(userId);
			if (commander.getPremissions().contains(ADMINPERMISSIONKEY)) {
				userService.removeUser(userId);
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

	@Path("/admin/{userId}")
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response changeDataADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId, User user) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN", request);
			User commander = sessionService.getUserUSER(sessionId);
			User target = userService.getUser(userId);
			if (commander.getPremissions().contains(ADMINPERMISSIONKEY)) {
				userService.changeUserData(userId, user);
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

	@Path("/admin/changePassword/{userId}")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response changePasswordADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId, ChangePasswordData data) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN", request);
			User commander = sessionService.getUserUSER(sessionId);
			User target = userService.getUser(userId);
			if (commander.getPremissions().contains(ADMINPERMISSIONKEY)) {
				userService.changeUserPasswordADMIN(userId, data.getNewPassword());
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

	@Path("/admin/{userId}/permissions")
	@GET
	@Produces("text/plain")
	public Response getUserPermissionsADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN", request);
			User commander = sessionService.getUserUSER(sessionId);
			User target = userService.getUser(userId);
			if (request != null)
				System.out.println("login {" + commander.getLogin() + "}\t" + "target {" + target.getLogin() + "}");
			if (commander.getPremissions().contains(ADMINPERMISSIONKEY)) {
				String s = userService.getUserPermissions(userId);
				return Response.status(200).entity(s).build();
			} else {
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
			}
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	@Path("/admin/{userId}/permissions/{permission}")
	@POST
	@Produces("text/plain")
	public Response addUserPermissionsADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId,
			@PathParam("permission") String permission) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN", request);
			User commander = sessionService.getUserUSER(sessionId);
			User target = userService.getUser(userId);
			if (commander.getPremissions().contains(ADMINPERMISSIONKEY)) {
				String s = userService.addUserPermissions(userId, permission);
				if (request != null)
					System.out.println("login {" + commander.getLogin() + "}\t" + "target {" + target.getLogin() + "}\t" + "add {" + permission + "}");
				return Response.status(200).entity(s).build();
			} else {
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
			}
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	@Path("/admin/{userId}/permissions/{permission}")
	@DELETE
	@Produces("text/plain")
	public Response removeUserPermissionsADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId,
			@PathParam("permission") String permission) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN", request);
			User commander = sessionService.getUserUSER(sessionId);
			User target = userService.getUser(userId);
			if (commander.getPremissions().contains(ADMINPERMISSIONKEY)) {
				String s = userService.removeUserPermissions(userId, permission);
				if (request != null)
					System.out.println("login {" + commander.getLogin()  + "}\t" + "target {" + target.getLogin() + "}\t" + "delete {" + permission + "}");
				return Response.status(200).entity(s).build();
			} else {
				throw new ServiceException(ServiceException.PERMISSION_DENIED);
			}
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

}
