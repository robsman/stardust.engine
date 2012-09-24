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
package org.eclipse.stardust.engine.ws.processinterface;

import java.io.Serializable;

import org.eclipse.stardust.common.Pair;
import org.w3c.dom.Document;


/**
 * <p>
 * This DTO class contains all required
 * information to bootstrap a Web Service Endpoint.
 * </p>
 * 
 * @author Nicolas.Werlein
 */
public final class EndpointConfiguration implements Serializable
{
   private static final long serialVersionUID = -1668417411693500570L;

   /**
    * This is the pair (Model Id, Relative Endpoint URL).
    */
   private final ModelIdUrlPair modelIdUrlPair;
   
   /**
    * The WSDL file.
    */
   private final Document wsdl;

   /**
    * The implementation class for the endpoint.
    */
   private final Class< ? > implClass;

   /**
    * The Authorization mode.
    */
   private final AuthMode authMode;

   /**
    * The partitionId where the model originates from.
    */
   private final String partitionId;
   
   /**
    * ctor
    */
   public EndpointConfiguration(final ModelIdUrlPair modelIdUrlPair, String partitionId, final Document wsdl, Class< ? > implClass, AuthMode authMode)
   {
      this.partitionId = partitionId;
      this.authMode = authMode;
      if (modelIdUrlPair == null)
      {
         throw new NullPointerException("Relative Endpoint URL must not be null.");
      }
      if (wsdl == null)
      {
         throw new NullPointerException("WSDL must not be null");
      }
      this.modelIdUrlPair = modelIdUrlPair;
      this.wsdl = wsdl;
      this.implClass = implClass;
   }
   
   public Class< ? > getImplClass()
   {
      return implClass;
   }

   public String id()
   {
      return buildEndpointId(partitionId,modelIdUrlPair.modelId(), authMode);
   }

   public ModelIdUrlPair modelIdUrlPair()
   {
      return modelIdUrlPair;
   }
   
   public String getPartitionId()
   {
      return partitionId;
   }
   
   public Document wsdl()
   {
      return wsdl;
   }
   
   public AuthMode getAuthMode()
   {
      return authMode;
   }
   
   @Override
   public int hashCode()
   {
      int result = 17;
      result = 31 * result + modelIdUrlPair.hashCode();
      result = 31 * result + wsdl.hashCode();
      return result;
   }
   
   @Override
   public boolean equals(final Object obj)
   {
      if (!(obj instanceof EndpointConfiguration))
      {
         return false;
      }
      
      final EndpointConfiguration that = (EndpointConfiguration) obj;
      return this.modelIdUrlPair.equals(that.modelIdUrlPair)
         && this.wsdl.equals(that.wsdl);
   }
   
   
   /**
    * <p>
    * This class represents the pair <i>modelId</i>
    * with the corresponding <i>URL</i>.
    * </p>
    * 
    * @author Nicolas.Werlein
    */
   public static final class ModelIdUrlPair extends Pair<String, String>
   {
      private static final long serialVersionUID = 2335963458316891662L;

      public ModelIdUrlPair(final String modelId, final String relativeUrl)
      {
         super(modelId, relativeUrl);

         if (modelId == null)
         {
            throw new NullPointerException("Model Id must not be null.");
         }
         if ("".equals(modelId))
         {
            throw new IllegalArgumentException("Model Id must not be empty.");
         }
         if (relativeUrl == null)
         {
            throw new NullPointerException("Relative URL must not be null.");
         }
         if ("".equals(relativeUrl))
         {
            throw new IllegalArgumentException("Relative URL must not be empty.");
         }
      }
      
      public String modelId()
      {
         return getFirst();
      }
      
      public String relativeUrl()
      {
         return getSecond();
      }
   }
   
   public static String buildEndpointId(String partitionId, String modelId, AuthMode authMode)
   {
      return partitionId + "/" + modelId + "/" + authMode;
   }
}
