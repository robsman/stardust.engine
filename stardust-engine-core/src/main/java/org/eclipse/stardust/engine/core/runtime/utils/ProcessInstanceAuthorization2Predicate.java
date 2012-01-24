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
import java.sql.SQLException;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;


/**
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class ProcessInstanceAuthorization2Predicate extends AbstractAuthorization2Predicate
{
   private static final Logger trace = LogManager.getLogger(ProcessInstanceAuthorization2Predicate.class);

   private static final FieldRef[] LOCAL_STRINGS = {
      ProcessInstanceBean.FR__OID,
      ProcessInstanceBean.FR__PROCESS_DEFINITION,
      ProcessInstanceBean.FR__MODEL,
      ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE
   };

   public ProcessInstanceAuthorization2Predicate(AuthorizationContext context)
   {
      super(context);
   }

   public FieldRef[] getLocalFields()
   {
      return LOCAL_STRINGS;
   }

   public boolean accept(Object o)
   {
      if (context.isAdminOverride())
      {
         return true;
      }
      boolean result = true;
      if (delegate != null)
      {
         result = delegate.accept(o);
      }
      if (result && super.accept(o))
      {
         if (o instanceof ResultSet)
         {
            ResultSet rs = (ResultSet) o;
            try
            {
               long processInstanceOid = rs.getLong(ProcessInstanceBean.FIELD__OID);
               Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
               if (session.existsInCache(ProcessInstanceBean.class, new Long(processInstanceOid)))
               {
                  IProcessInstance pi = (ProcessInstanceBean) session.findByOID(ProcessInstanceBean.class, processInstanceOid);
                  context.setProcessInstance(pi);
                  return Authorization2.hasPermission(context);
               }
               // cache miss
               long scopeProcessInstanceOid = rs.getLong(ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE);
               long processRtOid = rs.getLong(ProcessInstanceBean.FIELD__PROCESS_DEFINITION);
               long modelOid = rs.getLong(ProcessInstanceBean.FIELD__MODEL);
               context.setProcessData(scopeProcessInstanceOid, processRtOid, modelOid);
               return Authorization2.hasPermission(context);
            }
            catch (SQLException e)
            {
               trace.warn("", e);

               return false;
            }
         }
         else if (o instanceof IProcessInstance)
         {
            context.setProcessInstance((IProcessInstance) o);
            return Authorization2.hasPermission(context);
         }
      }
      return result;
   }
}
