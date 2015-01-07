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
package org.eclipse.stardust.engine.core.spi.cluster;

/**
 * The Watcher is a special object used to determine if a shared object needs to be
 * updated or not. The contract of the watcher specifies if the watched object needs to
 * be updated (it is out of sync) and to notify other nodes that the watched object has
 * been changed.
 * <p>The Watcher is needed only in a multi VM environment, where we must synchronize
 * the information.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface Watcher
{
   /**
    * Checks if the watched object needs to be updated.
    *
    * @return true if the watcher is dirty.
    */
   boolean isDirty();

   /**
    * Notifies the other nodes that the watched object have been modified.
    */
   void setDirty();

   /**
    * Gets the global state associated with the watched object.
    * The gobal state is semantically an immutable object which should not be modified by
    * the caller. It is used internally by the system to get a snapshot (memento) of the
    * global state, and is used later to update the local state of the watcher.
    *
    * @return an object representing the current global state.
    */
   Object getGlobalState();

   /**
    * Updates the local state of the watcher with the information provided in
    * the argument.
    *
    * @param globalState the global state of the watcher, obtained from getGlobalState.
    */
   void updateState(Object globalState);
}
