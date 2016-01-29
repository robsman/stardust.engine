/*******************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
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
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.pojo.data.PrimitiveValidator;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;
import org.eclipse.stardust.engine.core.spi.extensions.model.ExtendedDataValidator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataXMLValidator;


/**
 * Wraps meta information for a data accessPath as defined in the model.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DataPathDetails extends ModelElementDetails implements DataPath
{
   private static final String FALLBACK_TYPE_NAME = Object.class.getName();
   private static final String STRING_TYPE_NAME = String.class.getName();

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

      IData iData = path.getData();
      if (iData == null)
      {
         type = STRING_TYPE_NAME;
      }
      else
      {
         try
         {
            IDataType dataType = (IDataType) iData.getType();
            String validatorClass = dataType
                  .getStringAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT);
            final boolean isArchiveAuditTrail = ParametersFacade.instance().getBoolean(
                  Constants.CARNOT_ARCHIVE_AUDITTRAIL, false);

            if (!StringUtils.isEmpty(validatorClass))
            {
               ExtendedDataValidator validator = SpiUtils
                     .createExtendedDataValidator(validatorClass);

               type = isArchiveAuditTrail && !isPrimitiveOrStructValidator(validator)
                     ? FALLBACK_TYPE_NAME
                     : validator.getBridgeObject(iData, path.getAccessPath(),
                                 Direction.OUT.equals(path.getDirection())
                                       ? Direction.IN
                                       : Direction.OUT, null).getEndClass().getName();
            }
            else
            {
               type = FALLBACK_TYPE_NAME;
            }
         }
         catch (Exception e)
         {
            trace.warn("", e);
            this.type = FALLBACK_TYPE_NAME;
         }
      }

      direction = path.getDirection();
      descriptor = path.isDescriptor();
      keyDescriptor = path.isKeyDescriptor();
      accessPath = path.getAccessPath();
      data = iData == null ? null : iData.getId();

      ModelElement parent = path.getParent();
      this.processDefinitionId = (parent instanceof IProcessDefinition)
            ? ((IProcessDefinition) parent).getId()
            : null;
   }

   private boolean isPrimitiveOrStructValidator(ExtendedDataValidator validator)
   {
      return validator instanceof PrimitiveValidator
            || validator instanceof StructuredDataXMLValidator;
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
