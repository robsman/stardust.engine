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

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.spi.extensions.model.ExtendedDataValidator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;


/**
 * Wraps meta information for a data accessPath as defined in the model.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DataPathDetails extends ModelElementDetails implements DataPath
{
   private static final long serialVersionUID = -1198825507540508071L;

   private static final Logger trace = LogManager.getLogger(DataPathDetails.class);

   private String type;
   private Direction direction;
   private boolean descriptor;
   private boolean keyDescriptor;
   private String accessPath;
   private String data;
   
   private final String processDefinitionId;

   DataPathDetails(CaseDescriptorRef ref)
   {
      super(ref.getModelOID(), ref.getElementOID(), ref.getId(), ref.getName(), ref.getNamespace(), ref.getDescription());
      type = ref.getMappedType();
      direction = ref.getDirection();
      descriptor = true;
      keyDescriptor = ref.isKeyDescriptor();
      data = ref.getDataId();
      accessPath = ref.getAccessPath();
      processDefinitionId = ref.getProcessDefinitionId();
   }

   DataPathDetails(IDataPath path)
   {
      super(path);

      try
      {
         IDataType dataType = (IDataType) path.getData().getType();
         String validatorClass = dataType
               .getStringAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT);
         if ( !StringUtils.isEmpty(validatorClass))
         {
            ExtendedDataValidator validator = SpiUtils
                  .createExtendedDataValidator(validatorClass);
            type = validator.getBridgeObject(
                  path.getData(),
                  path.getAccessPath(),
                  Direction.OUT.equals(path.getDirection()) ? Direction.IN
                        : Direction.OUT, null).getEndClass().getName();
         }
         else
         {
            type = Object.class.getName();
         }
      }
      catch (Exception e)
      {
         trace.warn("", e);
         this.type = null;
      }

      direction = path.getDirection();
      descriptor = path.isDescriptor();
      keyDescriptor = path.isKeyDescriptor();
      accessPath = path.getAccessPath();
      data = path.getData().getId();
      
      ModelElement parent = path.getParent();
      this.processDefinitionId = (parent instanceof IProcessDefinition)
            ? ((IProcessDefinition) parent).getId()
            : null; 
   }

   public Class getMappedType()
   {
      return Reflect.getClassFromAbbreviatedName(type);
   }

   public Direction getDirection()
   {
      return direction;
   }

   public boolean isDescriptor()
   {
      return descriptor;
   }

   public boolean isKeyDescriptor()
   {
      return keyDescriptor;
   }

   public String getAccessPath()
   {
      return accessPath;
   }

   public String getData()
   {
      return data;
   }

   public String getProcessDefinitionId()
   {
      return processDefinitionId;
   }
}
