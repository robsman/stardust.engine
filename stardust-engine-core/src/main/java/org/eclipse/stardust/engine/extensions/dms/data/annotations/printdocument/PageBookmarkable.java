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

public interface PageBookmarkable
{

   /**
    * Adds the Bookmark.
    *
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the id of the Bookmark already exists.
    *
    * @param highlight
    */
   void addBookmark(PageBookmark bookmark);

   PageBookmark getBookmark(String id);

   Set<PageBookmark> getBookmarks();

   void moveBookmark(int sourceOrder, int targetOrder);

   void removeBookmark(String id);

   PageBookmark getDefaultBookmark();

   void setDefaultBookmark(String id);

   /**
    * Replaces the stored bookmarks with the given set.<br>
    *
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the given set contains more than one element with the same
    *            Identifiable#id.
    *
    * @param bookmarks
    */
   void setBookmarks(Set<PageBookmark> bookmarks);

   /**
    * Adds the given set of bookmarks to the stored ones.<br>
    *
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the given set contains more than one element with the same
    *            Identifiable#id.<br>
    * @throws org.eclipse.stardust.common.error.InvalidArgumentException
    *            if the given set contains at least one bookmark with the same
    *            Identifiable#id as a stored bookmark.
    *
    * @param bookmarks
    */
   void addAllBookmarks(Set<PageBookmark> bookmarks);

   /**
    * Removes all bookmarks.
    */
   void removeAllBookmarks();

}
