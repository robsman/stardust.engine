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
package org.eclipse.stardust.engine.core.model.convert.aris;

import org.w3c.dom.Element;

/**
 * @author fherinean
 * @version $Revision$
 */
public class Position
{
   public static final String TAG_NAME = "Position";

   private static final String POS_X_ATT = "Pos.X";
   private static final String POS_Y_ATT = "Pos.Y";

   public int x;
   public int y;

   public Position(Element position)
   {
      x = Integer.parseInt(position.getAttribute(POS_X_ATT));
      y = Integer.parseInt(position.getAttribute(POS_Y_ATT));
   }

   public String toString()
   {
      return Integer.toString(x) + "," + Integer.toString(y);
   }
}
