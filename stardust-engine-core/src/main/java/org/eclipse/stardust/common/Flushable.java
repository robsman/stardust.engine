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
package org.eclipse.stardust.common;


/**
 * Mixin interface which enables classes to flush their internal caches.
 * <p>
 * At the moment only supported by classes extending 
 * DynamicParticipantSynchronizationStrategy on external authentication.
 * 
 * @author sborn
 * @version $Revision$
 */
public interface Flushable
{
   /**
    * Resets internal caches.
    */
   void flush();
}
