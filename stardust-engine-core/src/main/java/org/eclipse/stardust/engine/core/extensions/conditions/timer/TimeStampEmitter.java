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
package org.eclipse.stardust.engine.core.extensions.conditions.timer;

import org.eclipse.stardust.engine.core.persistence.ClosableIterator;
import org.eclipse.stardust.engine.core.runtime.beans.EventBindingBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.BindingTableEventWrapper;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.PullEventEmitter;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TimeStampEmitter implements PullEventEmitter
{
   public ClosableIterator execute(long timeStamp)
   {
      return new BindingTableEventWrapper((ClosableIterator) EventBindingBean
            .findAllTimerEvents(timeStamp, SecurityProperties.getPartitionOid()));
   }
}
