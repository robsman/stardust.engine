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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DataPath extends IdentifiableElement
{
   private String direction;
   private String dataID;
   private String dataPath;
   private boolean descriptor;

   public DataPath(String id, String name, String path, String direction, boolean descriptor, int oid, Model model)
   {
      super(id, name, null);
      this.direction = direction;
      setPath(path);
      this.descriptor = descriptor;
      model.register(this, oid);
   }

    public void setPath(String path)
   {

      int ind = path.indexOf('.');
      if (ind == -1)
      {
         dataID = path;
         dataPath = null;
      }
      else
      {
         dataID = path.substring(0, ind);
         dataPath = path.substring(ind + 1);
      }
   }

   public boolean isDescriptor()
   {
      return descriptor;
   }
   
   void setDescriptor(boolean isDescriptor)
   {
      this.descriptor = isDescriptor;
   }
   
   public String getDirection()
   {
      return direction;
   }

   public String getDataPath()
   {
      return dataPath;
   }

   public String getDataID()
   {
      return dataID;
   }
}
