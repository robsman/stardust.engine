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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.ISchemaType;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.model.IXpdlType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.extensions.dms.data.AuditTrailUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.engine.extensions.dms.data.DmsFolderBean;
import org.eclipse.stardust.engine.extensions.dms.data.DmsResourceBean;
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

   private final ThreadLocal<SymbolTable> symbolTableHolder = new ThreadLocal<SymbolTable>();

   private final ThreadLocal adaptersHolder = new ThreadLocal();

   public GlobalVariablesScope(IModel model, Context context)
   {
      this.model = model;
      this.context = context;
   }

   public SymbolTable getSymbolTableForThread()
   {
      return symbolTableHolder.get();
   }

   public void bindThreadLocalSymbolTable(SymbolTable symbolTable)
   {
      symbolTableHolder.set(symbolTable);
   }

   public void unbindThreadLocalSymbolTable()
   {
      symbolTableHolder.remove();

      adaptersHolder.remove();
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
            if (value instanceof ProcessInstance)
            {
               System.out.println("Process Instance");
            }
            
            if (PredefinedConstants.SERIALIZABLE_DATA.equals(dataType.getId())) {
            	//If this is an Enum we'll wrap the contained values with the generic EnumAccessor
            	String className = (String) data.getAttribute(PredefinedConstants.CLASS_NAME_ATT);
            	try {
					if (className != null) {
	            		Class clazz = Reflect.getClassFromClassName(className);
						if (clazz.isEnum()) {
							Field[] fields = clazz.getFields();
							Method[] methods = clazz.getMethods();
						    value = new EnumAccessor(fields, methods, value);
							return value;
						}
					}
				} catch (InternalException e) {
					e.printStackTrace();
				}
            }
            if (PredefinedConstants.STRUCTURED_DATA.equals(dataType.getId()))
            {
               // Check if this a Enumeration --> return EnumAccessor
               List<XSDEnumerationFacet> facets = checkEnumeration(data);
               if (facets != null)
               {
                  value = new EnumAccessor(facets, value);
                  return value;
               }
               if ( !(value instanceof String))
               {
                  value = wrapStructuredDataMap(data, value, adapters);
               }
            }
            else if (StructuredTypeRtUtils.isDmsType(dataType.getId()))
            {
               if (value instanceof DmsResourceBean)
               {
                  // unwrap structured data lego resource map.
                  value = ((DmsResourceBean) value).vfsResource();
               }
               else if (value instanceof List)
               {
                  // wrap list type dms data back to structured data representation.
                  List< ? > valueList = (List< ? >) value;
                  if ( !valueList.isEmpty())
                  {
                     List<Map> legoList = null;
                     Object listEntry = valueList.get(0);
                     if (listEntry instanceof DmsDocumentBean)
                     {
                        legoList = convertToLegoList((List<DmsDocumentBean>) valueList);
                        value = Collections.singletonMap(AuditTrailUtils.DOCS_DOCUMENTS,
                              legoList);
                     }
                     else if (listEntry instanceof DmsFolderBean)
                     {
                        legoList = convertToLegoList((List<DmsFolderBean>) valueList);
                        value = Collections.singletonMap(AuditTrailUtils.FOLDERS_FOLDERS,
                              legoList);
                     }
                  }
               }

               value = wrapStructuredDataMap(data, value, adapters);
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

   private Object wrapStructuredDataMap(AccessPoint data, Object value, Map adapters)
   {
      IXPathMap xPathMap = DataXPathMap.getXPathMap(data);
      StructuredDataMapAccessor adapter = (StructuredDataMapAccessor) adapters.get(data.getId());
      if (null == adapter)
      {
         adapter = new StructuredDataMapAccessor(xPathMap.getRootXPath(),
               (Map) value, true);
         adapters.put(data.getId(), adapter);
      }
      adapter.bindValue(value);
      value = adapter;
      return value;
   }

   private List<Map> convertToLegoList(List< ? extends DmsResourceBean> valueList)
   {
      List<Map> legoList = CollectionUtils.newArrayList();
      for (Object object : valueList)
      {
         DmsResourceBean dmsResource = (DmsResourceBean) object;
         legoList.add(dmsResource.vfsResource());
      }
      return legoList;
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
      ITypeDeclaration decl = StructuredTypeRtUtils.getTypeDeclaration(data, model);
      if (decl != null)
      {
         IXpdlType xpdlType = decl.getXpdlType();
         if (xpdlType instanceof ISchemaType)
         {
            ISchemaType schema = (ISchemaType) xpdlType;
            List<XSDSchemaContent> schemaContent = schema.getSchema().getContents();
            if ( !schemaContent.isEmpty())
            {
               if (schemaContent.get(0) instanceof XSDSimpleTypeDefinition)
               {
                  XSDSimpleTypeDefinition st = (XSDSimpleTypeDefinition) schemaContent.get(0);
                  if ( !st.getEnumerationFacets().isEmpty())
                  {
                     return st.getEnumerationFacets();
                  }
               }
            }
         }
      }
      return null;
	}

}
