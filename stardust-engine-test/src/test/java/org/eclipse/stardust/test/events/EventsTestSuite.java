/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.events;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.events.ErrorEventHierarchyTest.ERROR_EVENTS_HIERARCHY_MODEL_NAME;
import static org.eclipse.stardust.test.events.ErrorEventTest.ERROR_EVENTS_MODEL_NAME;
import static org.eclipse.stardust.test.events.EscalationEventTest.ESCALATION_EVENTS_MODEL_NAME;
import static org.eclipse.stardust.test.events.MultipleStartEventsTest.START_EVENTS_MODEL_NAME;
import static org.eclipse.stardust.test.events.SignalEventTest.SIGNAL_EVENTS_MODEL_NAME;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestSuiteSetup;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

@RunWith(Suite.class)
@SuiteClasses({
               ErrorEventTest.class,
               ErrorEventHierarchyTest.class,
               EscalationEventTest.class,
               MultipleStartEventsTest.class,
               SignalEventTest.class
             })
public class EventsTestSuite
{
   /* test suite */

   @ClassRule
   public static final TestSuiteSetup testSuiteSetup = new TestSuiteSetup(new UsernamePasswordPair(MOTU, MOTU), ForkingServiceMode.JMS, ERROR_EVENTS_MODEL_NAME,
                                                                                                                                                            ERROR_EVENTS_HIERARCHY_MODEL_NAME,
                                                                                                                                                            ESCALATION_EVENTS_MODEL_NAME,
                                                                                                                                                            START_EVENTS_MODEL_NAME,
                                                                                                                                                            SIGNAL_EVENTS_MODEL_NAME);
}
