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

import java.security.Key;
import java.security.cert.Certificate;

public interface IWSSAuthenticationParameters extends IBasicAuthenticationParameters
{
   void setRequestConfigurationLocation(String requestConfigurationLocation);
   void setRequestKey(Key key);
   void setRequestCertificate(Certificate certificate);

   void setResponseConfigurationLocation(String responseConfigurationLocation);
   void setResponseKey(Key key);
   void setResponseCertificate(Certificate certificate);

   IKeystoreParameters getKeystoreParameters();
}
