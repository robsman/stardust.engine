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

import java.util.List;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.SingleRef;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.XPathAnnotations;


/**
 * @author ubirkemeyer
 */
public class DataPathBean extends IdentifiableElementBean
      implements IDataPath
{
   private static final long serialVersionUID = 2L;

   public static final String ACCESS_PATH_ATT = "Access Path";
   private String accessPath;

   private SingleRef data = new SingleRef(this, "Data");

   public static final String DIRECTION_ATT = "Direction";
   private Direction direction;

   public static final String DESCRIPTOR_ATT = "Descriptor";
   private boolean descriptor;
   private boolean keyDescriptor;

   DataPathBean()
   {
   }

   public DataPathBean(String id, String name, IData data, String path, Direction direction)
   {
      super(id, name);
      this.direction = direction;
      this.accessPath = path;
      this.data.setElement(data);
   }

   public Direction getDirection()
   {
      return direction;
   }

   public void setDirection(Direction value)
   {
      this.direction = value;
   }

   public IData getData()
   {
      return (IData) data.getElement();
   }

   public String getAccessPath()
   {
      return accessPath;
   }

   public void setAccessPath(String accessPath)
   {
      this.accessPath = accessPath;
   }

   public void checkConsistency(List inconsistencies)
   {
      checkId(inconsistencies);

      if (getId() != null)
      {
         // check for unique Id
         IDataPath dp = ((IProcessDefinition) getParent()).findDataPath(getId(), direction);
         if (dp != null && dp != this)
         {
            BpmValidationError error = BpmValidationError.DATA_DUPLICATE_ID_FOR_DATAPATH.raise(getName());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }
      }
      if (StringUtils.isEmpty(getName()))
      {
         BpmValidationError error = BpmValidationError.DATA_NO_NAME_SPECIFIED_FOR_DATAPATH.raise();
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
      }
      IData data = getData();
      if (data == null)
      {
         BpmValidationError error = BpmValidationError.DATA_NO_DATA_SPECIFIED_FOR_DATAPATH.raise();
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
      }
      else if (isKeyDescriptor())
      {
         // a key descriptor must be descriptor too.
         if (isDescriptor())
         {
            String dataTypeId = data.getType().getId();
            if (PredefinedConstants.PRIMITIVE_DATA.equals(dataTypeId))
            {
               // all primitive types can be key descriptors.
               return;
            }
            if (!PredefinedConstants.STRUCTURED_DATA.equals(dataTypeId))
            {
               BpmValidationError error = BpmValidationError.DATA_KEY_DESCRIPTORS_MUST_BE_PRIMITIVE_OR_STRUCTURED_TYPES.raise();
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }
            else if (accessPath == null || accessPath.length() == 0)
            {
               BpmValidationError error = BpmValidationError.DATA_STRUCTURED_KEY_DESCRIPTORS_MUST_HAVE_PRIMITIVE_TYPE.raise();
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }
            else
            {
               IXPathMap xPathMap = StructuredTypeRtUtils.getXPathMap(data);
               if (xPathMap == null)
               {
                  BpmValidationError error = BpmValidationError.DATA_NO_SCHEMA_FOUND_FOR_STRUCTURED_KEY_DESCRIPTOR.raise();
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
               }
               else if (StructuredDataXPathUtils.returnSinglePrimitiveType(accessPath, xPathMap) != BigData.STRING)
               {
                  BpmValidationError error = BpmValidationError.DATA_STRUCTURED_KEY_DESCRIPTORS_MUST_HAVE_PRIMITIVE_TYPE.raise();
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
               }
               else
               {
                  XPathAnnotations xPathAnnotations = StructuredDataXPathUtils.getXPathAnnotations(accessPath, xPathMap);
                  if (!xPathAnnotations.isIndexed() || !xPathAnnotations.isPersistent())
                  {
                     BpmValidationError error = BpmValidationError.DATA_STRUCTURED_KEY_DESCRIPTORS_MUST_BE_INDEXED_AND_PERSISTENT.raise();
                     inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
                  }
               }
            }
         }
         else
         {
            BpmValidationError error = BpmValidationError.DATA_DATAPATH_IS_NOT_A_DESCRIPTOR.raise();
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
         }
      }
   }

   public void setData(IData data)
   {
     this.data.setElement(data);
   }

   public boolean isDescriptor()
   {
      return descriptor;
   }

   public void setDescriptor(boolean descriptor)
   {
      this.descriptor = descriptor;
   }

   public boolean isKeyDescriptor()
   {
      return keyDescriptor;
   }

   public void setKeyDescriptor(boolean keyDescriptor)
   {
      this.keyDescriptor = keyDescriptor;
   }

   public String toString()
   {
      return "Data Path: '" + getId() + "'";
   }
}
