package es.ficonlan.web.api.model.util.exceptions;

@SuppressWarnings("serial")
public class DuplicatedInstanceException extends InstanceException {

    public DuplicatedInstanceException(Object key, String className) {
        super("Duplicate instance", key, className);
    }
    
}
