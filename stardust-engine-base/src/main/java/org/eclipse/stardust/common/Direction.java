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
package org.eclipse.stardust.common;

/**
 * The <code>Direction</code> class represents the direction in which a DataMapping is
 * performed or the direction in which an access path is applied to an access point.
 * 
 * Always use "==" to check for equality.
 *
 * @author mgille
 */
public class Direction extends StringKey
{
   private static final long serialVersionUID = 8536627149031620013L;

   /**
    * The <code>IN</code> direction represents a data mapping evaluation from the data
    * to the engine or application. In the case of access points, the <code>IN</code>
    * direction specifies that this access point accepts only set access.
    */
   public static final Direction IN = new Direction("IN", "IN");
   
   /**
    * The <code>OUT</code> direction represents a data mapping evaluation from the engine
    * or application to the data. In the case of access points, the <code>OUT</code>
    * direction specifies that this access point accepts only get access.
    */
   public static final Direction OUT = new Direction("OUT", "OUT");
   
   /**
    * The <code>IN_OUT</code> direction specifies that the access point accepts both
    * set and get access.
    */
   public static final Direction IN_OUT = new Direction("INOUT", "INOUT");
   
   private Direction(String id, String name)
   {
      super(id, name);
   }

   /**
    * Translates the stringified ID into the appropriate key instance.
    *  
    * @param id The stringified ID to be resolved.
    * @return The resolved key, <code>null </code> if no key could be resolved.
    */
   public static Direction getKey(String id)
   {
      return (Direction) getKey(Direction.class, id);
   }

   /**
    * Factory method to get the opposite direction to the specified one.
    *
    * @param direction the direction to which you want to obtain the opposite.
    *
    * @return the opposite direction.
    */
   public static Direction getDualValue(Direction direction)
   {
      if (IN_OUT == direction)
      {
         return IN_OUT;
      }
      else if (IN == direction)
      {
         return OUT;
      }
      else if (OUT == direction)
      {
         return IN;
      }
      throw new AssertionError("Unknown key value");
   }

   /**
    * Gets whether the supplied direction is compatible with this direction.
    *
    * @param direction to compare with.
    *
    * @return true
    */
   public boolean isCompatibleWith(Direction direction)
   {
      return this == direction || this == IN_OUT || direction == IN_OUT;
   }
   
   //TODO this should be the case with comparing these pseudo-enums but 
   //this breaks the contract as laid down by the StringKey equals 
   //which is already published as an API
   /*public boolean equals(Object that) {
      return this == that;      
   }*/
}
