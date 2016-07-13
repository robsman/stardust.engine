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
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.WorkflowServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.eclipse.stardust.engine.extensions.decorator.wrappers.ActivityInstanceWrapper;

public class DecoratorAppApplicationInstance implements SynchronousApplicationInstance
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
   private WorkflowService workflowService;

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
   private String eltId;  
   public void bootstrap(ActivityInstance activityInstance)
   {
      this.workflowService=new WorkflowServiceImpl();
      this.activityInstance = activityInstance;
      this.decoratorApplication = activityInstance.getActivity().getApplication();
      ModelManager modelManager = ModelManagerFactory.getCurrent();

      String modelId = getModelId(decoratorApplication);
      containingModel = findModel(modelId, modelManager);
      elementType = getElementType(decoratorApplication);
      this.eltId = getDecoratedElementID(decoratorApplication);

      if (StringUtils.isNotEmpty(elementType) && elementType.equals("application"))
      {
         BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();
         IActivityInstance ais = bpmRt.getCurrentActivityInstance();
         aiw = new ActivityInstanceWrapper(ais);

         this.eltId = this.eltId.replace(modelId + ":", "");
         decoratedApplication = findDecoratedApplication(this.eltId, containingModel);
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

   private String processElementId(String eltId){
      if(StringUtils.isNotEmpty(eltId) && eltId.contains(":")){
         String[] ids=eltId.split(":");
         return "{"+ids[0]+"}"+ids[1] ;
      }
       return null;  
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
         String runtimeBinding=processElementId(this.eltId);
         IProcessDefinition processDefinition = ModelUtils.getProcessDefinition(runtimeBinding);
         ProcessInstance pi=workflowService.startProcess(runtimeBinding, Collections.EMPTY_MAP, true);
         ProcessInstanceBean instance = ProcessInstanceBean.findByOID(pi.getOID());
         for (String key:inAccessPointValues.keySet())
         {
            Object bridgeObject=inAccessPointValues.get(key);
            IData subProcessData =ModelUtils.getMappedData(processDefinition, key);
            instance.setOutDataValue(subProcessData, null, bridgeObject);
         }

         //Process Out DataMappings
         Map<String, Object> outDataMappings =  new HashMap<String, Object>();
         if(!outDataTypes.isEmpty()){
            for(Object elt:outDataTypes)
            {
               outDataMappings.put((String)elt, instance.getDataValue(ModelUtils.getMappedData(processDefinition, (String)elt)).getValue());
            }
         }
         return outDataMappings;
      }
      
   }
}
