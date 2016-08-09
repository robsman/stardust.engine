package org.eclipse.stardust.engine.extensions.decorator;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ApplicationDetails;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.eclipse.stardust.engine.extensions.decorator.wrappers.ActivityInstanceWrapper;

public class DecoratorAppApplicationInstance implements SynchronousApplicationInstance
{
   private static final String ELEMENT_ID_ATT = "stardust:application::decorator::elementId";
   private static final String ELEMENT_TYPE_ATT = "stardust:application::decorator::elementType";
   private static final String MODEL_ID_ATT = "stardust:application::decorator::modelId";

   public static final Logger logger = LogManager.getLogger(DecoratorAppApplicationInstance.class);

   private SynchronousApplicationInstance decoratedApplicationInstance;
   private IProcessDefinition processDefinition;
   private Map<String, Object> inputData;

   public void bootstrap(ActivityInstance activityInstance)
   {
      ModelManager modelManager = ModelManagerFactory.getCurrent();

      Application decoratorApplication = activityInstance.getActivity().getApplication();

      String modelId = (String) decoratorApplication.getAttribute(MODEL_ID_ATT);
      IModel containingModel = modelManager.findActiveModel(modelId);

      String elementType = (String) decoratorApplication.getAttribute(ELEMENT_TYPE_ATT);
      String elementId = (String) decoratorApplication.getAttribute(ELEMENT_ID_ATT);

      if ("application".equals(elementType))
      {
         elementId = elementId.replace(containingModel.getId() + ":", "");
         IApplication decoratedApplication = containingModel.findApplication(elementId);
         ApplicationDetails ap = DetailsFactory.create(decoratedApplication, IApplication.class, ApplicationDetails.class);

         BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();
         IActivityInstance ais = bpmRt.getCurrentActivityInstance();
         ActivityInstanceWrapper aiw = new ActivityInstanceWrapper(ais);
         aiw.getActivity().setApplication(ap);

         String instanceType = decoratedApplication.getType().getStringAttribute(PredefinedConstants.APPLICATION_INSTANCE_CLASS_ATT);
         decoratedApplicationInstance = (SynchronousApplicationInstance) SpiUtils.createApplicationInstance(instanceType);
         decoratedApplicationInstance.bootstrap(aiw);
         if (logger.isDebugEnabled())
         {
            IModel currentModel = modelManager.findModel(decoratorApplication.getModelOID());
            logger.debug("Application " + decoratorApplication.getName()
                  + " from model " + currentModel.getName()
                  + " is configured to decorate application "
                  + decoratedApplication.getName() + " from model "
                  + containingModel.getName());
         }
      }
      else if ("process".equals(elementType))
      {
         processDefinition = ModelUtils.getProcessDefinition(processElementId(elementId));
         if (logger.isDebugEnabled())
         {
            IModel currentModel = modelManager.findModel(decoratorApplication.getModelOID());
            logger.debug("Application " + decoratorApplication.getName()
                  + " from model " + currentModel.getName()
                  + " is configured to decorate Process Interfaces for process "
                  + " from model " + containingModel.getName());
         }
      }

      if (decoratedApplicationInstance == null && processDefinition == null)
      {
         logger.debug(
               "Decorator Application Runtime invoked with an invalid configuration");
      }
   }

   public void setInAccessPointValue(String name, Object value)
   {
      if (logger.isDebugEnabled())
      {
         logger.debug("Setting IN access point '" + name + "' to value " + value);
      }

      if (decoratedApplicationInstance != null)
      {
         decoratedApplicationInstance.setInAccessPointValue(name, value);
      }
      else
      {
         IData subProcessData = ModelUtils.getMappedData(processDefinition, name);
         if (subProcessData != null)
         {
            if (inputData == null)
            {
               inputData = CollectionUtils.newMap();
            }
            inputData.put(subProcessData.getId(), value);
         }
      }
   }

   public Object getOutAccessPointValue(String name)
   {
      if (decoratedApplicationInstance != null)
      {
         return decoratedApplicationInstance.getOutAccessPointValue(name);
      }
      return null;
   }

   public void cleanup()
   {
      if (decoratedApplicationInstance != null)
      {
         decoratedApplicationInstance.cleanup();
      }
   }

   private String processElementId(String elementId)
   {
      if (StringUtils.isNotEmpty(elementId) && elementId.contains(":"))
      {
         String[] ids = elementId.split(":");
         return "{" + ids[0] + "}" + ids[1];
      }
      return null;
   }

   @SuppressWarnings("rawtypes")
   public Map invoke(Set outDataTypes) throws InvocationTargetException
   {
      if (decoratedApplicationInstance != null)
      {
         return decoratedApplicationInstance.invoke(outDataTypes);
      }
      else
      {
         ProcessInstance pi = new WorkflowServiceImpl().startProcess(processDefinition,
               inputData == null ? Collections.<String, Object>emptyMap() : inputData, true);
         ProcessInstanceBean instance = ProcessInstanceBean.findByOID(pi.getOID());

         // Process Out DataMappings
         Map<String, Object> outDataMappings = new HashMap<String, Object>();
         if (!outDataTypes.isEmpty())
         {
            for (Object outputAccessPointId : outDataTypes)
            {
               IDataValue dataValue = instance.getDataValue(ModelUtils.getMappedData(processDefinition, (String) outputAccessPointId));
               outDataMappings.put((String) outputAccessPointId, dataValue.getValue());
            }
         }
         return outDataMappings;
      }
   }
}
