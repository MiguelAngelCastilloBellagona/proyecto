package es.ficonlan.web.api.model.emailtemplate;

import java.util.List;

import es.ficonlan.web.api.model.util.dao.GenericDao;

/**
 * @author Miguel Ángel Castillo Bellagona

 */
public interface EmailTemplateDao extends GenericDao<EmailTemplate, Integer> {

	public List<EmailTemplate> getAllEmailTemplate();
    
    public EmailTemplate findByName(String name);
}
