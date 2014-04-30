/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.test.api.setup.TestRtEnvException;
import org.eclipse.stardust.test.api.setup.TestRtEnvException.TestRtEnvAction;

/**
 * <p>
 * This class represents a workspace
 * and offers convenient methods to
 * <ul>
 *   <li>create, and</li>
 *   <li>remove</li>
 * </ul>
 * the same.
 * </p>
 * 
 * <p>
 * It will be created in an OS dependent temporary
 * file location and removed when requested.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class Workspace
{
   private static final Log LOG = LogFactory.getLog(Workspace.class);
   
   private static final String WORKSPACE_PREFIX = "-workspace-";
   private static final String WORKSPACE_POSTFIX = "";
   
   private final String wsName;
   
   private File workspace;
   private State state;
   
   public Workspace(final String wsName)
   {
      if (wsName == null)
      {
         throw new NullPointerException("Workspace name must not be null.");
      }
      if (wsName.isEmpty())
      {
         throw new IllegalArgumentException("Workspace name must not be empty.");
      }
      this.wsName = wsName;
      
      state = State.UNINITIALIZED;
   }
   
   public void create() throws TestRtEnvException
   {
      if (state == State.CREATED)
      {
         throw new IllegalStateException(state.toString());
      }
      
      final File workspace;
      try
      {
         workspace = File.createTempFile(wsName + WORKSPACE_PREFIX, WORKSPACE_POSTFIX);
         workspace.delete();
         final boolean created = workspace.mkdir();
         if ( !created)
         {
            throw new IOException("Unable to create workspace");
         }
      }
      catch (final IOException e)
      {
         final String errorMsg = "Unable to create workspace.";
         LOG.error(errorMsg, e);
         throw new TestRtEnvException(errorMsg, e, TestRtEnvAction.WORKSPACE_SETUP);
      }
      
      state = State.CREATED;
      this.workspace = workspace;
   }
   
   public void remove() throws TestRtEnvException
   {
      if (state != State.CREATED)
      {
         throw new IllegalStateException(state.toString());
      }
      
      final boolean deleted = deleteFilesRecursively(workspace);
      if ( !deleted)
      {
         final String errorMsg = "Unable to remove workspace.";
         LOG.error(errorMsg);
         throw new TestRtEnvException(errorMsg, TestRtEnvAction.WORKSPACE_TEARDOWN);
      }
      
      state = State.REMOVED;
   }
   
   public File file()
   {
      if (state != State.CREATED)
      {
         throw new IllegalStateException(state.toString());
      }
      
      return workspace;
   }
   
   public String filePath()
   {
      if (state != State.CREATED)
      {
         throw new IllegalStateException(state.toString());
      }
      
      return workspace.getAbsolutePath();
   }
   
   public State state()
   {
      return state;
   }
   
   public String wsName()
   {
      return wsName;
   }
   
   private boolean deleteFilesRecursively(final File file)
   {
      final File[] files = file.listFiles();
      if (files != null)
      {
         for (final File f : files)
         {
            deleteFilesRecursively(f);
         }
      }
      return file.delete();
   }
   
   private static enum State { UNINITIALIZED, CREATED, REMOVED }
}
