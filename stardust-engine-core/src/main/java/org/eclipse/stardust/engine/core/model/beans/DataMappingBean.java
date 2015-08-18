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
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IApplicationContext;
import org.eclipse.stardust.engine.api.model.IApplicationContextType;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataMapping;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.SubProcessModeKey;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.api.runtime.UnresolvedExternalReference;
import org.eclipse.stardust.engine.core.model.utils.ConnectionBean;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;
import org.eclipse.stardust.engine.core.spi.extensions.model.ExtendedDataValidator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;


/**
 * @author mgille
 * @version $Revision$
 */
public class DataMappingBean extends ConnectionBean implements IDataMapping
{
   private static final long serialVersionUID = 1L;

   static final String ID_ATT = "Id";
   private String id;
   private String name;

   static final String DIRECTION_ATT = "Direction";
   private Direction direction = Direction.IN;

   static final String DATA_PATH_ATT = "Data path";
   private String dataPath = "";

   static final String APPLICATION_PATH_ATT = "Application path";
   private String applicationPath = "";

   static final String APPLICATION_ACCESS_POINT_ID_ATT = "Application Access Point";
   private String applicationAccessPointId;

   private String context = "default";

   DataMappingBean()
   {
   }

   public DataMappingBean(String id, String name, IData data, IActivity activity, Direction direction,
         String applicationAccessPointId)
   {
      super(data, activity);
      this.id = id;
      this.name = name;
      this.direction = direction;
      this.applicationAccessPointId = applicationAccessPointId;
   }

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies
    * of the data mapping.
    */
   public void checkConsistency(List inconsistencies)
   {
      // Rule: associated activity must be part of the same model
      IActivity activity = getActivity();
      if (activity == null)
      {
         BpmValidationError error = BpmValidationError.DATA_NO_ACTIVITY_SET_FOR_DATAMAPPING.raise(getErrorName());
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
      }

      // Rule: associated data must be part of the same model
      if (getData() == null)
      {
         BpmValidationError error = BpmValidationError.DATA_NO_DATA_SET_FOR_DATAMAPPING.raise(getErrorName());
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
      }

      if (getId() == null || getId().length() == 0)
      {
         BpmValidationError error = BpmValidationError.DATA_NO_USEFUL_ID_SET_FOR_DATAMAPPING.raise(getErrorName());
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
      }

      if (getName() == null || getName().length() == 0)
      {
         BpmValidationError error = BpmValidationError.DATA_NO_USEFUL_NAME_SET_FOR_DATAMAPPING.raise(getErrorName());
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
      }

      if (activity != null)
      {
         ModelElementList mappings = activity.getDataMappings();
         for (int i = 0, len = mappings.size(); i < len; i++)
         {
            IDataMapping dataMapping = (IDataMapping) mappings.get(i);
            if (!super.equals(dataMapping) && dataMapping.getId().equals(getId())
                  && dataMapping.getDirection().equals(getDirection()))
            {
               if (dataMapping.getContext() != null && getContext() != null)
               {
                  if (dataMapping.getContext().equals(getContext()))
                  {
                     BpmValidationError error = BpmValidationError.DATA_DATAMAPPING_HAS_NO_UNIQUE_ID_FOR_DIRECTION.raise(
                           getErrorName(), getDirection().toString());
                     inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
                  }
               }
               else
               {
                  BpmValidationError error = BpmValidationError.DATA_DATAMAPPING_HAS_NO_UNIQUE_ID_FOR_DIRECTION.raise(
                        getErrorName(), getDirection().toString());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
               }
            }
         }

         AccessPoint accessPoint = null;
         try
         {
            accessPoint = getActivityAccessPoint();
         }
         catch (InternalException ie)
         {
            Throwable t = ie.getCause();
            if (t instanceof ClassNotFoundException || t instanceof NoClassDefFoundError)
            {
               // For certain deployments (e.g. EJB) it may be possible that for interactive
               // applications (e.g. JSF) access points could not be evaluated on model
               // deployment time. For these interactive applications no warning is generated.
               if (!isInteractiveApplication())
               {
                  BpmValidationError error = BpmValidationError.DATA_CANNOT_RESOLVE_ACCESSPOINTPROVIDER_FOR_DATAMAPPING.raise(getId());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
                  return;
               }
            }
            else
            {
               throw ie;
            }
         }

         if (accessPoint != null)
         {
            boolean ctxMissing = false;
            if (activity.isInteractive())
            {
               IApplicationContext context = activity.getContext(getContext());
               if (null == context)
               {
                  BpmValidationError error = BpmValidationError.DATA_INVALID_CONTEXT_FOR_DATAMAPPING.raise(
                        getContext(), getErrorName());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
                  ctxMissing = true;
               }
            }
             // only if implementation type is application, an application must be set
            else if (ImplementationType.Application.equals(activity
                  .getImplementationType()))
            {
               IApplication application = activity.getApplication();
               if (null == application)
               {
                  BpmValidationError error = BpmValidationError.DATA_INVALID_APPLICATION_FOR_DATAMAPPING.raise(getErrorName());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
                  ctxMissing = true;
               }
            }
            if ( !ctxMissing && getData() != null)
            {
               IData data = getData();
               if (!BridgeObject.isValidMapping(context, direction, applicationAccessPointId, accessPoint, applicationPath,
                     data, dataPath, activity))
               {
                  BpmValidationError error = BpmValidationError.DATA_INVALID_DATATYPE_FOR_DATAMAPPING.raise(
                        getData().getName(), getErrorName());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
               }
            }
         }
         else
         {
            if (ImplementationType.SubProcess == activity.getImplementationType()
                  && activity.getExternalReference() != null)
            {
               BpmValidationError error = BpmValidationError.DATA_FORMAL_PARAMETER_NOT_RESOLVABLE_FOR_DATAMAPPING.raise(
                     applicationAccessPointId, getErrorName());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }
            IApplicationContext context = activity.getContext(this.context);
            if (context != null)
            {
               // If application path is set for non interactive application a warning is generated...
               IApplicationContextType contextType = (IApplicationContextType) context.getType();
               if (contextType.hasApplicationPath()
                     && !isInteractiveApplication())
               {
                  if (!StringUtils.isEmpty(applicationAccessPointId))
                  {
                     // SubProcess activities use application access points to convey
                     // their access point information in data mappings for mode sync_seperate
                     if (SubProcessModeKey.SYNC_SEPARATE != activity.getSubProcessMode())
                     {
                        BpmValidationError error = BpmValidationError.DATA_APPLICATION_ACCESS_POINT_NOT_RESOLVABLE_FOR_DATAMAPPING.raise(
                              applicationAccessPointId, getErrorName());
                        inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
                     }
                  }
                  else
                  {
                     {
                        BpmValidationError error = BpmValidationError.DATA_NO_APPLICATION_ACCESS_POINT_SET_FOR_DATAMAPPING.raise(getErrorName());
                        inconsistencies.add(new Inconsistency(error,
                              this, Inconsistency.WARNING));
                     }
                  }
               }
               else
               {
                  // ... otherwise at least the data part of the data mapping is validated.
                  ExtendedDataValidator leftValidator = (ExtendedDataValidator) ValidatorUtils
                        .getValidator(getData().getType(), this, inconsistencies);
                  if (null != leftValidator)
                  {
                     validatePath(inconsistencies, leftValidator);
                  }
               }
            }
            else
            {
               if (StringUtils.isEmpty(getContext()))
               {
                  BpmValidationError error = BpmValidationError.DATA_NO_CONTEXT_SET_FOR_DATAMAPPING.raise(getErrorName());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
               }
               else
               {
                  BpmValidationError error = BpmValidationError.DATA_CONTEXT_FOR_DATAMAPPING_UNDEFINED.raise(
                        getContext(), getErrorName());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
               }
            }
         }
      }
   }

