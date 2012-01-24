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
package org.eclipse.stardust.engine.core.runtime.utils;

import java.sql.ResultSet;

import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;


/**
 * Predicate class which is used to restrict access to work items based on its activity 
 * declarative security permission.
 * 
 * @author stephan.born
 * @version $Revision: 5162 $
 */
public class DataAuthorization2Predicate extends AbstractAuthorization2Predicate
{
//   private static final Logger trace = LogManager.getLogger(DataAuthorization2Predicate.class);

   private static final FieldRef[] LOCAL_STRINGS = {
   };
   
   private IProcessInstance processInstance;

   public DataAuthorization2Predicate(AuthorizationContext context)
   {
      super(context);
   }

   public FieldRef[] getLocalFields()
   {
      return LOCAL_STRINGS;
   }

   public boolean accept(Object o)
   {
      boolean result = true;
      if (delegate != null)
      {
         result = delegate.accept(o);
      }
      if (result && super.accept(o))
      {
         if (o instanceof ResultSet)
         {
            return false;
         }
         else if (o instanceof IData)
         {
            IData data = (IData) o;
            context.setData(processInstance, data);
            return Authorization2.hasPermission(context);
         }
      }
      return result;
   }

   public void setProcessInstance(IProcessInstance processInstance)
   {
      this.processInstance = processInstance;
   }
}
