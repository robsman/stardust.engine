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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.dms.data.JcrSecurityUtils;
import org.eclipse.stardust.vfs.jcr.spring.JcrSpringSessionFactory;

public class IppJcrSessionFactory extends JcrSpringSessionFactory
{
   protected final static String JCR_PASSWORD_DUMMY = "ipp-jcr-password";
   
   public Session createSession() throws RepositoryException
   {

      IUser user = SecurityProperties.getUser();

      SimpleCredentials jcrCredentials = JcrSecurityUtils.getCredentialsIncludingParticipantHierarchy(
            user, JCR_PASSWORD_DUMMY);

      Session session = getRepository().login(jcrCredentials, getWorkspaceName());

      return session;
   }

}
