package org.eclipse.stardust.engine.extensions.events.signal;

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
public class SendSignalEventAction extends AbstractThrowEventAction
{
   public static final String SIGNAL_EVENT_TYPE = "signalEvent";

   static final Logger trace = LogManager.getLogger(SendSignalEventAction.class);

   @Override
   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      Object code = actionAttributes.get(SignalMessageAcceptor.BPMN_SIGNAL_CODE);
      this.eventCode = null != code ? code.toString() : "";
   }

   @Override
   protected String getThrowEventType()
   {
      return SIGNAL_EVENT_TYPE;
   }

   @Override
   protected String getConditionType()
   {
      return PredefinedConstants.SIGNAL_CONDITION;
   }
}
