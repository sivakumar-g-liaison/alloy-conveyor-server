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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.liaison.mailbox.MailBoxConstants;

/**
 * @author kirithigad
 *
 */
public class EncryptionUtil {

    static byte[] STATIC_KEY = "A3$1E*8^%ER256%$".getBytes(Charset.forName(MailBoxConstants.CHARSETNAME));
	static byte[] DYNAMIC_KEY;
    
	/**
	 *  Method to retrieve encoded decrypted Token.
	 * 
	 * @param encryptString
	 * @param isDefaultKey
	 * @return byte
	 * @throws Exception
	 */
	public static byte[] encrypt(String encryptString, boolean isDefaultKey) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {

		if (!isDefaultKey && (DYNAMIC_KEY == null)) {

			byte[] buf = new byte[16];
			Random random = new Random();
			random.nextBytes(buf);
			DYNAMIC_KEY = buf;
		}

		byte[] key = isDefaultKey ? STATIC_KEY : DYNAMIC_KEY;
		byte[] ivBytes = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		SecretKeySpec skey = new SecretKeySpec(key, "AES");
		final IvParameterSpec iv = new IvParameterSpec(ivBytes);
		final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skey, iv);
		byte[] cipherbyte = cipher.doFinal(encryptString.getBytes(Charset.forName(MailBoxConstants.CHARSETNAME)));
		return cipherbyte;
	}
    
	/**
	 * Method to retrieve decoded decrypted Token.
	 * 
	 * @param encryptedBytes
	 * @param isDefaultKey
	 * @return String
	 * @throws Exception
	 */
	public static String decrypt(byte[] encryptedBytes, boolean isDefaultKey) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException
			 {

		byte[] key = isDefaultKey ? STATIC_KEY: DYNAMIC_KEY;
		byte[] ivBytes = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		SecretKeySpec skey = new SecretKeySpec(key, "AES");
		final IvParameterSpec iv = new IvParameterSpec(ivBytes);
		final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skey, iv);
		byte[] cipherbyte = cipher.doFinal(encryptedBytes);
		return new String(cipherbyte, MailBoxConstants.CHARSETNAME);
	}
	
}


