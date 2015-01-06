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
package org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument;

import java.util.List;

public interface PageSequenceAware
{
   List<Integer> getPageSequence();

   void movePage(int sourcePosition, int targetPosition);

   void movePages(int pageCount, int sourcePosition, int targetPosition);

   /**
    * Replaces the stored page sequence with the given one.
    *
    * @param pageSequence
    */
   void setPageSequence(List<Integer> pageSequence);

   /**
    * Resets the page sequence to the natural order.
    */
   void resetPageSequence();

}
