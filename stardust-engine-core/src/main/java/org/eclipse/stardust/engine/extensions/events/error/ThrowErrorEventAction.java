package org.eclipse.stardust.engine.extensions.events.error;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ActivityInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;
import org.eclipse.stardust.engine.extensions.events.AbstractThrowEventAction;

/**
 * @author Simon Nikles
 * @author Stéphane Ruffieux
 * @author rsauer
 *
 */
public class ThrowErrorEventAction extends AbstractThrowEventAction
{
   public static final String THROW_EVENT_TYPE = "errorEvent";

   @Override
   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      Object code = actionAttributes.get(ErrorMessageAcceptor.BPMN_ERROR_CODE);
      this.eventCode = null != code ? code.toString() : "";
   }

   @Override
   public Event execute(Event event) throws UnrecoverableExecutionException
   {
      if (Event.ACTIVITY_INSTANCE == event.getType())
      {
         ActivityInstanceBean ai = ActivityInstanceBean.findByOID(event.getObjectOID());
         ActivityInstanceUtils.abortActivityInstance(ai);
      }

      return super.execute(event);
   }

   @Override
   protected String getThrowEventType()
   {
      return THROW_EVENT_TYPE;
   }

   @Override
   protected String getConditionType()
   {
      return PredefinedConstants.SUBPROCESS_EXCEPTION;
   }
}
