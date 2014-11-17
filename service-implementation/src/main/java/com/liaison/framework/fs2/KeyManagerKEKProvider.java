/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.framework.fs2;

import static com.liaison.commons.acl.util.ACLUtil.HEADER_KEY_ACL_MANIFEST;
import static com.liaison.commons.acl.util.ACLUtil.HEADER_KEY_ACL_SIGNATURE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.liaison.commons.exception.BootstrapingFailedException;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.fs2.api.encryption.FS2KEKProvider;
import com.liaison.gem.service.client.GEMManifestResponse;
import com.liaison.mailbox.service.util.ACLClientUtil;
import com.liaison.mailbox.service.util.KMSUtil;

public class KeyManagerKEKProvider implements FS2KEKProvider {
    
    private static final DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();

    public static final String PROPERTY_KEK_CACHE_EXPIRATION = "data.encryption.kek.cache.expiration";
    public static final int DEFAULT_KEK_CACHE_EXPIRATION = 10;
    public static final String PATH_FOR_OPERATION_ENCRYPT = "/dek/encrypt";
    public static final String PATH_FOR_OPERATION_DECRYPT = "/dek/%s/decrypt";
    public static final String ENCRYPT_REQUEST_CONTENT_TYPE = "application/octet-stream";
    public static final String DECRYPT_REQUEST_CONTENT_TYPE = "text/plain";

    public static final String HEADER_ENCRYPTOR_ID = "g2-encryptor-id";
    public static final int SUCCESS_CODE_ENCRYPTED = 201;
    public static final int SUCCESS_CODE_DECRYPTED = 200; 
    
    private class KeyManagerKEKInfo {

        public String kekGuid;
        public byte[] encryptedSecretKey;

        public KeyManagerKEKInfo(String kekGuid, byte[] encryptedSecretKey) {
            this.kekGuid = kekGuid;
            this.encryptedSecretKey = encryptedSecretKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((kekGuid == null) ? 0 : kekGuid.hashCode());
            result = prime * result + Arrays.hashCode(encryptedSecretKey);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            KeyManagerKEKInfo other = (KeyManagerKEKInfo) obj;
            if (kekGuid == null) {
                if (other.kekGuid != null) return false;
            }
            else if (!kekGuid.equals(other.kekGuid)) return false;
            if (!Arrays.equals(encryptedSecretKey, other.encryptedSecretKey)) return false;
            return true;
        }
    }

    private LoadingCache<KeyManagerKEKInfo, byte[]> keyCache = getKeyCache();
    
    private LoadingCache<KeyManagerKEKInfo, byte[]> getKeyCache() {    	   
    	   int cacheSize = 100;
    	   CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
    	   cacheBuilder.maximumSize(cacheSize);
    	   cacheBuilder.expireAfterAccess(configuration.getInt(PROPERTY_KEK_CACHE_EXPIRATION, DEFAULT_KEK_CACHE_EXPIRATION), TimeUnit.MINUTES);
    	   
    	   return cacheBuilder.build(new CacheLoader<KeyManagerKEKInfo, byte[]>() {
                public byte[] load(KeyManagerKEKInfo kekInfo) {
                    return decrypt(kekInfo.kekGuid, kekInfo.encryptedSecretKey);
                }
            });
    }

    @Override
    public String encrypt(byte[] clearIn) {
        ByteArrayOutputStream response = new ByteArrayOutputStream(1024);        

        try {
        	
        	GEMManifestResponse manifestResponse = ACLClientUtil.retrieveSignedManifestDTO("");
        	
            HTTPRequest httpRequest = HTTPRequest.post(KMSUtil.getKeyManagementUrl(PATH_FOR_OPERATION_ENCRYPT))
                    .header(HEADER_KEY_ACL_MANIFEST, manifestResponse.getManifest())
                    .header(HEADER_KEY_ACL_SIGNATURE, manifestResponse.getSignature())
                    .header("acl_signer_public_key_guid", manifestResponse.getPublicKeyGuid())
                    .inputData(new ByteArrayInputStream(clearIn), ENCRYPT_REQUEST_CONTENT_TYPE)
                    .outputStream(response);

            synchronized (this) {
                HTTPResponse httpResponse = httpRequest.execute();
                if (httpResponse.getStatusCode() == SUCCESS_CODE_ENCRYPTED) {
                    return new Gson().toJson(new KeyManagerKEKInfo(httpResponse.getHeader(HEADER_ENCRYPTOR_ID), response.toByteArray()));
                }
                else {
                    throw new LiaisonException("Did not recieve a 200 OK from Key Manager service:  " + httpResponse.getReasonPhrease());
                }
            }
        }
        catch (LiaisonException | 
        	   CertificateEncodingException | 
        	   UnrecoverableKeyException | 
        	   OperatorCreationException | 
        	   KeyStoreException | 
        	   NoSuchAlgorithmException | 
        	   IOException | CMSException | 
        	   BootstrapingFailedException |
        	   JAXBException e) {        	
            throw new RuntimeException(e);
        } 
    }

    @Override
    public byte[] decrypt(String kekInfo) {
        try {
            return keyCache.get(new Gson().fromJson(kekInfo, KeyManagerKEKInfo.class));
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] decrypt(String kekGuid, byte[] clearIn) {
        ByteArrayOutputStream response = new ByteArrayOutputStream(1024);

        try {
        	
        	GEMManifestResponse manifestResponse = ACLClientUtil.retrieveSignedManifestDTO("");
        	
            ByteArrayInputStream bais = new ByteArrayInputStream(clearIn);
            HTTPResponse oResponse = HTTPRequest.put(KMSUtil.getKeyManagementUrl(String.format(PATH_FOR_OPERATION_DECRYPT, kekGuid)))
                    .header(HEADER_KEY_ACL_MANIFEST, manifestResponse.getManifest())
                    .header(HEADER_KEY_ACL_SIGNATURE, manifestResponse.getSignature())
                    .header("acl_signer_public_key_guid", manifestResponse.getPublicKeyGuid())
                    .inputData(bais, DECRYPT_REQUEST_CONTENT_TYPE)
                    .outputStream(response)
                    .execute();

            if (oResponse.getStatusCode() != SUCCESS_CODE_DECRYPTED) {
                throw new LiaisonException("Did not recieve a 201 Created from Key Manager service:  " + oResponse.getReasonPhrease());
            }

            return Base64.decodeBase64(response.toByteArray());
        }
        catch (LiaisonException | 
        		CertificateEncodingException | 
        		UnrecoverableKeyException | 
        		OperatorCreationException | 
        		KeyStoreException | 
        		NoSuchAlgorithmException | 
        		IOException | CMSException | 
        		BootstrapingFailedException |
        		JAXBException e) {        	
            throw new RuntimeException(e);
        } 
    }    
}
