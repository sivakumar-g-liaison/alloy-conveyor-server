package com.liaison.mailbox.service.dto.configuration;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.jpa.model.Account;
import com.liaison.mailbox.service.exception.MailBoxConfigurationServicesException;
import com.liaison.mailbox.service.util.MailBoxUtility;

public class AccountDTO {

	private String guid;
	private String description;
	private String activeState;
	private String loginId;
	private String smsNumber;
	private String tmpPswdHash;
	private String tmpPswdExp;
	private String crmURI;
	private AccountTypeDTO accountType;
	private LanguageDTO language;
	private LocaleDTO locale;
	private String currencyFormat;
	private String numberFormat;
	private String dateFormat;
	private String timeZone;
	
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getActiveState() {
		return activeState;
	}
	public void setActiveState(String activeState) {
		this.activeState = activeState;
	}
	public String getLoginId() {
		return loginId;
	}
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}
	public String getSmsNumber() {
		return smsNumber;
	}
	public void setSmsNumber(String smsNumber) {
		this.smsNumber = smsNumber;
	}
	public String getTmpPswdHash() {
		return tmpPswdHash;
	}
	public void setTmpPswdHash(String tmpPswdHash) {
		this.tmpPswdHash = tmpPswdHash;
	}
	public String getTmpPswdExp() {
		return tmpPswdExp;
	}
	public void setTmpPswdExp(String tmpPswdExp) {
		this.tmpPswdExp = tmpPswdExp;
	}
	public String getCrmURI() {
		return crmURI;
	}
	public void setCrmURI(String crmURI) {
		this.crmURI = crmURI;
	}
	public LocaleDTO getLocale() {
		return locale;
	}
	public void setLocale(LocaleDTO locale) {
		this.locale = locale;
	}
	public AccountTypeDTO getAccountType() {
		return accountType;
	}
	public void setAccountType(AccountTypeDTO accountType) {
		this.accountType = accountType;
	}
	public LanguageDTO getLanguage() {
		return language;
	}
	public void setLanguage(LanguageDTO language) {
		this.language = language;
	}
	public String getCurrencyFormat() {
		return currencyFormat;
	}
	public void setCurrencyFormat(String currencyFormat) {
		this.currencyFormat = currencyFormat;
	}
	public String getNumberFormat() {
		return numberFormat;
	}
	public void setNumberFormat(String numberFormat) {
		this.numberFormat = numberFormat;
	}
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	public String getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
	public void copyToEntity(Account account, boolean isCreate) throws MailBoxConfigurationServicesException,
	JsonGenerationException, JsonMappingException, JAXBException, IOException, SymmetricAlgorithmException {

		if (isCreate) {
			account.setPguid(MailBoxUtility.getGUID());
		}
		
		account.setDescription(this.getDescription());
		account.setActiveState(this.getActiveState());
		account.setLoginId(this.getLoginId());
		account.setCrmUri(this.getCrmURI());
		account.setSmsNumber(this.getSmsNumber());
		account.setTmpPswdExp(this.getTmpPswdExp());
		account.setTmpPswdHash(this.getTmpPswdHash());
		
		account.setCurrencyFormat(this.getCurrencyFormat());
		account.setDateFormat(this.getDateFormat());
		account.setNumberFormat(this.getNumberFormat());
		account.setTimeZone(this.getTimeZone());
		
		this.getLanguage().copyToEntity(account.getLanguage());
		this.getAccountType().copyToEntity(account.getAccountType());

	}
	
	public void copyFromEntity(Account account) throws MailBoxConfigurationServicesException{
		
		this.setGuid(account.getPguid());
		this.setDescription(account.getDescription());
		this.setCrmURI(account.getCrmUri());
		this.setLoginId(account.getLoginId());
		this.setSmsNumber(account.getSmsNumber());
		this.setTmpPswdExp(account.getTmpPswdExp());
		this.setTmpPswdHash(account.getTmpPswdHash());
		this.setActiveState(account.getActiveState());
		
		if (null != account.getAccountType()) {
			AccountTypeDTO accountTypeDTO = new AccountTypeDTO();
			accountTypeDTO.copyFromEntity(account.getAccountType());
		}
		if (null != account.getLanguage()) {
			LanguageDTO languageDTO = new LanguageDTO();
			languageDTO.copyFromEntity(account.getLanguage());
		}
		
		this.setCurrencyFormat(account.getCurrencyFormat());
		this.setDateFormat(account.getDateFormat());
		this.setNumberFormat(account.getNumberFormat());
		this.setTimeZone(account.getTimeZone());
	}
}
