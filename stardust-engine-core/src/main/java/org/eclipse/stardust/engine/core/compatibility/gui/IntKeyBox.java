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

import org.eclipse.stardust.common.StringKey;

/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class IntKeyBox extends GenericComboBox
{
   public IntKeyBox(boolean mandatory, boolean sorted)
   {
      this(StringKey.class, mandatory, sorted);
   }

   public IntKeyBox(Class keyType, boolean mandatory, boolean sorted)
   {
      // @todo (ub) implement some 'mandatory' behaviour?
      super(keyType, "name", StringKey.getKeys(keyType).toArray(), null, mandatory, sorted);
   }

   public void setKey(Class keyType)
   {
      setType(keyType, "name");
      setValues(StringKey.getKeys(keyType).toArray());
   }
}
