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
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;


/**
 * An <code>ApplicationInstance</code> keeps the runtime state and behaviour of
 * an <code>ApplicationType</code>. Application providers will typically implement
 * the sub interfaces of this interface.
 *
 * @see org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance
 * @see org.eclipse.stardust.engine.core.spi.extensions.runtime.AsynchronousApplicationInstance
 *
 * @author rsauer, ubirkemeyer
 * @version $Revision$
 */
public interface StatelessApplicationInstance
{
   /**
    * Callback allowing for initialization of newly created application instances.
    *
    * @param activityInstance The activity instances the application is executed on behalf
    *       of.
    */
   ApplicationInvocationContext bootstrap(ActivityInstance activityInstance);

   /**
    * Callback used by the CARNOT engine when the corresponding activity instance
    * processes it's in data mappings. It sets the result of the data mapping path
    * evaluation to the associated AcessPoint
    *
    * @param name the name of the IN or INOUT access point
    * @param value the value at the access point
    */
   void setInAccessPointValue(ApplicationInvocationContext context, String name,
         Object value);

   /**
    * Callback used by the CARNOT engine when the corresponding activity instance
    * processes it's in data mappings. This is needed if there is an application
    * path in the IN data mapping because an OUT access point is needed in that case
    * to set the value on the object gotten from applying the application path to
    * the access point.
    *
    * @param name the name of the OUT or INOUT access point
    * @return the value at the access point
    */
   Object getOutAccessPointValue(ApplicationInvocationContext context, String name);

   /**
    * Callback to possibly cleanup resources. It is assumed that this method will not
    * throw any exception.
    */
   void cleanup(ApplicationInvocationContext context);
}
