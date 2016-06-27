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
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.core.struct.StructuredDataConstants.URN_INTERNAL_PREFIX;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.emf.common.util.EList;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.ExternalReference;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IExternalReference;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IReference;
import org.eclipse.stardust.engine.api.model.ISchemaType;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.model.IXpdlType;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.Reference;
import org.eclipse.stardust.engine.api.model.SchemaType;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.model.XpdlType;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryManager;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryProviderUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.emfxsd.XPathFinder;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSchemaDirective;
import org.eclipse.xsd.XSDTypeDefinition;

public final class DocumentTypeUtils
{
   private static final String SCHEMA_XSD = "schema.xsd";

   private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

   private static final Logger trace = LogManager.getLogger(DocumentTypeUtils.class);

   private static void log(String string)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug(string);
      }
   }

   private DocumentTypeUtils()
   {
      // utility class
   }

   /**
    * for internal use
    *
    * Synchronizes the DocumentTypes of the specified model with the DMS.
    *
    * @param model
    * @throws InternalException
    */
   protected static void synchronizeDocumentTypeSchema(IModel model)
         throws InternalException
   {
      getDocumentTypes(model, true);
   }

   /**
    * for internal use
    *
    * @param model
    * @param synchronizeWithDms
    * @return
    * @throws InternalException
    */
   protected static List<DocumentType> getDocumentTypes(IModel model,
         boolean synchronizeWithDms) throws InternalException
   {
      List<DocumentType> documentTypes = CollectionUtils.newList();
      try
      {
         Set<String> typeDeclarationIds = new LinkedHashSet<String>();
         ModelElementList< ? > dataList = model.getData();
         for (int i = 0, len = dataList.size(); i < len; i++ )
         {
            IData data = (IData) dataList.get(i);

            String dataTypeId = data.getType().getId();
            if (isDmsDocumentData(dataTypeId))
            {
               String typeDeclarationId = (String) data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);

               IReference externalReference = data.getExternalReference();

               if (null == externalReference && !isEmpty(typeDeclarationId))
               {
                  typeDeclarationIds.add(typeDeclarationId);
               }

               String externalRef = "";
               if (externalReference != null)

               {
                  externalRef = " from external reference '"
                        + externalReference.getExternalPackage().getHref() + "'";
               }
               log("Found DmsData '" + data.getId() + "' using structured type '"
                     + typeDeclarationId + "'" + externalRef);
            }
         }

         List<DocumentTypeXsdSyncEntry> toSyncXsds = new LinkedList<DocumentTypeXsdSyncEntry>();
         Set<Pair<XSDSchema, XSDSchema>> transitiveXsdDependencies = new LinkedHashSet<Pair<XSDSchema, XSDSchema>>();
         ModelElementList< ? > typeDeclarations = model.getTypeDeclarations();
         for (int j = 0, len2 = typeDeclarations.size(); j < len2; j++ )
         {
            ITypeDeclaration typeDeclaration = (ITypeDeclaration) typeDeclarations.get(j);
            if (typeDeclarationIds.contains(typeDeclaration.getId()))
            {
               XSDSchema xsdSchema = StructuredTypeRtUtils.getXSDSchema(model,
                     typeDeclaration);

               DocumentType documentType = getDocumentType(typeDeclaration.getXpdlType(),
                     xsdSchema, model.getModelOID(), typeDeclaration.getId());

               if (synchronizeWithDms)
               {
                  // adapt xsd's schemaLocation to schemaLocation of the documentType.
                  xsdSchema.setSchemaLocation(documentType.getSchemaLocation());

                  Set<XSDTypeDefinition> allVisitedTypes = new LinkedHashSet<XSDTypeDefinition>();
                  XPathFinder.findAllXPaths(xsdSchema, typeDeclaration.getId(), true,
                        allVisitedTypes);

                  log("");
                  for (Iterator<XSDTypeDefinition> iterator = allVisitedTypes.iterator(); iterator.hasNext();)
                  {
                     XSDTypeDefinition xsdTypeDefinition = iterator.next();
                     log("Visited: " + xsdTypeDefinition.getName() + " in "
                           + xsdTypeDefinition.getSchema().getTargetNamespace()
                           + " (schemaLocation: "
                           + xsdTypeDefinition.getSchema().getSchemaLocation() + ")");

                     // exclude "http://www.w3.org/2001/XMLSchema"
                     if ( !W3C_XML_SCHEMA.equals(xsdTypeDefinition.getSchema()
                           .getTargetNamespace()))
                     {
                        transitiveXsdDependencies.add(new Pair<XSDSchema, XSDSchema>(
                              xsdSchema, xsdTypeDefinition.getSchema()));
                     }
                  }

                  toSyncXsds.add(new DocumentTypeXsdSyncEntry(typeDeclaration.getId(),
                        documentType.getDocumentTypeId(),
                        documentType.getSchemaLocation(), xsdSchema));
               }

               documentTypes.add(documentType);
            }
         } // for

         // Process found transitive xsd dependencies; filter doublets; adapt schema
         // location.
         if ( !transitiveXsdDependencies.isEmpty())
         {
            log("");
            log("Found " + transitiveXsdDependencies.size()
                  + " transitive dependencies: ");

            filterDoublets(transitiveXsdDependencies);

            log("Remaining " + transitiveXsdDependencies.size()
                  + " transitive dependencies: ");

            for (Pair<XSDSchema, XSDSchema> pair : transitiveXsdDependencies)
            {
               XSDSchema baseXsdSchema = (XSDSchema) pair.getFirst();
               XSDSchema transitiveXsdSchema = (XSDSchema) pair.getSecond();
               log("Dependency: " + baseXsdSchema.getTargetNamespace() + " -> "
                     + transitiveXsdSchema.getTargetNamespace());

               String transitiveSchemaLocation = inferTransitiveSchemaLocation(
                     baseXsdSchema, transitiveXsdSchema, model.getModelOID());

               // adapt xsd's schemaLocation.
               transitiveXsdSchema.setSchemaLocation(transitiveSchemaLocation);

               toSyncXsds.add(new DocumentTypeXsdSyncEntry(null, null,
                     transitiveSchemaLocation, transitiveXsdSchema));
            }

            adaptReferences(toSyncXsds);
         }

         // Synchronize directly referenced and transitive xsd schemas to the repository.
         if ( !toSyncXsds.isEmpty())
         {
            log("Synchronizing Xsds with DMS: ");
            for (DocumentTypeXsdSyncEntry xsdSyncEntry : toSyncXsds)
            {
               syncWithDms(xsdSyncEntry);
            }
         }

      }
      catch (Throwable t)
      {
         throw new InternalException(t);
      }
      return documentTypes;
   }

   /**
    * for internal use
    *
    * Checks if the xsd referenced by DocumentType.getSchemaLocation exists in the
    * repository. If it is not found it is searched across all deployed models and resynced if it was found.
    *
    * If the documentType only contains a DocumentTypeId, all currently active
    * models document types matching the DocumentTypeId are retrieved and the
    * schemaLocation is set to the first match. If no match is found the document type is
    * considered invalid.
    *
    * @param documentInfo
    * @return returns <code>true</code> for valid document types
    */
   public static boolean isValidForDeployment(DocumentInfo documentInfo)
   {
      DocumentType documentType = documentInfo.getDocumentType();
      if (documentType == null)
      {
         return true;
      }

      if ( !isEmpty(documentType.getDocumentTypeId()))
      {
         if ( !isEmpty(documentType.getSchemaLocation()))
         {
            EmbeddedServiceFactory sf = EmbeddedServiceFactory.CURRENT_TX();
            Document document = sf.getDocumentManagementService().getDocument(getXsdDocumentPath(documentType.getSchemaLocation()));
            sf.close();

            if (document != null)
            {
               return true;
            }
            else
            {
               // try to sync missing document type from deployed models.
               ModelManager mm = ModelManagerFactory.getCurrent();
               for (Iterator<IModel> allModels = mm.getAllModels(); allModels.hasNext();)
               {
                  IModel model = allModels.next();
                  List<DocumentType> documentTypes = getDocumentTypes(model, false);

                  for (DocumentType existingDocumentType : documentTypes)
                  {
                     if (documentType.equals(existingDocumentType))
                     {
                        // Only re-sync document types for the found model.
                        getDocumentTypes(model, true);
                        return true;
                     }
                  }
               }
            }
         }
         else
         {
            // get all active models,
            // find schema by DocumentTypeId

            ModelManager mm = ModelManagerFactory.getCurrent();
            for (Iterator<IModel> allModels = mm.getAllModels(); allModels.hasNext();)
            {
               IModel model = allModels.next();

               if (mm.isActive(model))
               {
                  List<DocumentType> documentTypes = getDocumentTypes(model, false);

                  for (DocumentType existingDocumentType : documentTypes)
                  {
                     if (documentType.getDocumentTypeId().equals(
                           existingDocumentType.getDocumentTypeId()))
                     {
                        // Set the inferred document type.
                        documentInfo.setDocumentType(existingDocumentType);

                        // Check if the document type is correctly synced to the repository
                        EmbeddedServiceFactory sf = EmbeddedServiceFactory.CURRENT_TX();
                        Document document = sf.getDocumentManagementService().getDocument(getXsdDocumentPath(documentType.getSchemaLocation()));
                        sf.close();

                        if (document == null)
                        {
                           // Only re-sync document types for the found model.
                           getDocumentTypes(model, true);
                        }
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;

   }

   /**
    * For internal use only.
    *
    * @param data
    * @return document type of the data if it is available
    */
   public static DocumentType inferDocumentType(IData data)
   {
      DocumentType documentType = null;

      IModel model = (IModel) data.getModel();

      String typeDeclarationId = (String) data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
      IReference ref = data.getExternalReference();
      if (ref != null)
      {
         model = ref.getExternalPackage().getReferencedModel();
         typeDeclarationId = ref.getId();
      }

      if (typeDeclarationId != null)
      {
         ITypeDeclaration typeDeclaration = model.findTypeDeclaration(typeDeclarationId);

         XSDSchema xsdSchema = StructuredTypeRtUtils.getXSDSchema(model, typeDeclaration);

         documentType = getDocumentType(typeDeclaration.getXpdlType(), xsdSchema,
               model.getModelOID(), typeDeclaration.getId());
      }
      return documentType;
   }

   /**
    * For internal use only.
    *
    * @param data
    * @param document
    * @param dms
    * @return document type of the data if it is available
    *
    * @throws InvalidValueException if a incompatible document type is set on the document.
    */
   public static DocumentType inferDocumentTypeAndStoreDocument(IData data, Document document, DocumentManagementService dms)
   {
      DocumentType inferredDocumentType = DocumentTypeUtils.inferDocumentType(data);
      if (inferredDocumentType != null)
      {
         {
            DocumentType inputDocumentType = document.getDocumentType();
            if (inputDocumentType == null)
            {
               document.setDocumentType(inferredDocumentType);

               dms.updateDocument(document, false, null, null, false);

               if (trace.isInfoEnabled())
               {
                  trace.info("Inferred document type of document '"
                        + document.getName() + "' as '"
                        + inferredDocumentType.getDocumentTypeId()
                        + "' based on data '" + data.getId() + "'.");
               }
            }
            else if ( !inferredDocumentType.equals(inputDocumentType))
            {
               throw new InvalidValueException(
                     BpmRuntimeError.DMS_DOCUMENT_TYPE_INVALID.raise(inputDocumentType.getDocumentTypeId()));
            }
         }
      }
      return inferredDocumentType;
   }

   private static void adaptReferences(List<DocumentTypeXsdSyncEntry> toSyncXsds)
   {
      for (DocumentTypeXsdSyncEntry syncEntry : toSyncXsds)
      {
         XSDSchema xsdSchema = syncEntry.getXsdSchema();
         EList<XSDSchemaDirective> referencingDirectives = xsdSchema.getReferencingDirectives();

         if (referencingDirectives != null)
         {
            for (XSDSchemaDirective xsdSchemaDirective : referencingDirectives)
            {
               xsdSchemaDirective.setSchemaLocation(syncEntry.getSchemaLocation());
            }
         }
      }
   }

   private static void filterDoublets(
         Set<Pair<XSDSchema, XSDSchema>> transitiveXsdDependencies)
   {
      Set<XSDSchema> set = new LinkedHashSet<XSDSchema>();
      List<Pair<XSDSchema, XSDSchema>> toRemove = new LinkedList<Pair<XSDSchema, XSDSchema>>();
      for (Pair<XSDSchema, XSDSchema> pair : transitiveXsdDependencies)
      {
         XSDSchema schema = (XSDSchema) pair.getSecond();
         if ( !set.add(schema))
         {
            XSDSchema baseSchema = (XSDSchema) pair.getFirst();
            log("Removing doublet: " + baseSchema.getTargetNamespace() + " -> "
                  + schema.getTargetNamespace());
            toRemove.add(pair);
         }
      }
      transitiveXsdDependencies.removeAll(toRemove);
   }

   private static String inferTransitiveSchemaLocation(XSDSchema baseXsdSchema,
         XSDSchema transitiveXsdSchema, int modelOID)
   {
      if (baseXsdSchema.getSchemaLocation().startsWith(URN_INTERNAL_PREFIX))
      {
         return buildInternalSchemaLocation(modelOID,
               transitiveXsdSchema.getTargetNamespace());
      }
      else
      {
         return buildExternalSchemaLocation(transitiveXsdSchema.getSchemaLocation());
      }
   }

   private static DocumentType getDocumentType(IXpdlType iXpdlType, XSDSchema xsdSchema,
         int modelOID, String typeDeclarationId)
   {
      DocumentType documentType = null;

      if (iXpdlType instanceof ISchemaType)
      {
         // Internally defined type
         documentType = buildInternalSchemaDocumentType(xsdSchema, modelOID,
               typeDeclarationId);
      }
      else if (iXpdlType instanceof IExternalReference)
      {
         // ExternalReference
         IExternalReference externalReference = (IExternalReference) iXpdlType;
         documentType = buildExternalSchemaDocumentType(externalReference.getLocation(),
               externalReference.getXref());
      }
      return documentType;
   }

   private static DocumentType buildExternalSchemaDocumentType(
         String externalSchemaLocation, String xref)
   {
      String schemaLocation = buildExternalSchemaLocation(externalSchemaLocation);
      String documentTypeId = xref;
      return new DocumentType(documentTypeId, schemaLocation);
   }

   private static String buildExternalSchemaLocation(String externalSchemaLocation)
   {
      if (externalSchemaLocation.startsWith("classpath:"))
      {
         return externalSchemaLocation;
      }
      else
      {
         return "classpath:" + externalSchemaLocation;
      }
   }

   private static DocumentType buildInternalSchemaDocumentType(XSDSchema xsdSchema,
         int modelOID, String typeDeclarationId)
   {
      String targetNamespace = xsdSchema.getTargetNamespace();
      String schemaLocation = buildInternalSchemaLocation(modelOID, targetNamespace);
      String documentTypeId = new QName(targetNamespace, typeDeclarationId).toString();
      return new DocumentType(documentTypeId, schemaLocation);
   }

   private static String buildInternalSchemaLocation(int modelOID, String targetNamespace)
   {
      if (targetNamespace.startsWith(URN_INTERNAL_PREFIX))
      {
         return targetNamespace + "?" + modelOID;
      }
      else
      {
         return URN_INTERNAL_PREFIX + targetNamespace + "?" + modelOID;
      }
   }

   private static synchronized void syncWithDms(
         DocumentTypeXsdSyncEntry documentTypeXsdSyncEntry)
   {
      final String schemaLocation = documentTypeXsdSyncEntry.getSchemaLocation();
      final String documentTypeId = documentTypeXsdSyncEntry.getDocumentTypeId();
      // final String typeDeclarationId = documentTypeXsdSyncEntry.getTypeDeclarationId();

      XSDSchema xsdSchema = documentTypeXsdSyncEntry.getXsdSchema();

      final String xsdPath = getXsdFolderPath(schemaLocation);
      final String xsdDocumentName = getXsdDocumentName();

      log("");
      log("DocumentTypeId: " + documentTypeId);
      log("SchemaLocation: " + schemaLocation);
      log("Schema: " + xsdSchema.toString());
      log("XsdPath: " + xsdPath + "/" + xsdDocumentName);
      if (documentTypeId != null)
      {
         log("VersionedPath: " + getVersionedInfoPath(documentTypeId, schemaLocation));
         log("UnversionedPath: " + getUnversionedInfoPath(documentTypeId));
      }

      PropertyLayer layer = ParametersFacade.pushLayer(new HashMap<String, Serializable>());
      RepositoryProviderUtils.setAdminSessionFlag(true, layer);

      ServiceFactory sf = null;
      try
      {
         sf = EmbeddedServiceFactory.CURRENT_TX();
         DocumentManagementService dms = sf.getDocumentManagementService();

         Document existingDoc = dms.getDocument(xsdPath + "/" + xsdDocumentName);
         final String xsdString = XmlUtils.toString(xsdSchema.getDocument()
               .getDocumentElement());
         if (existingDoc == null)
         {
            final DocumentInfo doc = new DmsDocumentBean();

            doc.setName(xsdDocumentName);
            doc.setContentType("text/xml");

            DmsUtils.ensureFolderHierarchyExists(xsdPath, dms);
            Document createdDocument = dms.createDocument(xsdPath, doc,
                  xsdString.getBytes(), null);
            dms.versionDocument(createdDocument.getId(), null, null);

         }
         else
         {
            byte[] retrievedDocumentContent = dms.retrieveDocumentContent(existingDoc.getPath());
            org.w3c.dom.Document w3cSchemaDocument = XmlUtils.parseString(new String(
                  retrievedDocumentContent));
            XSDSchema existingXsdSchema = XSDFactory.eINSTANCE.createXSDSchema();
            existingXsdSchema.setElement(w3cSchemaDocument.getDocumentElement());

            if ( !schemaEquals(xsdSchema, existingXsdSchema))
            {
               dms.updateDocument(existingDoc, xsdString.getBytes(), (String) null, true,
                     (String) null, (String) null, false);
            }
         }
      }
      finally
      {
         if (layer != null)
         {
            ParametersFacade.popLayer();
         }
         if(sf != null)
         {
            sf.close();
         }
      }
   }

   private static boolean schemaEquals(XSDSchema xsdSchema, XSDSchema existingXsdSchema)
   {
      String xsdString = XmlUtils.toString(xsdSchema.getDocument().getDocumentElement());
      String existingXsdString = XmlUtils.toString(existingXsdSchema.getDocument()
            .getDocumentElement());

      if (xsdString == null || existingXsdString == null)
      {
         return false;
      }

      return xsdString.equals(existingXsdString);

   }

   // private static boolean transitiveSchemaEquals(XSDSchema xsdSchema,
   // XSDSchema existingXsdSchema, String id)
   // {
   // Set<TypedXPath> allXPaths = XPathFinder.findAllXPaths(xsdSchema, id, true);
   // Set<TypedXPath> allExistingXPaths = XPathFinder.findAllXPaths(existingXsdSchema,
   // id, true);
   //
   // return allXPaths.equals(allExistingXPaths);
   // }

   private static String encodeUrl(String string)
   {
      try
      {
         return URLEncoder.encode(string, "UTF-8");
      }
      catch (UnsupportedEncodingException e)
      {
         throw new RuntimeException(e);
      }
   }

   public static DocumentType getDocumentType(String typeDeclarationId,
         Model model)
   {
      DocumentType result = null;
      if (typeDeclarationId != null)
      {
         TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);
         if (typeDeclaration != null)
         {
            XpdlType xpdlType = typeDeclaration.getXpdlType();
            if (xpdlType instanceof ExternalReference)
            {
               String xref = ((ExternalReference) xpdlType).getXref();
               String location = ((ExternalReference) xpdlType).getLocation();

               result = buildExternalSchemaDocumentType(location, xref);
            }
            else if (xpdlType instanceof SchemaType)
            {
               XSDSchema xsdSchema = ((SchemaType) xpdlType).getSchema();

               result = buildInternalSchemaDocumentType(xsdSchema, model.getModelOID(),
                     typeDeclarationId);
            }
         }
      }
      return result;
   }

   public static boolean isDmsDocumentData(String dataTypeId)
   {
      return DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId)
            || DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataTypeId);
   }

   public static String getMetaDataTypeDeclarationId(Data data)
   {
      return (String) data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
   }

   /**
    * Creates DocumentType objects based on the Document and DocumentList data having a
    * TypeDeclaration assigned in the given model. <br>
    * It does not resolve the DocumentType of Document or DocumentList data referencing a
    * TypeDeclaration from a external model reference or externally referenced data.
    *
    * @param model
    *           The model to search for declared DocumenTypes.
    * @return A list of DocumentType that are declared within the specified model.
    *
    * @see #getDeclaredDocumentTypes(Model, Map)
    */
   public static List<DocumentType> getDeclaredDocumentTypes(Model model)
   {
      return getDeclaredDocumentTypes(model, null);
   }

   /**
    * Creates DocumentType objects based on the Document and DocumentList data having a
    * TypeDeclaration assigned in the given model. <br>
    *
    * @param model
    *           The model to search for declared DocumenTypes.
    * @param referenceModels
    *           A map of models by modelOID. It should contain all models which could be referenced by the specified model.
    * @return A list of DocumentType that are declared within the specified model.
    */
   public static List<DocumentType> getDeclaredDocumentTypes(Model model, Map<Integer, Model> referenceModels)
   {
      int currentModelOid = model.getModelOID();
      List<DocumentType> documentTypes = CollectionUtils.newList();

      Set<Pair<Integer,String>> typeDeclarationIdsByModel = new LinkedHashSet<Pair<Integer,String>>();

      @SuppressWarnings("unchecked")
      List<Data> allData = model.getAllData();
      for (Data data : allData)
      {
         String dataTypeId = data.getTypeId();
         if (isDmsDocumentData(dataTypeId))
         {
            String metaDataTypeDeclarationId = getMetaDataTypeDeclarationId(data);
            if (!isEmpty(metaDataTypeDeclarationId))
            {
               int modelOid = -1;
               String resolvedMetaDataTypeDeclarationId = null;
               Reference reference = data.getReference();
               if (reference != null)
               {
                  modelOid = reference.getModelOid();
                  resolvedMetaDataTypeDeclarationId = reference.getId();
               }
               else
               {
                  modelOid = data.getModelOID();
                  resolvedMetaDataTypeDeclarationId = metaDataTypeDeclarationId;
               }

               Pair<Integer, String> typeDeclarationIdByModel = new Pair<Integer, String> (modelOid, resolvedMetaDataTypeDeclarationId);
               typeDeclarationIdsByModel.add(typeDeclarationIdByModel);
            }
         }
      }

      for (Pair<Integer,String> typeDeclarationIdByModel : typeDeclarationIdsByModel)
      {
         DocumentType documentType = null;
         Integer dataModelOid = typeDeclarationIdByModel.getFirst();
         if (currentModelOid != dataModelOid)
         {
            // is externally defined data, look it up
            Model lookupModel = null;
            if (referenceModels != null)
            {
               lookupModel = referenceModels.get(dataModelOid);
            }

            if (lookupModel != null)
            {
               documentType = getDocumentType(
                     typeDeclarationIdByModel.getSecond(), lookupModel);
            }
            else
            {
               trace.warn("Lookup for DocumentType in referenced model failed. Model not found in specified referencedModels: "
                     + dataModelOid);
            }
         }
         else
         {
            documentType = getDocumentType(
                  typeDeclarationIdByModel.getSecond(), model);
         }

         if (documentType != null)
         {
            documentTypes.add(documentType);
         }
      }

      return documentTypes;
   }

   /**
    * Retrieves data which are of type Document or DocumentList and having a Reference to
    * an external TypeDefinition or data which is externally defined itself.
    *
    * @param model
    *           The model to search for data referencing DocumentTypes.
    * @return A List of data referencing DocumentTypes.
    */
   public static List<Data> getReferencedDocumentData(DeployedModel model)
   {
      List<Data> externalDocumentData = new ArrayList<Data>();

      @SuppressWarnings("unchecked")
      List<Data> allData = model.getAllData();
      for (Data data : allData)
      {
         String dataTypeId = data.getTypeId();
         if (isDmsDocumentData(dataTypeId))
         {
            String typeDeclarationId = getMetaDataTypeDeclarationId(data);

            if ((data.getReference() != null || data.getModelOID() != model.getModelOID()) && !isEmpty(typeDeclarationId))
            {
               externalDocumentData.add(data);
            }
         }
      }
      return externalDocumentData;
   }

   /**
    * Retrieves Document and DocumentList data from the given model which uses the type
    * definition defined by the given DocumentType.
    *
    * @param model
    *           The model containing the data and type definitions.
    * @param documentType
    *           The documentType to retrieve matching data for. If documentType is null
    *           data not having a documentType assigned are returned.
    * @return Data using the specified DocumentType.
    */
   public static List<Data> getDataUsingDocumentType(DeployedModel model,
         DocumentType documentType)
   {
      List<Data> retData = CollectionUtils.newList();
      @SuppressWarnings("unchecked")
      List<Data> allData = model.getAllData();
      for (Data data : allData)
      {
         String dataTypeId = data.getTypeId();
         if (isDmsDocumentData(dataTypeId))
         {
            String typeDeclarationId = getMetaDataTypeDeclarationId(data);

            // data not having document type assigned
            if (documentType == null)
            {
               if (isEmpty(typeDeclarationId))
               {
                  retData.add(data);
               }
            }
            else
            {
               // data having document type assigned
               if (data.getReference() == null
                     && !isEmpty(typeDeclarationId)
                     && typeDeclarationId.equals(QName.valueOf(
                           documentType.getDocumentTypeId()).getLocalPart()))
               {
                  retData.add(data);
               }
            }
         }
      }

      return retData;
   }

   /**
    * Retrieves the DocumentTypes used in the specified data.
    *
    * @param model
    * @param dataList
    * @return Set of {@link DocumentType DocumentType's} of the data's
    */
   public static Set<DocumentType> getDocumentTypesFromData(Model model,
         List<Data> dataList)
   {
      Set<DocumentType> result = new LinkedHashSet<DocumentType>();

      for (Data data : dataList)
      {
         DocumentType documentTypeFromData = getDocumentTypeFromData(model, data);
         if (documentTypeFromData != null)
         {
            result.add(documentTypeFromData);
         }
      }

      return result;
   }

   /**
    * Retrieves the DocumentType used in the specified data.
    *
    * @param model
    * @param data
    * @return {@link DocumentType} of the data
    */
   public static DocumentType getDocumentTypeFromData(Model model, Data data)
   {
      DocumentType result = null;
      int modelOid = model.getModelOID();

      if (data.getModelOID() == modelOid && null != model.getData(data.getId())
            && isDmsDocumentData(data.getTypeId()))
      {
         String typeDeclarationId = getMetaDataTypeDeclarationId(data);
         if (typeDeclarationId != null)
         {
            result = getDocumentType(typeDeclarationId, model);

         }
      }

      return result;
   }

   /**
    * This folder hosts version unspecific settings for all Document Types of a particular
    * Document Type ID
    *
    * @param documentTypeId
    * @return the path for the document type ID
    */
   public static String getUnversionedInfoPath(String documentTypeId)
   {
      return "/documentTypes/types/" + encodeUrl(documentTypeId);
   }

   /**
    * This folder hosts version specific settings for a particular Document Type with the
    * specified Document Type Schema Location
    *
    * @param documentTypeId
    * @param schemaLocation
    * @return the path for the document type ID
    */
   public static String getVersionedInfoPath(String documentTypeId, String schemaLocation)
   {
      return getXsdFolderPath(schemaLocation) + "/types/" + encodeUrl(documentTypeId);
   }

   /**
    * The document name all xsd schemas are stored and retrieved with.
    *
    * @return The XSD document name 
    */
   public static String getXsdDocumentName()
   {
      return SCHEMA_XSD;
   }

   /**
    * @param schemaLocation
    * @return path of the schema location and the XSD document in the repository
    */
   public static String getXsdDocumentPath(String schemaLocation)
   {
      return getXsdFolderPath(schemaLocation) + "/" + getXsdDocumentName();
   }

   /**
    * @param schemaLocation
    * @return path of the schema location in the repository
    */
   public static String getXsdFolderPath(String schemaLocation)
   {
      // Path is always in system repository.
      return RepositoryIdUtils.addRepositoryId("/documentTypes/schemas/"
            + encodeUrl(schemaLocation), RepositoryManager.SYSTEM_REPOSITORY_ID);
   }

}
