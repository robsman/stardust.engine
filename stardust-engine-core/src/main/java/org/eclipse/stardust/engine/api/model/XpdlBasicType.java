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
package org.eclipse.stardust.engine.api.model;

import org.eclipse.stardust.common.StringKey;

/**
 * @author sauer
 * @version $Revision$
 */
public class XpdlBasicType extends StringKey
{
   public static final XpdlBasicType String = new XpdlBasicType("STRING");

   public static final XpdlBasicType Integer = new XpdlBasicType("INTEGER");

   public static final XpdlBasicType Boolean = new XpdlBasicType("BOOLEAN");

   public static final XpdlBasicType Float = new XpdlBasicType("FLOAT");

   public static final XpdlBasicType Datetime = new XpdlBasicType("DATETIME");

   public static final XpdlBasicType Reference = new XpdlBasicType("REFERENCE");

   public static final XpdlBasicType Performer = new XpdlBasicType("PERFORMER");

   private XpdlBasicType(String id)
   {
      super(id, id);
   }
   
   public static XpdlBasicType fromId(String id)
   {
      // TODO improve performance by caching domain in array or list (see ActivityInsatnceState)
      return (XpdlBasicType) StringKey.getKey(XpdlBasicType.class, id);
   }
}
