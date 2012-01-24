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

import java.io.InputStream;
import java.net.URL;

/**
 * @author sauer
 * @version $Revision: $
 */
public interface IRepositoryContentProvider
{

   /**
    * Returns an {@link java.io.InputStream} which can be used to retrieve the file's content.
    * 
    * @param fileId
    * @return
    */
   InputStream getContentStream(String fileId);

   /**
    * Returns a URL which can be used to retrieve the file's content.
    * 
    * TODO probably the URL must just support opening an input stream, please verify with
    * {@link ag.carnot.web.jsf.common.PortalPluginFaceletsResourceResolver} and 
    * {@link ag.carnot.web.jsf.common.PortalPluginResourceLoader}.
    * 
    * @param fileId
    * @return
    */
   URL getContentUrl(String fileId);

}
