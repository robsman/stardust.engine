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
 *	Constants for unknown state of literals or pseudo literals.
 */
public interface Unknown
{
   public static final char CHAR = Character.MIN_VALUE;
   public static final byte BYTE = Byte.MIN_VALUE;
   public static final short SHORT = Short.MIN_VALUE;
   public static final int INT = Integer.MIN_VALUE;
   public static final long LONG = 0x8000000000000000L;
   public static final float FLOAT = Float.MIN_VALUE;
   public static final double DOUBLE = Double.MIN_VALUE;
   public static final int KEY_VALUE = INT;
}