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
package org.eclipse.stardust.engine.api.query;

import org.eclipse.stardust.engine.core.runtime.beans.ModelAwareQueryPredicate;

public class ProcessDefinitionQuery extends Query
{
   private static final long serialVersionUID = 1L;

   private static final FilterVerifier FILTER_VERIFYER = new FilterScopeVerifier(
         new WhitelistFilterVerifyer(new Class[]
         {
            FilterTerm.class,
            UnaryOperatorFilter.class,
            BinaryOperatorFilter.class,
            TernaryOperatorFilter.class,
            // ???
         }),
         ProcessDefinitionQuery.class
   );

   public static final FilterableAttribute TRIGGER_TYPE = new FilterableAttributeImpl(ProcessDefinitionQuery.class, "triggerType");

   private static final FilterableAttribute MODEL_OID = new FilterableAttributeImpl(ProcessDefinitionQuery.class, ModelAwareQueryPredicate.INTERNAL_MODEL_OID_ATTRIBUTE);

   /**
    * Creates a query for finding all process definitions from the active model.
    *
    * @return The configured query.
    */
   public static ProcessDefinitionQuery findAll()
   {
      return new ProcessDefinitionQuery();
   }

   /**
    * Creates a query for finding manually startable processes.
    *
    * @return The configured query.
    *
    * @see #findStartable(String triggerType)
    */
   public static ProcessDefinitionQuery findStartable()
   {
      return findStartable("manual");
   }

   /**
    * Creates a query for finding manually startable processes contained in a specific model.
    *
    * @param modelOID specifies the model to retrieve manually startable processes from.
    * @return The configured query.
    *
    * @see #findStartable(String triggerType)
    */
   public static ProcessDefinitionQuery findStartable(long modelOID)
   {
      ProcessDefinitionQuery query = findStartable("manual");
      query.where(MODEL_OID.isEqual(modelOID));
      return query;
   }

   /**
    * Creates a query for finding the active deployed model having the specific id.
    *
    * @param modelId The id of the model to retrieve
    * @return The configured query.
    */
   public static ProcessDefinitionQuery findStartable(String triggerType)
   {
      ProcessDefinitionQuery query = new ProcessDefinitionQuery();
      query.where(TRIGGER_TYPE.isEqual(triggerType));
      return query;
   }

   /**
    * Initializes a query matching all deployed models.
    *
    * @see #findAll()
    */
   private ProcessDefinitionQuery()
   {
      super(FILTER_VERIFYER);
   }
}
