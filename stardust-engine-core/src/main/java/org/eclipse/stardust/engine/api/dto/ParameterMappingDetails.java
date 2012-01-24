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

import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.IParameterMapping;
import org.eclipse.stardust.engine.api.model.ParameterMapping;

/**
 * @author rsauer
 * @version $Revision$
 */
public class ParameterMappingDetails
      extends ModelElementDetails implements ParameterMapping
{
   private static final long serialVersionUID = 2L;
   
   private String dataId;
   private String dataPath;
   private AccessPoint parameter;
   private String parameterPath;

   ParameterMappingDetails(IParameterMapping mapping)
   {
      super(mapping, mapping.getData().getId(), mapping.getData().getId(), mapping.getDescription());
      dataId = mapping.getData().getId();
      dataPath = mapping.getDataPath();
      org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint accessPoint =
            mapping.getTrigger().findAccessPoint(mapping.getParameterId());
      if ( accessPoint != null)
      {
         parameter = new AccessPointDetails(accessPoint);
      }
      parameterPath = mapping.getParameterPath();
   }

   public String getDataId()
   {
      return dataId;
   }

   public String getDataPath()
   {
      return dataPath;
   }

   public AccessPoint getParameter()
   {
      return parameter;
   }

   public String getParameterPath()
   {
      return parameterPath;
   }
}
