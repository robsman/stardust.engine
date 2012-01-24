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
package org.eclipse.stardust.engine.extensions;

import org.eclipse.stardust.engine.core.pojo.app.PlainJavaApplicationInstanceTest;
import org.eclipse.stardust.engine.extensions.dms.data.VfsOperationApplicationInstanceTest;
import org.eclipse.stardust.engine.extensions.ejb.app.SessionBeanApplicationInstanceTest;
import org.eclipse.stardust.engine.extensions.ejb.ejb2.app.SessionBean20ApplicationInstanceTest;
import org.eclipse.stardust.engine.extensions.ejb.ejb3.app.SessionBean30ApplicationInstanceTest;
import org.eclipse.stardust.engine.extensions.jms.app.JMSApplicationInstanceTest;
import org.eclipse.stardust.engine.extensions.mail.app.MailApplicationInstanceTest;
import org.eclipse.stardust.engine.extensions.transformation.runtime.parsing.MessageParsingApplicationInstanceTest;
import org.eclipse.stardust.engine.extensions.transformation.runtime.serialization.MessageSerializationApplicationInstanceTest;
import org.eclipse.stardust.engine.extensions.transformation.runtime.transformation.MessageTransformationApplicationInstanceTest;
import org.eclipse.stardust.engine.extensions.transformation.runtime.transformation.xsl.XSLMessageTransformationApplicationInstanceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({  
                  JMSApplicationInstanceTest.class,
                  MailApplicationInstanceTest.class,
                  MessageParsingApplicationInstanceTest.class,
                  MessageSerializationApplicationInstanceTest.class,
                  MessageTransformationApplicationInstanceTest.class,
                  PlainJavaApplicationInstanceTest.class,
                  SessionBeanApplicationInstanceTest.class,
                  SessionBean20ApplicationInstanceTest.class,
                  SessionBean30ApplicationInstanceTest.class,
                  VfsOperationApplicationInstanceTest.class,
                  XSLMessageTransformationApplicationInstanceTest.class
               })
public class ApplicationInstanceTestSuite
{
   /* test suite */
}
