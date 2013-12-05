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
package org.eclipse.stardust.engine.core.model.beans;

import java.io.IOException;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IExternalReference;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.ISchemaType;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.model.IXpdlType;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.xsd.XSDSchema;
import org.w3c.dom.Element;


/**
 * @author sauer
 * @version $Revision$
 */
public class ExternalReferenceBean implements IExternalReference
{
   private static final Logger trace = LogManager.getLogger(ExternalReferenceBean.class);

   private final String location;
   
   private final String namespace;
   
   private final String xRef;
   
   private Element externalAnnotations;
   
   private String alternateLocation;

   /**
    * The cached value of the '{@link #getSchema() <em>Schema</em>}' reference.
    */
   private XSDSchema schema = null;

   private ITypeDeclaration parent;

   public ExternalReferenceBean(String location, String namespace, String xRef, Element externalAnnotations)
   {
      this.location = location;
      this.namespace = namespace;
      this.xRef = xRef;
      
      this.externalAnnotations = externalAnnotations;
   }

   public String getLocation()
   {
      return location;
   }

   public String getNamespace()
   {
      return namespace;
   }

   public String getXref()
   {
      return xRef;
   }

   public ITypeDeclaration getParent()
   {
      return parent;
   }

   public void setParent(ITypeDeclaration parent)
   {
      this.parent = parent;
      schema = null;
      alternateLocation = parent == null ? null
            : parent.getStringAttribute(StructuredDataConstants.RESOURCE_MAPPING_LOCAL_FILE);
   }

   public Element getExternalAnnotations()
   {
      return externalAnnotations;
   }

   /**
    * Simple caching mechanism to speed up the external schema retrieval
    * and to prohibit the existence of multiple instances of the same schema 
    * @generated NOT
    */
   public XSDSchema getSchema(IModel model) {
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

   private XSDSchema getInternalSchema(IModel model)
   {
      String typeId = location.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length());
      if (typeId.length() > 0)
      {
         ITypeDeclaration internalType = model.findTypeDeclaration(typeId);
         IXpdlType type = internalType.getXpdlType();
         if (type instanceof ISchemaType)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Loaded schema from TypeDeclaration: " + typeId);
            }
            return ((ISchemaType) type).getSchema();
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
         Map typeDeclarationAttributes = getParent().getAllAttributes();
         String namespaceURI = StructuredTypeRtUtils.parseNamespaceURI(xRef);
         String url = alternateLocation == null ? location : alternateLocation;
         try
         {
            
            schema = StructuredTypeRtUtils.getSchema(url, namespaceURI, typeDeclarationAttributes);
            if (trace.isDebugEnabled())
            {
               trace.debug("Loaded schema from location: " + url);
            }
         }
         catch (IOException e)
         {
            if (!url.equals(location))
            {
               // try to load from external url
               try
               {
                  schema = StructuredTypeRtUtils.getSchema(location, namespaceURI, typeDeclarationAttributes);
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Loaded schema from location: " + location);
                  }
               }
               catch (IOException e1)
               {
                  // TODO handle
               }
            }
         }
         StructuredTypeRtUtils.patchAnnotations(schema, getExternalAnnotations());
      }
      return schema;
   }
}
