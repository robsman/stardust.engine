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

import javax.swing.JPopupMenu;

/**
 *	Listener for PopupMenu. Used for update the state of popupmenus in tables
 * before it will be shown.
 */
public interface TablePopupMenuListener
{
   /**
    *	Invoked before the popupmenu for the node become visible.
    */
   public void updateMenuState(GenericTable table, Object rowObject, JPopupMenu menu);
}
