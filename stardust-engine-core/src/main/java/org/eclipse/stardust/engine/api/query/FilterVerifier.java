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

import java.io.Serializable;

import org.eclipse.stardust.common.StringKey;


/**
 * Interface declaration for custom filter verification strategies. May be used to
 * restrict use of certain filter types for specific queries.
 *
 * @author rsauer
 * @version $Revision$
 */
public interface FilterVerifier extends Serializable
{
   VerificationKey FILTER_UNSUPPORTED =
         new VerificationKey("unsupported", "unsupported");
   VerificationKey FILTER_SUPPORTED =
         new VerificationKey("supported", "supported");
   VerificationKey FILTER_IGNORED =
         new VerificationKey("ignored", "ignored");

   /**
    * Template method for customization of the set of valid filter conditions.
    *
    * @param filter
    * @return
    */
   VerificationKey verifyFilter(FilterCriterion filter);

   /**
    * Enumeration class used to mark filter verification results.
    */
   static class VerificationKey extends StringKey
   {
      private VerificationKey(String id, String defaultName)
      {
         super(id, defaultName);
      }
   }
}
