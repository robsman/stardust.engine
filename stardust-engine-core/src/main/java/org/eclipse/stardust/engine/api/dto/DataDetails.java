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

import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IReference;
import org.eclipse.stardust.engine.api.model.Reference;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;


/**
 * A read only view of the client side part of a data definition.
 * 
 * @version $Revision$
 */
public class DataDetails extends ModelElementDetails implements Data  
{
   private static final long serialVersionUID = 4597194992531713518L;
   private final String typeId;
   private final Reference reference;

   public DataDetails(IData data)
   {
      super(data);
      this.typeId = data.getType().getId();
      reference = DetailsFactory.create(data.getExternalReference(), IReference.class, ReferenceDetails.class);
   }

   public String getTypeId()
   {
      return typeId;
   }

   public Reference getReference()
   {
      return reference;
   }
}
