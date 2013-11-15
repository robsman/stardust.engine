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
package org.eclipse.stardust.engine.extensions.transformation.runtime.transformation;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.AccessPointDetails;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.eclipse.stardust.engine.extensions.transformation.javascript.JScriptManager3;
import org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping;
import org.eclipse.stardust.engine.extensions.transformation.runtime.transformation.javascript.ContextProvider3;
import org.mozilla.javascript.Context;



public class MessageTransformationApplicationInstance
      implements SynchronousApplicationInstance
{
   public static final Logger trace = LogManager.getLogger(MessageTransformationApplicationInstance.class);

   /**
    * ID and value of all IN access points.
    */
   private Map/*<String, Object>*/ inAccessPointValues = CollectionUtils.newMap();

   private Map/*<String, Object>*/ outputMessages = CollectionUtils.newMap();

   List sourceTypes;

   List targetTypes;

   private ContextProvider3 jsContextProvider;

   private JScriptManager3 jsManager;

   public void bootstrap(ActivityInstance activityInstance)
   {
      trace.info("bootstrap");
      Application application = activityInstance.getActivity().getApplication();
      this.jsContextProvider = new ContextProvider3();
      this.jsManager = jsContextProvider.getOrCreateSharedContext(application);
   }

   /**
     * 
     */
   public void setInAccessPointValue(String name, Object value)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Setting IN access point '" + name + "' to value " + value);
      }

      inAccessPointValues.put(name, value);
   }

   /**
    * Only for processing in data mappings.
    */
   public Object getOutAccessPointValue(String name)
   {
      try
      {
         return doGetOutAccessPointValue(name, false);
      }
      catch (InvocationTargetException e)
      {
         throw new InternalException(e.getMessage(), e.getTargetException());
      }
   }

   public void cleanup()
   {
      trace.info("Cleaning up");
   }

   /**
    * 
    * @param outDataTypes
    * @return
    * @throws InvocationTargetException
    */
   private Map doGetOutAccessPointValues(Set outDataTypes)
         throws InvocationTargetException
   {
      Map result = CollectionUtils.newMap();

      for (Iterator i = outDataTypes.iterator(); i.hasNext();)
      {
         String name = (String) i.next();

         result.put(name, doGetOutAccessPointValue(name, true));
      }

      return result;
   }

   /**
    * 
    * @param name
    * @param allowReturnValue
    * @return
    * @throws InvocationTargetException
    */
   private Object doGetOutAccessPointValue(String name, boolean allowReturnValue)
         throws InvocationTargetException
   {
      return outputMessages.get(name);
   }

   /**
    * 
    */
   public Map invoke(Set outDataTypes) throws InvocationTargetException
   {
      trace.info("Invoking message transformation");

      try
      {
         for (Iterator i = outDataTypes.iterator(); i.hasNext();)
         {
            String outApId = (String) i.next();

            // TODO must create initial value with AccessPathEvaluator?
            outputMessages.put(outApId, CollectionUtils.newMap());
         }

         // TODO initialize should precompile statements
         jsManager.initializeContext(inAccessPointValues, outputMessages, jsContextProvider.getExternalClasses());

         for (int i = 0; i < jsContextProvider.getFieldMappings().size(); i++ )
         {
            FieldMapping fieldMapping = (FieldMapping) jsContextProvider.getFieldMappings().get(i);
            if (fieldMapping.isContentMapping() && !fieldMapping.isAdvancedMapping()) {
            	String targetPath = (String) fieldMapping.getFieldPath();            	                  
                String jsPath = xPathToJavaScriptPath(targetPath);
            	fieldMapping.setMappingExpression(jsPath + ".setContent(" + fieldMapping.getMappingExpression() + ")"); 
            }            
            final Object result = jsManager.executeMapping(fieldMapping.getMappingExpression());

            if (!fieldMapping.isAdvancedMapping() || this.isPrimitiveTarget(fieldMapping))            
            {
               // assign result to slot in target message
               String targetPath = (String) fieldMapping.getFieldPath();
               String outputTarget = targetPath.substring(0, targetPath.indexOf("/"));
               if (outDataTypes.contains(outputTarget))
               {
                  targetPath = targetPath.substring(targetPath.indexOf("/") + 1,
                        targetPath.length());
                  
                  String jsPath = xPathToJavaScriptPath(targetPath);
                  
                  // directly assign to target message
                  String path = outputTarget + "." + jsPath; 
                  if (path.endsWith(".")) {
                     path = path.substring(0, path.length() - 1);
                     outputMessages.put(path, result);
                  } else {
                     jsManager.executeTargetAssignment(path, result);                     
                  }
               }
            }
         }

         // Write output messages

         for (Iterator iterator = outDataTypes.iterator(); iterator.hasNext();)
         {
            String accessPointID = (String) iterator.next();
            Object values = outputMessages.get(accessPointID);
            /*if (values instanceof Map)
            {
               Map mapValues = (Map) outputMessages.get(accessPointID);   
            }*/
            if (isJavaBeanAP(accessPointID))
            {
            	MessageTransformationScope scope = (MessageTransformationScope) jsManager.getScope();
            	outputMessages.put(accessPointID, scope.getOutputMessagAdapters().get(accessPointID));
            }
            else
            {
            	outputMessages.put(accessPointID, jsManager.unwrapJsValue(values));
            }
         }
      }
      catch (Exception e)
      {
         try
         {
            Context.exit();
         }
         catch (Exception ie) 
         {
            // log this exception...
            trace.warn("Exception during Context.exit()", ie);
            // ... but show original exception
         }
         throw new InvocationTargetException((Throwable) e,
               "Could not perform message transformation.");
      }

      return doGetOutAccessPointValues(outDataTypes);
   }

   private boolean isJavaBeanAP(String accessPointID) {
	Object o = jsManager.getOutAccessPointTypes().get(accessPointID);
	if (o instanceof AccessPointDetails) {
		AccessPointDetails apd = (AccessPointDetails)o;
		if (apd.getAccessPathEvaluatorClass().indexOf("Java") > -1) {
			return true;
		}
	}
	return false;
}
   
	private boolean isPrimitiveTarget(FieldMapping fieldMapping) {
        String targetPath = (String) fieldMapping.getFieldPath();
        String outputTarget = targetPath.substring(0, targetPath.indexOf("/"));
        MessageTransformationScope scope = (MessageTransformationScope) jsManager.getScope();
        Object outputAdapter = scope.getOutputMessagAdapters().get(outputTarget); 
        return (outputAdapter instanceof String);
	}

private String xPathToJavaScriptPath(String xPath)
   {
      StringBuffer buffer = new StringBuffer(xPath);

      // TODO map attribute names (prefix attr instead of @)?
      // TODO map namespace prefixes
      for (int i = buffer.indexOf("/"); -1 != i; i = buffer.indexOf("/", i))
      {
         buffer.replace(i, i + 1, ".");
      }

      int lastCharIndex = buffer.length() - 1;
      if (lastCharIndex >= 0 && buffer.charAt(lastCharIndex) == '.')
      {
         buffer.setLength(lastCharIndex);
      }
      return buffer.toString();
   }

}
