/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.impl;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * <p>
 * An {@link ApplicationListener} that cleans up some stuff after the
 * test environment {@link ApplicationContext} has been shutdown.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class ApplicationContextShutdownListener implements ApplicationListener<ContextClosedEvent>
{
   /* (non-Javadoc)
    * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
    */
   @Override
   public void onApplicationEvent(final ContextClosedEvent ignored)
   {
      Parameters.instance().flush();

      RepositoryManager.reset();
   }
}
