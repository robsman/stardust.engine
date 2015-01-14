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
import org.eclipse.stardust.engine.core.runtime.beans.ModelAwareQueryPredicate;

/**
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

   public static enum Option
   {
      WITH_DESCRIPTION, WITH_VALUES
   }

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

   public static final FilterVerifier FILTER_VERIFYER = new FilterScopeVerifier(
         new WhitelistFilterVerifyer(new Class[] {
               FilterTerm.class,
               UnaryOperatorFilter.class,
               BinaryOperatorFilter.class,
               TernaryOperatorFilter.class,
               DataFilter.class
         }), BusinessObjectQuery.class);

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

   public static BusinessObjectQuery findInModel(String modelId)
   {
      BusinessObjectQuery query = findAll();
      query.where(MODEL_ID.isEqual(modelId));
      return query;
   }

   public static BusinessObjectQuery findForBusinessObject(String qualifiedBusinessObjectId)
   {
      QName qname = QName.valueOf(qualifiedBusinessObjectId);
      BusinessObjectQuery query = BusinessObjectQuery.findInModel(qname.getNamespaceURI());
      query.where(BUSINESS_OBJECT_ID.isEqual(qname.getLocalPart()));
      return query;
   }

   public static BusinessObjectQuery findWithPrimaryKey(String qualifiedBusinessObjectId, Object pk)
   {
      BusinessObjectQuery query = BusinessObjectQuery.findForBusinessObject(qualifiedBusinessObjectId);
      query.where(pk instanceof Number
            ? PRIMARY_KEY.isEqual(((Number) pk).longValue())
            : PRIMARY_KEY.isEqual(pk.toString()));
      return query;
   }

   public static BusinessObjectQuery findInModel(long modelOid)
   {
      BusinessObjectQuery query = findAll();
      query.where(MODEL_OID.isEqual(modelOid));
      return query;
   }

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

   public static BusinessObjectQuery findWithPrimaryKey(long modelOid, String businessObjectId, Object pk)
   {
      BusinessObjectQuery query = BusinessObjectQuery.findForBusinessObject(modelOid, businessObjectId);
      query.where(pk instanceof Number
            ? PRIMARY_KEY.isEqual(((Number) pk).longValue())
            : PRIMARY_KEY.isEqual(pk.toString()));
      return query;
   }
}
