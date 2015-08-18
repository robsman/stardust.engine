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

import java.util.Set;

public interface PageOrientationAware
{

   void addPageOrientation(PageOrientation pageOrientation);

   PageOrientation getPageOrientation(int pageNumber);

   Set<PageOrientation> getPageOrientations();

   void removePageOrientation(int pageNumber);

   /**
    * Replaces the stored page orientations with the given set.<br>
    *
    * @param pageOrientations
    */
   void setPageOrientations(Set<PageOrientation> pageOrientations);

   /**
    * Adds the given set of page orientations to the stored ones.<br>
    *
    * @param pageOrientations
    */
   void addAllPageOrientations(Set<PageOrientation> pageOrientations);

   /**
    * Removes all page orientations.
    */
   void removeAllPageOrientations();

}
