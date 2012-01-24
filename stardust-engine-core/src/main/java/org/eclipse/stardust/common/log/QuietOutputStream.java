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
package org.eclipse.stardust.common.log;

import java.io.OutputStream;

/**
 * This class extends a java.io.OutputStream and overwrites all methods so that
 * the class does nothing. Using this OutputStream as for instance System.err
 * all message written to System.err using System.err.println() are thrown away.
 *
 * @author Sebastian Woelk
 * @version $Revision$
 */
public class QuietOutputStream extends OutputStream
{
   public QuietOutputStream()
   {}

   public void close()
   {}

   public void flush()
   {}

   public void write(byte[] b)
   {}

   public void write(byte[] b, int off, int len)
   {}

   public void write(int b)
   {}
}
