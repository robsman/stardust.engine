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

import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.FormalParameter;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.IFormalParameter;


public class FormalParameterDetails implements FormalParameter
{
   private static final long serialVersionUID = 1L;
   
   private String id;

   private String name;

   private Direction direction;

   private String typeId;

   private Map<String, Object> attributes;

   private String dataId;
   
   public FormalParameterDetails(IFormalParameter parameter)
   {
      id = parameter.getId();
      name = parameter.getName();
      direction = parameter.getDirection();
      IData data = parameter.getData();
      if (data != null)
      {
         dataId = data.getId();
         IDataType type = (IDataType) data.getType();
         if (type != null)
         {
            typeId = type.getId();
         }
      }
      attributes = data == null ? Collections.<String, Object>emptyMap() : Collections.unmodifiableMap(data.getAllAttributes());
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public Direction getDirection()
   {
      return direction;
   }

   public String getTypeId()
   {
      return typeId;
   }
   
   public String getDataId()
   {
      return dataId;
   }

   public Map getAllAttributes()
   {
      return attributes;
   }

   public Object getAttribute(String name)
   {
      return attributes.get(name);
   }
}
