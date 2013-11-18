package com.liaison.mailbox.service.dto.configuration;


public class LocaleDTO {

	private String guid;
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
	
	/*public void copyToEntity(Locale locale) throws MailBoxConfigurationServicesException {
		
		//locale.setPguid(MailBoxUtility.getGUID());
		locale.setCurrencyFormat(this.getCurrencyFormat());
		locale.setDateFormat(this.getDateFormat());
		locale.setNumberFormat(this.getNumberFormat());
		locale.setTimeZone(this.getTimeZone());
		
		
	}
	public void copyFromEntity(Locale locale) throws MailBoxConfigurationServicesException {
		
		//this.setGuid(locale.getPguid());
		this.setCurrencyFormat(locale.getCurrencyFormat());
		this.setDateFormat(locale.getDateFormat());
		this.setNumberFormat(locale.getNumberFormat());
		this.setTimeZone(locale.getTimeZone());
	}*/
}
