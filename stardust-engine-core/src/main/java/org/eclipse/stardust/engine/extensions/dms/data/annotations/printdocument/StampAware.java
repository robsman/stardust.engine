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

public interface StampAware
{
   /**
    * Adds the Stamp.
    *
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the id of the Stamp already exists.
    *
    * @param highlight
    */
   void addStamp(Stamp stamp);

   Stamp getStamp(String id);

   Set<Stamp> getStamps();

   void removeStamp(String id);

   /**
    * Replaces the stored stamps with the given set.<br>
    *
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the given set contains more than one element with the same
    *            Identifiable#id.
    *
    * @param stamps
    */
   void setStamps(Set<Stamp> stamps);

   /**
    * Adds the given set of stamps to the stored ones.<br>
    *
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the given set contains more than one element with the same
    *            Identifiable#id.<br>
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the set contains at least one stamp with the same Identifiable#id as a
    *            stored stamp.
    *
    * @param stamps
    */
   void addAllStamps(Set<Stamp> stamps);

   /**
    * Removes all stamps.
    */
   void removeAllStamps();

}
