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
package org.eclipse.stardust.engine.core.compatibility.el;


import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;

import org.eclipse.stardust.common.error.InternalException;

/**
 * @author fherinean
 * @version $Revision$
 */
public class ELTokenizer extends StreamTokenizer
{
   public ELTokenizer(String value)
   {
      super(new StringReader(value));
      ordinaryChar('/');
      ordinaryChar('.');
      wordChars('_', '_');
      slashSlashComments(false);
      slashStarComments(false);
   }

   public int nextToken()
   {
      try
      {
         return super.nextToken();
      }
      catch (IOException e)
      {
         throw new InternalException(e);
      }
   }
}
