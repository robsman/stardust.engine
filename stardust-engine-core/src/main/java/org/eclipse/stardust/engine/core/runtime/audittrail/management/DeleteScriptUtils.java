/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert.Sauer (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *    Sven.Rottstock (SunGard CSA LLC) - adjusted to the latest sources
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import java.util.List;

import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.DmlManager;

public class DeleteScriptUtils
{
   public static String applyBindVariables(String statement, List<Object> bindValues,
         DBDescriptor dbDescriptor)
   {
      int slot = 0;

      StringBuilder buffer = new StringBuilder();
      buffer.append(statement);
      int idx = 0;
      do
      {
         idx = buffer.indexOf("?", idx);
         if ( -1 != idx)
         {
            if (slot < bindValues.size())
            {
               Object slotValue = bindValues.get(slot++);

               String sqlValue = DmlManager.getSQLValue(slotValue.getClass(), slotValue,
                     dbDescriptor);

               buffer.replace(idx, idx + 1, sqlValue);
               idx += sqlValue.length() - 1;
            }
            else
            {
               // TODO
            }
         }
         else
         {
            break;
         }
      }
      while (true);

      return buffer.toString();
   }
}
