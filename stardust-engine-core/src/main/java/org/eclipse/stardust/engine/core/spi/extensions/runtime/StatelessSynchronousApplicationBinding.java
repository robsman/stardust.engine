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
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;


/**
 * @author sauer
 * @version $Revision$
 */
public class StatelessSynchronousApplicationBinding
      implements SynchronousApplicationInstance
{

   private final StatelessSynchronousApplicationInstance delegate;

   private ApplicationInvocationContext context;

   public StatelessSynchronousApplicationBinding(
         StatelessSynchronousApplicationInstance delegate)
   {
      this.delegate = delegate;
   }

   public void bootstrap(ActivityInstance activityInstance)
   {
      this.context = delegate.bootstrap(activityInstance);
   }

   public void setInAccessPointValue(String name, Object value)
   {
      delegate.setInAccessPointValue(context, name, value);
   }

   public Object getOutAccessPointValue(String name)
   {
      return delegate.getOutAccessPointValue(context, name);
   }

   public Map invoke(Set outDataTypes) throws InvocationTargetException
   {
      return delegate.invoke(context, outDataTypes);
   }

   public void cleanup()
   {
      delegate.cleanup(context);
      this.context = null;
   }

}
