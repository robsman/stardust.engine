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
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.util.Set;

import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;


/**
 * @author rsauer
 * @version $Revision$
 */
public interface ICriticalInstancesHistogram extends IInstancesHistogram
{
   long getCriticalInstancesCount(ProcessInstancePriority priority);

   long getCriticalInstancesCount(int priority);

   long getTotalCriticalInstancesCount();

   long getInterruptedInstancesCount();
   
   Set<Long> getCriticalInstances(ProcessInstancePriority priority);

   Set<Long> getCriticalInstances(int priority);

   Set<Long> getTotalCriticalInstances();
   
   Set<Long> getInterruptedInstances();
   
   
}
