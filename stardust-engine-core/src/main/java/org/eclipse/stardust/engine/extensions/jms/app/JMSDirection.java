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
package org.eclipse.stardust.engine.extensions.jms.app;

import org.eclipse.stardust.common.StringKey;

public class JMSDirection extends StringKey
{
   public static final JMSDirection IN = new JMSDirection("in", "Response");
   public static final JMSDirection OUT = new JMSDirection("out", "Request");
   public static final JMSDirection INOUT = new JMSDirection("inout", "Request / Response");

   public JMSDirection(String id, String defaultName)
   {
      super(id, defaultName);
   }

   public boolean isSending()
   {
      return this.equals(OUT) || this.equals(INOUT);
   }

   public boolean isReceiving()
   {
      return this.equals(IN) || this.equals(INOUT);
   }
}
