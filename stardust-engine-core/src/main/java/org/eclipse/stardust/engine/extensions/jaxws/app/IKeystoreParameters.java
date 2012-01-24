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

public interface IKeystoreParameters
{
   void setType(String type);
   void setLocation(String location);
   void setPassword(String password);
   void setKeyId(String keyId);
   void setKeyPassword(String keyPassword);
   void setCertificateId(String certificateId);
   
   void setResponseType(String responseType);
   void setResponseLocation(String responseLocation);
   void setResponsePassword(String responsePassword);
   void setResponseKeyId(String responseKeyId);
   void setResponseKeyPassword(String responseKeyPassword);
   void setResponseCertificateId(String responseCertificateId);
}
