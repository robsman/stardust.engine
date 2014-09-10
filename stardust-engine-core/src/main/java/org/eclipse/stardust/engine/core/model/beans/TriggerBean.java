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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailTriggerBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidatorEx;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;



/**
 * @author mgille
 * @version $Revision$
 */
public class TriggerBean extends IdentifiableElementBean
      implements ITrigger
{
   private static final long serialVersionUID = 2L;

   public static final Logger trace = LogManager.getLogger(TriggerBean.class);

   private ITriggerType type = null;

   private Link persistentAccessPoints = new Link(this, "Access Points");
   private transient AccessPointJanitor accessPoints;
   private List parameterMappings = null;

   TriggerBean()
   {
   }

   TriggerBean(String id, String name)
   {
      super(id, name);
   }

   public String toString()
   {
      return "Trigger: " + getId();
   }

   public PluggableType getType()
   {
      return type;
   }

   private AccessPointJanitor getAccessPointLink()
   {
      if (accessPoints == null)
      {
         accessPoints = new AccessPointJanitor(persistentAccessPoints);
      }
      return accessPoints;
   }

   public Iterator getAllAccessPoints()
   {
      return getAccessPointLink().iterator();
   }

   public Iterator getAllInAccessPoints()
   {
      return Collections.EMPTY_LIST.iterator();
   }

   public Iterator getAllOutAccessPoints()
   {
      return getAllAccessPoints();
   }

   public String getProviderClass()
   {
      return getType().getStringAttribute(PredefinedConstants.ACCESSPOINT_PROVIDER_ATT);
   }

   public void addIntrinsicAccessPoint(AccessPoint ap)
   {
      getAccessPointLink().addIntrinsicAccessPoint(ap);
   }

   public Iterator getAllPersistentAccessPoints()
   {
      return persistentAccessPoints.iterator();
   }

   public AccessPoint findAccessPoint(String id)
   {
      return findAccessPoint(id, null);
   }

   public AccessPoint findAccessPoint(String id, Direction direction)
   {
      return getAccessPointLink().findAccessPoint(id, direction);
   }


   public AccessPoint createAccessPoint(String id, String name, Direction direction,
         IDataType type, int elementOID)
   {
      IAccessPoint result = new AccessPointBean(id, name, direction);
      addToPersistentAccessPoints(result);
      result.register(elementOID);
      result.setDataType(type);
      return result;
   }

   public Iterator getAllParameterMappings()
   {
      return ModelUtils.iterator(parameterMappings);
   }

   public ModelElementList getParameterMappings()
   {
      return ModelUtils.getModelElementList(parameterMappings);
   }

   public IParameterMapping createParameterMapping(IData data, String dataPath, String parameterId,
         String parameterPath, int elementOID)
   {
      if (null != data)
      {
         if (parameterMappings == null)
         {
            parameterMappings = CollectionUtils.newList();
         }

         IParameterMapping result = new ParameterMappingBean(this, data, dataPath, parameterId, parameterPath);
         addToParameterMappings(result);
         result.register(elementOID);
         return result;
      }
      return null;
   }

   public void setType(ITriggerType type)
   {
      this.type = type;
   }

   public void addToParameterMappings(IParameterMapping mapping)
   {
      if (parameterMappings == null)
      {
         parameterMappings = CollectionUtils.newList();
      }
      parameterMappings.add(mapping);
   }

   public void removeFromParameterMappings(IParameterMapping mapping)
   {
      if (parameterMappings != null)
      {
         parameterMappings.remove(mapping);
      }
   }

   public void addToPersistentAccessPoints(IAccessPoint ap)
   {
      persistentAccessPoints.add(ap);
   }

   public void removeFromAccessPoints(AccessPoint ap)
   {
      getAccessPointLink().remove(ap);
   }

   public void removeFromPersistentAccessPoints(IAccessPoint ap)
   {
      persistentAccessPoints.remove(ap);
   }

   public void checkConsistency(List inconsistencies)
   {
      super.checkConsistency(inconsistencies);
      checkId(inconsistencies);

      if (getId() != null)
      {
         // check for unique Id
         ITrigger t = ((IProcessDefinition) getParent()).findTrigger(getId());
         if (t != null && t != this)
         {
            BpmValidationError error = BpmValidationError.TRIGG_DUPLICATE_ID_FOR_PROCESS_DEFINITION.raise(getName());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }

         // check id to fit in maximum length
         if (getId().length() > AuditTrailTriggerBean.getMaxIdLength())
         {
            BpmValidationError error = BpmValidationError.TRIGG_ID_EXCEEDS_MAXIMUM_LENGTH.raise(
                  getId(), getName(), AuditTrailTriggerBean.getMaxIdLength());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }
      }

      ITriggerType triggerType = (ITriggerType) getType();
      if (triggerType != null)
      {
         TriggerValidator validator = (TriggerValidator) ValidatorUtils.getValidator(triggerType, this, inconsistencies);
         if (null != validator)
         {
            if (validator instanceof TriggerValidatorEx)
            {
               inconsistencies.addAll(((TriggerValidatorEx) validator).validate(this));
            }
            else
            {
               Collection c = validator.validate(getAllAttributes(), getAllAccessPoints());
               for (Iterator i = c.iterator(); i.hasNext();)
               {
                  Inconsistency inc = (Inconsistency) i.next();
                  if (inc.getError() != null)
                  {
                     inconsistencies.add(new Inconsistency(inc.getError(), this,
                           inc.getSeverity()));
                  }
                  inconsistencies.add(new Inconsistency(inc.getMessage(), this,
                        inc.getSeverity()));
               }
            }
         }
      }
      else
      {
         BpmValidationError error = BpmValidationError.TRIGG_NO_TYPE_SET.raise();
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
      }

      // TODO: add warnings for duplicates
      for (Iterator j = getAllParameterMappings(); j.hasNext();)
      {
         BpmValidationError error = null;
         IParameterMapping pm = (IParameterMapping) j.next();
         if (pm.getData() == null)
         {
            error = BpmValidationError.TRIGG_PARAMETER_MAPPING_DOES_NOT_SPECIFY_DATA.raise();
         }
         else if (pm.getParameterId() == null)
         {
            error = BpmValidationError.TRIGG_PARAMETER_MAPPING_DOES_NOT_SPECIFY_PARAMETER.raise();
         }
         else if (findAccessPoint(pm.getParameterId()) == null)
         {
            error = BpmValidationError.TRIGG_PARAMETER_FOR_PARAMETER_MAPPING_INVALID.raise(pm.getParameterId());
         }
         else
         {
            IAccessPoint accessPoint = (IAccessPoint) findAccessPoint(pm.getParameterId());
            if (!StringUtils.isValidIdentifier(accessPoint.getId()))
            {
               error = BpmValidationError.TRIGG_ACCESSPOINT_HAS_INVALID_ID.raise();
            }
            if (triggerType != null && PredefinedConstants.SCAN_TRIGGER.equals(triggerType.getId()))
            {
               String apType = accessPoint.getType().getId();
               if (!DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(apType) && !DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(apType))
               {
                  error = BpmValidationError.TRIGG_SCAN_TRIGGERS_DO_NOT_SUPPORT_ACCESS_POINT_TYPE.raise(apType);
               }
            }
            else if (!BridgeObject.isValidMapping(null, Direction.OUT, accessPoint,
                  pm.getParameterPath(), pm.getData(), pm.getDataPath(), null))
            {
               error = BpmValidationError.TRIGG_PARAMETER_MAPPING_CONTAINS_AN_INVALID_TYPE_CONVERSION.raise(pm.getParameterId());
            }

         }
         if (error != null)
         {
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
         }
      }
   }

   public boolean isSynchronous()
   {
      return EngineProperties.THREAD_MODE_SYNCHRONOUS.equals(
            Parameters.instance().getString(
                  JmsProperties.JMS_TRIGGER_THREAD_MODE,
                  EngineProperties.THREAD_MODE_ASYNCHRONOUS));
   }
}