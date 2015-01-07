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

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;

public class PrintDocumentAnnotationsImpl
      implements PrintDocumentAnnotations, Serializable
{

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private transient static OrderFieldComparator<PageBookmark> comparator = new OrderFieldComparator<PageBookmark>();

   private Set<PageBookmark> pageBookmarks = new TreeSet<PageBookmark>(comparator);

   private String defaultPageBookmarkId;

   private Set<Note> notes = CollectionUtils.newSet();

   private Set<Highlight> highlights = CollectionUtils.newSet();

   private Set<Stamp> stamps = CollectionUtils.newSet();

   private Set<PageOrientation> pageOrientations = CollectionUtils.newSet();

   private List<Integer> pageSequence = CollectionUtils.newArrayList();

   private transient PageBookmark cachedDefaultBookmark;

   private String templateType;

   // CorrespondenceCapable (needs to be impl for map conversion)
   private CorrespondenceCapableImpl correspondenceCapable = new CorrespondenceCapableImpl();

   public void addBookmark(PageBookmark bookmark)
   {
      bookmark.order = pageBookmarks.size();

      addIdentifiable(bookmark, pageBookmarks);
   }

   public PageBookmark getBookmark(String id)
   {
      return getIdentifiable(id, pageBookmarks);
   }

   public Set<PageBookmark> getBookmarks()
   {
      return Collections.unmodifiableSet(pageBookmarks);
   }

   public void removeBookmark(String id)
   {
      PageBookmark removedBookmark = removeIdentifiable(id, pageBookmarks);

      if (removedBookmark != null)
      {
         int removedOrder = removedBookmark.getOrder();
         for (PageBookmark bookmark : pageBookmarks)
         {
            int bookmarkOrder = bookmark.getOrder();
            if (bookmarkOrder > removedOrder)
            {
               bookmark.order = bookmarkOrder - 1;
            }
         }
         if (defaultPageBookmarkId != null && defaultPageBookmarkId.equals(id))
         {
            cachedDefaultBookmark = null;
         }
      }
   }

   public void setBookmarks(Set<PageBookmark> bookmarks)
   {
      removeAllBookmarks();
      addAllBookmarks(bookmarks);
   }

   public void addAllBookmarks(Set<PageBookmark> bookmarks)
   {
      if (bookmarks != null)
      {
         if ( !containsIdentifiable(pageBookmarks, bookmarks))
         {
            for (PageBookmark pageBookmark : bookmarks)
            {
               addBookmark(pageBookmark);
            }
         }
      }
   }

   public void removeAllBookmarks()
   {
      pageBookmarks.clear();
      cachedDefaultBookmark = null;
   }

   public void moveBookmark(int sourceOrder, int targetOrder)
   {

      int size = pageBookmarks.size();
      if (sourceOrder >= size - 1 || targetOrder >= size - 1 || sourceOrder < 0
            || targetOrder < 0)
      {
         throw new IndexOutOfBoundsException();
      }

      PageBookmark moveBookmark = null;
      for (PageBookmark bookmark : pageBookmarks)
      {
         if (bookmark.getOrder() == sourceOrder)
         {
            moveBookmark = bookmark;
            break;
         }
      }

      if (moveBookmark != null)
      {
         TreeSet<PageBookmark> newPageBookmarks = new TreeSet<PageBookmark>(comparator);

         int insertIdx = 0;
         for (PageBookmark bookmark : pageBookmarks)
         {
            if (insertIdx == targetOrder)
            {
               moveBookmark.order = newPageBookmarks.size();
               newPageBookmarks.add(moveBookmark);
               insertIdx++ ;
            }
            if (bookmark.getOrder() != sourceOrder)
            {
               bookmark.order = newPageBookmarks.size();
               newPageBookmarks.add(bookmark);
               insertIdx++ ;
            }
         }
         pageBookmarks = newPageBookmarks;
      }

   }

   public PageBookmark getDefaultBookmark()
   {
      PageBookmark ret = null;
      if ( !isEmpty(defaultPageBookmarkId))
      {
         if (cachedDefaultBookmark != null)
         {
            ret = cachedDefaultBookmark;
         }
         else
         {
            for (PageBookmark bookmark : pageBookmarks)
            {
               if (bookmark.getId().equals(defaultPageBookmarkId))
               {
                  ret = bookmark;
                  cachedDefaultBookmark = bookmark;
               }
            }
         }
      }
      return ret;
   }

   public void setDefaultBookmark(String id)
   {
      if (defaultPageBookmarkId == null || !defaultPageBookmarkId.equals(id))
      {
         this.defaultPageBookmarkId = id;
         this.cachedDefaultBookmark = null;
      }
   }

   public void addNote(Note note)
   {
      addIdentifiable(note, notes);
   }

   public Note getNote(String id)
   {
      return getIdentifiable(id, notes);
   }

   public Set<Note> getNotes()
   {
      return Collections.unmodifiableSet(notes);
   }

   public void removeNote(String id)
   {
      removeIdentifiable(id, notes);
   }

   public void setNotes(Set<Note> notes)
   {
      removeAllNotes();
      addAllNotes(notes);
   }

   public void addAllNotes(Set<Note> notes)
   {
      if (notes != null)
      {
         if ( !containsIdentifiable(this.notes, notes))
         {
            for (Note note : notes)
            {
               addNote(note);
            }
         }
      }
   }

   public void removeAllNotes()
   {
      notes.clear();
   }

   public void addHighlight(Highlight highlight)
   {
      addIdentifiable(highlight, highlights);
   }

   public Highlight getHighlight(String id)
   {
      return getIdentifiable(id, highlights);
   }

   public Set<Highlight> getHighlights()
   {
      return Collections.unmodifiableSet(highlights);
   }

   public void removeHighlight(String id)
   {
      removeIdentifiable(id, highlights);
   }

   public void setHighlights(Set<Highlight> hightlights)
   {
      removeAllHighlights();
      addAllHighlights(hightlights);
   }

   public void addAllHighlights(Set<Highlight> highlights)
   {
      if (highlights != null)
      {
         if ( !containsIdentifiable(this.highlights, highlights))
         {
            for (Highlight highlight : highlights)
            {
               addHighlight(highlight);
            }
         }
      }
   }

   public void removeAllHighlights()
   {
      highlights.clear();
   }

   public void addStamp(Stamp stamp)
   {
      addIdentifiable(stamp, stamps);
   }

   public Stamp getStamp(String id)
   {
      return getIdentifiable(id, stamps);
   }

   public Set<Stamp> getStamps()
   {
      return Collections.unmodifiableSet(stamps);
   }

   public void removeStamp(String id)
   {
      removeIdentifiable(id, stamps);
   }

   public void setStamps(Set<Stamp> stamps)
   {
      removeAllStamps();
      addAllStamps(stamps);
   }

   public void addAllStamps(Set<Stamp> stamps)
   {
      if (stamps != null)
      {
         if ( !containsIdentifiable(this.stamps, stamps))
         {
            for (Stamp stamp : stamps)
            {
               addStamp(stamp);
            }
         }
      }
   }

   public void removeAllStamps()
   {
      stamps.clear();
   }

   public void addPageOrientation(PageOrientation pageOrientation)
   {
      pageOrientations.add(pageOrientation);
   }

   public PageOrientation getPageOrientation(int pageNumber)
   {
      for (PageOrientation element : pageOrientations)
      {
         if (element.getPageNumber() == pageNumber)
         {
            return element;
         }
      }
      return null;
   }

   public Set<PageOrientation> getPageOrientations()
   {
      return Collections.unmodifiableSet(pageOrientations);
   }

   public void removePageOrientation(int pageNumber)
   {
      PageOrientation removeEntry = getPageOrientation(pageNumber);
      if (removeEntry != null)
      {
         pageOrientations.remove(removeEntry);
      }
   }

   public void setPageOrientations(Set<PageOrientation> pageOrientations)
   {
      removeAllPageOrientations();
      addAllPageOrientations(pageOrientations);
   }

   public void addAllPageOrientations(Set<PageOrientation> pageOrientations)
   {
      if (pageOrientations != null)
      {
         for (PageOrientation pageOrientation : pageOrientations)
         {
            addPageOrientation(pageOrientation);
         }
      }
   }

   public void removeAllPageOrientations()
   {
      pageOrientations.clear();
   }

   public List<Integer> getPageSequence()
   {
      return new ArrayList<Integer>(pageSequence);
   }

   public synchronized void movePage(int sourcePosition, int targetPosition)
   {
      if (sourcePosition > targetPosition)
      {
         Integer source = pageSequence.get(sourcePosition);
         for (int i = sourcePosition; i > targetPosition; i-- )
         {
            Integer current = pageSequence.get(i - 1);
            pageSequence.set(i, current);
         }
         pageSequence.set(targetPosition, source);
      }
      else if (sourcePosition < targetPosition)
      {
         Integer source = pageSequence.get(sourcePosition);
         for (int i = sourcePosition; i < targetPosition; i++ )
         {
            Integer current = pageSequence.get(i + 1);
            pageSequence.set(i, current);
         }
         pageSequence.set(targetPosition, source);
      }
   }

   public synchronized void movePages(int pageCount, int sourcePosition,
         int targetPosition)
   {
      if (sourcePosition > targetPosition)
      {
         List<Integer> source = new ArrayList<Integer>();
         for (int i = 0; i < pageCount; i++ )
         {
            source.add(pageSequence.get(sourcePosition + i));
         }

         for (int i = sourcePosition + pageCount - 1; i > targetPosition + pageCount - 1; i-- )
         {
            Integer current = pageSequence.get(i - pageCount);
            pageSequence.set(i, current);
         }

         for (int i = 0; i < pageCount; i++ )
         {
            pageSequence.set(targetPosition + i, source.get(i));
         }
      }
      else if (sourcePosition < targetPosition)
      {
         List<Integer> source = new ArrayList<Integer>();
         for (int i = 0; i < pageCount; i++ )
         {
            source.add(pageSequence.get(sourcePosition + i));
         }

         for (int i = sourcePosition; i < targetPosition; i++ )
         {
            Integer current = pageSequence.get(i + pageCount);
            pageSequence.set(i, current);
         }

         for (int i = 0; i < pageCount; i++ )
         {
            pageSequence.set(targetPosition + i, source.get(i));
         }
      }

   }

   public void setPageSequence(List<Integer> pageSequence)
   {
      this.pageSequence = new ArrayList<Integer>(pageSequence);
   }

   public void resetPageSequence()
   {
      Collections.sort(pageSequence);
   }

   private <T extends Identifiable> T addIdentifiable(T identifiable,
         Collection<T> collection)
   {
      if (identifiable != null)
      {
         for (T t : collection)
         {
            if (t.getId() != null && t.getId().equals(identifiable.getId()))
            {
               throw new InvalidArgumentException(
                     BpmRuntimeError.DMS_ANNOTATIONS_ID_PRESENT.raise(identifiable.getId()));
            }
         }
         collection.add(identifiable);
         return identifiable;
      }
      return null;
   }

   private <T extends Identifiable> T getIdentifiable(String id, Collection<T> collection)
   {
      if ( !isEmpty(id))
      {
         for (T identifiable : collection)
         {
            if (id.equals(identifiable.getId()))
            {
               return identifiable;
            }
         }
      }
      return null;
   }

   private boolean containsIdentifiable(Set< ? extends Identifiable> target,
         Set< ? extends Identifiable> source)
   {
      Set<String> uniqueIds = CollectionUtils.newSet();
      for (Identifiable toAdd : source)
      {
         for (Identifiable existing : target)
         {
            if (toAdd.getId() != null && toAdd.getId().equals(existing.getId()))
            {
               throw new InvalidArgumentException(
                     BpmRuntimeError.DMS_ANNOTATIONS_ID_PRESENT.raise(toAdd.getId()));
            }
         }
         if ( !uniqueIds.add(toAdd.getId()))
         {
            throw new InvalidArgumentException(
                  BpmRuntimeError.DMS_ANNOTATIONS_ID_PRESENT.raise(toAdd.getId()));
         }
      }
      return false;
   }

   private <T extends Identifiable> T removeIdentifiable(String id,
         Collection<T> collection)
   {
      if ( !isEmpty(id))
      {
         T removeEntry = getIdentifiable(id, collection);

         if (removeEntry != null)
         {
            if (collection.remove(removeEntry))
            {
               return removeEntry;
            }
         }
      }
      return null;
   }

   public static class OrderFieldComparator<T> implements Comparator<T>, Serializable
   {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      public int compare(T o1, T o2)
      {
         int o1v = (Integer) Reflect.getFieldValue(o1, "order");
         int o2v = (Integer) Reflect.getFieldValue(o2, "order");

         if (o1v > o2v)
         {
            return 1;
         }
         else if (o1v < o2v)
         {
            return -1;
         }

         if (o1 instanceof Identifiable && o2 instanceof Identifiable)
         {
            String o1Id = ((Identifiable) o1).getId();
            if (o1Id != null)
            {
               return (o1Id.compareTo(((Identifiable) o2).getId()));
            }
         }

         return 0;
      }

   }

   public String getAttachments()
   {
      return correspondenceCapable.getAttachments();
   }

   public void setAttachments(String attachments)
   {
      correspondenceCapable.setAttachments(attachments);
   }

   public String getBlindCarbonCopyRecipients()
   {
      return correspondenceCapable.getBlindCarbonCopyRecipients();
   }

   public void setBlindCarbonCopyRecipients(String bccRecipients)
   {
      correspondenceCapable.setBlindCarbonCopyRecipients(bccRecipients);
   }

   public String getCarbonCopyRecipients()
   {
      return correspondenceCapable.getCarbonCopyRecipients();
   }

   public void setCarbonCopyRecipients(String ccRecipients)
   {
      correspondenceCapable.setCarbonCopyRecipients(ccRecipients);
   }

   public String getFaxNumber()
   {
      return correspondenceCapable.getFaxNumber();
   }

   public void setFaxNumber(String faxNumber)
   {
      correspondenceCapable.setFaxNumber(faxNumber);
   }

   public String getRecipients()
   {
      return correspondenceCapable.getRecipients();
   }

   public void setRecipients(String recipients)
   {
      correspondenceCapable.setRecipients(recipients);
   }

   public Date getSendDate()
   {
      return correspondenceCapable.getSendDate();
   }

   public void setSendDate(Date sendDate)
   {
      correspondenceCapable.setSendDate(sendDate);
   }

   public String getSender()
   {
      return correspondenceCapable.getSender();
   }

   public void setSender(String sender)
   {
      correspondenceCapable.setSender(sender);
   }

   public String getSubject()
   {
      return correspondenceCapable.getSubject();
   }

   public void setSubject(String subject)
   {
      correspondenceCapable.setSubject(subject);
   }

   public boolean isEmailEnabled()
   {
      return correspondenceCapable.isEmailEnabled();
   }

   public void setEmailEnabled(boolean emailEnabled)
   {
      correspondenceCapable.setEmailEnabled(emailEnabled);
   }

   public boolean isFaxEnabled()
   {
      return correspondenceCapable.isFaxEnabled();
   }

   public void setFaxEnabled(boolean faxEnabled)
   {
      correspondenceCapable.setFaxEnabled(faxEnabled);
   }

   public String getTemplateType()
   {
      return templateType;
   }

   public void setTemplateType(String templateType)
   {
      this.templateType = templateType;
   }

   public boolean isTemplate()
   {
      return (templateType != null && !templateType.isEmpty());
   }

}
