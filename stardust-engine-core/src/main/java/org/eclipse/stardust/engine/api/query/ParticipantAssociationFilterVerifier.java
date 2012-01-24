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
package org.eclipse.stardust.engine.api.query;


/**
 * This filter verifier ensures that only instances of
 * {@link ParticipantAssociationFilter} with a filter kind existing in the filter kind
 * whitelist are supported.
 * 
 * @author sborn
 * @version $Revision$
 */
public class ParticipantAssociationFilterVerifier implements FilterVerifier
{
   private final FilterVerifier predecessor;
   private final ParticipantAssociationFilter.Kind[] whitelist;

   public ParticipantAssociationFilterVerifier(
         ParticipantAssociationFilter.Kind[] filterKindWhitelist)
   {
      this(null, filterKindWhitelist);
   }

   public ParticipantAssociationFilterVerifier(FilterVerifier predecessor,
         ParticipantAssociationFilter.Kind[] filterKindWhitelist)
   {
      this.predecessor = predecessor;
      this.whitelist = filterKindWhitelist;
   }

   public VerificationKey verifyFilter(FilterCriterion filter)
   {
      FilterVerifier.VerificationKey result;

      if (null != predecessor)
      {
         result = predecessor.verifyFilter(filter);
      }
      else
      {
         result = FILTER_UNSUPPORTED;
      }
      
      // TODO (sb): this verifier relies on not having a {@link WhitelistFilterVerifyer}
      // as predecessor which on its part contains
      // (@link ParticipantAssociationFilter}.class.
      // In this case the following check would not be executed.

      if (FILTER_UNSUPPORTED.equals(result)
            && (filter instanceof ParticipantAssociationFilter)
            && isInWhitelist(((ParticipantAssociationFilter) filter).getFilterKind()))
      {
         result = FilterVerifier.FILTER_SUPPORTED;
      }

      return result;
   }

   private final boolean isInWhitelist(ParticipantAssociationFilter.Kind candidate)
   {
      boolean isElement = false;

      for (int i = 0; i < whitelist.length; i++)
      {
         if (whitelist[i].equals(candidate))
         {
            isElement = true;
            break;
         }
      }

      return isElement;
   }
}
