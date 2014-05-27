/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.persistence;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterInstance;


/**
 * A database driver encapsulates database sessions and connections.
 * It manages thread binding.
 * 
 * @author Marc Gille
 * @version $Revision$
 */
public interface Session
{
   static final QueryExtension NO_QUERY_EXTENSION = null;
   
   static final FetchPredicate NO_FETCH_PREDICATE = null;
   
   static final int NO_TIMEOUT = 0;
   
   /**
    * Initally create the persistent object in the database. This method is
    * mostly called by the constructors of Persistents.
    * 
    * @param object the object to create
    */
   public void cluster(Persistent object);

   public void cluster(DataClusterInstance cluster);

   /**
    * Closes the connection to the database and releases all database resources.
    */
   public void disconnect() throws SQLException;

   /**
    * This method checks whether at least one record exists for the provided predicate.
    *
    * @param type the type of objects which have to be counted
    * @param queryExtension the condition which the counted objects have to match
    * @return true if there is at least one record otherwise false
    */
   public boolean exists(Class type, QueryExtension queryExtension);

   /**
    * Count all objects of a specified type.
    *
    * @param type the type of objects which have to be counted
    * @return the number of objects available
    */
   public long getCount(Class type);

   /**
    * Counts all objects of a specified type which match the condition specified by the
    * <tt>queryExtension</tt> argument.
    *
    * @param type the type of objects which have to be counted
    * @param queryExtension the condition which the counted objects have to match
    * @return the number of records of the specified type
    */
   public long getCount(Class type, QueryExtension queryExtension);

   /**
    * Counts all objects of a specified type which match the condition specified by the
    * <tt>queryExtension</tt> argument.
    *
    * @param type the type of objects which have to be counted
    * @param queryExtension the condition which the counted objects have to match
    * @param timeout
    * @return the number of records of the specified type
    */
   public long getCount(Class type, QueryExtension queryExtension, int timeout);

   /**
    * Counts all objects of a specified type which match the condition specified by the
    * <tt>queryExtension</tt> argument.
    *
    * @param type the type of objects which have to be counted
    * @param queryExtension the condition which the counted objects have to match
    * @return the number of records of the specified type
    */
   public long getCount(Class type, QueryExtension queryExtension, boolean mayFail);

   /**
    * Counts all objects of a specified type which match the condition specified by the
    * <tt>queryExtension</tt> argument.
    *
    * @param type the type of objects which have to be counted
    * @param queryExtension specification of additional relations, predicates, order etc.
    * @param fetchPredicate optional predicate applied before rows are included into count
    * @param timeout
    * @return the number of records of the specified type
    */
   public long getCount(Class type, QueryExtension queryExtension,
         FetchPredicate fetchPredicate, int timeout);

   /**
    * Counts all objects of a specified type which match the condition specified by the
    * <tt>queryExtension</tt> argument.
    *
    * @param type the type of objects which have to be counted
    * @param queryExtension specification of additional relations, predicates, order etc.
    * @param fetchPredicate optional predicate applied before rows are included into count
    * @param timeout
    * @param totalCountThreshold the maximum value of total count items
    * @return the number of records of the specified type
    */
   public long getCount(Class type, QueryExtension queryExtension,
         FetchPredicate fetchPredicate, int timeOut, long totalCountThreshold);

   /**
    * Fires a query against the database and returns an iterator containing all
    * persistent object of the specified type.
    *
    * @param type the type of the persistent objects to retrieve.
    * @return A {@link ResultIterator} containing the requested objects.
    */
   public ResultIterator getIterator(Class type);

   /**
    * Fires a query against the database and returns an iterator containing all persistent
    * object of the specified <code>type</code> which match the specified
    * <code>queryExtension</code> in arbitrary order.
    *
    * @param type the type of objects to retrieve
    * @param queryExtension the condition which the retrieved objects have to match
    * @return A {@link ResultIterator} containing the requested objects.
    */
   public <E, V extends E> ResultIterator<E> getIterator(Class<V> type, QueryExtension queryExtension);

   /**
    * Fires a query against the database and returns an iterator containing a
    * maximum number given by the <code>extent</code> argument starting with the
    * <code>startIndex</code>th objects matching the given <code>queryExtension</code> in
    * arbitrary order.
    *
    * @param type       the type of objects to retrieve
    * @param queryExtension  the condition which the retrieved objects have to match
    * @param startIndex the index of the first object to retrieve
    * @param extent     the maximum number of objects which can be put into the
    *                   {@link ResultIterator}
    * @return A {@link ResultIterator} containing the requested objects.
    */
   public ResultIterator getIterator(
         Class type, QueryExtension queryExtension, int startIndex, int extent);

