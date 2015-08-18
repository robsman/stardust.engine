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
package org.eclipse.stardust.engine.api.ejb2.tunneling;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * @author sborn
 * @version $Revision: 12373 $
 */
public interface TunnelingLocalUserServiceHome extends EJBLocalHome
{
   public TunnelingLocalUserService create() throws CreateException;

}
