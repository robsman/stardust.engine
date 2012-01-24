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
package org.eclipse.stardust.engine.core.persistence;

import java.util.Iterator;

/**
 * Mimics the <code>java.util.Iterator</code>-interface, adding the
 * responsibility for users to explicitly clean-up via a final
 * <code>close()</code>-call.
 *
 * @see java.util.Iterator
 */
public interface ClosableIterator<E> extends Iterator<E>
{
   /**
    * Invalidates and disposes the instance. Client code is not allowed to use
    * the iterator any further after the call has finished.
    */
   public void close();
}
