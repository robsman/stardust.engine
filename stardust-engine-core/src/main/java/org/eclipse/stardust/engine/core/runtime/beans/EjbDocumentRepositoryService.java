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

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.eclipse.stardust.vfs.impl.jcr.JcrDocumentRepositoryService;
import org.eclipse.stardust.vfs.jcr.ISessionFactory;


/**
 * @author sauer
 * @version $Revision$
 */
public class EjbDocumentRepositoryService extends JcrDocumentRepositoryService implements ISessionFactory
{
   public EjbDocumentRepositoryService(ISessionFactory sessionFactory)
   {
      this.setSessionFactory(sessionFactory);
   }

   public Session getSession() throws RepositoryException
   {
      return this.getSessionFactory().getSession();
   }

   @Override
   public void releaseSession(Session session)
   {
      this.getSessionFactory().releaseSession(session);
   }

}
