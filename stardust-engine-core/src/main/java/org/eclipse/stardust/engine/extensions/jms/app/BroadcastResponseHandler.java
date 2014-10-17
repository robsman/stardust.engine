package org.eclipse.stardust.engine.extensions.jms.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ExpectedFailureException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.Modules;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Trigger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.TriggerDaemon;
import org.eclipse.stardust.engine.core.runtime.beans.WorkflowServiceImpl;
import org.eclipse.stardust.engine.extensions.jms.trigger.TriggerMessageAcceptor;
import org.eclipse.stardust.engine.extensions.jms.utils.JMSUtils;



/**
 * @author rsauer
 * @author Simon Nikles
 *
 */
public class BroadcastResponseHandler implements Action<Object>
{
	private static final Logger trace = LogManager.getLogger(BroadcastResponseHandlerFactory.class);
	private static final String CACHED_SIGNAL_TRIGGERS = BroadcastResponseHandler.class.getName() + ".SignalJmsTriggers";
	private static final String CACHED_SIGNAL_EVENTS = BroadcastResponseHandler.class.getName() + ".SignalEvents";

	private Message message;

	private List<ITrigger> signalTriggers = new ArrayList<ITrigger>();
	private List<IEventHandler> signalEvents = new ArrayList<IEventHandler>();

	public BroadcastResponseHandler(Message message) {}

	@Override
	public Object execute()
	{
		String signal = null;
		try {
			signal = message.getStringProperty("stardust.bpmn.signal");
		} catch (JMSException e) {
			e.printStackTrace();
		}
		if (trace.isDebugEnabled())
		{
			trace.debug("Handling signal broadcast '" + signal + "'.");
		}

		AdministrationServiceImpl session = new AdministrationServiceImpl();

		initializeFromModel();

		Set<Match> matches = findSignalAcceptors(signal);
		if (matches.size() > 0) {
			for (Match match : matches) {
				match.process(session, message);
			}
		}
		else
		{
			throw new ExpectedFailureException(
					BpmRuntimeError.JMS_NO_MESSAGE_ACCEPTORS_FOUND.raise(JMSUtils
							.messageToString(message)));
		}

		return null;
	}

	/**
	 * Collect triggers and activity-event handlers with a signal declaration.
	 */
	private void initializeFromModel() {
		if (trace.isDebugEnabled())
		{
			trace.debug("Bootstrapping signal acceptors");
		}

		// TODO for activities we have to consider all versions having running processInstances; For triggers we consider only the active/latest model version
		List<IModel> activeModels = ModelManagerFactory.getCurrent().findActiveModels();
		for (IModel model : activeModels) {
			Object cachedSignalTriggers = model.getRuntimeAttribute(CACHED_SIGNAL_TRIGGERS);
			Object cachedSignalEvents = model.getRuntimeAttribute(CACHED_SIGNAL_EVENTS);
			if (null == cachedSignalTriggers || null == cachedSignalEvents) {
				for (IProcessDefinition processDef : model.getProcessDefinitions()) {
					if (null != cachedSignalTriggers) {
						signalTriggers = (List<ITrigger>) cachedSignalTriggers;
					} else { 
						for (ITrigger trigger : processDef.getTriggers())
						{
							if (PredefinedConstants.JMS_TRIGGER.equals(trigger.getType()))
							{
								if (null != trigger.getAllAttributes() && trigger.getAllAttributes().containsKey("stardust.bpmn.signal")) {
									signalTriggers.add(trigger);
								}
							}
						}
					}
					if (null != cachedSignalEvents) {
						signalEvents = (List<IEventHandler>) cachedSignalEvents;
					} else {
						for (IActivity activity : processDef.getActivities()) {
							for (IEventHandler event : activity.getEventHandlers()) { 
								if (null != event.getAllAttributes() && event.getAllAttributes().containsKey("stardust.bpmn.signal")) {
									signalEvents.add(event);
								}		            		
							}
						}
					}
				}
				model.setRuntimeAttribute(CACHED_SIGNAL_TRIGGERS, signalTriggers);
				model.setRuntimeAttribute(CACHED_SIGNAL_EVENTS, signalEvents);
			}
		}
	}

	private Set<Match> findSignalAcceptors(String signal) {
		Set<Match> acceptors = new HashSet<BroadcastResponseHandler.Match>();

		acceptors.addAll(findTriggersForSignal(signal));
		acceptors.addAll(findActivitiesForSignal(signal));

		return acceptors;
	}

	private Collection<? extends Match> findActivitiesForSignal(String signal) {
		// TODO create matches for eventHandlers of activities (as found in model definitions) in active models
		return null;
	}

	private Collection<? extends Match> findTriggersForSignal(String signal) {
		// TODO create matches for startTriggers
		return null;
	}

	private interface Match {
		void process(AdministrationServiceImpl session, Message message);
	}

	/**
	 * copied from {@link ResponseHandlerImpl}
	 *
	 */
	private class TriggerMatch implements Match
	{
		private final Trigger triggerDetails;
		private final Map acceptedData;

		private TriggerMatch(TriggerMessageAcceptor acceptor, Trigger triggerDetails,
				Map acceptedData)
		{
			this.triggerDetails = triggerDetails;
			this.acceptedData = acceptedData;
		}

		public void process(AdministrationServiceImpl session, Message message)
		{
			String processId = triggerDetails.getProcessDefinition().getId();

			IModel model = ModelManagerFactory.getCurrent().findModel(triggerDetails.getModelOID());
			IProcessDefinition processDefinition = model.findProcessDefinition(processId);
			ITrigger trigger = processDefinition.findTrigger(triggerDetails.getId());

			if (trace.isDebugEnabled())
			{
				trace.debug("Executing activity thread for incoming message");
			}

			boolean isSync = triggerDetails.isSynchronous();

			if ( !isSync)
			{
				String syncFlag = Parameters.instance().getString(Modules.ENGINE.getId() + "."
						+ triggerDetails.getId() + "."
						+ org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties.THREAD_MODE,
						"");
				if (syncFlag.length() != 0)
				{
					isSync = Boolean.valueOf(syncFlag).booleanValue();
				}
			}

			Map startData = TriggerDaemon.performParameterMapping(trigger, acceptedData);
			new WorkflowServiceImpl().startProcess(processDefinition, startData, isSync);
		}
	}

}
