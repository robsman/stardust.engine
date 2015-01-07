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

import org.eclipse.stardust.engine.api.runtime.RuntimeObject;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;


/**
 * @author rsauer
 * @version $Revision$
 */
public class RuntimeObjectDetails implements RuntimeObject
{
   private static final long serialVersionUID = 5863534406760125921L;
   private final long oid;

   private final int modelOID;
   private final int modelElementOID;
   private final String modelElementID;

   protected RuntimeObjectDetails(IdentifiablePersistent runtimeObject)
   {
      this(runtimeObject, null);
   }

   protected RuntimeObjectDetails(IdentifiablePersistent runtimeObject, ModelElement definition)
   {
      this.oid = runtimeObject.getOID();

      if ((null != definition) && (null != definition.getModel()))
      {
         this.modelOID = definition.getModel().getModelOID();
         this.modelElementOID = definition.getElementOID();
      }
      else
      {
         this.modelOID = 0;
         this.modelElementOID = 0;
      }

      this.modelElementID = null;
   }

   protected RuntimeObjectDetails(IdentifiablePersistent runtimeObject,
         IdentifiableElement definition)
   {
      this.oid = runtimeObject.getOID();

      if ((null != definition) && (null != definition.getModel()))
      {
         this.modelOID = definition.getModel().getModelOID();
         this.modelElementOID = definition.getElementOID();
         this.modelElementID = definition.getId();
      }
      else
      {
         this.modelOID = 0;
         this.modelElementOID = 0;
         this.modelElementID = null;
      }
   }

   public long getOID()
   {
      return oid;
   }

   public int getModelOID()
   {
      return modelOID;
   }

   public int getModelElementOID()
   {
      return modelElementOID;
   }

   public String getModelElementID()
   {
      return modelElementID;
   }

   @Override
   public int hashCode()
   {
      final int PRIME = 31;
      int result = 1;
      result = PRIME * result + Long.valueOf(getOID()).hashCode();
      result = PRIME * result + Integer.valueOf(getModelOID()).hashCode();
      return result;
   }

   @Override
   public boolean equals(Object other)
   {
      if (other == null)
      {
         return false;
      }
      if (! (other instanceof RuntimeObjectDetails))
      {
         return false;
      };
      RuntimeObjectDetails otherElement = (RuntimeObjectDetails) other;
      if (otherElement.getOID() == getOID()
          && otherElement.getModelOID() == getModelOID()
        && otherElement.getModelElementOID() == otherElement.getModelElementOID())
      {
         return true;
      }
      return false;
   }

}
