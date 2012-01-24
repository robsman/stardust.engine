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

import org.eclipse.stardust.engine.api.model.ISchemaType;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.xsd.XSDSchema;


/**
 * @author sauer
 * @version $Revision$
 */
public class SchemaTypeBean implements ISchemaType
{
   private final XSDSchema xsdSchema;
   private ITypeDeclaration parent;
   
   public SchemaTypeBean(XSDSchema xsdSchema)
   {
      this.xsdSchema = xsdSchema;
   }

   public XSDSchema getSchema()
   {
      return xsdSchema;
   }

   public ITypeDeclaration getParent()
   {
      return parent;
   }

   public void setParent(ITypeDeclaration parent)
   {
      this.parent = parent;
   }
}
