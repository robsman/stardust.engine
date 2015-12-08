/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.query.BusinessObjects;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.BusinessObject;
import org.eclipse.stardust.engine.api.runtime.BusinessObject.Value;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.BusinessObjectRelationship.BusinessObjectReference;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.PropagateAccessEvaluator.IPropagatedAccessEvalFunction;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2Predicate;
import org.eclipse.stardust.engine.core.runtime.utils.ClientPermission;
import org.eclipse.stardust.engine.core.runtime.utils.DataAuthorization2Predicate;


/**
 * Handles access restrictions for Business Objects based on departments and access propagation.
 *
 * @author Roland.Stamm
 */
public class BusinessObjectSecurityUtils
{
   public static boolean isDepartmentReadAllowed(IData data, Object value)
   {
      // The check for possible propagated permissions (ignoring departments) is done when
      // filtering data for the query (BusinessObjectSecurityUtils.isUnscopedPropagatedAccessAllowed)

      // Declarative security is evaluated deferred.
      // Authorization2Predicate for ClientPermission.READ_DATA_VALUE already exists.
      final BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();
      Authorization2Predicate authorizationPredicate = bpmRt.getAuthorizationPredicate();



      return authorizationPredicate == null ? true : isDepartmentAllowed(data, value, authorizationPredicate);
   }

   public static void checkDepartmentModifyAllowed(IData data, Object value)
   {
      // Declarative security is evaluated deferred.
      // Authorization2Predicate for ClientPermission.MODIFY_DATA_VALUE already exists.
      BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();
      Authorization2Predicate authorizationPredicate = bpmRt.getAuthorizationPredicate();

      if (authorizationPredicate != null)
      {
         // check for possible propagated permissions (ignoring departments)
         // cached value is used later
         BusinessObjectSecurityUtils.isUnscopedPropagatedAccessAllowed(data,
               authorizationPredicate);

         if (!isDepartmentAllowed(data, value, authorizationPredicate))
         {
            IUser user = SecurityProperties.getUser();
            throw new AccessForbiddenException(
                  BpmRuntimeError.AUTHx_AUTH_MISSING_GRANTS.raise(user.getOID(),
                        String.valueOf(ClientPermission.MODIFY_DATA_VALUE),
                        user.getAccount()));
         }
      }
   }

   private static boolean isDepartmentAllowed(IData data, Object value,
         Authorization2Predicate authorizationPredicate)
   {
      boolean denied = false;

      if (authorizationPredicate != null
            && authorizationPredicate instanceof DataAuthorization2Predicate)
      {
         DataAuthorization2Predicate dataAuthPredicate = (DataAuthorization2Predicate) authorizationPredicate;

         if (authorizationPredicate.accept(data))
         {
            // Non scoped access allowed
            if (value != null)
            {
               // restrict on department
               denied |= !dataAuthPredicate.acceptBOValue(data, value);
            }
         }
         else if (dataAuthPredicate.getVisitedDataUnscoped() != null &&
               dataAuthPredicate.getVisitedDataUnscoped().containsKey(data))
         {
            // Direct non scoped access not allowed but has non scoped access on
            // propagated data.
            // Access is denied as long as no propagated accessible value is found
            if (value != null)
            {
               denied = !BusinessObjectSecurityUtils.isPropagatedDepartmentAllowed(data,
                     value);
            }

         }
         else
         {
            // no scoped access and no scoped propagated access
            denied = true;
         }
      }
      return !denied;
   }

   public static boolean isUnscopedPropagatedAccessAllowed(IData data, final Authorization2Predicate auth)
   {
      Boolean result = getCachedUnscopedResult(data, auth);
      if (result != null)
      {
         return result;
      }

      // Function checks if participant is allowed for a Data (defining a BusinessObject)
      // that propagates access. Not checking for granted departments.
      // Matching Scoped participantsIds are considered allowed without considering departments.
      IPropagatedAccessEvalFunction<Boolean> propagatedUnscopedParticipantAllowedFunction = new IPropagatedAccessEvalFunction<Boolean>()
      {
         @Override
         public Boolean execute(IData otherData,
               BusinessObjectRelationship businessObjectRelationship)
         {
            return auth.accept(otherData);
         }
      };
      result = evaluatePropagateAccessFunction(data, propagatedUnscopedParticipantAllowedFunction);

      // cache result
      ((DataAuthorization2Predicate)auth).getVisitedDataUnscoped().put(data, result);

      return result;
   }

