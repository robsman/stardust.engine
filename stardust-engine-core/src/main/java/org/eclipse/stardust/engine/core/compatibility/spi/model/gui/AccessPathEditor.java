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

import javax.swing.*;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


/**
 * The AccessPathEditor is a Swing component responsible for editing an AccessPath. The
 * editor is used in the ProcessDefinitionDesktop, as an embedded component in various
 * data mapping panels.
 *
 * The editor is informed about the <b>AccessPoint</b> on which the AccessPath will be
 * applied and of the evaluation <b>direction</b>.
 *
 * The evaluation direction can be <code>In</code> (for "setter" semantics) or
 * <code>Out</code> (for "getter" semantics).
 *
 * @see org.eclipse.stardust.engine.api.model.AccessPoint
 * @author rsauer
 * @version $Revision$
 */
public abstract class AccessPathEditor extends JComponent
{
   /**
    * Gets the current AccessPath
    *
    * @return the currently edited AccessPath.
    */
   public abstract String getPath();

   /**
    * Sets the current value of the AccessPath, together with the originating point and
    * the evaluation direction.
    *
    * @param accessPoint the AccessPoint on which the AccessPath will be applied
    * @param accessPath the initial value of the AccessPath
    * @param direction the evaluation direction
    */
   public abstract void setValue(AccessPoint accessPoint, String accessPath,
                                 Direction direction);
}
