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
/**
 * @author Mark Gille, j.talk() GmbH
 * @version 	%I%, %G%
 */

package org.eclipse.stardust.engine.core.compatibility.gui;

import java.beans.SimpleBeanInfo;

/**
 * Bean info for <code>DateEntry</code>.
 */
public class DateEntryBeanInfo extends SimpleBeanInfo
{
   /*
    * @return Icon to identify the bean in a visual editor.
    */
   public java.awt.Image getIcon()
   {
      return loadImage("images/date_entry.gif");
   }
}

