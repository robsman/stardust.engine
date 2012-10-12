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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.AnnotationUtils;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.DocumentAnnotations;



/**
 * @author rsauer
 * @version $Revision$
 */
public class DmsDocumentBean extends DmsResourceBean
      implements Document, DocumentInfo, Serializable
// , org.eclipse.stardust.engine.core.compatibility.extensions.dms.Document
{

   private static final long serialVersionUID = 1L;

   private DocumentAnnotations documentAnnotations;

   public DmsDocumentBean()
   {
      super(CollectionUtils.newHashMap());
   }

   public DmsDocumentBean(Map legoDocument)
   {
      super(legoDocument);

      Object map = vfsResource().get(
            AuditTrailUtils.FILE_ANNOTATIONS);
      if (map instanceof Map)
      {
    	  documentAnnotations = AnnotationUtils.fromMap((Map) map);
      }
   }

   public long getSize()
   {
      return (null != vfsResource().get(AuditTrailUtils.FILE_SIZE))
            ? ((Long) vfsResource().get(AuditTrailUtils.FILE_SIZE)).longValue()
            : 0l;
   }

   public String getContentType()
   {
      return (String) vfsResource().get(AuditTrailUtils.FILE_CONTENT_TYPE);
   }

   public void setContentType(String contentType)
   {
      vfsResource().put(AuditTrailUtils.FILE_CONTENT_TYPE, contentType);
   }

   public String getRevisionId()
   {
      return (String) vfsResource().get(AuditTrailUtils.FILE_REVISION_ID);
   }

   public String getRevisionName()
   {
      return (String) vfsResource().get(AuditTrailUtils.FILE_REVISION_NAME);
   }

   public String getRevisionComment()
   {
      return (String) vfsResource().get(AuditTrailUtils.FILE_REVISION_COMMENT);
   }

   public List getVersionLabels()
   {
      return (List) vfsResource().get(AuditTrailUtils.FILE_VERSION_LABELS);
   }

   /*
    * TODO not yet implemented
    */
   public String getLockOwner()
   {
      return (String) vfsResource().get(AuditTrailUtils.FILE_LOCK_OWNER);
   }

   public String getEncoding()
   {
      return (String) vfsResource().get(AuditTrailUtils.FILE_ENCODING);
   }

   public DocumentAnnotations getDocumentAnnotations()
   {
      return documentAnnotations;
   }

   public void setDocumentAnnotations(DocumentAnnotations documentAnnotations)
   {
      vfsResource().put(AuditTrailUtils.FILE_ANNOTATIONS,
            AnnotationUtils.toMap(documentAnnotations));
      this.documentAnnotations = documentAnnotations;
   }

   public void setDocumentType(DocumentType documentType)
   {
      String docTypeId = null;
      String schemaLocation = null;

      if (documentType != null)
      {
         docTypeId = documentType.getDocumentTypeId();
         schemaLocation = documentType.getSchemaLocation();

         // DocumentType mapping
         Map<String, String> docType = new HashMap<String, String>();
         docType.put(AuditTrailUtils.DOC_DOCUMENT_TYPE_ID, docTypeId);
         docType.put(AuditTrailUtils.DOC_DOCUMENT_TYPE_SCHEMA_LOCATION, schemaLocation);
         vfsResource().put(AuditTrailUtils.DOC_DOCUMENT_TYPE_MAP, docType);
      }
      else
      {
         vfsResource().put(AuditTrailUtils.DOC_DOCUMENT_TYPE_MAP, null);
      }
   }

   public DocumentType getDocumentType()
   {
      Object rawDocType = vfsResource().get(AuditTrailUtils.DOC_DOCUMENT_TYPE_MAP);
      Map<String,String> docType = null;
      if (rawDocType instanceof Map)
      {
         docType = (Map<String, String>) rawDocType;
      }

      if (docType != null)
      {
         String docTypeId = docType.get(AuditTrailUtils.DOC_DOCUMENT_TYPE_ID);
         String schemaLocation = docType.get(AuditTrailUtils.DOC_DOCUMENT_TYPE_SCHEMA_LOCATION);
         if (docTypeId != null || schemaLocation != null)
         {
            return new DocumentType(docTypeId, schemaLocation);
         }
      }
      return null;
   }

   @Override
   public String toString()
   {
      return getName();
   }
}