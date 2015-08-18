/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.ws.servlet;

import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.ws.processinterface.AuthMode;


public class DynamicEndpointNameProvider
{
   
   /**
    * Stores endpoint names for AuthMode.HttpBasicAuth per partition.
    */
   private Map</* partitionId */String, String> dynamicEndpointNameHttpBasicAuth = CollectionUtils.newMap();;

   /**
    * Stores endpoint names for AuthMode.HttpBasicAuthSsl per partition.
    */
   private Map</* partitionId */String, String> dynamicEndpointNameHttpBasicAuthSsl = CollectionUtils.newMap();;

   /**
    * Stores endpoint names for AuthMode.WssUsernameToken per partition.
    */
   private Map</* partitionId */String, String> dynamicEndpointNameWssUsernameToken = CollectionUtils.newMap();

   /**
    * Defines if the endpoint names are initialized for a partition
    */
   private Set<String> partitionEndpointNamesInitialized = CollectionUtils.newSet();
   
   public void initEndpointNames(String partitionId)
   {
      if ( !partitionEndpointNamesInitialized.contains(partitionId))
      {
         DynamicServletConfiguration servletConfiguration = DynamicServletConfiguration.getCurrentInstance();
         for (AuthMode authMode : AuthMode.values())
         {
            if (servletConfiguration.isEndpointEnabled(partitionId, authMode))
            {
               String endpointName = servletConfiguration.getEndpointName(partitionId,
                     authMode);
               getEndpointNameMap(authMode).put(partitionId, endpointName);
            }
         }
         partitionEndpointNamesInitialized.add(partitionId);
      }
   }


   public Set<Pair<AuthMode, String>> getEndpointNameSet(String partitionId)
   {
      Set<Pair<AuthMode, String>> endpointNames = CollectionUtils.newHashSet();

      for (AuthMode authMode : AuthMode.values())
      {
         if (DynamicServletConfiguration.getCurrentInstance().isEndpointEnabled(
               partitionId, authMode))
         {
            endpointNames.add(new Pair<AuthMode, String>(authMode, getEndpointNameMap(
                  authMode).get(partitionId)));
         }
      }

      return endpointNames;
   }

   private Map<String, String> getEndpointNameMap(AuthMode authMode)
   {
      switch (authMode)
      {
      case HttpBasicAuth:
         return dynamicEndpointNameHttpBasicAuth;
      case HttpBasicAuthSsl:
         return dynamicEndpointNameHttpBasicAuthSsl;
      case WssUsernameToken:
         return dynamicEndpointNameWssUsernameToken;
      }
      return null;
   }

   public String getEndpointName(String partitionId, AuthMode authMode)
   {
      switch (authMode)
      {
      case HttpBasicAuth:
         return dynamicEndpointNameHttpBasicAuth.get(partitionId);
      case HttpBasicAuthSsl:
         return dynamicEndpointNameHttpBasicAuthSsl.get(partitionId);
      case WssUsernameToken:
         return dynamicEndpointNameWssUsernameToken.get(partitionId);
      }
      return null;
   }

}
