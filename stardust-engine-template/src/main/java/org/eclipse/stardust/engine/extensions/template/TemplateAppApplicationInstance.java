package org.eclipse.stardust.engine.extensions.template;

import java.lang.reflect.InvocationTargetException;

import java.util.Map;
import java.util.Set;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ApplicationDetails;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.eclipse.stardust.engine.extensions.template.wrappers.ActivityInstanceWrapper;

public class TemplateAppApplicationInstance implements SynchronousApplicationInstance
{

   public static final Logger logger = LogManager
         .getLogger(TemplateAppApplicationInstance.class.getCanonicalName());

   private ActivityInstance activityInstance;

   private ActivityInstanceWrapper aiw;

   private Application templateApplication;

   private IModel containingModel;

   private IApplication decoratedApplication;

   private SynchronousApplicationInstance decoratedApplicationInstance;

   private String getElementType(Application templateApplication)
   {
      return (String) templateApplication
            .getAttribute("stardust:application::template::elementType");
   }

   private String getModelId(Application templateApplication)
   {
      return (String) templateApplication
            .getAttribute("stardust:application::template::modelId");
   }

   private String getDecoratedElementID(Application templateApplication)
   {
      return (String) templateApplication
            .getAttribute("stardust:application::template::elementId");
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
      return application.getType().getStringAttribute(
            PredefinedConstants.APPLICATION_INSTANCE_CLASS_ATT);
   }

   @Override
   public void bootstrap(ActivityInstance activityInstance)
   {
      this.activityInstance = activityInstance;
      BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();
      IActivityInstance ais = bpmRt.getCurrentActivityInstance();
      aiw = new ActivityInstanceWrapper(ais);
      this.templateApplication = activityInstance.getActivity().getApplication();

      ModelManager modelManager = ModelManagerFactory.getCurrent();
      String elementType = getElementType(templateApplication);
      if (StringUtils.isNotEmpty(elementType) && elementType.equals("application"))
      {
         String modelId = getModelId(templateApplication);
         String targetApplicationId = getDecoratedElementID(templateApplication);
         containingModel = findModel(modelId, modelManager);
         targetApplicationId = targetApplicationId.replace(modelId + ":", "");
         decoratedApplication = findDecoratedApplication(targetApplicationId,
               containingModel);
         ApplicationDetails ap = DetailsFactory.create(decoratedApplication,
               IApplication.class, ApplicationDetails.class);
         aiw.getActivity().setApplication(ap);
         String instanceType = getApplicationInstanceClass(decoratedApplication);
         decoratedApplicationInstance = (SynchronousApplicationInstance) SpiUtils
               .createApplicationInstance(instanceType);
         decoratedApplicationInstance.bootstrap(aiw);
         if (logger.isDebugEnabled())
         {
            IModel currentModel = modelManager.findModel(this.templateApplication
                  .getModelOID());
            logger.debug("Application " + this.templateApplication.getName()
                  + " from model " + currentModel.getName()
                  + " is configured to decorate application "
                  + decoratedApplication.getName() + " from model "
                  + containingModel.getName());
         }
      }
      else
      {
         logger.debug("Template AppApplication Runtime invoked with an invalid configuration");
      }

   }

   @Override
   public void setInAccessPointValue(String name, Object value)
   {
      if (logger.isDebugEnabled())
      {
         logger.debug("Class:" + decoratedApplicationInstance.getClass().getName()
               + ", Method setInAccessPoint invoked with the following parameters :("
               + name + ", " + value + ")");
      }
      decoratedApplicationInstance.setInAccessPointValue(name, value);

   }

   @Override
   public Object getOutAccessPointValue(String name)
   {

      return decoratedApplicationInstance.getOutAccessPointValue(name);
   }

   @Override
   public void cleanup()
   {
      decoratedApplicationInstance.cleanup();
   }

   @Override
   public Map invoke(Set outDataTypes) throws InvocationTargetException
   {
      return decoratedApplicationInstance.invoke(outDataTypes);
   }

}
