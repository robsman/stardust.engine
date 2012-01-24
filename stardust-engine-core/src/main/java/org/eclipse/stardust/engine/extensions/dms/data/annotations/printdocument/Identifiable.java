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
package org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument;

import java.io.Serializable;

public abstract class Identifiable implements Serializable
{
   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String id;

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof Identifiable)
      {
         Identifiable iobj = (Identifiable) obj;
         if (iobj.getId() == null && id == null)
         {
            return true;
         }
         else if (iobj.getId().equals(id))
         {
            return true;
         }
         return false;
      }
      return super.equals(obj);
   }

}
