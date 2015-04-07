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
package org.eclipse.stardust.engine.core.runtime.beans.removethis;

import org.eclipse.stardust.engine.api.model.Modules;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * Provides constants for currently supported JMS.xx properties.
 * 
 * @author Robert Sauer
 */
public interface JmsProperties
{
   String MDB_NAME_MESSAGE_LISTENER = "MessageListener";
   String MDB_NAME_DAEMON_LISTENER = "DaemonListener";
   String MDB_NAME_RESPONSE_HANDLER = "ResponseHandler";
   
   String QUEUE_CONNECTION_FACTORY_PROPERTY = "jms/CarnotXAConnectionFactory";

   String SYSTEM_QUEUE_NAME_PROPERTY = "jms/CarnotSystemQueue";
   String DAEMON_QUEUE_NAME_PROPERTY = "jms/CarnotDaemonQueue";
   String APPLICATION_QUEUE_NAME_PROPERTY = "jms/CarnotApplicationQueue";
   String AUDIT_TRAIL_QUEUE_NAME_PROPERTY = "jms/CarnotAuditTrailQueue";
   String ARCHIVE_QUEUE_NAME_PROPERTY = "jms/CarnotArchiveQueue";
   
   String PROP_PREFIX = "JMS.";
   
   String PROP_SUFFIX_MDB_RETRIES_SUPORTED = ".ProcessingFailure.RetriesSupported";
   String PROP_SUFFIX_MDB_RETRY_COUNT = ".ProcessingFailure.Retries";
   String PROP_SUFFIX_MDB_RETRY_PAUSE = ".ProcessingFailure.Pause";
   String PROP_SUFFIX_MDB_FAILURE_MODE = ".ProcessingFailure.Mode";

   String MESSAGE_LISTENER_RETRY_COUNT_PROPERTY = PROP_PREFIX + MDB_NAME_MESSAGE_LISTENER
         + PROP_SUFFIX_MDB_RETRY_COUNT;
   String MESSAGE_LISTENER_RETRY_PAUSE_PROPERTY = PROP_PREFIX + MDB_NAME_MESSAGE_LISTENER
         + PROP_SUFFIX_MDB_RETRY_PAUSE;
   String MESSAGE_LISTENER_FAILURE_MODE_PROPERTY = PROP_PREFIX
         + MDB_NAME_MESSAGE_LISTENER + PROP_SUFFIX_MDB_FAILURE_MODE;

   String DAEMON_LISTENER_RETRY_COUNT_PROPERTY = PROP_PREFIX + MDB_NAME_DAEMON_LISTENER
         + PROP_SUFFIX_MDB_RETRY_COUNT;
   String DAEMON_LISTENER_RETRY_PAUSE_PROPERTY = PROP_PREFIX + MDB_NAME_DAEMON_LISTENER
         + PROP_SUFFIX_MDB_RETRY_PAUSE;
   String DAEMON_LISTENER_FAILURE_MODE_PROPERTY = PROP_PREFIX + MDB_NAME_DAEMON_LISTENER
         + PROP_SUFFIX_MDB_FAILURE_MODE;

   String RESPONSE_HANDLER_RETRY_COUNT_PROPERTY = PROP_PREFIX + MDB_NAME_RESPONSE_HANDLER
         + PROP_SUFFIX_MDB_RETRY_COUNT;
   String RESPONSE_HANDLER_RETRY_PAUSE_PROPERTY = PROP_PREFIX + MDB_NAME_RESPONSE_HANDLER
         + PROP_SUFFIX_MDB_RETRY_PAUSE;
   String RESPONSE_HANDLER_FAILURE_MODE_PROPERTY = PROP_PREFIX
         + MDB_NAME_RESPONSE_HANDLER + PROP_SUFFIX_MDB_FAILURE_MODE;

   String RESPONSE_HANDLER_RETRIES_SUPPORTED_PROPERTY = PROP_PREFIX
         + MDB_NAME_RESPONSE_HANDLER + PROP_SUFFIX_MDB_RETRIES_SUPORTED;

   String PROCESSING_FAILURE_MODE_ROLLBACK = "rollback";
   String PROCESSING_FAILURE_MODE_FORGET = "forget";

   String JMS_TRIGGER_THREAD_MODE = PROP_PREFIX + "ProcessTrigger."
         + EngineProperties.THREAD_MODE;

}
