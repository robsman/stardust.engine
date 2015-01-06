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
public class MailTriggerMailFlags extends StringKey
{
   public static final MailTriggerMailFlags ANY = new MailTriggerMailFlags("any", "All existing mails");
   public static final MailTriggerMailFlags NOT_SEEN = new MailTriggerMailFlags("notSeen", "Mails not marked as seen");
   public static final MailTriggerMailFlags RECENT = new MailTriggerMailFlags("recent", "New mails since last fetch");
   
   public MailTriggerMailFlags(String id, String defaultName)
   {
      super(id, defaultName);
   }
}
