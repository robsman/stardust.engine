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
package org.eclipse.stardust.engine.api.spring;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;

public interface SpringConstants
{
   final String SCOPE_SPRING_RT = PredefinedConstants.ENGINE_SCOPE + "spring::";

   final String ATTR_BEAN_ID = SCOPE_SPRING_RT + "beanId";

   final String PRP_APPLICATION_CONTEXT_FILE = "Carnot.Spring.ApplicationContextFile";
   
   final String PRP_APPLICATION_CONTEXT_CLASS = "Carnot.Spring.ApplicationContextClass";

   final String PRP_APPLICATION_CONTEXT = "org.eclipse.stardust.engine.api.spring.applicationContext";

   final String PRP_TX_MANAGER = "org.eclipse.stardust.engine.api.spring.transactionManager";

   final String PRP_CACHED_APPLICATION_CONTEXT = "org.eclipse.stardust.engine.api.spring.cachedApplicationContext";

   final String PRP_CACHED_APPLICATION_CONTEXT_DISPOSER = "org.eclipse.stardust.engine.api.spring.cachedApplicationContext.disposer";

   final String PRP_CACHED_APPLICATION_DISPOSE_DELAY = "org.eclipse.stardust.engine.api.spring.cachedApplicationContext.disposeDelay";

   final String BEAN_ID_WORKFLOW_SERVICE = "carnotWorkflowService";

   final String BEAN_ID_ADMINISTRATION_SERVICE = "carnotAdministrationService";

   final String BEAN_ID_QUERY_SERVICE = "carnotQueryService";

   final String BEAN_ID_USER_SERVICE = "carnotUserService";

   final String BEAN_ID_FORKING_SERVICE = "carnotForkingService";
   
   String ATTR_CARNOT_PRINCIPAL = "carnot::principal";
}
