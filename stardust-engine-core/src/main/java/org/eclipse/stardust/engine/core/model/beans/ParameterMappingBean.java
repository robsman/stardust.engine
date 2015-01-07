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

import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IParameterMapping;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.core.model.utils.ConnectionBean;


/**
 * @author rsauer
 * @version $Revision$
 */
public class ParameterMappingBean extends ConnectionBean
      implements IParameterMapping
{
   private static final long serialVersionUID = 2L;

   private String dataPath;

   private String parameterId;

   private String parameterPath;

   ParameterMappingBean() {}

   public ParameterMappingBean(ITrigger trigger, IData data, String dataPath, String parameter, String parameterPath)
   {
      super(data, trigger);
      setParent(trigger);
      this.dataPath = dataPath;
      this.parameterId = parameter;
      this.parameterPath = parameterPath;
   }

   public IData getData()
   {
      return (IData) getFirst();
   }

   public String getDataPath()
   {
      return dataPath;
   }

   public String getParameterId()
   {
      return parameterId;
   }

   public String getParameterPath()
   {
      return parameterPath;
   }

   public ITrigger getTrigger()
   {
      return (ITrigger) getParent();
   }

   public void setData(IData data)
   {
      setFirst(data);
   }

   public void setParameterId(String parameterId)
   {
      this.parameterId = parameterId;
   }

   public String getId()
   {
      return getData().getId();
   }

   public String getName()
   {
      return getData() == null ? null : getData().getId();
   }

   public String toString()
   {
      return "Parameter Mapping: " + getName();
   }
}
