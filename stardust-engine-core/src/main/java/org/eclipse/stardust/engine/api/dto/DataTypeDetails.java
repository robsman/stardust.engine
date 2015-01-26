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

import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;

/**
 * Details class for a data type
 * @version $Revision$
 */
public class DataTypeDetails
{
   private String id;

   private String dataFilterExtensionClass;

   private String dataTypeLoaderClass;

   DataTypeDetails(IDataType dataType)
   {
      this.dataFilterExtensionClass = (String) dataType.getAttribute(PredefinedConstants.DATA_FILTER_EXTENSION_ATT);
      if (this.dataFilterExtensionClass == null)
      {
         // fallback to default implementation if the attribute is not set
         this.dataFilterExtensionClass = org.eclipse.stardust.engine.core.extensions.data.DefaultDataFilterExtension.class.getName();
      }

      this.dataTypeLoaderClass = (String) dataType.getAttribute(PredefinedConstants.DATA_LOADER_ATT);
      if (this.dataTypeLoaderClass == null)
      {
         // fallback to default implementation if the attribute is not set
         this.dataTypeLoaderClass = org.eclipse.stardust.engine.core.extensions.data.DefaultDataTypeLoader.class.getName();
      }
   }

   /**
    * @return class implementing the filter extension for this data type
    */
   public String getDataFilterExtensionClass()
   {
      return this.dataFilterExtensionClass;
   }

   /**
    * @return class implementing the data type loading extension for this data type
    */
   public String getDataTypeLoaderClass()
   {
      return this.dataTypeLoaderClass;
   }

   public boolean equals(Object object)
   {
      if (object instanceof DataTypeDetails)
      {
         DataTypeDetails target = (DataTypeDetails) object;

         if (target.id.equals(id))
         {
            return true;
         }
      }
      return false;
   }

}
