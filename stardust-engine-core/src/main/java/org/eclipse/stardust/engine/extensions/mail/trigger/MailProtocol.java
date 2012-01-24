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
package org.eclipse.stardust.engine.extensions.mail.trigger;

import org.eclipse.stardust.common.StringKey;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class MailProtocol extends StringKey
{
   public static final MailProtocol POP3 = new MailProtocol("pop3", "POP3");
   public static final MailProtocol IMAP = new MailProtocol("imap", "IMAP4");
   
   public MailProtocol(String id, String defaultName)
   {
      super(id, defaultName);
   }
}
