/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall 
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.service.util;

import org.apache.commons.codec.binary.Base64;

import com.liaison.commons.security.pkcs12.CryptoUtil;
import com.liaison.commons.security.pkcs12.SymmetricAlgorithmException;

/**
 * @author OFS
 * 
 */
public class MailBoxCryptoUtil {

	/** Key to encrypt and decrypt the password in credentials table */
	public final static String PASSWORD_ENCRYPT_KEY = ":[8pGN$";

	/**
	 * Applies Symmetrical Algorithm using PBEWithSHAAndTwofish-CBC.
	 * 
	 * @param data
	 *            The text to be encrypted or decrypted
	 * @param mode
	 *            Enc/dec direction (encrypt=1, decrypt=2)
	 * @return The encrypted or decrypted data as String
	 * @throws SymmetricAlgorithmException
	 */
	public static String doPasswordEncryption(String data, int mode) throws SymmetricAlgorithmException {

		if (1 == mode) {
			byte[] bytes = CryptoUtil.doPasswordBasedEncryption(data.getBytes(), PASSWORD_ENCRYPT_KEY, 1);
			return Base64.encodeBase64String(bytes);
		} else {
			byte[] bytes = CryptoUtil.doPasswordBasedEncryption(Base64.decodeBase64(data), PASSWORD_ENCRYPT_KEY, 2);
			return new String(bytes);
		}

	}

}
