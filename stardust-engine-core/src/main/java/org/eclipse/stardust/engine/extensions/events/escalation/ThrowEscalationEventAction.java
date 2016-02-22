package org.eclipse.stardust.engine.extensions.events.escalation;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.extensions.events.AbstractThrowEventAction;

/**
 * @author Simon Nikles
 * @author Stéphane Ruffieux
 * @author rsauer
 *
 */
public class ThrowEscalationEventAction extends AbstractThrowEventAction
{

   public static final String THROW_EVENT_TYPE = "escalationEvent";

   static final Logger trace = LogManager.getLogger(EscalationMessageAcceptor.class);

   @Override
   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      Object code = actionAttributes.get(EscalationMessageAcceptor.BPMN_ESCALATION_CODE);
      this.eventCode = null != code ? code.toString() : "";
   }

   @Override
   protected String getThrowEventType()
   {
      return THROW_EVENT_TYPE;
   }

   @Override
   protected String getConditionType()
   {
      return PredefinedConstants.ESCALATION_CONDITION;
   }
}
