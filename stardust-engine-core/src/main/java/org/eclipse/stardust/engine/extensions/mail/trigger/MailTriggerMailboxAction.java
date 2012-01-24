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
public class MailTriggerMailboxAction extends StringKey
{
   public static final MailTriggerMailboxAction LEAVE = new MailTriggerMailboxAction("leave", "Leave mail as is");
   public static final MailTriggerMailboxAction READ = new MailTriggerMailboxAction("read", "Mark mail as seen");
   public static final MailTriggerMailboxAction REMOVE = new MailTriggerMailboxAction("remove", "Remove mail from server");
   
   public MailTriggerMailboxAction(String id, String defaultName)
   {
      super(id, defaultName);
   }
}
