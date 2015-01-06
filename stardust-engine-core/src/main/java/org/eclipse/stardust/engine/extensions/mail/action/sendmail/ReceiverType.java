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
package org.eclipse.stardust.engine.extensions.mail.action.sendmail;

import org.eclipse.stardust.common.StringKey;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ReceiverType extends StringKey
{
   public static final ReceiverType CurrentUserPerformer = new ReceiverType("currentPerformer", "Current Performer");
   public static final ReceiverType Participant = new ReceiverType("participant", "Model Participant");
   public static final ReceiverType EMail = new ReceiverType("email", "EMail");

   public ReceiverType(String id, String defaultName)
   {
      super(id, defaultName);
   }
}
