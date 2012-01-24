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
package org.eclipse.stardust.engine.core.compatibility.extensions.dms.data;

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.core.compatibility.extensions.dms.Document;
import org.eclipse.stardust.engine.core.compatibility.extensions.dms.DocumentSet;


/**
 * @author rsauer
 * @version $Revision$
 */
public class DocumentSetStorageBean implements Serializable, List/*<Document>*/, DocumentSet
{
   public static final String TABLE_NAME = "dms_documentset";
   
   private static final long serialVersionUID = 1175807355815409855L;  
   
   private long oid;
   
   private List documents = new ArrayList();
   
   ////
   //// List<Document>
   ////
   
   public void add(int index, Object element)
   {
      documents.add(index, element);
   }

   public boolean add(Object o)
   {
      return documents.add(o);
   }

   public boolean addAll(Collection c)
   {
      return documents.addAll(c);
   }

   public boolean addAll(int index, Collection c)
   {
      return documents.addAll(index, c);
   }

   public void clear()
   {
      documents.clear();
   }

   public boolean contains(Object o)
   {
      return documents.contains(o);
   }

   public boolean containsAll(Collection c)
   {
      return documents.containsAll(c);
   }

   public Object get(int index)
   {
      return documents.get(index);
   }

   public int indexOf(Object o)
   {
      return documents.indexOf(o);
   }

   public boolean isEmpty()
   {
      return documents.isEmpty();
   }

   public Iterator iterator()
   {
      return Collections.unmodifiableList(documents).iterator();
   }

   public int lastIndexOf(Object o)
   {
      return documents.lastIndexOf(o);
   }

   public ListIterator listIterator()
   {
      return Collections.unmodifiableList(documents).listIterator();
   }

   public ListIterator listIterator(int index)
   {
      return Collections.unmodifiableList(documents).listIterator(index);
   }

   public Object remove(int index)
   {
      return documents.remove(index);
   }

   public boolean remove(Object o)
   {
      return documents.remove(o);
   }

   public boolean removeAll(Collection c)
   {
      return documents.removeAll(c);
   }

   public boolean retainAll(Collection c)
   {
      return documents.retainAll(c);
   }

   public Object set(int index, Object element)
   {
      return documents.set(index, element);
   }

   public int size()
   {
      return documents.size();
   }

   public List subList(int fromIndex, int toIndex)
   {
      return Collections.unmodifiableList(documents).subList(fromIndex, toIndex);
   }

   public Object[] toArray()
   {
      return documents.toArray();
   }

   public Object[] toArray(Object[] a)
   {
      return documents.toArray(a);
   }

   ////
   //// legacy DocumentSet
   ////

   public long getOid()
   {
      return oid;
   }

   public void setOid(long oid)
   {
      this.oid = oid;
   }

   public int getSize()
   {
      return documents.size();
   }
   
   public void addDocument(Document doc)
   {
      // TODO prevent duplicate IDs?
      add(doc);
   }

   public void addDocuments(DocumentSet docs)
   {
      // TODO prevent duplicate IDs?
      addAll((List) docs);
   }

   public Document getFirstDocument()
   {
      return !isEmpty() ? (Document) get(0) : null;
   }
   
   public Document getDocument(String id)
   {
      Document result = null;
      
      for (Iterator i = iterator(); i.hasNext();)
      {
         Document doc = (Document) i.next();
         if (CompareHelper.areEqual(id, doc.getId()))
         {
            result = doc;
            break;
         }
      }
      
      return result;
   }

   public Document getDocument(int idx)
   {
      return ((0 <= idx) && (idx < size()))
            ? (Document) get(idx)
            : null;
   }
   
   public Collection getDocuments()
   {
      return Collections.unmodifiableList(documents);
   }

   public void updateDocument(Document doc)
   {
      if (null != doc)
      {
         Document document = getDocument(doc.getId());
         if ((document instanceof DocumentStorageBean)
               && (doc instanceof DocumentStorageBean))
         {
            ((DocumentStorageBean) document).mergeDocuments((DocumentStorageBean) doc);
         }
         else
         {
            if (null != document)
            {
               remove(document);
            }
            add(doc);
         }
      }
   }
   
   public void updateDocuments(DocumentSet docs)
   {
      for (Iterator i = docs.getDocuments().iterator(); i.hasNext();)
      {
         Document doc = (Document) i.next();
         updateDocument(doc);
      }
   }

   public void removeDocument(String id)
   {
      Document doc = getDocument(id);
      if (null != doc)
      {
         remove(doc);
      }
   }
   
   public void removeDocument(Document doc)
   {
      if (null != doc)
      {
         removeDocument(doc.getId());
      }
   }

   public void removeDocuments(DocumentSet docs)
   {
      for (Iterator i = docs.getDocuments().iterator(); i.hasNext();)
      {
         Document doc = (Document) i.next();
         removeDocument(doc);
      }
   }

   public void mergeDocumentSets(DocumentSetStorageBean docs)
   {
      mergeDocumentSets(docs, true);
   }

   public void mergeDocumentSets(DocumentSetStorageBean docs, boolean mergeOid)
   {
      if (0 == oid && mergeOid)
      {
         this.oid = docs.oid;
      }
      
      documents = docs.documents;
      // TODO if needed, add more sophisticated code to copy documents
      /*
      if (null != documents)
      {
         Map docIds = new HashMap();
         for (Iterator i = documents.iterator(); i.hasNext();)
         {
            Document doc = (Document) i.next();
            docIds.put(doc.getId(), doc);
         }
         
         if (null != docs.documents)
         {
            for (Iterator i = docs.documents.iterator(); i.hasNext();)
            {
               Document doc = (Document) i.next();
               
               Document oldDoc = (Document) docIds.get(doc.getId());
               if (null != oldDoc)
               {
                  documents.remove(oldDoc);
               }
               documents.add(doc);
            }
         }
      }
      else
      {
         documents = docs.documents;
      }
      */
   }
}
