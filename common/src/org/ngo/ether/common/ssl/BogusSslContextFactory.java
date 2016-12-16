/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.ngo.ether.common.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * Factory to create a bogus SSLContext.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class BogusSslContextFactory {

    /**
     * Protocol to use.
     */
    private static final String PROTOCOL = "TLS";

    private static final String KEY_MANAGER_FACTORY_ALGORITHM;

    static {
        String algorithm = Security
                .getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = KeyManagerFactory.getDefaultAlgorithm();
        }

        KEY_MANAGER_FACTORY_ALGORITHM = algorithm;
    }

    /**
     * Bougus Server certificate keystore file name.
     */
    private static final String BOGUS_KEYSTORE = "bogus.cert";

    // NOTE: The keystore was generated using keytool:
    //   keytool -genkey -alias bogus -keysize 512 -validity 3650
    //           -keyalg RSA -dname "CN=bogus.com, OU=XXX CA,
    //               O=Bogus Inc, L=Stockholm, S=Stockholm, C=SE"
    //           -keypass boguspw -storepass boguspw -keystore bogus.cert

    //XHO: for bridge server version
    private static final String BRIDGE_KEYSTORE = "bridge.cert";

    //keytool -genkey -alias bridge -keysize 1024 -validity 3650 -keyalg RSA -dname "CN=bridge, OU=edu.ngo.cn,O=ngo.cn, L=TianJin, S=TianJin, C=CN" -keypass kpbeaman -storepass spbeaman -keystore bridge.cert
    
    //XHO: keysize should not less than 1024, otherwise leads to "Certificates does not conform to algorithm constraints" error
    
    // this is bridge keystore password.
     private static final char[] BRIDGE_PW = { 's', 'p', 'b', 'e', 'a', 'm', 'a', 'n' };
    
    //this is bridge keypass password
    private static final char[] BRIDGE_PW2 = { 'k', 'p', 'b', 'e', 'a', 'm', 'a', 'n' };
    
    private static SSLContext serverInstance = null;
    
    private static SSLContext clientInstance = null;

    /**
     * Get SSLContext singleton.
     *
     * @param server A flag to tell if this is a Client or Server instance we want to create
     * @return SSLContext The created SSLContext 
     * @throws GeneralSecurityException If we had an issue creating the SSLContext
     */
    public static SSLContext getInstance(boolean server)
            throws GeneralSecurityException {
        SSLContext retInstance = null;
        if (server) {
            synchronized(BogusSslContextFactory.class) {
                if (serverInstance == null) {
                    try {
                        serverInstance = createBougusServerSslContext();
                    } catch (Exception ioe) {
                        throw new GeneralSecurityException(
                                "Can't create Server SSLContext:" + ioe);
                    }
                }
            }
            retInstance = serverInstance;
        } else {
            synchronized (BogusSslContextFactory.class) {
                if (clientInstance == null) {
                    clientInstance = createBougusClientSslContext();
                }
            }
            retInstance = clientInstance;
        }
        return retInstance;
    }

    private static SSLContext createBougusServerSslContext()
            throws GeneralSecurityException, IOException {
        // Create keystore
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream in = null;
        try {
            in = BogusSslContextFactory.class.getResourceAsStream(BRIDGE_KEYSTORE);
            ks.load(in, BRIDGE_PW);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

        // Set up key manager factory to use our key store
        KeyManagerFactory kmf = KeyManagerFactory
                .getInstance(KEY_MANAGER_FACTORY_ALGORITHM);
        kmf.init(ks, BRIDGE_PW2);

        // Initialize the SSLContext to work with our key managers.
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(kmf.getKeyManagers(),
                BogusTrustManagerFactory.X509_MANAGERS, null);

        return sslContext;
    }

    private static SSLContext createBougusClientSslContext()
            throws GeneralSecurityException {
        SSLContext context = SSLContext.getInstance(PROTOCOL);
        context.init(null, BogusTrustManagerFactory.X509_MANAGERS, null);
        return context;
    }

}
