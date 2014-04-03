package org.eclipse.stardust.engine.extensions.camel.monitoring;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_CONTEXT_ID_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_TRIGGER_TYPE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.INVOCATION_PATTERN_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.INVOCATION_TYPE_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.PRP_APPLICATION_CONTEXT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.InvocationPatterns.SENDRECEIVE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.InvocationTypes.ASYNCHRONOUS;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.getRouteId;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.isProducerApplication;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.stopAndRemoveRunningRoute;

import java.util.Iterator;

import org.apache.camel.CamelContext;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserRealm;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.spi.monitoring.IPartitionMonitor;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Monitoring class used to stop created route when a model is deleted. the
 * class checks first the routes created by camel triggers then Camel
 * Application type.
 * 
 * 
 * 
 */
public class CamelRouteCleanupMonitor implements IPartitionMonitor {

	public static final Logger logger = LogManager
			.getLogger(CamelRouteCleanupMonitor.class);

	public void modelDeleted(IModel model) throws DeploymentException {

		logger.info("Model deleted:" + model.getId());

		AbstractApplicationContext applicationContext = (AbstractApplicationContext) Parameters
				.instance().get(PRP_APPLICATION_CONTEXT);

		if (applicationContext != null) {

			for (Iterator pi = AuditTrailPartitionBean.findAll(); pi.hasNext();) {
				AuditTrailPartitionBean p = (AuditTrailPartitionBean) pi.next();
				logger.debug("Partition :" + p.getId() + " will be used.");
				if (ModelManagerFactory.getCurrent().isActive(model)) {
					// remove consumer route
					ModelElementList<IApplication> apps = model
							.getApplications();

					for (int ai = 0; ai < apps.size(); ai++) {

						IApplication app = apps.get(ai);

						if (app != null
								&& app.getType() != null
								&& (app.getType()
										.getId()
										.equals(CamelConstants.CAMEL_CONSUMER_APPLICATION_TYPE) || app
										.getType()
										.getId()
										.equals(CamelConstants.CAMEL_PRODUCER_APPLICATION_TYPE))) {
							{
								logger.debug("Stopping route associated to Application type "
										+ app.getId()
										+ " defined in "
										+ model.getId());
								String camelContextId = (String) app
										.getAttribute(CAMEL_CONTEXT_ID_ATT);
								CamelContext camelContext = (CamelContext) applicationContext
										.getBean(camelContextId);
								String processId = null; // ((ModelBean)
															// app.getParent()).getId();
								if (app.getAttribute(INVOCATION_PATTERN_EXT_ATT) != null
										&& app.getAttribute(INVOCATION_TYPE_EXT_ATT) != null
										&& app.getAttribute(
												INVOCATION_PATTERN_EXT_ATT)
												.equals(SENDRECEIVE)
										&& app.getAttribute(
												INVOCATION_TYPE_EXT_ATT)
												.equals(ASYNCHRONOUS)) {
									// remove consumer/producer route for
									// sendReceive Async
									logger.debug("Stopping Consumer Route <"
											+ getRouteId(p.getId(), app
													.getModel().getId(),
													processId, app.getId(),
													false)
											+ "> associated to Application type "
											+ app.getId() + " defined in "
											+ model.getId());
									stopAndRemoveRunningRoute(
											camelContext,
											getRouteId(p.getId(), app
													.getModel().getId(),
													processId, app.getId(),
													false));
									logger.debug("Stopping Producer Route <"
											+ getRouteId(p.getId(), app
													.getModel().getId(),
													processId, app.getId(),
													true)
											+ "> associated to Application type "
											+ app.getId() + " defined in "
											+ model.getId());
									stopAndRemoveRunningRoute(
											camelContext,
											getRouteId(p.getId(), app
													.getModel().getId(),
													processId, app.getId(),
													true));

								} else {
									String routeId = getRouteId(p.getId(), app
											.getModel().getId(), processId,
											app.getId(),
											isProducerApplication(app));
									if (isProducerApplication(app))
										logger.debug("Stopping Producer Route <"
												+ routeId
												+ "> associated to Application type "
												+ app.getId()
												+ " defined in "
												+ model.getId());
									else
										logger.debug("Stopping Consumer Route <"
												+ routeId
												+ "> associated to Application type "
												+ app.getId()
												+ " defined in "
												+ model.getId());
									stopAndRemoveRunningRoute(camelContext,
											routeId);
								}
							}
						}

					}
					// remove Routes Defined by Camel Trigger
					for (int pd = 0; pd < model.getProcessDefinitions().size(); pd++) {

						IProcessDefinition processDefinition = model
								.getProcessDefinitions().get(pd);

						for (int i = 0; i < processDefinition.getTriggers()
								.size(); i++) {

							ITrigger trigger = (ITrigger) processDefinition
									.getTriggers().get(i);

							if (CAMEL_TRIGGER_TYPE.equals(trigger.getType()
									.getId())) {

								String camelContextId = (String) trigger
										.getAttribute(CAMEL_CONTEXT_ID_ATT);

								if (logger.isDebugEnabled()) {
									logger.debug("Camel context "
											+ camelContextId + " used.");
								}

								CamelContext camelContext = (CamelContext) applicationContext
										.getBean(camelContextId);
								String processId = ((IProcessDefinition) trigger
										.getParent()).getId();
								String modelId = trigger.getModel().getId();
								stopAndRemoveRunningRoute(
										camelContext,
										getRouteId(p.getId(), modelId,
												processId, trigger.getId(),
												false));
							}
						}
					}
				}

			}

		}

	}

	public void modelDeployed(IModel model, boolean isOverwrite)
			throws DeploymentException {
	}

	public void userCreated(IUser user) {
	}

	public void userDisabled(IUser user) {
	}

	public void userEnabled(IUser user) {
	}

	public void userRealmCreated(IUserRealm userRealm) {
	}

	public void userRealmDropped(IUserRealm userRealm) {
	}

	public void modelLoaded(IModel model) {
	}

}