   private void validatePath(List inconsistencies, ExtendedDataValidator leftValidator)
   {
      try
      {

         AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(
               null, null, null, getActivity());
         leftValidator.getBridgeObject(getData(), dataPath, Direction.OUT
               .equals(getDirection()) ? Direction.IN : Direction.OUT,
               evaluationContext);
      }
      catch (UnresolvedExternalReference ex)
      {
         throw ex;
      }
      catch (Exception e)
      {
         BpmValidationError error = BpmValidationError.DATA_INVALID_DATAPATH_FOR_DATAMAPPING.raise(getErrorName());
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
      }
   }

   private String getErrorName()
   {
      IActivity act = getActivity();
      IProcessDefinition proc = act.getProcessDefinition();
      StringBuffer result = new StringBuffer();
      result.append('/').append(direction).append('/').append(context).append('/').append(id);
      result.append("' in activity '").append(act.getId()).append("' of process '{").append(proc.getModel().getId()).append('}').append(proc.getId());
      return result.toString();
   }

   public String toString()
   {
      return "Data Mapping: " + getName() +
            (getData() == null ? "" : " (" + getData().getId() + ")");
   }

   public IData getData()
   {
      return (IData) getFirst();
   }

   public String getId()
   {
      return id;
   }

   public String getUniqueId()
   {
      return id + "::" + direction + "::" + context +
            (getData() == null ? "" : "::" + getData().getId());
   }

   public String getName()
   {
      if(StringUtils.isEmpty(name))
      {
         return getId();
      }

      return name;
   }

   public IActivity getActivity()
   {
      return (IActivity) parent;
   }

   public Direction getDirection()
   {
      return direction;
   }

   public void setDirection(Direction direction)
   {
      markModified();

      this.direction = direction;
   }

   public String getDataPath()
   {
      return dataPath;
   }

   public void setDataPath(String dataPath)
   {
      this.dataPath = dataPath;
   }

   public String getActivityPath()
   {
      return applicationPath;
   }

   public void setActivityPath(String applicationPath)
   {
      this.applicationPath = applicationPath;
   }

   public String getActivityAccessPointId()
   {
      return applicationAccessPointId;
   }

   public void setActivityAccessPointId(String id)
   {
      applicationAccessPointId = id;
   }

   public AccessPoint getActivityAccessPoint()
   {
      AccessPoint accessPoint = getActivity().getAccessPoint(context, applicationAccessPointId, direction);
      if (accessPoint == null && !StringUtils.isEmpty(this.applicationPath))
      {
         // support the cases, where for example, for IN data mappings,
         // the access point is a getter method (will therefore have direction OUT)
         // and application (access) path is set (CRNT-10904) -> search for access point of any direction
         accessPoint = getActivity().getAccessPoint(context, applicationAccessPointId);
      }
      return accessPoint;
   }

   public String getContext()
   {
      return context;
   }

   public void setContext(String context)
   {
      markModified();
      this.context = context;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setData(IData data)
   {
      setFirst(data);
   }

   private boolean isInteractiveApplication()
   {
      IActivity activity = getActivity();
      IApplication application = activity.getApplication();
      return activity.getImplementationType().equals(ImplementationType.Application)
            && application != null && application.isInteractive();
   }
}