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
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
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
         BpmValidationError error = BpmValidationError.TRIGG_UNSPECIFIED_USER_NAME_FOR_MAIL_TRIGGER.raise();
         inconsistencies.add(new Inconsistency(error, trigger, Inconsistency.ERROR));
      }
      if (trigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_PASSWORD_ATT) == null)
      {
         BpmValidationError error = BpmValidationError.TRIGG_UNSPECIFIED_PASSWORD_FOR_MAIL_TRIGGER.raise();
         inconsistencies.add(new Inconsistency(error, trigger, Inconsistency.ERROR));
      }
      if (trigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_SERVER_ATT) == null)
      {
         BpmValidationError error = BpmValidationError.TRIGG_UNSPECIFIED_SERVER_NAME_FOR_MAIL_TRIGGER.raise();
         inconsistencies.add(new Inconsistency(error, trigger, Inconsistency.ERROR));
      }
      if (trigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_PROTOCOL_ATT) == null)
      {
         BpmValidationError error = BpmValidationError.TRIGG_UNSPECIFIED_PROTOCOL_FOR_MAIL_TRIGGER.raise();
         inconsistencies.add(new Inconsistency(error, trigger, Inconsistency.ERROR));
      }
      // todo (fh): check access points
      return inconsistencies;
   }
}
