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
package org.eclipse.stardust.common.utils.ejb;

/**
 * Provides constants for currently supported EJB.xx properties.
 * 
 * @author rsauer
 * @version $Revision$
 */
public interface EjbProperties
{
   String LOCAL_JNDI_ENV = "java:comp/env";
   
   String SERVER_VENDOR_PROPERTY = "EJB.ServerVendor";

   String BES = "BES";
   String JBOSS = "JBOSS";
   String JRUN = "JRUN";
   String OAS = "OAS";
   String PRAMATI = "PRAMATI";
   String WAS = "WAS";
   String WEBLOGIC = "WEBLOGIC";
   String TRIFORK = "TRIFORK";
   String SAP = "NETWEAVER";

   String CONTAINER_TYPE = "Engine.ContainerType";
   String JNDI_URL = "JNDI.URL";
   String INITIAL_CONTEXT_FACTORY = "JNDI.InitialContextFactory";
   String USER_NAME = "JNDI.User";
   String USER_PASS = "JNDI.Password";
   String PKG_PREFIXES = "JNDI.PackagePrefixes";
}
