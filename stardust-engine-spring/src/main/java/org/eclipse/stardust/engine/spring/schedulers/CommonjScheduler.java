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
package org.eclipse.stardust.engine.spring.schedulers;

import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonCarrier;
import org.springframework.scheduling.commonj.TimerManagerFactoryBean;


/**
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class CommonjScheduler implements DaemonScheduler
{
   private TimerManagerFactoryBean factory;

   public TimerManagerFactoryBean getFactory()
   {
      return factory;
   }

   public void setFactory(TimerManagerFactoryBean factory)
   {
      this.factory = factory;
   }

   public void start(DaemonCarrier carrier, long period, Runnable runnable)
   {
      // TODO Auto-generated method stub
      
   }

   public void stop(DaemonCarrier carrier)
   {
      // TODO Auto-generated method stub
      
   }

   public boolean isScheduled(DaemonCarrier carrier)
   {
      // TODO Auto-generated method stub
      return false;
   }

}
