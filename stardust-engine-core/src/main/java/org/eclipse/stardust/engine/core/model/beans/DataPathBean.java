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

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.model.utils.SingleRef;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.*;
import org.eclipse.stardust.engine.core.struct.spi.StructDataTransformerKey;

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
   
   private Pattern pattern = Pattern.compile("(\\%\\{[^{}]+\\})");

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
         if (!Direction.IN.isCompatibleWith(direction))
         {
            BpmValidationError error = BpmValidationError.DATA_NO_DATA_SPECIFIED_FOR_DATAPATH.raise();
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }
         if (isDescriptor()) 
         {
            String type = this.getStringAttribute("type");
            if (type != null) 
            {
               validateDescriptor(getAccessPath(), inconsistencies);   
            }            
         }
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
      else
      {
         String dataTypeId = data.getType().getId();
         if (PredefinedConstants.STRUCTURED_DATA.equals(dataTypeId))
         {
            IXPathMap xPathMap = StructuredTypeRtUtils.getXPathMap(data);
            if (xPathMap != null)
            {
               if (!StringUtils.isEmpty(accessPath))
               {
                  String xPathWithoutIndexes = StructuredDataXPathUtils.getXPathWithoutIndexes(StructDataTransformerKey.stripTransformation(accessPath));
                  TypedXPath xPath = xPathMap.getXPath(xPathWithoutIndexes);
                  if (xPath == null)
                  {
                     BpmValidationError error = BpmValidationError.ACCESSPATH_INVALID_FOR_DATAPATH.raise(accessPath, getId());
                     inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
                  }
               }
            }
         }
      }
   }
   
   private String validateDescriptor(String value, List inconsistencies)
   {      
      if (!getDirection().equals(Direction.IN))
      {
         BpmValidationError error = BpmValidationError.COMPOSITE_LINK_DESCRIPTOR_HAS_TO_BE_IN_DATAPATH
               .raise(this.getId());
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
      }
      if (value == null)
      {
         BpmValidationError error = BpmValidationError.COMPOSITE_LINK_DESCRIPTOR_NO_DATAPATH
               .raise(this.getId());
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
         return null;
      }
  
      String id = null;
      String newValue = value;
      Matcher matcher = pattern.matcher(newValue);
      while (matcher.find())
      {
         if ((matcher.start() == 0) || ((matcher.start() > 0)
               && (newValue.charAt(matcher.start() - 1) != '\\')))
         {
            String ref = newValue.substring(matcher.start(), matcher.end());
            ref = ref.trim();
            id = ref;
            id = id.replace("%{", "");
            id = id.replace("}", "");
            if (id.equals(this.getId()))
            {
               BpmValidationError error = BpmValidationError.REFERENCED_DATAPTH_IS_A_CIRCULAR_DEPENDENCY
                     .raise(this.getId());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
               return value;
            }

            IProcessDefinition process = (IProcessDefinition) this.getParent();
            IDataPath refDataPath = findDataPath(process, id);
            if (refDataPath == null)
            {
               BpmValidationError error = BpmValidationError.REFERENCED_DESCRIPTOR_DOES_NOT_EXIST
                     .raise(this.getId(), ref);
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
               return value;
            }
            String refAccessPath = refDataPath.getAccessPath();
            if (refAccessPath == null)
            {
               BpmValidationError error = BpmValidationError.REFERENCED_DESCRIPTOR_NO_DATAPATH
                     .raise(ref);
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
               refAccessPath = "";
            }           
            value = ModelUtils.replaceDescriptorVariable("%{" + id + "}", value, refAccessPath);
         }          
         else
         {
            if (newValue.charAt(matcher.start() - 1) == '\\')
            {
               value = value.replaceFirst("\\\\\\%\\{", "*0*0*0*0*");
            }
         }
      }
      if (value.indexOf("%{") > -1)
      {
         return validateDescriptor(value, inconsistencies);
      }
      return value;
   }
   
   private IDataPath findDataPath(IProcessDefinition process, String ref)
   {
      for (Iterator<IDataPath> i = process.getDataPaths().iterator(); i.hasNext();)
      {
         IDataPath dataPath = i.next();
         if (dataPath.getId().equals(ref))
         {
            return dataPath;
         }
      }
      return null;
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
