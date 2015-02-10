package es.ficonlan.web.api.jersey.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
@Provider
public class CustomExceptionMapper implements ExceptionMapper<Exception> {
	
	@Override
	public Response toResponse(Exception error) {
	    Response response;
	    if (error instanceof WebApplicationException) {
	        WebApplicationException webEx = (WebApplicationException)error;
	        System.out.println(webEx.toString());
	        
	        response = webEx.getResponse();
	    } else {
	        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	                .entity("Internal error").type("text/plain").build();
	    }
	    return response;
	}
    
}