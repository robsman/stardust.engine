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
package org.eclipse.stardust.engine.core.compatibility.gui;

import java.util.EventListener;

/**
 *	Listener for MandatoryEvents.
 */
public interface MandatoryListener extends EventListener
{
   /**
    *	Invoked when an empty mandatory component is found
    */
   public void processEvent(MandatoryEvent e);
}
