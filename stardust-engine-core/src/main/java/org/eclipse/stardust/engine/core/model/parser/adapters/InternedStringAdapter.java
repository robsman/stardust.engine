/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.model.parser.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class InternedStringAdapter extends XmlAdapter<String,String>
{
   @Override
   public String unmarshal(String v) throws Exception
   {
      return v.trim().intern();
   }

   /**
    * No-op.
    */
   @Override
   public String marshal(String v) throws Exception
   {
      return v;
   }
}
