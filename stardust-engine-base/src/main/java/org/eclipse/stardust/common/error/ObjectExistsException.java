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
package org.eclipse.stardust.common.error;

import java.io.Serializable;

/**
 * Thrown when an object was not found.
 */
public class ObjectExistsException extends PublicException
{

   private static final long serialVersionUID = 1L;

   private final Serializable identifier;

   /**
    * Creates the exception with the provided error code.
    *
    * @param errorCase the error code.
    */
   public ObjectExistsException(ErrorCase errorCase)
   {
      this(errorCase, null);
   }

   /**
    * Creates the exception with the provided message.
    *
    * @param message the exception message.
    * 
    * @deprecated Use of error codes is strongly recommended.
    */
   public ObjectExistsException(String message)
   {
      this(false, null, message, null);
   }

   /**
    * Creates the exception with the provided error code and the OID of the object.
    * The OID will be appended to the message.
    *
    * @param errorCase the error code.
    * @param oid the OID of the object.
    */
   public ObjectExistsException(ErrorCase errorCase, long oid)
   {
      this(errorCase, new Long(oid));
   }

   /**
    * Creates the exception with the provided message and the OID of the object.
    * The OID will be appended to the message.
    *
    * @param message the root message
    * @param oid the OID of the object.
    * 
    * @deprecated Use of error codes is strongly recommended.
    */
   public ObjectExistsException(String message, long oid)
   {
      this(false, null, message, new Long(oid));
   }

   /**
    * Creates the exception with the provided error code and the identifier of the object.
    * The identifier will be appended to the message.
    *
    * @param errorCase the error code.
    * @param identifier the identifier, such as an ID or an OID.
    */
   public ObjectExistsException(ErrorCase errorCase, Serializable identifier)
   {
      this(false, errorCase, errorCase.toString(), identifier);
   }

   /**
    * Creates the exception with the provided message and the identifier of the object.
    * The identifier will be appended to the message.
    *
    * @param message the root message.
    * @param identifier the identifier, such as an ID or an OID.
    * 
    * @deprecated Use of error codes is strongly recommended.
    */
   public ObjectExistsException(String message, Serializable identifier)
   {
      this(false, null, message, identifier);
   }

   /**
    * Creates the exception with the provided message.
    *
    * @param errorCase the error code.
    * @param identifier the identifier, such as an ID or an OID.
    */
   public ObjectExistsException(boolean s, ErrorCase errorCase, Serializable identifier)
   {
      this(s, errorCase, null, identifier);
   }

   /**
    * Creates the exception with the provided message.
    *
    * @param message the exception message.
    */
   public ObjectExistsException(boolean s, ErrorCase errorCase, String message,
         Serializable identifier)
   {
      super(errorCase, identifier == null ? message : message + ": " + identifier, null);
      this.identifier = identifier;
      this.setLogged(s);
   }

   /**
    * Gets the identifier of the object. It can be the OID, the elementOID or the ID of
    * the object.
    *
    * @return a String in case of an ID, or a Long in case of an OID or elementOID,
    *         identifying the object or <code>null</code> if no identifier was provided.
    */
   public Serializable getIdentifier()
   {
      return identifier;
   }
}