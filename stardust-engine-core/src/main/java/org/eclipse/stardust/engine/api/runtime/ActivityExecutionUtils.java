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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.List;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IWorkItem;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.IActivityExecutionStrategy;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.IActivityExecutionStrategy.Factory;


/**
 * @author rsauer
 * @version $Revision$
 */
public class ActivityExecutionUtils
{
   private static final String RESOLVED_EXECUTION_STRATEGY = ActivityExecutionUtils.class.getName() + ".ResolvedActivityExecutionStrategy";
   
   private static final IActivityExecutionStrategy NONE = new UnknownActivityExecutionStrategy();
   
   public static IActivityExecutionStrategy getExecutionStrategy(IActivity activity)
   {
      IActivityExecutionStrategy strategy = (IActivityExecutionStrategy) activity.getRuntimeAttribute(RESOLVED_EXECUTION_STRATEGY);

      if (null == strategy)
      {
         List/*<Factory>*/ factories = ExtensionProviderUtils.getExtensionProviders(IActivityExecutionStrategy.Factory.class);
         
         for (int i = 0; i < factories.size(); ++i)
         {
            final Factory aesFactory = (Factory) factories.get(i);
            
            strategy = aesFactory.getExecutionStrategy(activity);
            if (null != strategy)
            {
               break;
            }
         }

         activity.setRuntimeAttribute(RESOLVED_EXECUTION_STRATEGY,
               (Serializable) ((null != strategy) ? strategy : NONE));
      }
      
      return (NONE == strategy) ? null : strategy;
   }
   
   private ActivityExecutionUtils()
   {
      // utility class
   }

   private static final class UnknownActivityExecutionStrategy
         implements IActivityExecutionStrategy, Serializable
   {

      static final long serialVersionUID = 1L;

      public void startActivityInstance(IActivityInstance activityInstance)
            throws IllegalStateChangeException
      {
         raiseUnsupportedOperationException();
      }

      public void updateWorkItem(IActivityInstance activityInstance)
      {
         raiseUnsupportedOperationException();
      }

      public void updateWorkItem(IActivityInstance activityInstance, IWorkItem workItem)
      {
         raiseUnsupportedOperationException();
      }

      public void completeActivityInstance(IActivityInstance activityInstance)
            throws IllegalStateChangeException
      {
         raiseUnsupportedOperationException();
      }

      private void raiseUnsupportedOperationException()
      {
         throw new UnsupportedOperationException(
               "Methods on marker instance must never be invoked.");
      }
   }
}
