/*
 * Copyright (C) 2007, 2011 The Guava Authors and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.stardust.common.utils.io;

import java.io.Closeable;
import java.io.IOException;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.sequence.CachingSequenceGenerator;


/**
 * Utility methods for working with {@link Closeable} objects.
 * 
 * The code has been taken from the google guava-libraries
 * http://code.google.com/p/guava-libraries/, class
 * <code>com.google.common.io.Closeables</code>.
 *
 * The following modification has been done:
 * <ol>
 * <li>changed logger to infinity logger
 * </ol>
 */
public final class CloseableUtil
{
   private static final Logger logger = LogManager.getLogger(CachingSequenceGenerator.class);

   private CloseableUtil()
   {}

   /**
    * Closes a {@link Closeable}, with control over whether an {@code IOException} may be
    * thrown. This is primarily useful in a finally block, where a thrown exception needs
    * to be logged but not propagated (otherwise the original exception will be lost).
    * 
    * <p>
    * If {@code swallowIOException} is true then we never throw {@code IOException} but
    * merely log it.
    * 
    * <p>
    * Example:
    * 
    * <p>
    * 
    * <pre>
    * public void useStreamNicely() throws IOException {
    * SomeStream stream = new SomeStream("foo");
    * boolean threw = true;
    * try {
    *   // Some code which does something with the Stream. May throw a
    *   // Throwable.
    *   threw = false; // No throwable thrown.
    * } finally {
    *   // Close the stream.
    *   // If an exception occurs, only rethrow it if (threw==false).
    *   Closeables.close(stream, threw);
    * }
    * </pre>
    * 
    * @param closeable
    *           the {@code Closeable} object to be closed, or null, in which case this
    *           method does nothing
    * @param swallowIOException
    *           if true, don't propagate IO exceptions thrown by the {@code close} methods
    * @throws IOException
    *            if {@code swallowIOException} is false and {@code close} throws an
    *            {@code IOException}.
    */
   public static void close(Closeable closeable, boolean swallowIOException)
         throws IOException
   {
      if (closeable == null)
      {
         return;
      }
      try
      {
         closeable.close();
      }
      catch (IOException e)
      {
         if (swallowIOException)
         {
            logger.warn("IOException thrown while closing Closeable.", e);
         }
         else
         {
            throw e;
         }
      }
   }

   /**
    * Equivalent to calling {@code close(closeable, true)}, but with no IOException in the
    * signature.
    * 
    * @param closeable
    *           the {@code Closeable} object to be closed, or null, in which case this
    *           method does nothing
    */
   public static void closeQuietly(Closeable closeable)
   {
      try
      {
         close(closeable, true);
      }
      catch (IOException e)
      {
         logger.error("IOException should not have been thrown.", e);
      }
   }
}
