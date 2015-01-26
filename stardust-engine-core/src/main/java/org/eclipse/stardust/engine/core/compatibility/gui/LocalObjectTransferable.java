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
package org.eclipse.stardust.engine.core.compatibility.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * Class to transport object references of a single VM via drag and drop.
 */
public class LocalObjectTransferable implements Transferable
{
   private Object object;
   private DataFlavor flavor;

   public LocalObjectTransferable(Object object)
   {
      this.object = object;
      flavor = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType + "; class=" + object.getClass().getName(),
            "Workflow Element");
   }

   /**
    * Returns an object which represents the data to be transferred.
    **/
   public Object getTransferData(DataFlavor flavor)
   {
      if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType) ||
          flavor.match(this.flavor))
      {
         return object;
      }
      else if (flavor.equals(DataFlavor.stringFlavor))
      {
         return object.toString();
      }

      return null;
   }

   /**
    * Returns an array of DataFlavor objects indicating the flavors the data
    * can be provided in.
    */
   public DataFlavor[] getTransferDataFlavors()
   {
      return new DataFlavor[]{flavor, DataFlavor.stringFlavor};
   }

   /**
    * Returns whether or not the specified data flavor is supported.
    */
   public boolean isDataFlavorSupported(DataFlavor flavor)
   {
      if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType) ||
            flavor.equals(DataFlavor.stringFlavor) || flavor.match(this.flavor))
      {
         return true;
      }

      return false;
   }
}
