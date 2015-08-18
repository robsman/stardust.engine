package org.eclipse.stardust.engine.extensions.camel.converter;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_TRIGGER_TYPE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.IPP_ENDPOINT_PROPERTIES;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.*;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.OriginValue.TRIGGER_CONSUMER;
import static org.eclipse.stardust.engine.extensions.camel.Util.copyInToOut;
import static org.eclipse.stardust.engine.extensions.camel.Util.performParameterMapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Property;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.trigger.AccessPointProperties;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;

public class BpmTypeConverter
{
   public static final Logger logger = LogManager.getLogger(BpmTypeConverter.class);

   public void toJSON(Exchange exchange) throws Exception
   {
      processMarshalling(exchange, new JsonTypeConverter.ApplicationTypeConverter(exchange));
   }

   public void fromJSON(Exchange exchange) throws Exception
   {
      //processUnmarshalling(exchange, new JsonTypeConverter(exchange));
      final String origin = extractOrigin(exchange);
      if (origin != null && StringUtils.isNotEmpty(origin)
            && origin.equalsIgnoreCase(TRIGGER_CONSUMER))
      {
         processUnmarshalling(exchange, new JsonTypeConverter.TriggerTypeConverter(exchange));
      }
      else
      {
         processUnmarshalling(exchange, new JsonTypeConverter.ApplicationTypeConverter(exchange));
      }
      
   }

   public void toNativeObject(Exchange exchange) throws Exception
   {
      processMarshalling(exchange, new JavaScriptTypeConverter(exchange,
            JsonTypeConverter.LONG_DATA_FORMAT));
   }

   public void fromNativeObject(Exchange exchange) throws Exception
   {
      processUnmarshalling(exchange, new JavaScriptTypeConverter(exchange,
            JsonTypeConverter.ISO_DATE_FORMAT));
   }

   public void fromXML(Exchange exchange) throws Exception
   {
      final String origin = extractOrigin(exchange);
      if (origin != null && StringUtils.isNotEmpty(origin)
            && origin.equalsIgnoreCase(TRIGGER_CONSUMER))
      {
         processUnmarshalling(exchange, new XmlTypeConverter.TriggerTypeConverter(exchange));
      }
      else
      {
         processUnmarshalling(exchange, new XmlTypeConverter.ApplicationTypeConverter(exchange));
      }
   }
   
   public void toXML(Exchange exchange) throws Exception
   {
      processMarshalling(exchange, new XmlTypeConverter.ApplicationTypeConverter(exchange));
   }

   public void fromList(Exchange exchange) throws Exception
   {
      processUnmarshalling(exchange, new ListTypeConverter(exchange));
   }

   public void toList(Exchange exchange) throws Exception
   {
      processMarshalling(exchange, new ListTypeConverter(exchange));
   }

   public void fromCSV(Exchange exchange, @Property(IPP_ENDPOINT_PROPERTIES)
   Map<String, Object> parameters) throws Exception
   {
     // processUnmarshalling(exchange, new CsvTypeConverter(exchange, parameters));
      
      final String origin = extractOrigin(exchange);
      if (origin != null && StringUtils.isNotEmpty(origin)
            && origin.equalsIgnoreCase(TRIGGER_CONSUMER))
      {
         processUnmarshalling(exchange, new CsvTypeConverter.TriggerTypeConverter(exchange, parameters));
      }
      else
      {
         processUnmarshalling(exchange, new CsvTypeConverter.ApplicationTypeConverter(exchange, parameters));
      }
   }

   public void toCSV(Exchange exchange, @Property(IPP_ENDPOINT_PROPERTIES)
   Map<String, Object> parameters) throws Exception
   {
      processMarshalling(exchange, new CsvTypeConverter.ApplicationTypeConverter(exchange, parameters));
   }

   private void processUnmarshalling(Exchange exchange, ITriggerTypeConverter converter)
         throws Exception
   {
      copyInToOut(exchange);
      final String modelId = extractModelId(exchange);
      final String processId = extractProcessId(exchange);
      final String triggerId = extractTriggerId(exchange);
      
      ServiceFactory sf =  ClientEnvironment.getCurrentServiceFactory();   
      IModel model = extractModel(sf, modelId);
      ITrigger trigger = extractTriggerDefinitionFromModel(model, processId, triggerId);
      List<AccessPointProperties> accessPointList = performParameterMapping(trigger);

      for (AccessPointProperties accessPoint : accessPointList)
      {
         if (accessPoint.getAccessPointType().equalsIgnoreCase("struct") && StringUtils.isEmpty(accessPoint.getDataPath()))
            converter.unmarshal(model, accessPoint);
         else
            logger.debug("ignoring conversion for "+accessPoint);
            
      }
   }

