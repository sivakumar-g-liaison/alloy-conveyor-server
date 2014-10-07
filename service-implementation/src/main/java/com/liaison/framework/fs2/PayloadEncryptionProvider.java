/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.framework.fs2;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.fs2.api.encryption.FS2EncryptionInfo;
import com.liaison.fs2.api.encryption.FS2EncryptionProvider;
import com.liaison.fs2.api.encryption.FS2KEKProvider;

public class PayloadEncryptionProvider implements FS2EncryptionProvider {

	static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final DecryptableConfiguration config = LiaisonConfigurationFactory.getConfiguration();
	private static Logger logger = LogManager.getLogger(PayloadEncryptionProvider.class);

    public static final String CONFIG_CIPHER_ALGORITHM  = "data.encryption.cipher.algorithm";
    public static final String CONFIG_KEY_ALGORITHM     = "data.encryption.key.algorithm";
    public static final String CONFIG_KEY_SIZE          = "data.encryption.key.size";
    public static final String CONFIG_KEY_MAX_USES      = "data.encryption.key.maxuses";

	public static final String DEFAULT_KEY_ALGORITHM = "AES";
	public static final String DEFAULT_CIPHER_ALGORITHM = "AES";
	public static final String PROVIDER = "BC";

	private String keyAlg;
	private String cipherAlg;
	private int keySize;
	private int maxUses;
	private SecureRandom secureRandom;
	private Cipher cipher;
	private KeyGenerator kgen;

	private FS2EncryptionInfo cachedEncryptionInfo;
	private AtomicInteger uses = new AtomicInteger(0);

	public PayloadEncryptionProvider() {
        keyAlg = config.getString(CONFIG_KEY_ALGORITHM, DEFAULT_KEY_ALGORITHM);
        keySize = config.getInt(CONFIG_KEY_SIZE, 128);
        maxUses = config.getInt(CONFIG_KEY_MAX_USES, 10000);
        cipherAlg = config.getString(CONFIG_CIPHER_ALGORITHM, DEFAULT_KEY_ALGORITHM);
		secureRandom = new SecureRandom();

		try {
			kgen = KeyGenerator.getInstance(keyAlg);
			kgen.init(keySize, secureRandom);
		}
		catch (NoSuchAlgorithmException e) {
			try {
				kgen = KeyGenerator.getInstance(DEFAULT_KEY_ALGORITHM);
				kgen.init(keySize, new SecureRandom());
			}
			catch (NoSuchAlgorithmException e1) {
				throw new RuntimeException(e);
			}
		}

		try {
			cipher = Cipher.getInstance(cipherAlg, PROVIDER);
		}
		catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
			try {
				cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM, PROVIDER);
			}
			catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	@Override
	public FS2EncryptionInfo generateEncryptionInfo(FS2KEKProvider kekProvider) {
		if (cachedEncryptionInfo == null || uses.get() > maxUses) {
			synchronized (this) {
				if (cachedEncryptionInfo == null || uses.get() > maxUses) {
					logger.debug("Initializing a new symmetric data encryption key.");
					cachedEncryptionInfo = generateNewEncryptionInfo(kekProvider);
					uses.set(0);
				}
			}
		}
		uses.addAndGet(1);

		return cachedEncryptionInfo;
	}

	private FS2EncryptionInfo generateNewEncryptionInfo(FS2KEKProvider kekProvider) {
        SecretKey secretKey = kgen.generateKey();

        // is KEKProvider is provided, use it to encrypt the secret key
        String kekInfo = null;
        if(kekProvider != null) {
            kekInfo = kekProvider.encrypt(secretKey.getEncoded());
        }

		/** TODO (RKOH): zip option does not work currently */
		return new FS2EncryptionInfo(cipherAlg, keyAlg, secretKey, keySize, generateIVBytes(), false, kekInfo);
	}

	private byte[] generateIVBytes() {
		byte[] ivBytes = new byte[cipher.getBlockSize()];
		secureRandom.nextBytes(ivBytes);

        return ivBytes;
    }
}
