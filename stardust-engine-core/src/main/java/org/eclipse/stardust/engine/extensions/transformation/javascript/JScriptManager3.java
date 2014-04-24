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
package org.eclipse.stardust.engine.extensions.transformation.javascript;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.javascript.AbstractStructuredDataAccessPointAdapter;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.transformation.runtime.transformation.MessageTransformationScope;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;



public class JScriptManager3
{

   private Map/*<String, TypedXPath>*/ inAccessPointTypes = CollectionUtils.newMap();

   private Map/*<String, TypedXPath>*/ outAccessPointTypes = CollectionUtils.newMap();

   public Map getOutAccessPointTypes() {
	return outAccessPointTypes;
}

private ContextFactory contextFactory;

   private Context context;

   private ScriptableObject scope;

   public ScriptableObject getScope() {
	return scope;
}

public void registerInAccessPointType(String apId, TypedXPath rootXPath)
   {
      inAccessPointTypes.put(apId, rootXPath);
   }

   public void registerOutAccessPointType(String apId, TypedXPath rootXPath)
   {
      outAccessPointTypes.put(apId, rootXPath);
   }
   
   public void registerInAccessPointType(IModel model, AccessPoint accessPoint)
   {
      if (accessPoint.getAccessPathEvaluatorClass().indexOf("Structured") > -1) {
         String typeDeclarationId = (String) accessPoint.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
         Set xPaths = StructuredTypeRtUtils.getAllXPaths(model, typeDeclarationId);
         IXPathMap xPathMap = new ClientXPathMap(xPaths);
         inAccessPointTypes.put(accessPoint.getId(), xPathMap.getRootXPath());         
      } else {
         inAccessPointTypes.put(accessPoint.getId(), accessPoint);
      }
   }

   public void registerOutAccessPointType(IModel model, AccessPoint accessPoint)
   {
      if (accessPoint.getAccessPathEvaluatorClass().indexOf("Structured") > -1) {
         String typeDeclarationId = (String) accessPoint.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
         Set xPaths = StructuredTypeRtUtils.getAllXPaths(model, typeDeclarationId);
         IXPathMap xPathMap = new ClientXPathMap(xPaths);
         outAccessPointTypes.put(accessPoint.getId(), xPathMap.getRootXPath());         
      } else {
         outAccessPointTypes.put(accessPoint.getId(), accessPoint);
      }
   }

   // //////////////////////////////////////////////////////////////////////////////////////////////////////////

   public void initializeContext(Map inputMessagesMap, Map outputMessagesMap, List externalClasses)
   {
      this.contextFactory = ContextFactory.getGlobal();

      this.context = contextFactory.enterContext();
      context.setOptimizationLevel( -1);

      this.scope = new MessageTransformationScope(inputMessagesMap, outputMessagesMap,
            inAccessPointTypes, outAccessPointTypes, externalClasses);

      context.initStandardObjects(scope);
   }

   public Object executeMapping(String mappingExpression)
   {
      if (!StringUtils.isEmpty(mappingExpression))
      {
         Script expression = context.compileString(mappingExpression, mappingExpression, 1, null);
         try
         {
            return expression.exec(context, scope);
         }
         catch (Throwable t)
         {
            throw new RuntimeException("Could not execute mapping expression '" + mappingExpression + "'", t);
         }
      }
      return null;
   }

   public Object executeTargetAssignment(String mappingExpression, Object rhsValue)
   {
      if (null != mappingExpression)
      {
         Script expression = context.compileString(mappingExpression + " = ippRhsValue",
               mappingExpression, 1, null);
         try
         {
            scope.defineProperty("ippRhsValue", rhsValue, ScriptableObject.READONLY);
            try
            {
               Object result = expression.exec(context, scope);
               return result;
            }
            finally
            {
               scope.delete("ippRhsValue");
            }
         }
         catch (Throwable t)
         {
            throw new RuntimeException("Could not execute target assignment to '"+mappingExpression+"'", t);
         }
      }
      return null;
   }

   public Context getContext()
   {
      return context;
   }

   public Object unwrapJsValue(Object value)
   {
	 Object result;
	 result = value;     
	 if (value instanceof AbstractStructuredDataAccessPointAdapter)
	 {
	    result = ((AbstractStructuredDataAccessPointAdapter) value).unwrap();
	 }
	 else if (value instanceof NativeArray)
	 {
	    result = unwrapNativeArray((NativeArray) value);
	 }
	 else if (value instanceof NativeJavaObject)
	 {
	    result = ((NativeJavaObject)value).unwrap();
	 }
	 else if (value instanceof ScriptableObject)        
	 {
	    ScriptableObject so = (ScriptableObject)value;
	    if (so.toString().indexOf("NativeDate") > 0) {
	       Double seconds = (Double) Reflect.getFieldValue(value, "date");
	       Date date = new Date(seconds.longValue());
	       result = date; 
	    }  	    
	 }	 
     // unwrap hierarchically
     // TODO prevent infinity recursion?
     if (result instanceof Map)
     {
        for (Iterator i = ((Map) result).entrySet().iterator(); i.hasNext();)
        {
           final Map.Entry entry = (Map.Entry) i.next();
           final Object innerValue = entry.getValue();

           Object unwrappedInnerValues = unwrapJsValue(innerValue);
           if (unwrappedInnerValues != innerValue)
           {
              entry.setValue(unwrappedInnerValues);
           }
        }
     }
     else if (result instanceof List)
     {
        final List list = (List) result;
        for (int i = 0; i < list.size(); ++i)
        {
           final Object innerValue = list.get(i);
            
           Object unwrappedInnerValue = unwrapJsValue(innerValue);
           if (unwrappedInnerValue != innerValue)
           {
              list.set(i, unwrappedInnerValue);
           }
        }
     }
      
     return result;
   }
   
   public List unwrapNativeArray(NativeArray _array)
   {
      if (_array == null)
      {
         return null;
      }

      List list = CollectionUtils.newList((int) _array.getLength());
      for (int i = 0; i < _array.getLength(); i++ )
      {
         Object o = _array.get(i, null);
         if ((o instanceof NativeObject))
         {
            Map map = CollectionUtils.newMap();
            NativeObject no = (NativeObject) o;
            Object[] ids = no.getAllIds();
            for (int j = 0; j < ids.length; j++ )
            {
               String idName = (String) ids[j];
               Object value = no.get(idName, scope);
               if (value instanceof NativeArray)
               {
                  value = this.unwrapNativeArray((NativeArray) value);
               }
               map.put(idName, value);
            }
            list.add(map);
         }
         else if (o instanceof AbstractStructuredDataAccessPointAdapter)
         {
            list.add(unwrapJsValue(o));
         }
         else
         {
            list.add(Context.jsToJava(o, String.class));
         }
      }
      return list;
   }

}
