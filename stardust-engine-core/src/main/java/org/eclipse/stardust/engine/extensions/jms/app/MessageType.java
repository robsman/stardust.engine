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

/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class MessageType extends StringKey
{
   public static final MessageType MAP = new MessageType("Map", "Map message");
   public static final MessageType STREAM = new MessageType("Stream", "Stream message");
   public static final MessageType TEXT = new MessageType("Text", "Text message");
   public static final MessageType OBJECT = new MessageType("Object", "Object message");

   private MessageType(String id, String defaultName)
   {
      super(id, defaultName);
   }
}

