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
public class ParameterMapping extends ModelElement
{
   private String dataId;
   private String parameterId;
   private String parameterPath;

   public ParameterMapping(String data, String parameter, String parameterPath, int oid, Model model)
   {
      this.dataId = data;
      this.parameterId = parameter;
      this.parameterPath = parameterPath;
      model.register(this, oid);
   }

   public String getDataId()
   {
      return dataId;
   }

   public String getParameterId()
   {
      return parameterId;
   }

   public String getParameterPath()
   {
      return parameterPath;
   }
}
