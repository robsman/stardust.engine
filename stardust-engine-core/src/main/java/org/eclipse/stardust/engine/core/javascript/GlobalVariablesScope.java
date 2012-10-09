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
package org.eclipse.stardust.engine.core.javascript;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.ISchemaType;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.model.IXpdlType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.xsd.XSDEnumerationFacet;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


/**
 * @author sauer
 * @version $Revision$
 */
public class GlobalVariablesScope extends ScriptableObject
{
   private static final long serialVersionUID = 1L;

   private final IModel model;
   
   private Context context;
   
   private final ThreadLocal symbolTableHolder = new ThreadLocal();
   
   private final ThreadLocal adaptersHolder = new ThreadLocal();

   public GlobalVariablesScope(IModel model, Context context)
   {
      this.model = model;
      this.context = context;
   }
   
   public SymbolTable getSymbolTableForThread()
   {
      return (SymbolTable) symbolTableHolder.get();
   }
   
   public void bindThreadLocalSymbolTable(SymbolTable symbolTable)
   {
      symbolTableHolder.set(symbolTable);
   }
   
   public void unbindThreadLocalSymbolTable()
   {
      symbolTableHolder.set(null);

      adaptersHolder.set(null);
   }
   
   public String getClassName()
   {
      return "Global Variables Scope";
   }

   public boolean has(String name, Scriptable start)
   {
      if (start == this)
      {
         SymbolTable symbolTable = getSymbolTableForThread();
         if (null != symbolTable)
         {
            if (symbolTable.lookupSymbolType(name) != null)
            {
               return true;
            }
         }
      }
      return super.has(name, start);
   }

   public Object get(String name, Scriptable start)
   {
      SymbolTable symbolTable = getSymbolTableForThread();
      if (null != symbolTable)
      {
         AccessPoint data = symbolTable.lookupSymbolType(name);
         if (data != null)
         {
            Object value = symbolTable.lookupSymbol(name);
            if (null != value && null != data.getType())
            {
               ExtendedAccessPathEvaluator evaluator = SpiUtils.createExtendedAccessPathEvaluator(data, null);
               
               AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(symbolTable, null);
               value = evaluator.evaluate(data, value, null, evaluationContext);
            }
            
            // wrap non-Java APs with accessor
            // TODO implement in terms of SPI, may be on Evaluator or Validator
            IDataType dataType = (IDataType) data.getType();
            Map adapters = (Map) adaptersHolder.get();
            if (null == adapters)
            {
               adapters = CollectionUtils.newMap();
               adaptersHolder.set(adapters);
            }
            if (value instanceof ActivityInstance)
            {
               value = new ActivityInstanceAccessor((ActivityInstance) value);
               return value;
            }
            if (PredefinedConstants.SERIALIZABLE_DATA.equals(dataType.getId())) {               
            	//If this is an Enum we'll wrap the contained values with the generic EnumAccessor
            	String className = (String) data.getAttribute(PredefinedConstants.CLASS_NAME_ATT);
            	try {
					if (className != null) {
	            		Class clazz = Class.forName(className);
						if (clazz.isEnum()) {
							Field[] fields = clazz.getFields();
							Method[] methods = clazz.getMethods();							
						    value = new EnumAccessor(fields, methods, value);
							return value;
						}						
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
            }            
            if (PredefinedConstants.STRUCTURED_DATA.equals(dataType.getId()))
            {          	  
              //Check if this a Enumeration --> return EnumAccessor
              List<XSDEnumerationFacet> facets = checkEnumeration(data);
        	  if (facets != null) {
        		  value = new EnumAccessor(facets, value);
        		  return value;
        	  }	
        	  if (!(value instanceof String)) {
                  IXPathMap xPathMap = DataXPathMap.getXPathMap(data);   
                  StructuredDataMapAccessor adapter = (StructuredDataMapAccessor) adapters.get(data.getId());
                  if (null == adapter)
                  {                                                      
                	 adapter = new StructuredDataMapAccessor(xPathMap.getRootXPath(), (Map) value, true);                  
                     adapters.put(data.getId(), adapter);
                  }               
                  adapter.bindValue(value);              
                  value = adapter;        		  
        	  }
            }
            else if (PredefinedConstants.PLAIN_XML_DATA.equals(dataType.getId()))
            {
               PlainXmlDataAccessor adapter = (PlainXmlDataAccessor) adapters.get(data.getId());
               if (null == adapter)
               {
                  // TODO (ab) XPathEvaluator converts the data value to DOM element and then to String
                  // PlainXmlDataAccessor converts it in turn to SXML document. This needs to be simplified
                  adapter = new PlainXmlDataAccessor((String)value);
                  adapters.put(data.getId(), adapter);
               }
               
               value = adapter;
            }
            // compatibility with carnotEL expressions: all objects that are not Scriptable will be wrapped
            if (value != null && !(value instanceof Scriptable))
            {
               // this is needed, otherwise CRNT-10902
               context.getWrapFactory().setJavaPrimitiveWrap(false);
               value = context.getWrapFactory().wrap(context, this, value, value.getClass());
            }
            return value;
         }
      }
      return super.get(name, start);
   }

   public int getAttributes(String name)
   {
      if (null != model.findData(name))
      {
         return READONLY | PERMANENT;
      }
      else
      {
         return super.getAttributes(name);
      }
   }
   
   private List<XSDEnumerationFacet> checkEnumeration(AccessPoint data) {
		Object type = data.getAttribute("carnot:engine:dataType");
		  if (type != null) {                	  
			  String typeString = type.toString();
			  ITypeDeclaration decl = model.findTypeDeclaration(typeString);
			  IXpdlType xpdlType = decl.getXpdlType();
			  if (xpdlType instanceof ISchemaType) {
				ISchemaType schema = (ISchemaType)xpdlType;
				List<XSDSchemaContent> schemaContent = schema.getSchema().getContents();
				if (!schemaContent.isEmpty()) {
					if (schemaContent.get(0) instanceof XSDSimpleTypeDefinition) {
						XSDSimpleTypeDefinition st = (XSDSimpleTypeDefinition) schemaContent.get(0);
						if (!st.getEnumerationFacets().isEmpty()) {
							return st.getEnumerationFacets();						
						}
					}
				}
			  }                	  
		  }
		  return null;
	}

}
