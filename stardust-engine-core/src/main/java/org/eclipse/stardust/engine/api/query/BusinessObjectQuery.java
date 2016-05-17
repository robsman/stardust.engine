/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.beans.ModelAwareQueryPredicate;

/**
 * Query container for building complex queries for business objects.
 * <p/>
 * <p>Valid filter criteria are:
 * <ul>
 *    <li>{@link FilterTerm} for building complex criteria.</li>
 *    <li>{@link DataFilter} for finding business objects containing specific data.</li>
 * </ul>
 * </p>
 * <p>Supported evaluation policies are:
 * <ul>
 *    <li>{@link BusinessObjectQuery.Policy} to specify if the result should contain the
 *    business objects descriptions or/and the values.</li>
 *    <li>{@link TimeoutPolicy} to specify an explicit query timeout.</li>
 *    <li>{@link SubsetPolicy} to specify an explicit result subset (from, to).</li>
 * </ul>
 * </p>
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class BusinessObjectQuery extends Query
{
   private static final long serialVersionUID = 1L;

   public static final String MODEL_ID_ATTRIBUTE = "modelId";
   public static final String ID_ATTRIBUTE = "businessObjectId";
   public static final String PK_ATTRIBUTE = "primaryKey";

   /**
    * Policy options.
    *
    * @author Florin.Herinean
    * @version $Revision: $
    */
   public static enum Option
   {
      /**
       * Result includes business object description.
       */
      WITH_DESCRIPTION,

      /**
       * Result includes business object instance values.
       */
      WITH_VALUES,

      /**
       * Result includes only the last business object instance values eventually upgraded to current schema.
       */
      UPGRADE
   }

   /**
    * Policy that specifies the query options.
    *
    * @author Florin.Herinean
    * @version $Revision: $
    */
   public static class Policy implements EvaluationPolicy
   {
      private static final long serialVersionUID = 1L;

      private Option[] options;

      public Policy(Option... options)
      {
         this.options = options;
      }

      public boolean hasOption(Option option)
      {
         if (options != null)
         {
            for (Option opt : options)
            {
               if (opt == option)
               {
                  return true;
               }
            }
         }
         return false;
      }
   }

   /**
    * Attribute to filter for the business object id.
    */
   private static final FilterableAttribute BUSINESS_OBJECT_ID = new FilterableAttributeImpl(
         BusinessObjectQuery.class, ID_ATTRIBUTE);

   /**
    * Attribute to filter for the business object primary key.
    */
   private static final FilterableAttribute PRIMARY_KEY = new FilterableAttributeImpl(
         BusinessObjectQuery.class, PK_ATTRIBUTE);

   /**
    * Attribute to filter for a specific model. <br>
    * <b>Please Note: </b>Currently only supports one single Operator.isEqual(modelId) term to
    * filter for exactly one modelId.
    *
    * @see {@link #findAllForModel(String)}
    * @see {@link #findUsedInProcess(String, String)}
    *
    */
   private static final FilterableAttribute MODEL_ID = new FilterableAttributeImpl(
         BusinessObjectQuery.class, MODEL_ID_ATTRIBUTE);

   /**
    * Attribute to filter for a specific model. <br>
    * <b>Please Note: </b>Currently only supports one single Operator.isEqual(modelOid) term to
    * filter for exactly one modelOid.
    *
    * @see {@link #findAllForModel(long)}
    * @see {@link #findUsedInProcess(long, String)}
    *
    */
   private static final FilterableAttribute MODEL_OID = new FilterableAttributeImpl(
         BusinessObjectQuery.class, ModelAwareQueryPredicate.INTERNAL_MODEL_OID_ATTRIBUTE);

   /**
    * List of valid filters.
    */
   public static final FilterVerifier FILTER_VERIFYER = new FilterScopeVerifier(
         new WhitelistFilterVerifyer(new Class[] {
               FilterTerm.class,
               UnaryOperatorFilter.class,
               BinaryOperatorFilter.class,
               TernaryOperatorFilter.class,
               DataFilter.class
         }), BusinessObjectQuery.class);

   /**
    * Default constructor.
    */
   private BusinessObjectQuery()
   {
      super(FILTER_VERIFYER);
   }

   /**
    * Creates a query for finding all business objects.
    *
    * @return The configured query.
    */
   public static BusinessObjectQuery findAll()
   {
      return new BusinessObjectQuery();
   }

   /**
    * Creates a query for finding all business objects declared in the specified model.
    *
    * @param modelId the id of the model.
    * @return The configured query.
    */
   public static BusinessObjectQuery findInModel(String modelId)
   {
      BusinessObjectQuery query = findAll();
      query.where(MODEL_ID.isEqual(modelId));
      return query;
   }

   /**
    * Creates a query for finding a specific business object.
    *
    * @param qualifiedBusinessObjectId the qualified id of the business object (in the form '{' + modelId + '}' + dataId).
    * @return The configured query.
    */
   public static BusinessObjectQuery findForBusinessObject(String qualifiedBusinessObjectId)
   {
      QName qname = QName.valueOf(qualifiedBusinessObjectId);
      BusinessObjectQuery query = BusinessObjectQuery.findInModel(qname.getNamespaceURI());
      query.where(BUSINESS_OBJECT_ID.isEqual(qname.getLocalPart()));
      return query;
   }

   /**
    * Creates a query for finding a specific instance of a business object.
    *
    * @param qualifiedBusinessObjectId the qualified id of the business object (in the form '{' + modelId + '}' + dataId).
    * @param pk the primary key value of the business object instance.
    * @return The configured query.
    */
   public static BusinessObjectQuery findWithPrimaryKey(String qualifiedBusinessObjectId, Object pk)
   {
      BusinessObjectQuery query = BusinessObjectQuery.findForBusinessObject(qualifiedBusinessObjectId);
      query.where(((FilterableAttributeImpl) PRIMARY_KEY).isEqual(pk));
      return query;
   }

   /**
    * Creates a query for finding all business objects declared in the specified model.
    *
    * @param modelOid the oid of a concrete deployed model or one of the predefined meta oids:
    * <ul>
    *    <li>{@link PredefinedConstants#ALL_MODELS} includes business objects from all models</li>
    *    <li>{@link PredefinedConstants#ACTIVE_MODEL} includes business objects from the active models</li>
    *    <li>{@link PredefinedConstants#LAST_DEPLOYED_MODEL} includes business objects from the last deployed models</li>
    *    <li>{@link PredefinedConstants#ALIVE_MODELS} includes business objects from alive models</li>
    *    <li>{@link PredefinedConstants#ANY_MODEL} includes business objects from the first matching model</li>
    * </ul>
    * @return The configured query.
    */
   public static BusinessObjectQuery findInModel(long modelOid)
   {
      BusinessObjectQuery query = findAll();
      query.where(MODEL_OID.isEqual(modelOid));
      return query;
   }

   /**
    * Creates a query for finding a business object.
    *
    * @param modelOid the oid of a concrete deployed model or one of the predefined meta oids
    * @param businessObjectId the id of the business object, either qualified or simple.
    * @return The configured query.
    */
   public static BusinessObjectQuery findForBusinessObject(long modelOid, String businessObjectId)
   {
      QName qname = QName.valueOf(businessObjectId);
      BusinessObjectQuery query = BusinessObjectQuery.findInModel(modelOid);
      if (!StringUtils.isEmpty(qname.getNamespaceURI()))
      {
         query.where(MODEL_ID.isEqual(qname.getNamespaceURI()));
      }
      query.where(BUSINESS_OBJECT_ID.isEqual(qname.getLocalPart()));
      return query;
   }

   /**
    * Creates a query for finding a business object instance.
    *
    * @param modelOid the oid of a concrete deployed model or one of the predefined meta oids
    * @param businessObjectId the id of the business object, either qualified or simple.
    * @param pk the primary key value of the business object instance.
    * @return The configured query.
    */
   public static BusinessObjectQuery findWithPrimaryKey(long modelOid, String businessObjectId, Object pk)
   {
      BusinessObjectQuery query = BusinessObjectQuery.findForBusinessObject(modelOid, businessObjectId);
      query.where(((FilterableAttributeImpl) PRIMARY_KEY).isEqual(pk));
      return query;
   }
}
