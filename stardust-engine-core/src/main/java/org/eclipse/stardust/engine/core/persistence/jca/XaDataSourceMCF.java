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
package org.eclipse.stardust.engine.core.persistence.jca;

import java.sql.SQLException;

import javax.resource.ResourceException;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.tranql.connector.AllExceptionsAreFatalSorter;
import org.tranql.connector.CredentialExtractor;
import org.tranql.connector.ExceptionSorter;
import org.tranql.connector.NoExceptionsAreFatalSorter;
import org.tranql.connector.jdbc.AbstractXADataSourceMCF;

/**
 * @author sauer
 * @version $Revision$
 */
public class XaDataSourceMCF extends AbstractXADataSourceMCF
{

   private static final long serialVersionUID = 1L;

   public XaDataSourceMCF(XADataSource xaDataSource)
   {
      this(xaDataSource, false);
   }

   public XaDataSourceMCF(XADataSource xaDataSource, boolean allExceptionsAreFatal)
   {
      this(xaDataSource, allExceptionsAreFatal
            ? (ExceptionSorter) new AllExceptionsAreFatalSorter()
            : (ExceptionSorter) new NoExceptionsAreFatalSorter());
   }

   public XaDataSourceMCF(XADataSource xaDataSource, ExceptionSorter exceptionSorter)
   {
      super(xaDataSource, exceptionSorter);
   }

   public String getUserName()
   {
      return null;
   }

   public String getPassword()
   {
      return null;
   }

   protected XAConnection getPhysicalConnection(Subject subject,
         CredentialExtractor credentialExtractor) throws ResourceException
   {
      try
      {
         // obtain connection without explicitly passing credentials
         return xaDataSource.getXAConnection();
      }
      catch (SQLException e)
      {
         throw new ResourceAdapterInternalException(
               "Unable to obtain physical connection to " + xaDataSource, e);
      }
   }

}
