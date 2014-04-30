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
package org.eclipse.stardust.engine.spring.utils;

import static java.util.Collections.emptyMap;
import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.security.Principal;
import java.util.Map;

import org.eclipse.stardust.engine.api.spring.InvokerPrincipal;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.security.PrincipalProvider;
import org.springframework.beans.factory.InitializingBean;


/**
 * @author rsauer
 * @version $Revision$
 */
public class StaticPrincipalProvider implements PrincipalProvider, InitializingBean
{
   private String userId;

   private String partition;

   private String domain;

   private String realm;

   private byte[] signature;

   private InvokerPrincipal principal;

   public String getUserId()
   {
      return userId;
   }

   public void setUserId(String userId)
   {
      this.userId = userId;
   }

   public String getPartition()
   {
      return partition;
   }

   public void setPartition(String partition)
   {
      this.partition = partition;
   }

   public String getDomain()
   {
      return domain;
   }

   public void setDomain(String domain)
   {
      this.domain = domain;
   }

   public String getRealm()
   {
      return realm;
   }

   public void setRealm(String realm)
   {
      this.realm = realm;
   }

   public byte[] getSignature()
   {
      return signature;
   }

   public void setSignature(byte[] signature)
   {
      this.signature = signature;
   }

   public void afterPropertiesSet() throws Exception
   {
      if (isEmpty(userId))
      {
         throw new IllegalArgumentException("Property 'userId' must not be null.");
      }

      Map<String, String> properties = newHashMap();

      if ( !isEmpty(partition))
      {
         properties.put(SecurityProperties.PARTITION, partition);
      }
      if ( !isEmpty(domain))
      {
         properties.put(SecurityProperties.DOMAIN, domain);
      }
      if ( !isEmpty(realm))
      {
         properties.put(SecurityProperties.REALM, realm);
      }

      if (isEmpty(properties))
      {
         properties = emptyMap();
      }

      this.principal = new InvokerPrincipal(userId, properties, signature);
   }

   public Principal getPrincipal()
   {
      return principal;
   }

}
