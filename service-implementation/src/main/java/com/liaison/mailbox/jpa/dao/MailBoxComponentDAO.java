package com.liaison.mailbox.jpa.dao;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.MailBoxComponent;


@NamedQueries({
    @NamedQuery(name=MailBoxComponentDAO.FIND_MAILBOX_COMP_BY_PROFILE,
            query="SELECT mbc FROM MailBoxComponent mbc WHERE mbc.profile = :" + MailBoxComponentDAO.PROFILE)
})

public interface MailBoxComponentDAO extends GenericDAO <MailBoxComponent>{
	
	public static final String FIND_MAILBOX_COMP_BY_PROFILE = "findMailBoxCompByProfile";
	public static final String PROFILE = "profile";
	public MailBoxComponent find(String profile);

}
