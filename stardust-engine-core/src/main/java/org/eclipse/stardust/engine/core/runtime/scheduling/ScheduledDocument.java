/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.runtime.scheduling;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.AbstractLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.security.utils.SecurityUtils;

import com.google.gson.JsonObject;

public abstract class ScheduledDocument
{
   private static final String DEFAULT_REALM_ID = "carnot";

   private static final Logger trace = LogManager.getLogger(ScheduledDocument.class);

   private JsonObject documentJson;
   private QName owner;
   private String documentName;

   public ScheduledDocument(JsonObject documentJson, QName owner, String documentName)
   {
      this.documentJson = documentJson;
      this.owner = owner;
      this.documentName = documentName;
   }

   public abstract void execute();

   protected JsonObject getDocumentJson()
   {
      return documentJson;
   }

   protected QName getOwner()
   {
      return owner;
   }

   protected String getDocumentName()
   {
      return documentName;
   }

   protected IUser getUser()
   {
      boolean invalidUser = true;

      String realmId = owner.getNamespaceURI();
      if (XMLConstants.NULL_NS_URI.equals(realmId))
      {
         realmId = DEFAULT_REALM_ID;
      }
      String userId = owner.getLocalPart();
      if (StringUtils.isEmpty(userId))
      {
//         IUser tu = TransientUser.getInstance();
//         IUserRealm tr = tu.getRealm();
//         return UserBean.createTransientUser(tu.getAccount(), tu.getFirstName(), tu.getLastName(),
//               UserRealmBean.createTransientRealm(tr.getId(), tr.getName(), tr.getPartition()));
         userId = "motu";
      }

      IUser user = new UserServiceImpl().internalGetUser(realmId, userId);

      if (user != null)
      {
         invalidUser = SecurityUtils.isUserDisabled(user) || SecurityUtils.isUserInvalid(user);
         if (invalidUser)
         {
            trace.info("Invalid user: " + owner);
            return null;
         }
      }
      return user;
   }

   protected EmbeddedServiceFactory getServiceFactory(final IUser user)
   {
      EmbeddedServiceFactory sf = new EmbeddedServiceFactory()
      {
         @Override
         protected MethodInterceptor getLoginInterceptor(boolean withLogin)
         {
            return new AbstractLoginInterceptor()
            {
               private static final long serialVersionUID = 1L;

               @Override
               public Object invoke(MethodInvocation invocation) throws Throwable
               {
                  PropertyLayer layer = PropertyLayerProviderInterceptor.getCurrent();
                  setCurrentUser(layer, user);
                  return invocation.proceed();
               }
            };
         }
      };
      return sf;
   }
}