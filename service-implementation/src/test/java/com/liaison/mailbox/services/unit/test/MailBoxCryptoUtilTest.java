/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.services.unit.test;

import org.testng.annotations.Test;
import org.testng.Assert;
import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.service.util.MailBoxCryptoUtil;

/**
 * @author OFS
 * 
 */
public class MailBoxCryptoUtilTest {

	private String text = "14Demo!@";
    
	/**
	 * Method to test password Encryption.
	 * 
	 * @throws SymmetricAlgorithmException
	 */
	@Test
	public void testDoPasswordEncryption() throws SymmetricAlgorithmException {

		String encryptedString = MailBoxCryptoUtil.doPasswordEncryption(text, 1);
		String decryptedString = MailBoxCryptoUtil.doPasswordEncryption(encryptedString, 2);
		Assert.assertEquals(text, decryptedString);
	}
    
	/**
	 * Method to test password Encryption with wrong text.
	 * 
	 * @throws SymmetricAlgorithmException
	 */
	@Test
	public void testDoPasswordEncryption_WithWrongtext() throws SymmetricAlgorithmException {

		String encryptedString = MailBoxCryptoUtil.doPasswordEncryption("1253", 1);
		String decryptedString = MailBoxCryptoUtil.doPasswordEncryption(encryptedString, 2);
		Assert.assertNotSame(text, decryptedString);
	}
    
	/**
	 * Method to test password Encryption with empty.
	 * 
	 * @throws SymmetricAlgorithmException
	 */
	@Test
	public void testDoPasswordEncryption_WithEmpty() throws SymmetricAlgorithmException {

		String encryptedString = MailBoxCryptoUtil.doPasswordEncryption("", 1);
		String decryptedString = MailBoxCryptoUtil.doPasswordEncryption(encryptedString, 2);
		Assert.assertEquals("", decryptedString);
	}
    
	/**
	 * Method to test password Encryption with null.
	 * 
	 * @throws SymmetricAlgorithmException
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testDoPasswordEncryption_WithNull() throws SymmetricAlgorithmException {

		String encryptedString = MailBoxCryptoUtil.doPasswordEncryption(null, 1);
		MailBoxCryptoUtil.doPasswordEncryption(encryptedString, 2);
	}

}
