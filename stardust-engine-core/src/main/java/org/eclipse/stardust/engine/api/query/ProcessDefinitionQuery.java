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

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
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

   /**
    * Predefined filter matching process definition interfaces defining the invocation
    * type REST
    */
   public static final String REST_INVOCATION = PredefinedConstants.PROCESSINTERFACE_INVOCATION_REST;

   /**
    * Predefined filter matching process definition interfaces defining the invocation
    * type SOAP
    */
   public static final String SOAP_INVOCATION = PredefinedConstants.PROCESSINTERFACE_INVOCATION_SOAP;

   /**
    * Predefined filter matching process definition interfaces defining the invocation
    * type BOTH (REST and SOAP)
    */
   public static final String BOTH_INVOCATION = PredefinedConstants.PROCESSINTERFACE_INVOCATION_BOTH;

   public static final FilterableAttribute TRIGGER_TYPE = new FilterableAttributeImpl(ProcessDefinitionQuery.class, "triggerType");

   public static final FilterableAttribute INVOCATION_TYPE = new FilterableAttributeImpl(ProcessDefinitionQuery.class, "invocationType");

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
    * @see #findStartable(Strinzg triggerType)
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
    * @param triggerType a string identifying the type of the triggers used to start processes, i.e. "manual" or "scan".
    * @return The configured query.
    */
   public static ProcessDefinitionQuery findStartable(String triggerType)
   {
      ProcessDefinitionQuery query = new ProcessDefinitionQuery();
      query.where(TRIGGER_TYPE.isEqual(triggerType));
      return query;
   }

   /**
    * Create a query for finding a process definition that providing a process interface with the given invocation type.
    *
    * @param invocationType a string identifying the type of invocation for the process interface. Can be </br>
    *    {@link ProcessDefinitionQuery#REST_INVOCATION} </br>
    *    {@link ProcessDefinitionQuery#SOAP_INVOCATION} </br>
    *    {@link ProcessDefinitionQuery#BOTH_INVOCATION} </br>
    * @return The configured query.
    */
   public static ProcessDefinitionQuery findProcessInterface(String invocationType)
   {
      ProcessDefinitionQuery query = new ProcessDefinitionQuery();
      query.where(INVOCATION_TYPE.isEqual(invocationType));
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
