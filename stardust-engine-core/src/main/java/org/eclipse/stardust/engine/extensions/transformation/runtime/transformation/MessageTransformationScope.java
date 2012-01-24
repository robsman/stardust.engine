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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.AccessPointDetails;
import org.eclipse.stardust.engine.core.javascript.StaticJavaClassAccessor;
import org.eclipse.stardust.engine.core.javascript.StructuredDataMapAccessor;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.transformation.model.mapping.ExternalClass;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;



/**
 * @author sauer
 * @version $Revision$
 */
public class MessageTransformationScope extends ScriptableObject
{
   
   private static final long serialVersionUID = 1L;

   private final Map inputMessagAdapters;

   private final Map outputMessagAdapters;
   
   private final Map externalClassesAdapters;
     
   public MessageTransformationScope(Map inputMessages, Map outputMessages,
         Map inApTypes, Map outApTypes, List externalClasses)
   {
      this.inputMessagAdapters = CollectionUtils.newMap();
      for (Iterator i = inputMessages.keySet().iterator(); i.hasNext();)
      {
         String msgId = (String) i.next();
         Object value = inApTypes.get(msgId);
         //StructuredData ?
         if (value instanceof TypedXPath) {
            TypedXPath msgType = (TypedXPath) value;
            inputMessagAdapters.put(msgId, new StructuredDataMapAccessor(msgType,
                  (Map) inputMessages.get(msgId), true));            
         } else {                                    
            inputMessagAdapters.put(msgId, inputMessages.get(msgId));        	 
         }
      }

      this.outputMessagAdapters = CollectionUtils.newMap();
      for (Iterator i = outputMessages.keySet().iterator(); i.hasNext();)
      {
         String msgId = (String) i.next();
         Object value = outApTypes.get(msgId);
         //StructuredData ?
         if (value instanceof TypedXPath) {
            TypedXPath msgType = (TypedXPath) value;
            outputMessagAdapters.put(msgId, new StructuredDataMapAccessor(msgType,
                  (Map) outputMessages.get(msgId), false));            
         } else {
            	if (value instanceof AccessPointDetails) {
            		AccessPointDetails apd = (AccessPointDetails)value;
            		if (apd.getAccessPathEvaluatorClass().indexOf("Java") > -1) {
            			String className = (String) apd.getAttribute("carnot:engine:className");
                    	try {
             				outputMessagAdapters.put(msgId, Class.forName(className).newInstance());
             			} catch (InstantiationException e) {
             				e.printStackTrace();
             			} catch (IllegalAccessException e) {
             				e.printStackTrace();
             			} catch (ClassNotFoundException e) {
             				e.printStackTrace();
             			}
            		} else {
            			outputMessagAdapters.put(msgId, "primitive");	
            		}
            	} 
            }
      }
      
      this.externalClassesAdapters = CollectionUtils.newMap();
      for (Iterator i = externalClasses.iterator(); i.hasNext();) {
    	  ExternalClass externalClass = (ExternalClass) i.next();    	  
    	  String fullClassName = externalClass.getClassName();    	      	 
    	  String instanceName = externalClass.getInstanceName();	 
    	  try {
			externalClassesAdapters.put(instanceName, Class.forName(fullClassName).newInstance());	
		} catch (Throwable t) {
			t.printStackTrace();
			try {
				externalClassesAdapters.put(instanceName, new StaticJavaClassAccessor(Class.forName(fullClassName)));				
			}
			catch(Throwable e) {
				e.printStackTrace();
			}
		}
       }
   }
   
   public String getClassName()
   {
      return "Message Transformation Scope";
   }

   public boolean has(String name, Scriptable start)
   {
      if ((start == this)
            && (inputMessagAdapters.containsKey(name) || outputMessagAdapters.containsKey(name) || externalClassesAdapters.containsKey(name)))
      {
         return true;
      }
      else
      {
         return super.has(name, start);
      }
   }

   public Object get(String name, Scriptable start)
   {
      if (this == start)
      {
         if (inputMessagAdapters.containsKey(name))
         {
            return inputMessagAdapters.get(name);
         }
         else if (outputMessagAdapters.containsKey(name))
         {
            return outputMessagAdapters.get(name);
         }
         else if (externalClassesAdapters.containsKey(name))
         {
            return externalClassesAdapters.get(name);
         }         
      }

      return super.get(name, start);
   }

   public int getAttributes(String name)
   {
      if (inputMessagAdapters.containsKey(name))
      {
         return READONLY | PERMANENT;
      }
      else if (outputMessagAdapters.containsKey(name))
      {
         return PERMANENT;
      }
      else if (externalClassesAdapters.containsKey(name))
      {
         return PERMANENT;
      }
      else
      {
         return super.getAttributes(name);
      }
   }

   public Map getInputMessagAdapters()
   {
      return Collections.unmodifiableMap(inputMessagAdapters);
   }

   public Map getOutputMessagAdapters()
   {
      return Collections.unmodifiableMap(outputMessagAdapters);
   }
   
   public Map getExternalClassesAdapters()
   {
      return Collections.unmodifiableMap(externalClassesAdapters);
   }
}
