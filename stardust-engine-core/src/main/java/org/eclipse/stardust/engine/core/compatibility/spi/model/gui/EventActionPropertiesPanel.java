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
package org.eclipse.stardust.engine.core.compatibility.spi.model.gui;

import javax.swing.JPanel;

import org.eclipse.stardust.engine.api.model.EventHandlerOwner;
import org.eclipse.stardust.engine.api.model.IAction;


/**
 * Base class for a event action implementation to provide a <code>EventAction</code>
 * specific panel in the <code>EventActionPropertiesDialog</code>.
 * Event action providers have to subclass this class if they want to use the CARNOT
 * definitiondesktop for modelling event action specific attributes.
 * 
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class EventActionPropertiesPanel extends JPanel
{
   /**
    * Callback to associate the panel with the underlying event action.
    * 
    * @param owner  the event handler the action is attached to
    * @param action the event action to be attached
    */
   public abstract void setData(EventHandlerOwner owner, IAction action);

   /**
    * Callback to inform the panel to write its current state into the associated event
    * action.
    *
    * @see #setData(EventHandlerOwner, IAction)
    */
   public abstract void apply();
}
