package org.eclipse.stardust.engine.extensions.camel.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;

public class BpmTypeConverter
{
   public static final Logger logger = LogManager.getLogger(BpmTypeConverter.class);

   private static final Map<String, Class< ? >> primitiveClasses = new HashMap<String, Class< ? >>();

   static
   {
      primitiveClasses.put("byte", byte.class);
      primitiveClasses.put("short", short.class);
      primitiveClasses.put("char", char.class);
      primitiveClasses.put("int", int.class);
      primitiveClasses.put("long", long.class);
      primitiveClasses.put("float", float.class);
      primitiveClasses.put("double", double.class);
   }

   public void toJSON(Exchange exchange) throws Exception
   {
      processMarshalling(exchange, new JsonTypeConverter(exchange));
   }

   public void fromJSON(Exchange exchange) throws Exception
   {
      processUnmarshalling(exchange, new JsonTypeConverter(exchange));
   }

   public void toNativeObject(Exchange exchange) throws Exception
   {
      processMarshalling(exchange, new JavaScriptTypeConverter(exchange, JsonTypeConverter.LONG_DATA_FORMAT));
   }

   public void fromNativeObject(Exchange exchange) throws Exception
   {
      processUnmarshalling(exchange, new JavaScriptTypeConverter(exchange, JsonTypeConverter.ISO_DATE_FORMAT));
   }

   public void fromXML(Exchange exchange) throws Exception
   {
      processUnmarshalling(exchange, new XmlTypeConverter(exchange));
   }

   public void toXML(Exchange exchange) throws Exception
   {
      processMarshalling(exchange, new XmlTypeConverter(exchange));
   }

   public void fromList(Exchange exchange) throws Exception
   {
      processUnmarshalling(exchange, new ListTypeConverter(exchange));
   }

   public void toList(Exchange exchange) throws Exception
   {
      processMarshalling(exchange, new ListTypeConverter(exchange));
   }

   @SuppressWarnings("unchecked")
   private void processMarshalling(Exchange exchange, IBpmTypeConverter converter) throws Exception
   {
      this.copyInToOut(exchange);

      List<ActivityInstance> instances = lookupActivityInstance(exchange);
      for (Iterator<ActivityInstance> i = instances.iterator(); i.hasNext();)
      {
         ActivityInstance activityInstance = i.next();
         ApplicationContext ctx = lookupApplicationContext(activityInstance);

         Map<String, Object> extendedAttributes = activityInstance.getActivity().getApplication().getAllAttributes();

         List<DataMapping> dataMappings = new ArrayList<DataMapping>();
         dataMappings = activityInstance.getActivity().getApplicationContext(ctx.getId()).getAllInDataMappings();
         for (Iterator<DataMapping> ai = dataMappings.iterator(); ai.hasNext();)
         {
            DataMapping mapping = ai.next();
            converter.marshal(mapping, extendedAttributes);
         }
      }
   }

   @SuppressWarnings("unchecked")
   private void processUnmarshalling(Exchange exchange, IBpmTypeConverter converter) throws Exception
   {
      this.copyInToOut(exchange);

      List<ActivityInstance> instances = lookupActivityInstance(exchange);
      for (Iterator<ActivityInstance> i = instances.iterator(); i.hasNext();)
      {
         ActivityInstance activityInstance = i.next();
         ApplicationContext ctx = lookupApplicationContext(activityInstance);

         Map<String, Object> extendedAttributes = activityInstance.getActivity().getApplication().getAllAttributes();

         List<DataMapping> dataMappings = new ArrayList<DataMapping>();
         dataMappings = activityInstance.getActivity().getApplicationContext(ctx.getId()).getAllOutDataMappings();
         for (Iterator<DataMapping> ai = dataMappings.iterator(); ai.hasNext();)
         {
            DataMapping mapping = ai.next();
            converter.unmarshal(mapping, extendedAttributes);
         }
      }
   }

   private void copyInToOut(Exchange exchange)
   {
      exchange.getOut().setHeaders(exchange.getIn().getHeaders());
      exchange.getOut().setBody(exchange.getIn().getBody());
   }

   public static ApplicationContext lookupApplicationContext(ActivityInstance activityInstance)
   {

      return activityInstance.getActivity().getApplicationContext("application") != null ? activityInstance
            .getActivity().getApplicationContext("application") : activityInstance.getActivity().getApplicationContext(
            "default");
   }

   public static List<ActivityInstance> lookupActivityInstance(Exchange exchange)
   {

      List<ActivityInstance> instances = new ArrayList<ActivityInstance>();

      BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();

      if (bpmRt != null && bpmRt.getCurrentActivityInstance() != null)
      {

         IActivityInstance activityInstance = bpmRt.getCurrentActivityInstance();

         instances.add(DetailsFactory.create(activityInstance, IActivityInstance.class, ActivityInstanceDetails.class));
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

         activityInstances = (ActivityInstances) message.getHeader(CamelConstants.MessageProperty.ACTIVITY_INSTANCES);

         return activityInstances;

      }

      return instances;
   }
}