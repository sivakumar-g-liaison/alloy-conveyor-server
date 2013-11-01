/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.jpa.dao;

import java.util.Set;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.liaison.commons.jpa.GenericDAO;
import com.liaison.mailbox.jpa.model.Account;

@NamedQueries({
		@NamedQuery(name = UserAccountConfigurationDAO.GET_TYPE_ACCOUNT,
			query = "SELECT acc FROM Account acc "
					+ " inner join acc.idpProfiles idpprofile "
					+ " inner join idpprofile.idpProvider provider "
					+ " where acc.loginId like :" + UserAccountConfigurationDAO.ACC_NAME
					+ " and acc.accountType.name like :" + UserAccountConfigurationDAO.ACC_TYPE_NAME
					+ " and provider.name like :" + UserAccountConfigurationDAO.IDP_PRO_NAME
					+ " order by acc.accountType.name, provider.name, acc.loginId"),
		@NamedQuery(name = UserAccountConfigurationDAO.GET_ALL_ACC, query = "SELECT acc FROM Account acc WHERE acc.activeState = 'ACTIVE'")
})
/**
 * 
 * 
 * @author praveenu
 */
public interface UserAccountConfigurationDAO extends GenericDAO <Account>{
	
	public static final String GET_TYPE_ACCOUNT = "findUserAccount";
	public static final String ACC_NAME = "acc_name";
	public static final String ACC_TYPE_NAME = "acc_type_name";
	public static final String IDP_PRO_NAME = "idp_pro_name";
	public static final String GET_ALL_ACC = "get_all_acc";
	
	public Set<Account> find(String accType, String providerName, String loginId);
	public Set<Account> findAllAcc();

}
