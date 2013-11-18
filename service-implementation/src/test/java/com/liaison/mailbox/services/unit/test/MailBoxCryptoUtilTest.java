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

import junit.framework.Assert;

import org.junit.Test;

import com.liaison.commons.security.pkcs7.SymmetricAlgorithmException;
import com.liaison.mailbox.service.util.MailBoxCryptoUtil;

/**
 * @author praveenu
 * 
 */
public class MailBoxCryptoUtilTest {

	private String text = "14Demo!@";

	@Test
	public void testDoPasswordEncryption() throws SymmetricAlgorithmException {

		String encryptedString = MailBoxCryptoUtil.doPasswordEncryption(text, 1);
		String decryptedString = MailBoxCryptoUtil.doPasswordEncryption(encryptedString, 2);
		Assert.assertEquals(text, decryptedString);
	}

	@Test
	public void testDoPasswordEncryption_WithWrongtext() throws SymmetricAlgorithmException {

		String encryptedString = MailBoxCryptoUtil.doPasswordEncryption("1253", 1);
		String decryptedString = MailBoxCryptoUtil.doPasswordEncryption(encryptedString, 2);
		Assert.assertNotSame(text, decryptedString);
	}

	@Test
	public void testDoPasswordEncryption_WithEmpty() throws SymmetricAlgorithmException {

		String encryptedString = MailBoxCryptoUtil.doPasswordEncryption("", 1);
		String decryptedString = MailBoxCryptoUtil.doPasswordEncryption(encryptedString, 2);
		Assert.assertEquals("", decryptedString);
	}

	@Test(expected = NullPointerException.class)
	public void testDoPasswordEncryption_WithNull() throws SymmetricAlgorithmException {

		String encryptedString = MailBoxCryptoUtil.doPasswordEncryption(null, 1);
		MailBoxCryptoUtil.doPasswordEncryption(encryptedString, 2);
	}

}
