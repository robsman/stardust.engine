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

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;

/**
 * Describes the meta data structure of a document which can be set by using
 * {@link Document#setProperties(java.util.Map) and retrieved by using
 * {@link Document#getProperties()}<br>
 * The prefered way of retrieving DocumentTypes should be by usage of
 * {@link DocumentTypeUtils}
 * 
 */
public class DocumentType implements Serializable
{

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String documentTypeId;

   private String schemaLocation;

   public DocumentType(String documentTypeId, String schemaLocation)
   {
      super();
      this.documentTypeId = documentTypeId;
      this.schemaLocation = schemaLocation;
   }

   /**
    * 
    * @return String - the qualified Id of a particular document type in the repository
    */
   public String getDocumentTypeId()
   {
      return documentTypeId;
   }

   /**
    * sets the unique qualified Id of particular document type within the repository
    * 
    * @param documentTypeId
    */
   public void setDocumentTypeId(String documentTypeId)
   {
      this.documentTypeId = documentTypeId;
   }

   /**
    * 
    * @return String - the identifier for the schema location in the repository
    * @see DocumentManagementService#getSchemaDefinition(String)
    */
   public String getSchemaLocation()
   {
      return schemaLocation;
   }

   /**
    * Sets the identifier for exactly one XSD schema location in the repository
    * 
    * @param schemaLocation
    * @see DocumentManagementService#getSchemaDefinition(String)
    */
   public void setSchemaLocation(String schemaLocation)
   {
      this.schemaLocation = schemaLocation;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof DocumentType)
      {
         boolean equal = true;
         DocumentType docType = (DocumentType) obj;

         String docTypeId = docType.getDocumentTypeId();
         String schemaLoc = docType.getSchemaLocation();

         if (docTypeId != null && documentTypeId == null || docTypeId == null
               && documentTypeId != null)
         {
            equal = false;
         }
         if (schemaLoc != null && schemaLocation == null || schemaLoc == null
               && schemaLocation != null)
         {
            equal = false;
         }
         if (equal && docTypeId != null && !docTypeId.equals(documentTypeId))
         {
            equal = false;
         }
         if (equal && schemaLoc != null && !schemaLoc.equals(schemaLocation))
         {
            equal = false;
         }
         return equal;
      }

      return super.equals(obj);
   }

}
