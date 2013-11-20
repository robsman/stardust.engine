package org.eclipse.stardust.engine.extensions.camel.monitoring;

import static org.eclipse.stardust.engine.extensions.camel.Util.createApplicationRoute;
import static org.eclipse.stardust.engine.extensions.camel.Util.createTriggerRoute;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserRealm;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.monitoring.IPartitionMonitor;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.springframework.context.support.AbstractApplicationContext;

public class CamelRouteUpdateMonitor implements IPartitionMonitor {

	public static final Logger logger = LogManager
			.getLogger(CamelRouteCleanupMonitor.class);

	public void modelDeleted(IModel model) throws DeploymentException {
	}

	public void modelDeployed(IModel model, boolean isOverwrite)
			throws DeploymentException {

		String partitionId = SecurityProperties.getPartition().getId();

		logger.info("Model " + model.getId() + " is loaded from partition "
				+ partitionId);

		AbstractApplicationContext applicationContext = (AbstractApplicationContext) Parameters
				.instance().get(CamelConstants.PRP_APPLICATION_CONTEXT);

		// create and start routes based on triggers
		ModelElementList<IProcessDefinition> processes = model
				.getProcessDefinitions();
		for (int pd = 0; pd < processes.size(); pd++) {
			IProcessDefinition process = model.getProcessDefinitions().get(pd);
			createTriggerRoute(partitionId, process, applicationContext);
		}

		// create and start routes based on applications
		createApplicationRoute(partitionId, model.getApplications(),
				applicationContext);

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
