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
package org.eclipse.stardust.engine.core.struct;

import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;

/**
 * Creates instances of structured data internal representation
 */
public interface IStructuredDataValueFactory
{

   /**
    * Creates an {@link IStructuredDataValue} instance, that corresponds to an XML element
    * 
    * @param rootOid
    * @param parentOid
    * @param xPathOid
    * @param index
    * @param value
    * @param typeKey
    * @return
    */
   public IStructuredDataValue createKeyedElementEntry(IProcessInstance scopeProcessInstance, long parentOid,
         long xPathOid, String index, String value, int typeKey);

   /**
    * Creates an {@link IStructuredDataValue} instance, that corresponds to the root element 
    * of the structured data
    * @param rootOid
    * @param xPathOid
    * @param key
    * @param value
    * @return
    */
   public IStructuredDataValue createRootElementEntry(IProcessInstance scopeProcessInstance, long xPathOid, String key,
         String value);

}