   private static Boolean getCachedUnscopedResult(IData data, Authorization2Predicate auth)
   {
      DataAuthorization2Predicate dataAuth = (DataAuthorization2Predicate) auth;

      Map<IData, Boolean> visitedDataUnscoped = dataAuth.getVisitedDataUnscoped();
      if (visitedDataUnscoped == null)
      {
         visitedDataUnscoped = CollectionUtils.newHashMap();
         dataAuth.setVisitedDataUnscoped(visitedDataUnscoped);
      }
      if (visitedDataUnscoped.containsKey(data))
      {
         return visitedDataUnscoped.get(data);
      }
      else
      {
         visitedDataUnscoped.put(data, null);
      }
      return null;
   }

   private static boolean isPropagatedDepartmentAllowed(IData data, final Object structuredDataValue)
   {
      // prevent recursive loops analyzing the same data values multiple times.
      if (isDataAlreadyVisited(data, structuredDataValue))
      {
         return false;
      }

      IPropagatedAccessEvalFunction<Boolean> departmentAccessibleFunction = new IPropagatedAccessEvalFunction<Boolean>()
      {
         @Override
         public Boolean execute(IData otherData,
               BusinessObjectRelationship businessObjectRelationship)
         {
            // check access to related objects including check for department
            Map<?,?> structuredDataMap = null;
            if (structuredDataValue instanceof Map)
            {
               structuredDataMap = (Map< ? , ? >) structuredDataValue;
            }
            Object foreignKey = structuredDataMap.get(businessObjectRelationship.otherForeignKeyField);

            BusinessObjectReference otherBusinessObject = businessObjectRelationship.otherBusinessObject;
            String otherBusinessObjectQualifiedId = new QName(otherBusinessObject.modelId,
                  otherBusinessObject.id).toString();

            // Contains query which can make a recursive call to the same data again in case propagation is cyclic.
            // Evaluation of same data is prevented directly at the start of #isPropagatedDepartmentAllowed
            if (isRelatedBusinessObjectAccessible(foreignKey, otherBusinessObjectQualifiedId))
            {
               return true;
            }
            return false;
         }
      };

      return evaluatePropagateAccessFunction(data, departmentAccessibleFunction);
   }

   private static boolean isDataAlreadyVisited(IData data, Object structuredDataValue)
   {
      final BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();
      Authorization2Predicate auth = bpmRt.getAuthorizationPredicate();

      DataAuthorization2Predicate dataAuth = (DataAuthorization2Predicate) auth;

      Set<String> visitedDepartmentData = dataAuth.getVisitedData();
      if (visitedDepartmentData == null)
      {
         visitedDepartmentData = CollectionUtils.newHashSet();
         dataAuth.setVisitedData(visitedDepartmentData);
      }
      String structValue = structuredDataValue == null ? "" : structuredDataValue.toString();
      String key = data.getModel().getId() + ":" + data.getId() + ":" + structValue;

      if (visitedDepartmentData.contains(key))
      {
         return true;
      }
      else
      {
         visitedDepartmentData.add(key);
      }
      return false;
   }

   private static boolean evaluatePropagateAccessFunction(IData data,
         IPropagatedAccessEvalFunction<Boolean> function)
   {
      PropagateAccessEvaluator propagateAccessEvaluator = new PropagateAccessEvaluator(data, function);

      return propagateAccessEvaluator.evaluate();
   }

   private static boolean isRelatedBusinessObjectAccessible(Object foreignKey, String otherBusinessObjectId)
   {
      List<String> foreignKeys = null;
      if (foreignKey instanceof List)
      {
         foreignKeys = (List<String>) foreignKey;
      }
      else
      {
         foreignKeys = Collections.singletonList((String)foreignKey);
      }

      if (foreignKeys != null && !foreignKeys.isEmpty())
      {
         // TODO (performance) query for all PKs at once or in batches
         for (String pk : foreignKeys)
         {
            if (StringUtils.isEmpty(pk))
            {
               break;
            }
            BusinessObjectQuery query = BusinessObjectQuery
                  .findWithPrimaryKey(otherBusinessObjectId, pk);
            query.setPolicy(
                  new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
            BusinessObjects businessObjects = BusinessObjectUtils
                  .getBusinessObjects(query);
            if (businessObjects != null)
            {
               for (BusinessObject businessObject : businessObjects)
               {
                  List<Value> boValues = businessObject.getValues();
                  if (boValues != null && !boValues.isEmpty())
                  {
                     // access to related value allowed
                     return true;
                  }
               }
            }
         }
      }
      return false;
   }
}
