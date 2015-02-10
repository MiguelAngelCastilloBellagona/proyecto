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

import es.ficonlan.web.api.jersey.util.ApplicationContextProvider;
import es.ficonlan.web.api.jersey.util.RequestControl;
import es.ficonlan.web.api.model.user.User;
import es.ficonlan.web.api.model.userService.UserService;
import es.ficonlan.web.api.model.util.exceptions.ServiceException;
import es.ficonlan.web.api.model.session.Session;
import es.ficonlan.web.api.model.util.session.SessionData;


/**
 * @author Miguel Ángel Castillo Bellagona
 */
@Path("user")
public class UserResource {
	
	private String[] s = {"userId","name","login","dni","email","phoneNumber","shirtSize","dob"};
	private ArrayList<String> l;
	
	static class ChangePasswordData {
		private String oldPassword;
		private String newPassword;

		public ChangePasswordData(){}
		public ChangePasswordData(String oldPassword, String newPassword) {
			super();
			this.oldPassword = oldPassword;
			this.newPassword = newPassword;
		}
		public String getOldPassword() {
			return oldPassword;
		}
		public void setOldPassword(String oldPassword) {
			this.oldPassword = oldPassword;
		}
		public String getNewPassword() {
			return newPassword;
		}
		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}	
	}
	
	static class LoginData {

		private String login;
		private String password;

		public LoginData() {
		}

		public LoginData(String login, String password) {
			this.login = login;
			this.password = password;
		}

		public String getLogin() {
			return login;
		}

		public void setLogin(String login) {
			this.login = login;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}
	
	@Autowired
    private UserService userService;
    
	public UserResource(){
		this.userService  = ApplicationContextProvider.getApplicationContext().getBean(UserService.class);
		l = new ArrayList<String>();
		l.add(s[0]);l.add(s[1]);l.add(s[2]);l.add(s[3]);l.add(s[4]);l.add(s[5]);l.add(s[6]);l.add(s[7]);
	}
    
	//ANONYMOUS
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response adduser(@Context Request request, User user) {
		try {
			RequestControl.showContextData("adduser",request);
			User u = userService.addUser(user);
			if(request!=null) System.out.println("login = " + user.getLogin());
			return Response.status(201).entity(u).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
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
	
	@Path("/login/")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@Context Request request, @HeaderParam("sessionId") String sessionId, LoginData loginData) {
		try {
			RequestControl.showContextData("login",request);
			SessionData u = userService.login(loginData.getLogin(), loginData.getPassword());
			if(request!=null) System.out.println("login {" + loginData.getLogin() + "}");
			return Response.status(200).entity(u).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	//USER
	
	@Path("/session/")
	@DELETE
	public Response closeSessionUSER(@Context Request request, @HeaderParam("sessionId") String sessionId) {
		RequestControl.showContextData("closeSessionUSER",request);
		if(request!=null) System.out.println("sessionId {" + sessionId + "}");
		try {
			userService.closeUserSession(sessionId);
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
		return Response.status(204).build();
	}
	
	@DELETE
	@Consumes({MediaType.APPLICATION_JSON})
	public Response removeUserUSER(@Context Request request, @HeaderParam("sessionId") String sessionId)  {
		try {
			RequestControl.showContextData("removeUserUSER",request);
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			userService.removeUserUSER(sessionId);
			if(request!=null) System.out.println("login {" + login + "}");
			return Response.status(204).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	@Path("/{userId}")
	@PUT
	@Consumes({MediaType.APPLICATION_JSON})
	public Response changeDataUSER(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId, User user) {
		try {
			RequestControl.showContextData("changeDataUSER",request);
			userService.changeUserDataUSER(sessionId, user);
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			if(request!=null) System.out.println("login {" + login + "}");
			return Response.status(204).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	@Path("/changePassword/{userId}")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	public Response changePasswordUSER(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId, ChangePasswordData data) {
		try {
			RequestControl.showContextData("changePasswordUSER",request);
			userService.changeUserPasswordUSER(sessionId, data.getOldPassword(), data.getNewPassword());
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			if(request!=null) System.out.println("login {" + login + "}");
			return Response.status(204).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

	@Path("/currentUser/")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response currentUserUSER(@Context Request request, @HeaderParam("sessionId") String sessionId) {
		try {
			RequestControl.showContextData("currentUserUSER",request);
			User u = userService.getCurrenUserUSER(sessionId);
			if(request!=null) System.out.println("login {" + u.getLogin() + "}");
			return Response.status(200).entity(u).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	@Path("/getPermissions/")
	@GET
	@Produces("text/plain")
	public Response getUserPermissionsUSER(@Context Request request, @HeaderParam("sessionId") String sessionId) {
		try {
			RequestControl.showContextData("getUserPermissionsUSER",request);
			String p = userService.getUserPermissionsUSER(sessionId);
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			if(request!=null) System.out.println("login {" + login + "}");
			return Response.status(200).entity(p).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
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
	
	@Path("/admin/getAllUser/query")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response getAllUsersADMIN(@Context Request request, 
			@HeaderParam("sessionId") String sessionId,
			@PathParam("eventId")                          int eventId,
			@DefaultValue("1") @QueryParam("page")         int page, 
			@DefaultValue("0") @QueryParam("pageTam")      int pageTam,
			@DefaultValue("login") @QueryParam("orderBy")  String orderBy,
			@DefaultValue("1") @QueryParam("desc")         int desc) {
		RequestControl.showContextData("closeAllUserSessionsADMIN",request);
		int startIndex = page*pageTam - pageTam;
		int cont = pageTam;
		boolean b = true;
		if(desc==0) b = false;
		try {
			if(l.indexOf(orderBy)<0) throw new ServiceException(ServiceException.INCORRECT_FIELD,"orderBy");
			List<User> l = userService.getAllUsersADMIN(sessionId,startIndex,cont,orderBy,b);
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			if(request!=null) System.out.println("login {" + login + "}");
			return Response.status(200).entity(l).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	@Path("/admin/getAllUserTAM/")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response getAllUsersTAMADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN",request);
			long l = userService.getAllUsersTAMADMIN(sessionId);
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			if(request!=null) System.out.println("login {" + login + "}");
			return Response.status(200).entity(l).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	@Path("/admin/delete/{userId}")
	@DELETE
	@Consumes({MediaType.APPLICATION_JSON})
	public Response removeUserADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN",request);
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			String target = userService.getUserADMIN(sessionId, userId).getLogin();
			userService.removeUserADMIN(sessionId, userId);
			if(request!=null) System.out.println("login {" + login + "}\t" + "target {" + target + "}");
			return Response.status(203).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	@Path("/admin/changeData/{userId}")
	@PUT
	@Consumes({MediaType.APPLICATION_JSON})
	public Response changeDataADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId, User user) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN",request);
			userService.changeUserDataADMIN(sessionId, userId, user);
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			String target = userService.getUserADMIN(sessionId, userId).getLogin();
			if(request!=null) System.out.println("login {" + login + "}\t" + "target {" + target + "}");
			return Response.status(203).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	@Path("/admin/changePassword/{userId}")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	public Response changePasswordADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId, ChangePasswordData data) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN",request);
			userService.changeUserPasswordADMIN(sessionId, userId, data.getOldPassword(), data.getNewPassword());
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			String target = userService.getUserADMIN(sessionId, userId).getLogin();
			if(request!=null) System.out.println("login {" + login + "}\t" + "target {" + target + "}");
			return Response.status(203).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	@Path("/admin/permissions/{userId}")
	@GET
	@Produces("text/plain")
	public Response getUserPermissionsADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN",request);
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			String target = userService.getUserADMIN(sessionId, userId).getLogin();
			if(request!=null) System.out.println("login {" + login + "}\t" + "target {" + target + "}");
			String s = userService.getUserPermissionsADMIN(sessionId, userId);
			return Response.status(200).entity(s).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	@Path("/admin/permissions/{userId}/{permission}")
	@POST
	@Produces("text/plain")
	public Response addUserPermissionsADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId, @PathParam("permission") String permission) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN",request);
			String s = userService.addUserPermissionsADMIN(sessionId, userId, permission);
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			String target = userService.getUserADMIN(sessionId, userId).getLogin();
			if(request!=null) System.out.println("login {" + login + "}\t" + "target {" + target + "}\t" + "add {" + permission + "}");
			return Response.status(200).entity(s).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}
	
	@Path("/admin/permissions/{userId}/{permission}")
	@DELETE
	@Produces("text/plain")
	public Response removeUserPermissionsADMIN(@Context Request request, @HeaderParam("sessionId") String sessionId, @PathParam("userId") int userId, @PathParam("permission") String permission) {
		try {
			RequestControl.showContextData("closeAllUserSessionsADMIN",request);
			String s = userService.removeUserPermissionsADMIN(sessionId, userId, permission);
			String login = userService.getCurrenUserUSER(sessionId).getLogin();
			String target = userService.getUserADMIN(sessionId, userId).getLogin();
			if(request!=null) System.out.println("login {" + login + "}\t" + "target {" + target + "}\t" + "delete {" + permission + "}");
			return Response.status(200).entity(s).build();
		} catch (ServiceException e) {
			System.out.println(e.toString());
			return Response.status(e.getHttpErrorCode()).entity(e.toString()).build();
		}
	}

}
