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
package org.eclipse.stardust.test.api;

/**
 * <p>
 * This <code>RuntimeException</code> indicates that something
 * during setup or teardown of the test environment went terribly
 * wrong so that testing cannot proceed. 
 * </p>
 * 
 * <p>
 * An instance of this class contains a field that states the action
 * during which the test environment exception occured.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TestRtEnvException extends RuntimeException
{
   private static final long serialVersionUID = -7309551291881945176L;

   private final TestRtEnvAction action;
   
   /**
    * <p>
    * Creates a new test runtime environment exception with the specified parameters.
    * </p>
    * 
    * @param message the message stating what went wrong
    * @param cause the cause if the creation of this exception was triggered by another one
    * @param action the test environment action during which an exception occured
    */
   public TestRtEnvException(final String message, final Exception cause, final TestRtEnvAction action)
   {
      super(message, cause);
      
      if (action == null)
      {
         throw new NullPointerException("Runtime Environment Action must not be null.");
      }
      
      this.action = action;
   }
   
   /**
    * <p>
    * Creates a new test runtime environment exception with the specified parameters.
    * </p>
    * 
    * @param message the message stating what went wrong
    * @param action the test environment action during which an exception occured
    */
   public TestRtEnvException(final String message, final TestRtEnvAction action)
   {
      this(message, null, action);
   }         
   
   /**
    * <p>
    * Returns the action during which the test environment exception occured.
    * </p>
    * 
    * @return the action during which the test environment exception occured
    */
   public TestRtEnvAction action()
   {
      return action;
   }
   
   /**
    * <p>
    * Represents the action during which the test environment exception occured.
    * </p>
    * 
    * @author Nicolas.Werlein
    */
   public static enum TestRtEnvAction { 
                                          WORKSPACE_SETUP, WORKSPACE_TEARDOWN,
                                          DB_SETUP, DB_TEARDOWN,
                                          APP_CTX_SETUP, APP_CTX_TEARDOWN,
                                          DAEMON_TEARDOWN
                                      };
}
