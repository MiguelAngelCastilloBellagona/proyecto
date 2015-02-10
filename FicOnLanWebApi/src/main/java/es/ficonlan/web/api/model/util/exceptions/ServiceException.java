package es.ficonlan.web.api.model.util.exceptions;

/**
 * Exception Codes:<br>
 * 01 - Invalid session<br>
 * 02 - Permission denied<br>
 * 03 - Duplicated unique field<br>
 * 04 - Incorrect field<br>
 * 05 - Missing required field<br>
 * 06 - Instance not found<br>
 * 07 - There is already a session<br>
 * 08 - Event maximum number of participants reached<br>
 * 09 - Registration out of time<br>
 * 10 - User isn't registered in the event<br>
 * 11 - Activity maximum number of participants reached<br>
 * 99 - System unexpected error (RuntimeException)
 * 
 * @author Miguel Ángel Castillo Bellagona
 */
public class ServiceException extends Exception {

	public static final int INVALID_SESSION = 1;
	public static final int PERMISSION_DENIED=2;
	public static final int DUPLICATED_FIELD=3;
	public static final int INCORRECT_FIELD=4;
	public static final int MISSING_FIELD=5;
	public static final int INSTANCE_NOT_FOUND=6;
	public static final int OTHER=99;
	
	private static final long serialVersionUID = 1L;
	private int errorCode;
	private String useCase;
	private String field;

	public ServiceException(int errorCode) {
		this.errorCode = errorCode;
        this.useCase = Thread.currentThread().getStackTrace()[2].getMethodName();
	}

	public ServiceException(int errorCode, String field) {
		this.errorCode = errorCode;
        this.useCase = Thread.currentThread().getStackTrace()[2].getMethodName();
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public String getMessage() {
		switch (errorCode) {
		case 1:
			return "Invalid session";
		case 2:
			return "Permission denied";
		case 3:
			return "Duplicated unique field";
		case 4:
			return "Incorrect field";
		case 5:
			return "Missing required field";
		case 6:
			return "Instance not found";
		case 99:
			return "System unexpected error";
		default:
			return null;
		}
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getUseCase() {
		return useCase;
	}
	
	@Override
	public String toString() {
		return "ServiceException {errorCode=" + errorCode + ", useCase=" + useCase + ", field=" + field + ", message=" + this.getMessage() + "}";
	}
	
	public int getHttpErrorCode() {
		
		switch (this.getErrorCode()) {
		case 1:
			return 403;
		case 2:
			return 401;
		default:
			return 400;
		}	
	}

}
