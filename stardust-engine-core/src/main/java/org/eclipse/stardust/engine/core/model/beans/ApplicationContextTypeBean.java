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

import org.eclipse.stardust.engine.api.model.IApplicationContextType;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;


public class ApplicationContextTypeBean extends IdentifiableElementBean implements IApplicationContextType
{
   static final String HAS_MAPPING_ID_ATT = "Has Mapping Id";
   private boolean hasMappingId;

   static final String HAS_APPLICATION_PATH_ATT = "Has Application Path";
   private boolean hasApplicationPath;

   public ApplicationContextTypeBean()
   {
   }

   public ApplicationContextTypeBean(String id, String name, boolean predefined,
         boolean hasMappingId, boolean hasApplicationPath)
   {
      super(id, name);
      setPredefined(predefined);
      this.hasMappingId = hasMappingId;
      this.hasApplicationPath = hasApplicationPath;
   }

   public boolean hasMappingId()
   {
      return hasMappingId;
   }

   public boolean hasApplicationPath()
   {
      return hasApplicationPath;
   }

   public void setHasMappingId(boolean hasMappingId)
   {
      this.hasMappingId = hasMappingId;
   }

   public void setHasApplicationPath(boolean hasApplicationPath)
   {
      this.hasApplicationPath = hasApplicationPath;
   }

   public String toString()
   {
      return "ApplicationContextType: " + getName();
   }
}
