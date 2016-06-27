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

public interface HighlightCapable
{
   /**
    * Adds the Highlight.
    *
    * @param highlight
    *
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the id of the Highlight already exists.
    *
    */
   void addHighlight(Highlight highlight);

   Highlight getHighlight(String id);

   Set<Highlight> getHighlights();

   void removeHighlight(String id);

   /**
    * Replaces the stored highlights with the given set.<br>
    *
    * @param hightlights
    *
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the given set contains more than one element with the same
    *            Identifiable#id.
    */
   void setHighlights(Set<Highlight> hightlights);

   /**
    *
    * Adds the given set of highlights to the stored ones.<br>
    *
    * @param highlights
    * 
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the given set contains more than one element with the same
    *            Identifiable#id.<br>
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the set contains at least one highlight with the same Identifiable#id
    *            as a stored highlight.
    */
   void addAllHighlights(Set<Highlight> highlights);

   /**
    * Removes all highlights.
    */
   void removeAllHighlights();

}
