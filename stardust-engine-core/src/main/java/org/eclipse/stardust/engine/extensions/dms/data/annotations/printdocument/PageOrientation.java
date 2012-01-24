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

public class PageOrientation implements Serializable
{
   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int pageNumber;

   private int rotation;

   public int getPageNumber()
   {
      return pageNumber;
   }

   public void setPageNumber(int pageNumber)
   {
      this.pageNumber = pageNumber;
   }

   public int getRotation()
   {
      return rotation;
   }

   public void setRotation(int rotation)
   {
      this.rotation = rotation;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof PageOrientation)
      {
         PageOrientation po = (PageOrientation) obj;
         if (pageNumber == po.getPageNumber())
         {
            return true;
         }
         else
         {
            return false;
         }
      }

      return super.equals(obj);
   }

}
