/**
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.framework.fs2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.liaison.commons.exception.LiaisonException;
import com.liaison.commons.util.client.http.HTTPRequest;
import com.liaison.commons.util.client.http.HTTPResponse;
import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.fs2.api.encryption.FS2KEKProvider;

public class KeyManagerKEKProvider implements FS2KEKProvider {

    private static final DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();

    public static final String PROPERTY_KEK_CACHE_EXPIRATION = "data.encryption.kek.cache.expiration";
    public static final int DEFAULT_KEK_CACHE_EXPIRATION = 10;
    public static final String PATH_FOR_OPERATION_ENCRYPT = "dek/encrypt";
    public static final String PATH_FOR_OPERATION_DECRYPT = "dek/%s/decrypt";
    public static final String ENCRYPT_REQUEST_CONTENT_TYPE = "application/octet-stream";
    public static final String DECRYPT_REQUEST_CONTENT_TYPE = "text/plain";

    public static final String HEADER_ENCRYPTOR_ID = "g2-encryptor-id";
    public static final int SUCCESS_CODE_ENCRYPTED = 201;
    public static final int SUCCESS_CODE_DECRYPTED = 200;

    public static final String aclManifestValue = "H4sIAAAAAAAAAKWRb2+CMBDGvwrpy00TUKcZ7wBxIzowQLYXxpgOboastKRFMmP87mtr9ydbnC9WEprj7vndc8cBAe2AsAaQe0AFB9xCOZUv5KKB7Yz69qQ/cHJn7Dojdzi+tuVBPbQl7BmTqJRVcRL389SLMy/IoyT2Fv1lmgRhlrnWza0/sv3Z1PZsbzhz5O2PJoMw9INheDsdzySowRxoq0B0R4iMOStAiH+TjxJFcPvCeI3c1eEziHGtRsvC9DEKwo2fJvMwlT44I+BjAaVXqP4Bo638pKUlq3FFzwlP2Xzf6GzibUyFYUoCkuvENW4a4F5ZVxSt1ZhVVxHYgu7AQbAdLxThSulMaKApiDb9qPjKKovw1hqNhNeVEBWjmthhsoNfKQN8uo/ycBFlOTqu1dNDVLaAMgPeVQVMoQFaAi32egtyulYzxSltFnEHFHhVWEZk+Zy9Akd6tBrzvf6DzoWDfrRWotV5P3/YmMPeesAUb7UHU7P8ZmVw4ZhdHN8BuJ+GKxEDAAA=";
    public static final String aclSignatureValue = "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";
    public static final String aclSignerPublicGuidValue = "CD4FFDC50A9270E0142300DCDBDD70BC";

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

    private LoadingCache<KeyManagerKEKInfo, byte[]> keyCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(configuration.getInt(PROPERTY_KEK_CACHE_EXPIRATION, DEFAULT_KEK_CACHE_EXPIRATION), TimeUnit.MINUTES)
            .build(new CacheLoader<KeyManagerKEKInfo, byte[]>() {
                public byte[] load(KeyManagerKEKInfo kekInfo) {
                    return decrypt(kekInfo.kekGuid, kekInfo.encryptedSecretKey);
                }
            });

    @Override
    public String encrypt(byte[] clearIn) {
        ByteArrayOutputStream response = new ByteArrayOutputStream(1024);

        try {
            HTTPRequest httpRequest = HTTPRequest.post(getUrl(PATH_FOR_OPERATION_ENCRYPT))
                    .header(com.liaison.commons.acl.util.ACLUtil.HEADER_KEY_ACL_MANIFEST, aclManifestValue)
                    .header(com.liaison.commons.acl.util.ACLUtil.HEADER_KEY_ACL_SIGNATURE, aclSignatureValue)
                    .header("acl_signer_public_key_guid", aclSignerPublicGuidValue)
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
        catch (LiaisonException e) {
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
            ByteArrayInputStream bais = new ByteArrayInputStream(clearIn);
            HTTPResponse oResponse = HTTPRequest.put(getUrl(String.format(PATH_FOR_OPERATION_DECRYPT, kekGuid)))
                    .header(com.liaison.commons.acl.util.ACLUtil.HEADER_KEY_ACL_MANIFEST, aclManifestValue)
                    .header(com.liaison.commons.acl.util.ACLUtil.HEADER_KEY_ACL_SIGNATURE, aclSignatureValue)
                    .header("acl_signer_public_key_guid", aclSignerPublicGuidValue)
                    .inputData(bais, DECRYPT_REQUEST_CONTENT_TYPE)
                    .outputStream(response)
                    .execute();

            if (oResponse.getStatusCode() != SUCCESS_CODE_DECRYPTED) {
                throw new LiaisonException("Did not recieve a 201 Created from Key Manager service:  " + oResponse.getReasonPhrease());
            }

            return Base64.decodeBase64(response.toByteArray());
        }
        catch (LiaisonException e) {
            throw new RuntimeException(e);
        }
    }

    protected URL getUrl(String path) {
        try {
            return new URL("http://10.146.20.35:8989/key-management/" + path);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
