/*******************************************************************************
* Copyright (c) 2014 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Holger.Prause (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.engine.api.query;


/**
 * Convenience class providing smoother handling of AND NOT filter terms.
 * @author Holger.Prause
 * @version $Revision: $
 */
public class FilterAndNotTerm extends FilterTerm
{
   /**
    *
    */
   private static final long serialVersionUID = 1L;

   FilterAndNotTerm(FilterVerifier verifier)
   {
      super(verifier, ANDNOT);
   }

   @Override
   protected FilterTerm createOfSameKind(FilterVerifier verifier)
   {
      if (null == verifier)
      {
         verifier = getVerifier();
      }
      return new FilterAndNotTerm(verifier);
   }
}
