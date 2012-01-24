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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Map;

/**
 * @author ubirkemeyer
 * @author rsauer
 * @version $Revision$
 */
public interface ManagedService
{
   /**
    * Calls in EJB mode the method "void javax.ejb.EJBObject.remove()".
    */
   void remove();

   LoggedInUser login(String username, String password, Map properties);

   void logout();
}
