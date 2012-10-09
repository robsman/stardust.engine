/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.struct;

import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.struct.IStructuredDataValueFactory;
import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;


public class TestStructuredDataValueFactory implements IStructuredDataValueFactory
{
   private OidGeneratorForTest oidGenerator = new OidGeneratorForTest();

   public IStructuredDataValue createKeyedElementEntry(IProcessInstance scopeProcessInstance, long parentOid,
         long xPathOid, String index, String value, int typeKey)
   {
      return new TestStructuredDataValue(oidGenerator.getNextOid(), scopeProcessInstance, parentOid,
            xPathOid, value, index, typeKey);
   }

   public IStructuredDataValue createRootElementEntry(IProcessInstance scopeProcessInstance, long xPathOid,
         String key, String value)
   {
      return new TestStructuredDataValue(scopeProcessInstance.getScopeProcessInstanceOID(), scopeProcessInstance,
            IStructuredDataValue.NO_PARENT, xPathOid, value, key, BigData.NULL);
   }
}
