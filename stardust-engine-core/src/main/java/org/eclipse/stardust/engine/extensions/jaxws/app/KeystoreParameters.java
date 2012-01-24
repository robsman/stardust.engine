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

import java.security.KeyStore;

public class KeystoreParameters implements IKeystoreParameters
{
   private String type;
   private String location;
   private String password;
   private String keyId;
   private String keyPassword;
   private String certificateId;

   private String responseType;
   private String responseLocation;
   private String responsePassword;
   private String responseKeyId;
   private String responseKeyPassword;
   private String responseCertificateId;

   public String getType()
   {
      return type == null ? KeyStore.getDefaultType() : type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getLocation()
   {
      return location;
   }

   public void setLocation(String location)
   {
      this.location = location;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public String getKeyId()
   {
      return keyId;
   }

   public void setKeyId(String keyId)
   {
      this.keyId = keyId;
   }

   public String getKeyPassword()
   {
      return keyPassword;
   }

   public void setKeyPassword(String keyPassword)
   {
      this.keyPassword = keyPassword;
   }

   public String getCertificateId()
   {
      return certificateId;
   }

   public void setCertificateId(String certificateId)
   {
      this.certificateId = certificateId;
   }

   public String getResponseType()
   {
      return responseType == null ? getType() : responseType;
   }

   public void setResponseType(String responseType)
   {
      this.responseType = responseType;
   }

   public String getResponseLocation()
   {
      return responseLocation;
   }

   public void setResponseLocation(String responseLocation)
   {
      this.responseLocation = responseLocation;
   }

   public String getResponsePassword()
   {
      return responsePassword == null ? getPassword() : responsePassword;
   }

   public void setResponsePassword(String responsePassword)
   {
      this.responsePassword = responsePassword;
   }

   public String getResponseKeyId()
   {
      return responseKeyId == null ? getKeyId() : responseKeyId;
   }

   public void setResponseKeyId(String responseKeyId)
   {
      this.responseKeyId = responseKeyId;
   }

   public String getResponseKeyPassword()
   {
      return responseKeyPassword == null ? getKeyPassword() : responseKeyPassword;
   }

   public void setResponseKeyPassword(String responseKeyPassword)
   {
      this.responseKeyPassword = responseKeyPassword;
   }

   public String getResponseCertificateId()
   {
      return responseCertificateId == null ? getCertificateId() : responseCertificateId;
   }

   public void setResponseCertificateId(String responseCertificateId)
   {
      this.responseCertificateId = responseCertificateId;
   }
}
