package org.eclipse.stardust.engine.extensions.decorator;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ApplicationDetails;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityThread;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AsynchronousApplicationInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.eclipse.stardust.engine.extensions.decorator.wrappers.ActivityInstanceWrapper;

public class DecoratorAppApplicationInstance implements SynchronousApplicationInstance,  AsynchronousApplicationInstance
{

   public static final Logger logger = LogManager
         .getLogger(DecoratorAppApplicationInstance.class.getCanonicalName());

   private ActivityInstance activityInstance;

   private ActivityInstanceWrapper aiw;

   private Application decoratorApplication;

   private IModel containingModel;

   private IApplication decoratedApplication;

   private SynchronousApplicationInstance decoratedApplicationInstance;

   private Map<String, Object> inAccessPointValues = CollectionUtils.newMap();

   private String getElementType(Application decoratorApplication)
   {
      return (String) decoratorApplication
            .getAttribute("stardust:application::decorator::elementType");
   }

   private String getModelId(Application decoratorApplication)
   {
      return (String) decoratorApplication
            .getAttribute("stardust:application::decorator::modelId");
   }

   private String getDecoratedElementID(Application decoratorApplication)
   {
      return (String) decoratorApplication
            .getAttribute("stardust:application::decorator::elementId");
   }

   private IModel findModel(String modelId, ModelManager modelManager)
   {
      return modelManager.findActiveModel(modelId);
   }

   private IApplication findDecoratedApplication(String id, IModel containingModel)
   {

      return containingModel.findApplication(id);
   }

   private String getApplicationInstanceClass(IApplication application)
   {
      return application.getType()
            .getStringAttribute(PredefinedConstants.APPLICATION_INSTANCE_CLASS_ATT);
   }

   private String elementType;

   public void bootstrap(ActivityInstance activityInstance)
   {
      this.activityInstance = activityInstance;
      this.decoratorApplication = activityInstance.getActivity().getApplication();
      ModelManager modelManager = ModelManagerFactory.getCurrent();

      String modelId = getModelId(decoratorApplication);
      containingModel = findModel(modelId, modelManager);
      elementType = getElementType(decoratorApplication);
      String eltId = getDecoratedElementID(decoratorApplication);

      if (StringUtils.isNotEmpty(elementType) && elementType.equals("application"))
      {
         BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();
         IActivityInstance ais = bpmRt.getCurrentActivityInstance();
         aiw = new ActivityInstanceWrapper(ais);

         eltId = eltId.replace(modelId + ":", "");
         decoratedApplication = findDecoratedApplication(eltId, containingModel);
         ApplicationDetails ap = DetailsFactory.create(decoratedApplication,
               IApplication.class, ApplicationDetails.class);
         aiw.getActivity().setApplication(ap);
         String instanceType = getApplicationInstanceClass(decoratedApplication);
         decoratedApplicationInstance = (SynchronousApplicationInstance) SpiUtils
               .createApplicationInstance(instanceType);
         decoratedApplicationInstance.bootstrap(aiw);
         if (logger.isDebugEnabled())
         {
            IModel currentModel = modelManager
                  .findModel(this.decoratorApplication.getModelOID());
            logger.debug("Application " + this.decoratorApplication.getName()
                  + " from model " + currentModel.getName()
                  + " is configured to decorate application "
                  + decoratedApplication.getName() + " from model "
                  + containingModel.getName());
         }
      }
      else if (StringUtils.isNotEmpty(elementType) && elementType.equals("process"))
      {
         // sync_separate

         if (logger.isDebugEnabled())
         {
            IModel currentModel = modelManager
                  .findModel(this.decoratorApplication.getModelOID());
            logger.debug("Application " + this.decoratorApplication.getName()
                  + " from model " + currentModel.getName()
                  + " is configured to decorate Process Interfaces for process "
                  + " from model " + containingModel.getName());
         }
      }
      else
      {
         logger.debug(
               "Decorator Application Runtime invoked with an invalid configuration");
      }

   }

   public void setInAccessPointValue(String name, Object value)
   {
      if (logger.isDebugEnabled())
      {
         logger.debug("Class:" + decoratedApplicationInstance.getClass().getName()
               + ", Method setInAccessPoint invoked with the following parameters :("
               + name + ", " + value + ")");
      }
      if (StringUtils.isNotEmpty(this.elementType)
            && this.elementType.equals("application"))
      {
         decoratedApplicationInstance.setInAccessPointValue(name, value);
      }
      else
      {
         if (logger.isDebugEnabled())
         {
            logger.debug("Setting IN access point '" + name + "' to value " + value);
         }

         inAccessPointValues.put(name, value);
      }

   }

   public Object getOutAccessPointValue(String name)
   {
      if (StringUtils.isNotEmpty(this.elementType)
            && this.elementType.equals("application"))
      {
         return decoratedApplicationInstance.getOutAccessPointValue(name);
      }
      else
      {
         return null;
      }
   }

   public void cleanup()
   {
      if (decoratedApplicationInstance != null)
         decoratedApplicationInstance.cleanup();
   }

   public Map invoke(Set outDataTypes) throws InvocationTargetException
   {
      if (StringUtils.isNotEmpty(this.elementType)
            && this.elementType.equals("application"))
      {
         return decoratedApplicationInstance.invoke(outDataTypes);
      }
      else
      {
         String binding = "{Model27}Impl";
         boolean copyAllData = false;
         boolean separateData = true;
         boolean synchronous = false;
         IProcessDefinition processDefinition = ModelUtils.getProcessDefinition(binding);
         IProcessInstance pi=ProcessInstanceBean.findByOID(this.activityInstance.getProcessInstanceOID());
         
         ProcessInstanceBean instance = ProcessInstanceBean.createInstance(processDefinition,pi,
               SecurityProperties.getUser(), Collections.EMPTY_MAP);
         
         for (String key:inAccessPointValues.keySet())
         {
            Object bridgeObject=inAccessPointValues.get(key);
            IData subProcessData =ModelUtils.getMappedData(processDefinition, key);
            instance.setOutDataValue(subProcessData, null, bridgeObject);
         }
         
         ActivityThread.schedule(instance, instance.getProcessDefinition()
               .getRootActivity(), null, synchronous, null, Collections.EMPTY_MAP,
               synchronous);
         
         return Collections.EMPTY_MAP;
      }
      
   }

   @Override
   public void send() throws InvocationTargetException
   {
      System.out.println("");
      String binding = "{Model27}Impl";
      boolean copyAllData = false;
      boolean separateData = true;
      boolean synchronous = false;
      IProcessDefinition processDefinition = ModelUtils.getProcessDefinition(binding);
      IProcessInstance pi=ProcessInstanceBean.findByOID(this.activityInstance.getProcessInstanceOID());
      
      ProcessInstanceBean instance = ProcessInstanceBean.createInstance(processDefinition,pi,
            SecurityProperties.getUser(), Collections.EMPTY_MAP);
      
      for (String key:inAccessPointValues.keySet())
      {
         Object bridgeObject=inAccessPointValues.get(key);
         IData subProcessData =ModelUtils.getMappedData(processDefinition, key);
         instance.setOutDataValue(subProcessData, null, bridgeObject);
      }
      
      ActivityThread.schedule(instance, instance.getProcessDefinition()
            .getRootActivity(), null, synchronous, null, Collections.EMPTY_MAP,
            synchronous);
   }

   @Override
   public Map receive(Map data, Iterator outDataTypes)
   {

      System.out.println("");
      return null;
   }

   @Override
   public boolean isSending()
   {

      return true;
   }

   @Override
   public boolean isReceiving()
   {
      return true;
   }
}
