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
package org.eclipse.stardust.engine.core.extensions.interactive.contexts.jfc;

import java.util.Iterator;
import java.util.Map;

import javax.swing.*;

// @todo (france, ub): remove??!. This semi contract is only used in the workflow gui adapter
// Could be useful if we allow (as in the deep past) other contexts than jfc to execute in
// the workflow gui adapter

/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface InteractiveApplicationInstance
{
   /**
    * Callback used by the CARNOT engine when the corresponding activity instance
    * is run.
    *
    * @param outDataTypes A set of AccessPointBean names to be expected as return values.
    *        This is filled by the CARNOT engine and is an optimization hint to prevent
    *        the application instance to evaluate all possible OUT AccessPoints.
    * @return A map with the provided AccessPointBean names as keys and the values at
    *        this access points as values.
    */
   Map invoke(Iterator outDataTypes);

   // @todo (france, ub): remove. Misplaced here. It is an attribute of the context.
   JPanel getPanel();

   /**
    * Callback used by the CARNOT engine when the corresponding activity instance
    * processes it's in data mappings. It sets the result of the data mapping path
    * evaluation to the associated AcessPoint
    *
    * @param name the name of the IN or INOUT access point
    * @param value the value at the access point
    */
   void setInAccessPointValue(String name, Object value);

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
   Object getOutAccessPointValue(String name);

   void cleanup();

}
