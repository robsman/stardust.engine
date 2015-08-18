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

import org.eclipse.xsd.XSDSchema;

public class DocumentTypeXsdSyncEntry
{

   private final String typeDeclarationId;

   private final String documentTypeId;

   private final String schemaLocation;

   private final XSDSchema xsdSchema;

   public DocumentTypeXsdSyncEntry(String typeDeclarationId, String documentTypeId,
         String schemaLocation, XSDSchema xsdSchema)
   {
      this.typeDeclarationId = typeDeclarationId;
      this.documentTypeId = documentTypeId;
      this.schemaLocation = schemaLocation;
      this.xsdSchema = xsdSchema;

   }

   public String getTypeDeclarationId()
   {
      return typeDeclarationId;
   }

   public String getDocumentTypeId()
   {
      return documentTypeId;
   }

   public String getSchemaLocation()
   {
      return schemaLocation;
   }

   public XSDSchema getXsdSchema()
   {
      return xsdSchema;
   }

}
