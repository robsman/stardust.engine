/*******************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.dto;

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.AccessPointOwner;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;


/**
 * This class is used to evaluate AccessPointDetails for AccessPointOwner.
 *
 * @author sborn
 * @version $Revision$
 */
public class AccessPointDetailsEvaluator implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String providerClassName;
   private List persistentAccessPoints;
   private List transientAccessPoints = null;

   private List allAccessPoints = null;

   private final Map detailsAttributes;
   private final Map typeAttributes;

   /**
    * This utility method helps to determine if an activity is implemented in terms
    * of an interactive application.
    *
    * @param activity the activity;
    * @return true if activity is implemented in terms of an interactive application.
    */
   public static boolean isInteractive(IActivity activity)
   {
      return ImplementationType.Application.equals(activity.getImplementationType())
            && activity.getApplication().isInteractive();
   }

   // TODO: calculate both attribute maps for owner internally.
   /**
    * @param owner the access point owner.
    * @param isInteractiveApplication  If set to true, then the evaluation of the access
    *                                  points will be done in the context of the client.
    * @param detailsAttributes the attributes of the owner, owner.getAllAttributes()
    * @param typeAttributes the type attributes of the owner, i.e. ((Typeable)owner).getType().getAllAttributes().
    */
   public AccessPointDetailsEvaluator(AccessPointOwner owner,
         boolean isInteractiveApplication, Map detailsAttributes, Map typeAttributes)
   {
      super();

      if (isInteractiveApplication)
      {
         // prepare lazy evaluation.
         persistentAccessPoints = createDetailsCollection(owner
               .getAllPersistentAccessPoints());
      }
      else
      {
         final boolean isArchiveAuditTrail = Parameters.instance().getBoolean(
               Constants.CARNOT_ARCHIVE_AUDITTRAIL, false);
         // do instant evaluation
         allAccessPoints = isArchiveAuditTrail ? Collections.emptyList()
               : createDetailsCollection(owner.getAllAccessPoints());
      }

      // store info for transient access point initialization
      this.providerClassName = owner.getProviderClass();
      this.detailsAttributes = Collections.unmodifiableMap(detailsAttributes);
      this.typeAttributes = Collections.unmodifiableMap(typeAttributes);
   }

   private static List createDetailsCollection(Iterator iterator)
   {
      return DetailsFactory.createCollection(iterator, AccessPoint.class,
            AccessPointDetails.class);
   }

   public List getAccessPoints()
   {
      if (null == allAccessPoints || null == transientAccessPoints)
      {
         initTransientAccessPoints();
         if (transientAccessPoints != null)
         {
            if (persistentAccessPoints != null)
            {
               allAccessPoints = CollectionUtils.union(persistentAccessPoints,
                     transientAccessPoints);
            }
            else
            {
               allAccessPoints = transientAccessPoints;
            }
         }
      }

      return allAccessPoints;
   }

   public AccessPointDetails findAccessPoint(String id)
   {
      for (Iterator persIter = getAccessPoints().iterator(); persIter.hasNext();)
      {
         AccessPointDetails details = (AccessPointDetails) persIter.next();
         if (id.equals(details.getId()))
         {
            return details;
         }
      }

      return null;
   }

   private void initTransientAccessPoints()
   {
      if (null == transientAccessPoints)
      {
         if (StringUtils.isEmpty(providerClassName))
         {
            transientAccessPoints = Collections.EMPTY_LIST;
         }
         else
         {
            Collection tmpAccessPoints = new ArrayList();

            AccessPointProvider provider = (AccessPointProvider) Reflect
                  .getInstance(providerClassName);

            Iterator i = provider.createIntrinsicAccessPoints(detailsAttributes,
                  typeAttributes);
            if (i != null)
            {
               while (i.hasNext())
               {
                  tmpAccessPoints.add((AccessPoint) i.next());
               }

               transientAccessPoints = DetailsFactory.createCollection(
                     tmpAccessPoints.iterator(), AccessPoint.class,
                     AccessPointDetails.class);
            }
         }
      }
   }
}
