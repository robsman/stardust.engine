/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.extensions.jaxws.app;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;

import org.eclipse.stardust.engine.core.runtime.beans.IUser;


/**
 * @author fherinean
 * @version $Revision$
 */
public class AuthenticationParameters implements IWSSAuthenticationParameters
{
   private String mechanism;
   private String variant;

   private String username;
   private String password;

   private IUser user;
   
   private String requestConfigurationLocation;
   private String responseConfigurationLocation;
   
   private KeystoreParameters keyStoreParameters = new KeystoreParameters();
   
   private Key requestKey;
   private Certificate requestCertificate;
   private KeyStore requestKeyStore;

   private Key responseKey;
   private Certificate responseCertificate;
   private KeyStore responseKeyStore;

   public AuthenticationParameters(String mechanism)
   {
      this.mechanism = mechanism;
   }

   public String getMechanism()
   {
      return mechanism;
   }

   public void setMechanism(String mechanism)
   {
      this.mechanism = mechanism;
   }

   public String getVariant()
   {
      return variant;
   }

   public void setVariant(String variant)
   {
      this.variant = variant;
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public IUser getUser()
   {
      return user;
   }

   public void setUser(IUser user)
   {
      this.user = user;
   }
   
   public String getRequestConfigurationLocation()
   {
      return requestConfigurationLocation;
   }

   public void setRequestConfigurationLocation(String requestConfigurationLocation)
   {
      this.requestConfigurationLocation = requestConfigurationLocation;
   }

   public String getResponseConfigurationLocation()
   {
      return responseConfigurationLocation;
   }

   public void setResponseConfigurationLocation(String responseConfigurationLocation)
   {
      this.responseConfigurationLocation = responseConfigurationLocation;
   }

   public Key getRequestKey() throws Exception
   {
      if (requestKey == null)
      {
         requestKey = getRequestKey(keyStoreParameters.getKeyId());
      }
      return requestKey;
   }

   public Key getRequestKey(String alias) throws Exception
   {
      KeyStore ks = getRequestKeyStore();
      return ks == null ? null : ks.getKey(alias, keyStoreParameters.getKeyPassword().toCharArray());
   }

   public void setRequestKey(Key key)
   {
      this.requestKey = key;
   }

   public Certificate getRequestCertificate() throws Exception
   {
      if (requestCertificate == null)
      {
         requestCertificate = getRequestCertificate(keyStoreParameters.getCertificateId());
      }
      return requestCertificate;
   }

   public Certificate getRequestCertificate(String alias) throws Exception
   {
      KeyStore ks = getRequestKeyStore();
      return ks == null ? null : ks.getCertificate(alias);
   }

   public void setRequestCertificate(Certificate certificate)
   {
      this.requestCertificate = certificate;
   }
   
   public IKeystoreParameters getKeystoreParameters()
   {
      return keyStoreParameters;
   }

   private KeyStore getRequestKeyStore() throws Exception
   {
      if (requestKeyStore == null && keyStoreParameters.getLocation() != null)
      {
         requestKeyStore = KeyStore.getInstance(keyStoreParameters.getType());
         InputStream res = AuthenticationParameters.class.getResourceAsStream(keyStoreParameters.getLocation());
         requestKeyStore.load(res, keyStoreParameters.getPassword().toCharArray());
      }
      return requestKeyStore;
   }

   public Key getResponseKey() throws Exception
   {
      if (responseKey == null)
      {
         KeyStore ks = getResponseKeyStore();
         if (ks != null)
         {
            responseKey = ks.getKey(keyStoreParameters.getResponseKeyId(), keyStoreParameters.getResponseKeyPassword().toCharArray());
         }
      }
      return responseKey;
   }

   public void setResponseKey(Key key)
   {
      this.responseKey = key;
   }

   public Certificate getResponseCertificate() throws Exception
   {
      if (responseCertificate == null)
      {
         KeyStore ks = getResponseKeyStore();
         if (ks != null)
         {
            responseCertificate = ks.getCertificate(keyStoreParameters.getResponseCertificateId());
         }
      }
      return responseCertificate;
   }

   public void setResponseCertificate(Certificate certificate)
   {
      this.responseCertificate = certificate;
   }

   private KeyStore getResponseKeyStore() throws Exception
   {
      if (responseKeyStore == null && keyStoreParameters.getResponseLocation() != null)
      {
         if (keyStoreParameters.getResponseLocation().equals(keyStoreParameters.getLocation()))
         {
            responseKeyStore = getRequestKeyStore();
         }
         else
         {
            requestKeyStore = KeyStore.getInstance(keyStoreParameters.getResponseType());
            InputStream res = AuthenticationParameters.class.getResourceAsStream(keyStoreParameters.getResponseLocation());
            requestKeyStore.load(res, keyStoreParameters.getResponsePassword().toCharArray());
         }
      }
      return requestKeyStore;
   }
   
   @Override
   public boolean equals(final Object obj)
   {
      // TODO override (see CRNT-21251)
      return super.equals(obj);
   }
   
   @Override
   public int hashCode()
   {
      // TODO override (see CRNT-21251)
      return super.hashCode();
   }
}