   /**
    * Fires a query against the database and returns an iterator containing a
    * maximum number given by the <code>extent</code> argument starting with the
    * <code>startIndex</code>th objects matching the given <code>queryExtension</code> in
    * arbitrary order.
    *
    * @param type       the type of objects to retrieve
    * @param queryExtension  the condition which the retrieved objects have to match
    * @param startIndex the index of the first object to retrieve
    * @param extent     the maximum number of objects which can be put into the
    *                   {@link ResultIterator}
    * @param timeout
    * @return A {@link ResultIterator} containing the requested objects.
    */
   public ResultIterator getIterator(
         Class type, QueryExtension queryExtension, int startIndex, int extent, int timeout);

   /**
    * Fires a query against the database and returns an iterator containing a
    * maximum number given by the <code>extent</code> argument starting with the
    * <code>startIndex</code>th objects matching the given <code>predicate</code> in
    * an order according to <code>order</code>.
    *
    * @param type                the type of objects to retrieve
    * @param queryExtension      specification of additional relations, predicates, order
    *                            etc.
    * @param startIndex          the index of the first object to retrieve
    * @param extent              the maximum number of objects which can be put into the
    * @param fetchPredicate      optional predicate applied before rows are fetched into memory
    *                            {@link ResultIterator}
    * @param timeout
    * @return A {@link ResultIterator} containing the requested objects.
    */
   public ResultIterator getIterator(Class type, QueryExtension queryExtension,
         int startIndex, int extent, FetchPredicate fetchPredicate, boolean countAll,
         int timeout);

   /**
    * <p/>
    * This method retrieves the requested object from the persistent storage put
    * them into a vector and return this vector to the caller.</p>
    * <p/>
    * Any resources aquired (i.e. ResultSet, Connections, whatever) to get the
    * result is  released when this method returns.</p>
    *
    * @param type the type of objects which have to be retrieved
    * @return a Vector containing the requested objects
    */
   public Vector getVector(Class type);

   /**
    * This method retrieves the requested objects in arbitrary order from the persistent
    * storage put them into a vector and return this vector to the caller.
    *
    * @param type      the type of objects which have to be retrieved
    * @param queryExtension the condition which the retrieved objects have to match
    * @see #getVector(Class)
    */
   public Vector getVector(Class type, QueryExtension queryExtension);

   /**
    * Flushes any, by the current transaction cached, objects and send a COMMIT
    * to the database. The used connection is put back to the connection pool
    * and the cache is cleared.
    */
   public void save();

   /**
    * Rolls back the current transaction removes all objects from the cache
    * which were affected by this transaction.
    */
   public void rollback();

   /**
    * @param type the type of objects which have to be counted
    */
   public <T extends Persistent> T findByOID(Class<T> type, long oid);

   /**
    * @param type the type of objects which have to be counted
    */
   public <T extends Persistent> T findFirst(Class<T> type, QueryExtension queryExtension);

   /**
    * @param type the type of objects which have to be counted
    * @param timeout
    */
   public <T extends Persistent> T findFirst(Class<T> type, QueryExtension queryExtension, int timeout);

   /**
    * Creates a persistent vector.
    */
   public PersistentVector createPersistentVector();

   /**
    * Initializes an object reference from the database.
    */
   public Persistent fetchLink(Persistent persistent, String linkName);

   /**
    * Clears tables and indexes of a persistent capable
    * class.
    */
   public void deleteAllInstances(Class type, boolean delay);

   void delete(Class type, PredicateTerm predicate, boolean delay);

   void delete(Class type, PredicateTerm predicate, Join join, boolean delay);

   void delete(Class type, PredicateTerm predicate, Joins joins, boolean delay);

   /**
    * Tries to lock an object from concurrent access.
    *
    * @param type    the type of the object to be locked
    * @param oid     the OID of the object to be locked
    *
    * @throws ConcurrencyException
    */
   void lock(Class type, long oid) throws ConcurrencyException;

   /**
    * Tries to lock an object from concurrent access, providing the
    * ability to specify a timeout.
    *
    * @param type    the type of the object to be locked
    * @param oid     the OID of the object to be locked
    * @param timeout the timeout in seconds to wait in case the object is currently locked
    *
    * @throws ConcurrencyException
    */
   void lock(Class type, long oid, int timeout) throws ConcurrencyException;

   boolean isSynchronized(Persistent persistent);

   void setSynchronized(Persistent persistent);

   /**
    * Shows whether an object identified by its type and oid is already loaded to the
    * session cache.
    *
    * @param type The type of the object to queried.
    * @param identityKey A per PK unique key of the object to be queried.
    * @return true in case the queried object is already loaded into the cache.
    */
   public boolean existsInCache(Class type, Object identityKey);

   public <T> Iterator<T> getSessionCacheIterator(Class<T> type, FilterOperation<T> op);

   /**
    * Returns true if no changes are allowed to this audit trail (e.g. it is an archive audit trail)
    *
    * @return true if the audit trail is read-only
    */
   public boolean isReadOnly();

   interface NotJoinEnabled
   {

   }

   public interface FilterOperation<T>
   {
      public FilterResult filter(T persistentToFilter);

      public static enum FilterResult { ADD, OMIT }
   }
}
