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
package org.eclipse.stardust.engine.core.repository;

/**
 * Interface to provide an implementation specific HTTP base URL access
 * 
 * @version $Revision: 12295 $
 */
public interface IDocumentServiceStreamingSupport
{

   /**
    * @return base URL of the dms-content servlet, e.g.
    *         http://localhost:8080/mywebapp/dms-content/
    */
   String getContentServletUrlBase();

}
