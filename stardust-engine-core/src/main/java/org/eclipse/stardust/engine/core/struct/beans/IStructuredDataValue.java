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
package org.eclipse.stardust.engine.core.struct.beans;

import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;

/**
 * Describes the value of a structured data entry  
 */
public interface IStructuredDataValue
{

   public static final String STRUCTURED_DATA_NAMESPACE = "uri:ag.carnot.structureddata";

   public static final String STRUCTURED_DATA_NAMESPACE_PREFIX = "structureddata";

   public static final long NO_PARENT = -1;
   
   public static final String ROOT_ELEMENT_NAME = "structureddataroot";

   public static final String ENTRY_OID_ATTRIBUTE_NAME = "oid";

   public long getOID();

   public IProcessInstance getProcessInstance();

   public long getParentOID();

   public String getEntryKey();

   public long getXPathOID();

   public Object getValue();

   public int getType();
   
   public boolean isRootEntry();

   public boolean isAttribute();

   public boolean isElement();

   void refresh();

}
