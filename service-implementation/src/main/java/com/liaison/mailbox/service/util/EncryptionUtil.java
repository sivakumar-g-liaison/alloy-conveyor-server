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

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author kirithigad
 * 
 * The sole purpose of this class is to make the token non-readable/non-changeable by any process other than UMGT.
 * It is not intended to 'encrypt' sensitive data
 * 
 */
public class EncryptionUtil {

	/**
	 *  Method to encrypt/decrypt the token.
	 * 
	 * @param string
	 * @param isDefaultKey
	 * @return byte
	 * @throws Exception
	 */
	public static byte[] encrypt(final byte[] string, final byte[] key, final byte[] ivBytes, final int mode) {

		try {

			final IvParameterSpec iv = new IvParameterSpec(ivBytes);
			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(mode, new SecretKeySpec(key, "AES"), iv);
			return cipher.doFinal(string);
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}

}