   @SuppressWarnings("unchecked")
   private void processMarshalling(Exchange exchange, IApplicationTypeConverter converter)
         throws Exception
   {
      copyInToOut(exchange);

      List<ActivityInstance> instances = lookupActivityInstance(exchange);
      for (Iterator<ActivityInstance> i = instances.iterator(); i.hasNext();)
      {
         ActivityInstance activityInstance = i.next();
         ApplicationContext ctx = lookupApplicationContext(activityInstance);

         Map<String, Object> extendedAttributes = activityInstance.getActivity()
               .getApplication().getAllAttributes();

         List<DataMapping> dataMappings = new ArrayList<DataMapping>();
         dataMappings = activityInstance.getActivity().getApplicationContext(ctx.getId())
               .getAllInDataMappings();
         for (Iterator<DataMapping> ai = dataMappings.iterator(); ai.hasNext();)
         {
            DataMapping mapping = ai.next();
            if (!(mapping.getMappedType().getName().equals(Document.class.getName())))
            {
               converter.marshal(mapping, extendedAttributes);
            }
            else
            {
               if (converter instanceof AbstractIApplicationTypeConverter)
               {
                  if (((AbstractIApplicationTypeConverter) converter)
                        .isStuctured(mapping))
                  {
                     Object dataMap = ((AbstractIApplicationTypeConverter) converter)
                           .findDataValue(mapping, extendedAttributes);
                     ((AbstractIApplicationTypeConverter) converter).replaceDataValue(
                           mapping, dataMap, extendedAttributes);
                  }
               }
            }
         }
      }
   }

   private IModel extractModel(final ServiceFactory sf, final String modelId)
   {
      return (IModel) sf.getWorkflowService().execute(new ServiceCommand()
     {
        private static final long serialVersionUID = 1L;

        public Serializable execute(ServiceFactory sf)
        {
           return ModelManagerFactory.getCurrent().findActiveModel(modelId);
        }
     });
   }

   private ITrigger extractTriggerDefinitionFromModel(IModel model, String processId,
         String triggerId)
   {

      ModelElementList<IProcessDefinition> processes = model.getProcessDefinitions();
      for (int pd = 0; pd < processes.size(); pd++)
      {

         IProcessDefinition process = model.getProcessDefinitions().get(pd);
         if (process.getId().equalsIgnoreCase(processId))
         {
            for (int i = 0; i < process.getTriggers().size(); i++)
            {

               ITrigger trigger = (ITrigger) process.getTriggers().get(i);

               if (CAMEL_TRIGGER_TYPE.equals(trigger.getType().getId())
                     && trigger.getId().equalsIgnoreCase(triggerId))
               {
                  return trigger;

               }
            }
         }
         continue;
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   private void processUnmarshalling(Exchange exchange,
         IApplicationTypeConverter converter) throws Exception
   {
      copyInToOut(exchange);
      List<ActivityInstance> instances = lookupActivityInstance(exchange);
      for (Iterator<ActivityInstance> i = instances.iterator(); i.hasNext();)
      {
         ActivityInstance activityInstance = i.next();
         ApplicationContext ctx = lookupApplicationContext(activityInstance);
         Map<String, Object> extendedAttributes = activityInstance.getActivity()
               .getApplication().getAllAttributes();
         List<DataMapping> dataMappings = new ArrayList<DataMapping>();
         dataMappings = activityInstance.getActivity().getApplicationContext(ctx.getId())
               .getAllOutDataMappings();
         for (Iterator<DataMapping> ai = dataMappings.iterator(); ai.hasNext();)
         {
            DataMapping mapping = ai.next();
            if (!(mapping.getMappedType().getName().equals(Document.class.getName())))
            {
               converter.unmarshal(mapping, extendedAttributes);
            }
         }
      }
   }

  
   private String extractPartitionId(Exchange exchange)
   {
      return (String) exchange.getIn().getHeader(PARTITION);
   }

   private String extractModelId(Exchange exchange)
   {
      return (String) exchange.getIn().getHeader(MODEL_ID);
   }

   private String extractProcessId(Exchange exchange)
   {
      return (String) exchange.getIn().getHeader(PROCESS_ID);
   }

   private String extractOrigin(Exchange exchange)
   {
      return (String) exchange.getIn().getHeader(ORIGIN);
   }

   private String extractTriggerId(Exchange exchange)
   {
      return (String) exchange.getIn().getHeader(TRIGGER_ID);
   }

   public static ApplicationContext lookupApplicationContext(
         ActivityInstance activityInstance)
   {

      return activityInstance.getActivity().getApplicationContext("application") != null
            ? activityInstance.getActivity().getApplicationContext("application")
            : activityInstance.getActivity().getApplicationContext("default");
   }

   public static List<ActivityInstance> lookupActivityInstance(Exchange exchange)
   {

      List<ActivityInstance> instances = new ArrayList<ActivityInstance>();

      BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();

      if (bpmRt != null && bpmRt.getCurrentActivityInstance() != null)
      {

         IActivityInstance activityInstance = bpmRt.getCurrentActivityInstance();

         instances.add(DetailsFactory.create(activityInstance, IActivityInstance.class,
               ActivityInstanceDetails.class));
      }
      else
      {
         Message message = null;
         ActivityInstances activityInstances = null;

         if (!exchange.hasOut())
         {
            message = exchange.getIn();
         }
         else
         {
            message = exchange.getOut();
         }

         activityInstances = (ActivityInstances) message
               .getHeader(CamelConstants.MessageProperty.ACTIVITY_INSTANCES);

         return activityInstances;

      }

      return instances;
   }
}