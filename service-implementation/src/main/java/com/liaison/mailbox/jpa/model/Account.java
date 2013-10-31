package com.liaison.mailbox.jpa.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.liaison.commons.jpa.Identifiable;


/**
 * The persistent class for the ACCOUNT database table.
 * 
 */
@Entity
@Table(name = "ACCOUNT")
@SecondaryTable(name = "LOCALE", pkJoinColumns = {@PrimaryKeyJoinColumn(name = "pguid", referencedColumnName = "pguid")})
@NamedQuery(name="Account.findAll", query="SELECT a FROM Account a")
public class Account implements Identifiable {
	
	private static final long serialVersionUID = 1L;

	private String pguid;
	private String activeState;
	private String crmUri;
	private String description;
	private String loginId;
	private String smsNumber;
	private String tmpPswdExp;
	private String tmpPswdHash;
	private AccountType accountType;
	private Language language;
	private List<IdpProfile> idpProfiles;
	private String currencyFormat;
	private String dateFormat;
	private String numberFormat;
	private String timeZone;

	public Account() {
	}

	@Id
	public String getPguid() {
		return this.pguid;
	}

	public void setPguid(String pguid) {
		this.pguid = pguid;
	}

	@Column(name="ACTIVE_STATE")
	public String getActiveState() {
		return this.activeState;
	}

	public void setActiveState(String activeState) {
		this.activeState = activeState;
	}

	@Column(name="CRM_URI")
	public String getCrmUri() {
		return this.crmUri;
	}

	public void setCrmUri(String crmUri) {
		this.crmUri = crmUri;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="LOGIN_ID")
	public String getLoginId() {
		return this.loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	@Column(name="SMS_NUMBER")
	public String getSmsNumber() {
		return this.smsNumber;
	}

	public void setSmsNumber(String smsNumber) {
		this.smsNumber = smsNumber;
	}

	@Column(name="TMP_PSWD_EXP")
	public String getTmpPswdExp() {
		return this.tmpPswdExp;
	}

	public void setTmpPswdExp(String tmpPswdExp) {
		this.tmpPswdExp = tmpPswdExp;
	}

	@Column(name="TMP_PSWD_HASH")
	public String getTmpPswdHash() {
		return this.tmpPswdHash;
	}

	public void setTmpPswdHash(String tmpPswdHash) {
		this.tmpPswdHash = tmpPswdHash;
	}

	@Column(name="CURRENCY_FORMAT", table = "LOCALE")
	public String getCurrencyFormat() {
		return this.currencyFormat;
	}

	public void setCurrencyFormat(String currencyFormat) {
		this.currencyFormat = currencyFormat;
	}


	@Column(name="DATE_FORMAT", table = "LOCALE")
	public String getDateFormat() {
		return this.dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}


	@Column(name="NUMBER_FORMAT", table = "LOCALE")
	public String getNumberFormat() {
		return this.numberFormat;
	}

	public void setNumberFormat(String numberFormat) {
		this.numberFormat = numberFormat;
	}


	@Column(name="TIME_ZONE", table = "LOCALE")
	public String getTimeZone() {
		return this.timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
	//bi-directional one-to-one association to AccountType
	@Column(name="USER_TYPE_GUID")
	public AccountType getAccountType() {
		return this.accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	@Column(name="LANGUAGE_GUID")
	public Language getLanguage() {
		return this.language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}


	@OneToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST,
			CascadeType.MERGE,CascadeType.DETACH, CascadeType.REMOVE, CascadeType.REFRESH })
    @JoinColumn(name="ACCOUNT_GUID")
	public List<IdpProfile> getIdpProfiles() {
		return this.idpProfiles;
	}

	public void setIdpProfiles(List<IdpProfile> idpProfiles) {
		this.idpProfiles = idpProfiles;
	}


	@Override
	@Transient
	public Object getPrimaryKey() {
		return getPguid();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transient
	public Class getEntityClass() {
		return this.getClass();
	}

}