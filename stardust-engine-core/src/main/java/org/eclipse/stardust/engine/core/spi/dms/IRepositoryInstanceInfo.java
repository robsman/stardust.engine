/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.dms;

/**
 * Contains information about the {@link IRepositoryInstance} which are relevant to a client.
 * 
 * @author Roland.Stamm
 */
public interface IRepositoryInstanceInfo extends IRepositoryCapabilities
{
   /**
    * @return the Id of the {@link IRepositoryProvider} this instance was created from.
    */
   public String getProviderId();

   /**
    * @return the Id which was configured for this {@link IRepositoryInstance}.
    */
   public String getRepositoryId();

   /**
    * @return the name of the Repository implementation. E.g. 'Jackrabbit'.
    */
   public String getRepositoryName();

   /**
    * @return the version of the Repository implementation. E.g. '2.6.1'.
    */
   public String getRepositoryVersion();

   /**
    * @return the name of the Repository implementation. E.g. 'Java Content Repository Standard 2.0'.
    */
   public String getRepositoryType();
}
