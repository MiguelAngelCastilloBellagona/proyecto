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
import es.ficonlan.web.api.jersey.util.ApplicationContextProvider;
import es.ficonlan.web.api.jersey.util.RequestControl;
import es.ficonlan.web.api.model.session.Session;
import es.ficonlan.web.api.model.userService.UserService;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;
import es.ficonlan.web.api.model.util.session.SessionData;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@Path("session")
public class SessionResource {

	@Autowired
    private UserService userService;
    
	public SessionResource(){
		this.userService  = ApplicationContextProvider.getApplicationContext().getBean(UserService.class);
	}
	
	//ANONYMOUS
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@Context Request request, @HeaderParam("sessionId") String sessionId, LoginData loginData) {
		try {
			RequestControl.showContextData("login",request);
			SessionData u = userService.login(loginData.getLogin(), loginData.getPassword());
			if(request!=null) System.out.println("login {" + loginData.getLogin() + "}\t" + "sessionId {" + u.getSessionId() + "}");
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
			RequestControl.showContextData("passwordRecover",request);
			boolean b =  userService.passwordRecover(email);
			if(request!=null) System.out.println("email {" + b + "} = " + email);
			return Response.status(200).entity(b).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}	
	
	//USER
	
	@DELETE
	public Response closeSessionUSER(@Context Request request, @HeaderParam("sessionId") String sessionId) {
		RequestControl.showContextData("closeSessionUSER",request);
		if(request!=null) 
		try {
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			userService.closeUserSession(sessionId);
			System.out.println("login {" + login + "}\t" + "sessionId {" + sessionId + "}");
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
		return Response.status(204).build();
	}
	
	//ADMIN
	
	@Path("/admin/allUserSession/{userId}")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response getSllUserSessionsADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId) {
		try {
			RequestControl.showContextData("getSllUserSessionsADMIN",request);
			List<Session> l = userService.getAllUserSessionsADMIN(sessionId,userId);
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			String target = userService.getUserADMIN(sessionId, userId).getLogin();
			if(request!=null) System.out.println("login {" + login + "}\t" + "target {" + target + "}");
			return Response.status(200).entity(l).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	@Path("/admin/allUserSession/{userId}")
	@DELETE
	public Response closeAllUserSessionsADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN",request);
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			String target = userService.getUserADMIN(sessionId, userId).getLogin();
			userService.closeAllUserSessionsADMIN(sessionId,userId);
			if(request!=null) System.out.println("login {" + login + "}\t" + "target {" + target + "}");
			return Response.status(203).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}	
	}
	
	
	
	
}
