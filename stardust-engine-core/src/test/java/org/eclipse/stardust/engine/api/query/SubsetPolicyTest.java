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

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;

import junit.framework.TestCase;


public class SubsetPolicyTest extends TestCase
{
   public void testPositiveSkippedEntriesNumber()
   {
      try
      {
         new SubsetPolicy(5, 1, false);
      }
      catch (PublicException e)
      {
         fail("Expected no PublicException");
      }
   }

   public void testNegativeSkippedEntriesNumber()
   {
      try
      {
         new SubsetPolicy(5, -1, false);
         fail("Expected PublicException");
      }
      catch (PublicException e)
      {
      }
   }

   public void testZeroSkippedEntriesNumber()
   {
      try
      {
         new SubsetPolicy(5, 0, false);
      }
      catch (PublicException e)
      {
         fail("Expected no PublicException");
      }
   }
}
