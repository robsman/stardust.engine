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
package org.eclipse.stardust.engine.core.runtime.command.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.ServiceCommandException;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes;
import org.eclipse.stardust.engine.api.model.ContextData;
import org.eclipse.stardust.engine.api.runtime.ActivityCompletionLog;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;


/**
 * Client side command class for using the quality assurance feature
 * 
 * @author holger.prause
 * @version $Revision: $
 */
public class QualityAssuranceCompleteCommand implements ServiceCommand
{
   private static final long serialVersionUID = 1L;

   private final ActivityInstanceAttributes aiAttribute;

   private final ContextData context;

   /**
    * Creates the complete command
    * 
    * @param aiAttribute - the attributes to set
    * @param context - the context data used for the complete command
    */
   public QualityAssuranceCompleteCommand(ActivityInstanceAttributes aiAttribute,
         ContextData context)
   {
      super();
      this.aiAttribute = aiAttribute;
      this.context = context;
   }

   /**
    * Executes the complete command for a quality assurance instance
    */
   public Serializable execute(ServiceFactory sf)
   {
      try
      {
         final WorkflowService workflowService = sf.getWorkflowService();

         workflowService.setActivityInstanceAttributes(aiAttribute);
         ActivityCompletionLog acl = workflowService.complete(
               aiAttribute.getActivityInstanceOid(), context.getContext(),
               context.getData(), WorkflowService.FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE);
         return acl;
      }
      catch (Exception f)
      {
         handleException(f);
         return null;
      }
   }

   private static void handleException(Exception f)
   {
      if (f instanceof UndeclaredThrowableException)
      {
         Throwable undeclaredThrowable = ((UndeclaredThrowableException) f)
               .getUndeclaredThrowable();
         if (undeclaredThrowable instanceof InvocationTargetException)
         {
            Throwable targetException = ((InvocationTargetException) undeclaredThrowable)
                  .getTargetException();
            throw createServiceCommandException(targetException);
         }
         else
         {
            throw createServiceCommandException(f);
         }
      }
      else
      {
         throw createServiceCommandException(f);
      }
   }

   private static ErrorCase getErrorCase(Throwable t)
   {
      if (t instanceof ApplicationException)
      {
         return ((ApplicationException) t).getError();
      }

      return null;
   }

   private static ServiceCommandException createServiceCommandException(Throwable t)
   {
      ErrorCase errorCase = getErrorCase(t);
      if (errorCase == null)
      {
         return new ServiceCommandException((String) null, t);
      }
      return new ServiceCommandException(errorCase, t);
   }
}