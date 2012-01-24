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

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IExternalReference;
import org.eclipse.stardust.engine.api.model.ISchemaType;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.model.IXpdlType;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.model.XpdlType;


public class TypeDeclarationDetails extends ModelElementDetails
      implements TypeDeclaration
{

   private static final long serialVersionUID = 248940915821866051L;
   
   private XpdlType xpdlType = null;

   public TypeDeclarationDetails(ITypeDeclaration typeDeclaration)
   {
      super(typeDeclaration);
      
      IXpdlType xpdlType = typeDeclaration.getXpdlType();
      if (xpdlType instanceof IExternalReference)
      {
         this.xpdlType = new ExternalReferenceDetails((IExternalReference)xpdlType, this);
      }
      else if (xpdlType instanceof ISchemaType)
      {
         this.xpdlType = new SchemaTypeDetails((ISchemaType)xpdlType);
      }
      else if (xpdlType != null)
      {
         // TODO: (fh) move the check somewhere else.
         throw new InternalException("Not supported IXpdlType implementation: "+xpdlType);
      }
   }
   
   public XpdlType getXpdlType()
   {
      return this.xpdlType;
   }
}
