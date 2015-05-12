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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IApplicationContext;
import org.eclipse.stardust.engine.api.model.IApplicationContextType;
import org.eclipse.stardust.engine.api.model.IDataMapping;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.data.PrimitiveValidator;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;
import org.eclipse.stardust.engine.core.spi.extensions.model.ExtendedDataValidator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataXMLValidator;


/**
 * A read only view of the client side part of a data mapping.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DataMappingDetails extends ModelElementDetails implements DataMapping
{
   private static final String FALLBACK_TYPE_NAME = Object.class.getName();

   private static final long serialVersionUID = 2L;

   private String applicationPath;
   private String type;
   private Direction direction;

   private final String activityId;
   private final String processDefinitionId;

   private final String context;
   private String dataId;
   private String dataPath;

   private AccessPointDetailsEvaluator apEvaluator;
   private String accessPointId;

   DataMappingDetails(IDataMapping mapping)
   {
      super(mapping, mapping.getId(), mapping.getName(), mapping.getDescription());
      this.applicationPath = mapping.getActivityPath();

      // prepare attribute maps for application context.
      IActivity activity = mapping.getActivity();
      IApplicationContext applContext = activity.getContext(
            mapping.getContext());
      Map contextAttributes = new HashMap();
      Map typeAttributes = new HashMap();
      initAttributeMaps(applContext, contextAttributes, typeAttributes);

      boolean isInteractive = AccessPointDetailsEvaluator.isInteractive(activity);
      apEvaluator = new AccessPointDetailsEvaluator(applContext, isInteractive,
            contextAttributes, typeAttributes);
      accessPointId = mapping.getActivityAccessPointId();

      this.direction = mapping.getDirection();
      this.dataPath = mapping.getDataPath();

      // todo: (france, fh) only data bridge object is considered ???
      try
      {
         String validatorClass = mapping.getData().getType().getStringAttribute(
               PredefinedConstants.VALIDATOR_CLASS_ATT);
         ExtendedDataValidator validator = SpiUtils
               .createExtendedDataValidator(validatorClass);
         AccessPathEvaluationContext context = new AccessPathEvaluationContext(null,
               null, null, activity);

         final boolean isArchiveAuditTrail = Parameters.instance().getBoolean(
               Constants.CARNOT_ARCHIVE_AUDITTRAIL, false);

         type = isArchiveAuditTrail && !isPrimitiveOrStructValidator(validator)
                  ? FALLBACK_TYPE_NAME
                  : validator.getBridgeObject(mapping.getData(),
                        mapping.getDataPath(),
                        Direction.IN.equals(direction)
                           ? Direction.OUT
                           : Direction.IN, context)
                     .getEndClass().getName();
      }
      catch (Exception e)
      {
         LogUtils.traceException(e, false);
         type = FALLBACK_TYPE_NAME;
      }

      this.activityId = activity.getId();
      this.processDefinitionId = activity.getProcessDefinition().getId();

      this.context = mapping.getContext();
      this.dataId = mapping.getData().getId();
   }
   
   private boolean isPrimitiveOrStructValidator(ExtendedDataValidator validator)
   {
      return validator instanceof PrimitiveValidator
            || validator instanceof StructuredDataXMLValidator;
   }

   public String getApplicationPath()
   {
      return applicationPath;
   }

   public AccessPoint getApplicationAccessPoint()
   {
      return apEvaluator.findAccessPoint(accessPointId);
   }

   public Class getMappedType()
   {
      return Reflect.getClassFromAbbreviatedName(type);
   }

   public Direction getDirection()
   {
      return direction;
   }

   public String getActivityId()
   {
      return activityId;
   }

   public String getProcessDefinitionId()
   {
      return processDefinitionId;
   }

   public String getContext()
   {
      return context;
   }

   public String getDataId()
   {
      return this.dataId;
   }

   public String getDataPath()
   {
      return this.dataPath;
   }

   private static void initAttributeMaps(IApplicationContext context,
         Map contextAttributes, Map typeAttributes)
   {
      contextAttributes.putAll(context.getAllAttributes());

      IApplicationContextType contextType = (IApplicationContextType) context.getType();
      if (null != contextType)
      {
         typeAttributes.putAll(contextType.getAllAttributes());
      }
   }
}