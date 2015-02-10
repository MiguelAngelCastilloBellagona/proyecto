package es.ficonlan.web.api.model.util.dao;

import java.io.Serializable;

import es.ficonlan.web.api.model.util.exceptions.InstanceException;

/**
 * @author Miguel Ángel Castillo Bellagona
 */
public interface GenericDao<E, PK extends Serializable> {

	void save(E entity);

	E find(PK id) throws InstanceException;

	boolean exists(PK id);

	void remove(PK id) throws InstanceException;

}
