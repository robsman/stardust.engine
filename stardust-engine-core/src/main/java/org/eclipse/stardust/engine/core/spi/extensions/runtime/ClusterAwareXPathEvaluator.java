/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;

public class ClusterAwareXPathEvaluator implements ExtendedAccessPathEvaluator, Stateless
{
   public ClusterAwareXPathEvaluator()
   {   
   }
      
   public Object evaluate(IProcessInstance processInstance, IData data, String xPathExpression)
   {
      //workaround for modeler 'bug'
      final String xPath;
      if(xPathExpression != null)
      {
         xPath = xPathExpression;
      }
      else
      {
         xPath = "";
      }
      
      IXPathMap xPathMap = DataXPathMap.getXPathMap(data);
      TypedXPath typedXPath = xPathMap.getXPath(xPath); 
      if(typedXPath != null)
      {
         if(StructuredDataXPathUtils.isPrimitiveType(typedXPath))
         {
            return getValue(processInstance, xPathMap, typedXPath);
         }
         else if(StructuredDataXPathUtils.isMapType(typedXPath))
         {
            Map complexType = new HashMap();
            evaluate(complexType, processInstance, xPathMap, typedXPath);
            return complexType;
         }
      }

      return null;
   }
   
   private void evaluate(Map parent, IProcessInstance processInstance, IXPathMap xPathMap, TypedXPath typedXPath)
   {
      List<TypedXPath> childs = typedXPath.getChildXPaths();
      for (TypedXPath child : childs)
      {
         if(StructuredDataXPathUtils.isPrimitiveType(child))
         {
            Object value = getValue(processInstance, xPathMap, child);
            addToParent(parent, child.getId(), value);
         } 
         if (StructuredDataXPathUtils.isMapType(child))
         {
            Map newParent = new HashMap();
            addToParent(parent, child.getId(), newParent);
            evaluate(newParent, processInstance, xPathMap, child);
         }
      }
   }
   
   private void addToParent(Object listOrMapParent, String attributeName, Object value)
   {
      if(listOrMapParent instanceof Map)
      {
         ((Map) listOrMapParent).put(attributeName, value);
      }
      else if(listOrMapParent instanceof List) {
         ((List) listOrMapParent).add(value);
      }
   }
   
   private Object getValue(IProcessInstance processInstance, IXPathMap xPathMap, TypedXPath typedXPath)
   {
      if(StructuredDataXPathUtils.isPrimitiveType(typedXPath))
      {
         ProcessInstanceBean dataSource = (ProcessInstanceBean) processInstance;         
         long xPathOid = xPathMap.getXPathOID(typedXPath.getXPath());
         StructuredDataValueBean valueHolder = (StructuredDataValueBean) dataSource.getCachedStructuredDataValue(xPathOid);
         if(valueHolder != null)
         {
            return valueHolder.getValue();
         }
      }
      
      if(StructuredDataXPathUtils.isMapType(typedXPath))
      {
         return new HashMap();
      }
      
      return null;
   }
   
   @Override
   public boolean isStateless()
   {
      return false;
   }

   @Override
   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance,
         String outPath, AccessPathEvaluationContext accessPathEvaluationContext)
   {
      IProcessInstance processInstance = accessPathEvaluationContext.getProcessInstance();
      IData data = (IData) accessPointDefinition;
      return evaluate(processInstance, data, outPath);
   }

   @Override
   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance,
         String inPath, AccessPathEvaluationContext accessPathEvaluationContext,
         Object value)
   {
      IProcessInstance processInstance = accessPathEvaluationContext.getProcessInstance();
      IData data = (IData) accessPointDefinition;
      return evaluate(processInstance, data, inPath);
   }

   @Override
   public Object createInitialValue(AccessPoint accessPointDefinition,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return null;
   }

   @Override
   public Object createDefaultValue(AccessPoint accessPointDefinition,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return null;
   }
}
