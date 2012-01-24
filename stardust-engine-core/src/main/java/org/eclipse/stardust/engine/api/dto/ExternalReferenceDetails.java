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
package org.eclipse.stardust.engine.api.dto;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.stardust.engine.api.model.ExternalReference;
import org.eclipse.stardust.engine.api.model.IExternalReference;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.SchemaType;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.model.XpdlType;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.xsd.XSDSchema;


public class ExternalReferenceDetails implements ExternalReference, Serializable
{
   private static final long serialVersionUID = 1L;
   
   private String namespace;
   private String location;
   private String xref;
   private String alternateLocation;

   /**
    * The cached value of the '{@link #getSchema() <em>Schema</em>}' reference.
    */
   private transient XSDSchema schema = null;

   public ExternalReferenceDetails(IExternalReference externalReference, TypeDeclaration parent)
   {
      this.xref = externalReference.getXref();
      this.location = externalReference.getLocation();
      this.namespace = externalReference.getNamespace();
      this.alternateLocation = (String) parent.getAttribute(StructuredDataConstants.RESOURCE_MAPPING_LOCAL_FILE);
   }

   public String getNamespace()
   {
      return namespace;
   }

   public String getLocation()
   {
      return location;
   }

   public String getXref()
   {
      return xref;
   }

   /**
    * Simple caching mechanism to speed up the external schema retrieval
    * and to prohibit the existence of multiple instances of the same schema 
    * @generated NOT
    */
   public XSDSchema getSchema(Model model) {
      if (location != null)
      {
         if (location.startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
         {
            return getInternalSchema(model);
         }
         else
         {
            return getExternalSchema();
         }
      }
      return null;
   }

   private XSDSchema getInternalSchema(Model model)
   {
      String typeId = location.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length());
      if (typeId.length() > 0)
      {
         TypeDeclaration internalType = model.getTypeDeclaration(typeId);
         XpdlType type = internalType.getXpdlType();
         if (type instanceof SchemaType)
         {
            return ((SchemaType) type).getSchema();
         }
      }
      return null;
   }
   
   /**
    * We must synchronize that method entirely to ensure cache consistency.
    */
   private synchronized XSDSchema getExternalSchema()
   {
      if (schema == null)
      {
         String namespaceURI = StructuredTypeRtUtils.parseNamespaceURI(xref);
         String url = alternateLocation == null ? location : alternateLocation;
         try
         {
            schema = StructuredTypeRtUtils.getSchema(url, namespaceURI);
         }
         catch (IOException e)
         {
            if (!url.equals(location))
            {
               // try to load from external url
               try
               {
                  schema = StructuredTypeRtUtils.getSchema(location, namespaceURI);
               }
               catch (IOException e1)
               {
                  // TODO handle
               }
            }
         }
      }
      return schema;
   }
}
