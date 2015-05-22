package org.eclipse.stardust.engine.extensions.events.error;

import static java.util.Collections.singletonList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.extensions.events.AbstractThrowEventAction;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;
import org.eclipse.stardust.engine.extensions.jms.app.JMSLocation;
import org.eclipse.stardust.engine.extensions.jms.app.MessageAcceptor;
import org.eclipse.stardust.engine.extensions.jms.app.MessageType;
import org.eclipse.stardust.engine.extensions.jms.app.ResponseHandlerImpl.Match;

/**
 * @author Simon Nikles
 * @author Stéphane Ruffieux
 * @author rsauer
 *
 */
public class ErrorMessageAcceptor implements MessageAcceptor, Stateless
{

   private static final Logger trace = LogManager.getLogger(ErrorMessageAcceptor.class);

   public static final String BPMN_ERROR_CODE = "carnot:engine:errorCode";

   @Override
   public boolean isStateless()
   {
      return true;
   }

   @Override
   public Iterator<IActivityInstance> getMatchingActivityInstances(Message message)
   {
      List<IActivityInstance> result = newArrayList();
      try
      {
         if (message.propertyExists(DefaultMessageHelper.ACTIVITY_INSTANCE_OID_HEADER)
               && message.propertyExists(AbstractThrowEventAction.THROW_EVENT_TYPE_HEADER))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Using activity instance OID.");
            }
            if (ThrowErrorEventAction.THROW_EVENT_TYPE.equals(message
                  .getStringProperty(AbstractThrowEventAction.THROW_EVENT_TYPE_HEADER)))
            {
               long activityInstanceOID = message.getLongProperty(DefaultMessageHelper.ACTIVITY_INSTANCE_OID_HEADER);

               IActivityInstance matchingActivity = ActivityInstanceBean.findByOID(activityInstanceOID);
               if (ImplementationType.SubProcess.equals(matchingActivity.getActivity().getImplementationType()))
               {
                  result = singletonList(matchingActivity);
               }
            }
         }
      }
      catch (ObjectNotFoundException o)
      {
         // TODO - bpmn-2-events - left empty deliberately?
      }
      catch (JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e);
      }
      return result.iterator();
   }

   @Override
   public Match finalizeMatch(IActivityInstance activityInstance)
   {
      if (!activityInstance.isTerminated())
      {
         return new ErrorMessageMatch(this, activityInstance);
      }
      return null;
   }

   @Override
   public Map<String, Object> getData(Message message, StringKey id, Iterator accessPoints)
   {
      try
      {
         Object text = ((TextMessage) message).getText();
         return Collections.singletonMap(BPMN_ERROR_CODE, text);
      }
      catch (JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e);
      }
   }

   @Override
   public String getName()
   {
      return "BPMN2.0 Error Message Acceptor";
   }

   @Override
   public boolean hasPredefinedAccessPoints(StringKey id)
   {
      return DefaultMessageHelper.hasPredefinedAccessPoints(id);
   }

   @Override
   public Collection<AccessPoint> getAccessPoints(StringKey messageType)
   {
      List<AccessPoint> intrinsicAccessPoints = null;

      if (messageType.equals(MessageType.TEXT))
      {
         intrinsicAccessPoints = newArrayList();
         AccessPoint ap = JavaDataTypeUtils.createIntrinsicAccessPoint(BPMN_ERROR_CODE, BPMN_ERROR_CODE,
               String.class.getName(), Direction.OUT, false, null);
         ap.setAttribute(PredefinedConstants.JMS_LOCATION_PROPERTY, JMSLocation.BODY);

         intrinsicAccessPoints.add(ap);
      }
      return intrinsicAccessPoints;
   }

   @Override
   public Collection<MessageType> getMessageTypes()
   {
      return Collections.singleton(MessageType.TEXT);
   }

   @Override
   public List<Match> getTriggerMatches(Message message)
   {
      return Collections.emptyList();
   }

   private class ErrorMessageMatch implements Match
   {
      private final MessageAcceptor acceptor;

      private final IActivityInstance activityInstance;

      private ErrorMessageMatch(MessageAcceptor acceptor, IActivityInstance activityInstance)
      {
         this.acceptor = acceptor;
         this.activityInstance = activityInstance;
      }

      public void process(AdministrationServiceImpl session, Message message)
      {
         IProcessInstance subProcessInstance = ProcessInstanceBean.findForStartingActivityInstance(activityInstance
               .getOID());
         trace.info("Abort Process Instance " + subProcessInstance.getOID() + " due to Error Message");
         // TODO - bpmn-2-events - handle concurrency exception / trigger retry
         ProcessInstanceUtils.abortProcessInstance(subProcessInstance);
         Map<String, Object> data = acceptor.getData(message, null, null);

         trace.debug("Executing activity thread for incoming error event message; " + "activity instance = "
               + activityInstance.getOID());

         String errorCode = null != data.get(BPMN_ERROR_CODE) ? data.get(BPMN_ERROR_CODE).toString() : "";

         activityInstance.lock();
         activityInstance.setPropertyValue(ActivityInstanceBean.BOUNDARY_EVENT_HANDLER_ACTIVATED_PROPERTY_KEY,
               errorCode);

         activityInstance.activate();
      }
   }
}
