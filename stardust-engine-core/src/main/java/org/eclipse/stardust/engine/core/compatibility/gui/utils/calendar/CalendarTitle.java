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
package org.eclipse.stardust.engine.core.compatibility.gui.utils.calendar;

import javax.swing.JLabel;

/**
 * @author Claude Duguay
 * @author rsauer
 * @version $Revision$
 */
public class CalendarTitle extends JLabel
{
   public CalendarTitle(String text)
   {
      super(text);
      setHorizontalAlignment(JLabel.CENTER);
      setBorder(new ThinBorder(ThinBorder.RAISED));
   }
}