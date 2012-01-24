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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidatorEx;


public class MailTriggerValidator implements TriggerValidator, TriggerValidatorEx
{
   public Collection validate(Map attributes, Iterator accessPoints)
   {
      throw new UnsupportedOperationException();
   }
   
   public List validate(ITrigger trigger)
   {
      List inconsistencies = CollectionUtils.newList();
      if (trigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_USER_ATT) == null)
      {
         inconsistencies.add(new Inconsistency("Unspecified user name for mail trigger",
               trigger, Inconsistency.ERROR));
      }
      if (trigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_PASSWORD_ATT) == null)
      {
         inconsistencies.add(new Inconsistency("Unspecified password for mail trigger",
               trigger, Inconsistency.ERROR));
      }
      if (trigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_SERVER_ATT) == null)
      {
         inconsistencies.add(new Inconsistency("Unspecified server name for mail trigger",
               trigger, Inconsistency.ERROR));
      }
      if (trigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_PROTOCOL_ATT) == null)
      {
         inconsistencies.add(new Inconsistency("Unspecified protocol for mail trigger",
               trigger, Inconsistency.ERROR));
      }
      // todo (fh): check access points
      return inconsistencies;
   }
}
