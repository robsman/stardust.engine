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

/**
 * Query container for building complex queries for deployed models.
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public final class DeployedModelQuery extends Query
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
         DeployedModelQuery.class
   );
   
   /**
    * Filters the models based on the oid attribute.
    * 
    * Example to get a range of models:
    * <pre>
    *    DeployedModelQuery query = new DeployedModelQuery();
    *    query.where(OID.between(2, 5));
    * </pre>
    */
   public static final FilterableAttribute OID = new FilterableAttributeImpl(DeployedModelQuery.class, "oid");

   /**
    * Filters the models based on the id attribute.
    * 
    * Example to get a range of models using an SQL style pattern:
    * <pre>
    *    DeployedModelQuery query = new DeployedModelQuery();
    *    query.where(ID.like("%AN%"));
    * </pre>
    */
   public static final FilterableAttribute ID = new FilterableAttributeImpl(DeployedModelQuery.class, "id");

   /**
    * Filters the models based on the state attribute.
    * 
    * Example to get all valid models with a specific id:
    * <pre>
    *    DeployedModelQuery query = new DeployedModelQuery();
    *    query.where(ID.isEqual(modelId)).and(STATE.isEqual("VALID"));
    * </pre>
    */
   public static final FilterableAttribute STATE = new FilterableAttributeImpl(DeployedModelQuery.class, "state");
   
   /**
    * Filters the models based on references to a specific model.
    * 
    * Example to get all models using a specific one:
    * <pre>
    *    DeployedModelQuery query = new DeployedModelQuery();
    *    query.where(PROVIDER.isEqual(modelOid));
    * </pre>
    */
   public static final FilterableAttribute PROVIDER = new FilterableAttributeImpl(DeployedModelQuery.class, "provider");
   
   /**
    * Filters the models based on references to a specific model.
    * 
    * Example to get all models used by a specific one:
    * <pre>
    *    DeployedModelQuery query = new DeployedModelQuery();
    *    query.where(CONSUMER(modelOid));
    * </pre>
    */
   public static final FilterableAttribute CONSUMER = new FilterableAttributeImpl(DeployedModelQuery.class, "consumer");

   /**
    * Enumeration of model states.
    */
   public static enum DeployedModelState
   {
      ACTIVE, ALIVE, DISABLED, INACTIVE, VALID
   }

   /**
    * Creates a query for finding all deployed models.
    *
    * @return The configured query.
    */
   public static DeployedModelQuery findAll()
   {
      return new DeployedModelQuery();
   }

   /**
    * Creates a query for finding active models.
    * It's a shortcut for findInState(DeployedModelState.ACTIVE).
    *
    * @return The configured query.
    *
    * @see #findInState(DeployedModelState)
    * @see DeployedModelState#ACTIVE
    */
   public static DeployedModelQuery findActive()
   {
      DeployedModelQuery query = new DeployedModelQuery();
      query.where(STATE.isEqual(DeployedModelState.ACTIVE.name()));
      return query;
   }

   /**
    * Creates a query for finding the active deployed model having the specific id.
    *
    * @param modelId The id of the model to retrieve
    * @return The configured query.
    */
   public static DeployedModelQuery findActiveForId(String modelId)
   {
      DeployedModelQuery query = new DeployedModelQuery();
      query.where(ID.isEqual(modelId)).and(STATE.isEqual(DeployedModelState.ACTIVE.name()));
      return query;
   }
   
   /**
    * Creates a query for finding deployed models currently being in the specified
    * state.
    *
    * @param modelState The state the model should be in.
    * @return The configured query.
    */
   public static DeployedModelQuery findInState(
         DeployedModelState modelState)
   {
      DeployedModelQuery query = new DeployedModelQuery();
      query.where(STATE.isEqual(modelState.name()));
      return query;
   }

   /**
    * Creates a query for finding all deployed model versions having the specific id.
    *
    * @param modelId The id of the models to retrieve
    * @return The configured query.
    */
   public static DeployedModelQuery findForId(String modelId)
   {
      DeployedModelQuery query = new DeployedModelQuery();
      query.where(ID.isEqual(modelId));
      return query;
   }

   /**
    * Creates a query for finding all models used by the specified model.
    *
    * @param modelId The model oid of the model.
    * @return The configured query.
    */
   public static DeployedModelQuery findUsedBy(long modelOid)
   {
      DeployedModelQuery query = new DeployedModelQuery();
      query.where(CONSUMER.isEqual(modelOid));
      return query;
   }

   /**
    * Creates a query for finding all models using the specified model.
    *
    * @param modelId The model oid of the model.
    * @return The configured query.
    */
   public static DeployedModelQuery findUsing(long modelOid)
   {
      DeployedModelQuery query = new DeployedModelQuery();
      query.where(PROVIDER.isEqual(modelOid));
      return query;
   }

   /**
    * Initializes a query matching all deployed models.
    *
    * @see #findAll()
    */
   private DeployedModelQuery()
   {
      super(FILTER_VERIFYER);
   }
}