package org.eclipse.stardust.engine.extensions.events.signal;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
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
   protected boolean hasMatchingCatchEvent(IActivityInstance startingActivityInstance)
   {
      ModelElementList<IEventHandler> eventHandlers = startingActivityInstance.getActivity().getEventHandlers();
      for (IEventHandler handler : eventHandlers)
      {
         if (handler.getType().getId().equals(PredefinedConstants.SIGNAL_CONDITION))
         {
            Object signalName = handler.getAttribute(PredefinedConstants.SIGNAL_CLASS_ATT);
            if (null != signalName && signalName.toString().equals(Signal.class.getName())) {
               if (handler.getId().equals(eventCode)) {
                  return true;
               }
            }
         }
      }
      return false;
   }
}
