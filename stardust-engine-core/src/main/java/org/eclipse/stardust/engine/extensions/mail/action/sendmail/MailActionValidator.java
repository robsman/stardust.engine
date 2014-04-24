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

import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.spi.extensions.model.EventActionValidator;


public class MailActionValidator implements EventActionValidator
{
   public Collection validate(Map attributes)
   {
      ArrayList list = new ArrayList();
      Object receiverType = attributes.get(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT);
      if (!(receiverType instanceof ReceiverType))
      {
         BpmValidationError error = BpmValidationError.ACTN_NO_RECEIVER_TYPE_SPECIFIED.raise();
         list.add(new Inconsistency(error, Inconsistency.WARNING));
      }
      else
      {
         ReceiverType type = (ReceiverType) receiverType;
         if (type.equals(ReceiverType.Participant))
         {
            Object receiver = attributes.get(PredefinedConstants.MAIL_ACTION_RECEIVER_ATT);
            if (!(receiver instanceof String) || StringUtils.isEmpty((String) receiver))
            {
               BpmValidationError error = BpmValidationError.ACTN_NO_RECEIVING_PARTICIPANT_SPECIFIED.raise();
               list.add(new Inconsistency(error, Inconsistency.WARNING));
            }
         }
         else if (type.equals(ReceiverType.EMail))
         {
            Object address = attributes.get(PredefinedConstants.MAIL_ACTION_ADDRESS_ATT);
            if (!(address instanceof String) || StringUtils.isEmpty((String) address))
            {
               BpmValidationError error = BpmValidationError.ACTN_NO_RECEIVING_PARTICIPANT_SPECIFIED.raise();
               list.add(new Inconsistency(error, Inconsistency.WARNING));
            }
         }
      }
      return list;
   }
}
