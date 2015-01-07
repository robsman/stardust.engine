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
package org.eclipse.stardust.engine.core.compatibility.el;

import java.util.HashMap;

import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.model.beans.DataBean;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataType;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.extensions.xml.data.XMLDataType;


public class TestSymbolTable implements SymbolTable
{
   private static final IDataType javaType = new JavaType();
   private static final IDataType xmlType = new XmlType();

   private HashMap types = new HashMap();
   private HashMap values = new HashMap();

   public AccessPoint lookupSymbolType(String name)
   {
      return (IData) types.get(name);
   }

   public Object lookupSymbol(String name)
   {
      return values.get(name);
   }

   public void registerSer(String name, Object value)
   {
      values.put(name, value);
      types.put(name, new DataBean(name, javaType, name, null, false, new HashMap()));
   }

   public void registerXml(String name, Object value)
   {
      values.put(name, value);
      types.put(name, new DataBean(name, xmlType, name, null, false, new HashMap()));
   }

   private static class XmlType extends XMLDataType
   {
      private static final long serialVersionUID = 1L;
   }

   private static class JavaType extends JavaDataType
   {
      private static final long serialVersionUID = 1L;
   }
}