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

import javax.xml.namespace.QName;

import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ModelAwareQueryPredicate;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;



/**
 * Query container for building complex queries for data.
 *
 * @author roland.stamm
 *
 */
public class DataQuery extends Query
{
   private static final long serialVersionUID = 1L;

   /**
    * Attribute to filter for the process id.
    */
   public static final FilterableAttribute PROCESS_ID = new FilterableAttributeImpl(
         DataQuery.class, "processId");

   /**
    * Attribute to filter for a specific data type. See {@link DataTypeConstants}.
    */
   public static final FilterableAttribute DATA_TYPE_ID = new FilterableAttributeImpl(
         DataQuery.class, "dataTypeId");

   /**
    * Attribute to filter for dms data having a specific declared type id assigned.<br>
    * <p>
    * This is only valid for dms types as document, document list, folder or folder list.
    */
   public static final FilterableAttribute DECLARED_TYPE_ID = new FilterableAttributeImpl(
         DataQuery.class, "declaredTypeId");

   /**
    * Attribute to filter for a specific model. <br>
    * <b>Please Note: </b>Currently only supports one single Operator.isEqual(modelOid) term to
    * filter for exactly one modelOid.
    *
    * @see {@link #findAllForModel(long)}
    * @see {@link #findUsedInProcess(long, String)}
    *
    */
   public static final FilterableAttribute MODEL_OID = new FilterableAttributeImpl(
         DataQuery.class, ModelAwareQueryPredicate.INTERNAL_MODEL_OID_ATTRIBUTE);

   public static final FilterVerifier FILTER_VERIFYER = new FilterScopeVerifier(
         new WhitelistFilterVerifyer(new Class[] {FilterTerm.class,//
               UnaryOperatorFilter.class,//
               BinaryOperatorFilter.class,//
               TernaryOperatorFilter.class//
               ,}), DataQuery.class);

   private DataQuery()
   {
      super(FILTER_VERIFYER);
   }

   /**
    * Creates a query for finding all data.
    *
    * @return The configured query.
    */
   public static DataQuery findAll()
   {
      DataQuery query = new DataQuery();

      return query;
   }

   /**
    * Creates a query for finding all data used in the specified model.
    *
    * @param modelOid The model to retrieve the data from.
    * @return The configured query.
    */
   public static DataQuery findAllForModel(long modelOid)
   {
      DataQuery query = new DataQuery();

      query.where(MODEL_OID.isEqual(modelOid));

      return query;
   }

   /**
    * Creates a query for finding all data used in a specified model and process.
    *
    * @param modelOid The model to retrieve the data from.
    * @param processId The process to search used data for.
    * @return The configured query.
    */
   public static DataQuery findUsedInProcess(long modelOid, String processId)
   {
      DataQuery query = DataQuery.findAllForModel(modelOid);

      query.where(PROCESS_ID.isEqual(processId));

      return query;
   }

   /**
    * Creates a query for finding data of a specified type (see {@link DataTypeConstants}) used in a specified model and process.
    *
    * @param modelOid The model to retrieve the data from.
    * @param processId The process to search used data for.
    * @param dataTypeId The data type from {@link DataTypeConstants}.
    * @return The configured query.
    *
    * @see DataTypeConstants
    */
   public static DataQuery findUsedInProcessHavingDataType(long modelOid,
         String processId, String dataTypeId)
   {
      DataQuery query = DataQuery.findAllForModel(modelOid);

      query.where(PROCESS_ID.isEqual(processId));

      query.where(DATA_TYPE_ID.isEqual(dataTypeId));

      return query;
   }

   /**
    * Creates a query for finding document data used in a specified process having the specified {@link DocumentType} assigned.
    *
    * @param modelOid The model to retrieve the data from.
    * @param processId The process to search used data for.
    * @param documentType The document type to search used data for.
    * @return The configured query.
    *
    * @see DocumentType
    * @see DocumentTypeUtils
    */
   public static DataQuery findUsedInProcessHavingDocumentWithDocType(long modelOid,
         String processId, DocumentType documentType)
   {
      DataQuery query = DataQuery.findAllForModel(modelOid);

      query.where(PROCESS_ID.isEqual(processId));

      query.where(DATA_TYPE_ID.isEqual(DataTypeConstants.DMS_DOCUMENT_DATA));

      String documentTypeId = documentType == null
            ? null
            : documentType.getDocumentTypeId();
      String localPart = documentTypeId == null ? null : QName.valueOf(documentTypeId)
            .getLocalPart();
      query.where(DECLARED_TYPE_ID.isEqual(localPart));

      return query;
   }

   /**
    * Creates a query for finding document data used in a specified process having no document type assigned.
    *
    * @param modelOid The model to retrieve the data from.
    * @param processId The process to search used data for.
    * @return The configured query.
    */
   public static DataQuery findUsedInProcessHavingDocumentWithoutDocType(
         long modelOid, String processId)
   {
      DataQuery query = DataQuery.findAllForModel(modelOid);

      query.where(PROCESS_ID.isEqual(processId));

      query.where(DATA_TYPE_ID.isEqual(DataTypeConstants.DMS_DOCUMENT_DATA));

      query.where(DECLARED_TYPE_ID.isEqual(null));

      return query;
   }

}
