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

import java.io.*;

import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource.Internal;
import org.eclipse.stardust.engine.api.model.ISchemaType;
import org.eclipse.stardust.engine.api.model.SchemaType;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDParser;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.xml.sax.InputSource;

public class SchemaTypeDetails implements SchemaType, Serializable
{
   private static final long serialVersionUID = -8298045794982231707L;

   private transient XSDSchema xsdSchema;
   private transient volatile boolean resolved = false;

   public SchemaTypeDetails(ISchemaType schemaType)
   {
      xsdSchema = schemaType.getSchema();
   }

   public boolean isResolved()
   {
      return resolved;
   }

   public void setResource(Internal resource)
   {
      if (xsdSchema != null)
      {
         ((InternalEObject) xsdSchema).eSetResource(resource, null);
      }
   }

   public synchronized XSDSchema getSchema()
   {
      if (xsdSchema != null && !resolved)
      {
         xsdSchema.reset();
         resolved = true;
      }
      return xsdSchema;
   }

   private void writeObject(ObjectOutputStream stream) throws IOException
   {
      // Call even if there is no default serializable fields.
      stream.defaultWriteObject();

      // Serialize the schema
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      XSDResourceImpl.serialize(bout, xsdSchema.getElement());
      bout.close();
      stream.writeObject(bout.toByteArray());
   }

   private void readObject(ObjectInputStream stream) throws IOException,
         ClassNotFoundException
   {
      // Call even if there is no default serializable fields.
      stream.defaultReadObject();

      // Restore the schema. This schema and it's content is unresolved.
      byte [] schemaBytes = (byte[])stream.readObject();
      XSDParser parser = new XSDParser(null);
      parser.parse(new InputSource(new ByteArrayInputStream(schemaBytes)));
      xsdSchema = parser.getSchema();
   }
}