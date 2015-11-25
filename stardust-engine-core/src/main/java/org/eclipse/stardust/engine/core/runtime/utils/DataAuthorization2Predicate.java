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
package org.eclipse.stardust.engine.core.runtime.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;

/**
 * Predicate class which is used to restrict access to work items based on its activity
 * declarative security permission.
 *
 * @author stephan.born
 * @version $Revision: 5162 $
 */
public class DataAuthorization2Predicate extends AbstractAuthorization2Predicate
{
   private List<IOrganization> scopedOrganizations;

   public DataAuthorization2Predicate(AuthorizationContext context)
   {
      super(context);
   }

   public boolean accept(Object o)
   {
      if (o instanceof IData)
      {
         context.setModelElementData((IData) o);
         return Authorization2.hasPermission(context);
      }
      return false;
   }

   public static void verify(IData data, ClientPermission permission)
   {
      BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      if (runtimeEnvironment.isSecureContext())
      {
         AuthorizationContext context = AuthorizationContext.create(permission);
         DataAuthorization2Predicate authorizationPredicate = new DataAuthorization2Predicate(context);
         if (!authorizationPredicate.accept(data))
         {
            IUser user = context.getUser();
            throw new AccessForbiddenException(BpmRuntimeError.AUTHx_AUTH_MISSING_GRANTS.raise(
                  user.getOID(), String.valueOf(permission), user.getAccount()));
         }
      }
   }

   public synchronized boolean acceptBOValue(IData data, Object structuredDataValue)
   {
      // get auth context#grants -> get IOrganization -> is scoped -> data path
      // ->  data path to get BO value for department -> set prefetch value
      // -> check authorization using context.

      Map<?,?> structuredDataMap = null;
      if (structuredDataValue instanceof Map)
      {
         structuredDataMap = (Map< ? , ? >) structuredDataValue;
      }

      List<IOrganization> scopedOrganizations = getScopedOrganizations();
      for (IOrganization scopedOrganization : scopedOrganizations)
      {
         if (scopedOrganization != null)
         {
            String dataId = scopedOrganization
                  .getAttribute(PredefinedConstants.BINDING_DATA_ID_ATT);
            String dataPath = scopedOrganization
                  .getAttribute(PredefinedConstants.BINDING_DATA_PATH_ATT);

            if (structuredDataMap != null)
            {
               String departmentId = (String) structuredDataMap.get(dataPath);

               String qualifiedDataId = new QName(scopedOrganization.getModel().getId(),
                     dataId).toString();
               context.setPrefetchedDataValue(qualifiedDataId, dataPath, departmentId);

               boolean resetPrefetchFlag = false;
               if (!context.isPrefetchDataAvailable())
               {
                  context.setPrefetchDataAvailable(true);
                  resetPrefetchFlag = true;
               }

               boolean allowed = this.accept(data);

               if (resetPrefetchFlag)
               {
                  context.setPrefetchDataAvailable(false);
               }

               if (allowed)
               {
                  return true;
               }
            }
         }
      }
      return false;
   }

   private List<IOrganization> getScopedOrganizations()
   {
      if (this.scopedOrganizations != null)
      {
         return this.scopedOrganizations;
      }
      Set<IOrganization> scopedOrganizations = CollectionUtils.newSet();
      String[] grants = context.getGrants();
      for (String participantId : grants)
      {
         List<IModel> models = context.getModels();
         for (IModel iModel : models)
         {
            IModelParticipant participant = iModel.findParticipant(participantId);
            IOrganization firstScopedOrganization = DepartmentUtils
                  .getFirstScopedOrganization(participant);
            if (firstScopedOrganization != null)
            {
               scopedOrganizations.add(firstScopedOrganization);
            }
         }
      }
      this.scopedOrganizations = new ArrayList(scopedOrganizations);
      return this.scopedOrganizations;
   }
}
