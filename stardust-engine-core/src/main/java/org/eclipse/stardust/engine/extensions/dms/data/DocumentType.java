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

   public String getDocumentTypeId()
   {
      return documentTypeId;
   }

   public void setDocumentTypeId(String documentTypeId)
   {
      this.documentTypeId = documentTypeId;
   }

   public String getSchemaLocation()
   {
      return schemaLocation;
   }

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

         if (docTypeId != null && documentTypeId == null || docTypeId == null && documentTypeId != null)
         {
            equal = false;
         }
         if (schemaLoc != null && schemaLocation == null || schemaLoc == null && schemaLocation != null)
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
