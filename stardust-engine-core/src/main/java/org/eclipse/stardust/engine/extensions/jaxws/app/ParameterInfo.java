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
package org.eclipse.stardust.engine.extensions.jaxws.app;

import java.util.Set;

import javax.xml.namespace.QName;

/**
 * @author fherinean
 * @version $Revision$
 */
public class ParameterInfo
{
   /** Field IN */
   public static final int IN = 1;

   /** Field OUT */
   public static final int OUT = 2;

   /** Field INOUT */
   public static final int INOUT = 3;

   private static final String[] directionNames = {"Unset", "IN", "OUT", "INOUT"};

   private String name;
   private QName qName;
   private QName type;
   private int direction;
   private boolean inHeader;
   private boolean outHeader;
   private Set xmlTypes;
   private Set defaults;

   public ParameterInfo(String name, QName qName, QName type, int direction,
                        boolean inHeader, boolean outHeader, Set xmlTypes, Set defaults)
   {
      this.name = name;
      this.qName = qName;
      this.type = type;
      this.direction = direction;
      this.inHeader = inHeader;
      this.outHeader = outHeader;
      this.xmlTypes = xmlTypes;
      this.defaults = defaults;
   }

   public String getName()
   {
      return name;
   }

   public QName getQName()
   {
      return qName;
   }

   public QName getType()
   {
      return type;
   }

   public int getDirection()
   {
      return direction;
   }

   public boolean isInHeader()
   {
      return inHeader;
   }

   public boolean isOutHeader()
   {
      return outHeader;
   }

   public boolean isInput()
   {
      return direction == IN || direction == INOUT;
   }

   public boolean isOutput()
   {
      return direction == OUT || direction == INOUT;
   }

   public Set getXmlTypes()
   {
      return xmlTypes;
   }

   public Set getDefaults()
   {
      return defaults;
   }

   public String getDirectionAsString()
   {
      return directionNames[direction];
   }

   public static int getDirectionFromString(String direction)
   {
      for (int i = 1; i < directionNames.length; i++)
      {
         if (directionNames[i].equals(direction))
         {
            return i;
         }
      }
      // not found
      return 0;
   }
}
