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

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;

import com.sungard.infinity.bpm.vfs.impl.jcr.JcrDocumentRepositoryService;
import com.sungard.infinity.bpm.vfs.jcr.ISessionFactory;


/**
 * @author sauer
 * @version $Revision$
 */
public class EjbDocumentRepositoryService extends JcrDocumentRepositoryService
      implements ISessionFactory
{

   private Repository repository;

   public Repository getRepository()
   {
      return repository;
   }

   public void setRepository(Repository repository)
   {
      this.repository = repository;
   }

   public ISessionFactory getSessionFactory()
   {
      return this;
   }

   public Session getSession() throws RepositoryException
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

      return rtEnv.retrieveJcrSession(repository);
   }

   public void releaseSession(Session session)
   {
      // ignore, session will be automatically released by RT env
   }

}
