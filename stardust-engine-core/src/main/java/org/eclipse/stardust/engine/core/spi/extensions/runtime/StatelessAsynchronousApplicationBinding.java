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
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;


/**
 * @author sauer
 * @version $Revision$
 */
public class StatelessAsynchronousApplicationBinding
      implements AsynchronousApplicationInstance
{

   private final StatelessAsynchronousApplicationInstance delegate;

   private ApplicationInvocationContext context;

   public StatelessAsynchronousApplicationBinding(
         StatelessAsynchronousApplicationInstance delegate)
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

   public boolean isSending()
   {
      return delegate.isSending(context);
   }
   
   public void send() throws InvocationTargetException
   {
      delegate.send(context);
   }
   
   public boolean isReceiving()
   {
      return delegate.isReceiving(context);
   }

   public Map receive(Map data, Iterator outDataTypes)
   {
      return delegate.receive(context, data, outDataTypes);
   }

   public void cleanup()
   {
      delegate.cleanup(context);
      this.context = null;
   }

}
