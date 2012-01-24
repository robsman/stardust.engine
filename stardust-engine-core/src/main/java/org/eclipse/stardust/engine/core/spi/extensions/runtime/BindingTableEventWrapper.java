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

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.persistence.ClosableIterator;
import org.eclipse.stardust.engine.core.runtime.beans.EventBindingBean;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class BindingTableEventWrapper implements ClosableIterator
{
   private ClosableIterator inner;

   public BindingTableEventWrapper(ClosableIterator inner)
   {
      this.inner = inner;
   }

   public boolean hasNext()
   {
      return inner.hasNext();
   }

   public Object next()
   {
      EventBindingBean binding = (EventBindingBean) inner.next();
      Event event = new Event(binding.getType(), binding.getObjectOID(),
            binding.getHandlerOID(), Event.PULL_EVENT);
      event.setAttribute(PredefinedConstants.TARGET_TIMESTAMP_ATT,
            new Long(binding.getTargetStamp()));
      return event;
   }

   public void remove()
   {
      throw new UnsupportedOperationException();
   }

   public void close()
   {
      inner.close();
   }
}
