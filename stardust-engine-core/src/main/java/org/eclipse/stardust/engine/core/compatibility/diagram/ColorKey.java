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
package org.eclipse.stardust.engine.core.compatibility.diagram;

import org.eclipse.stardust.common.Key;

/** */
public class ColorKey extends Key
{
    public static final ColorKey UNKNOWN = new ColorKey(-1);
    public static final ColorKey BLACK = new ColorKey(0);
    public static final ColorKey DARK_BLUE = new ColorKey(1);
    public static final ColorKey DARK_GRAY = new ColorKey(2);
    public static final ColorKey BLUE = new ColorKey(3);
    public static final ColorKey LIGTH_GRAY = new ColorKey(4);
    public static final ColorKey RED = new ColorKey(5);
    public static final ColorKey YELLOW = new ColorKey(6);

    static String[] keyList = {"black"
                               , "dark blue"
                               , "dark gray"
                               , "blue"
                               , "ligth gray"
                               , "red"
                               , "yellow"};

    /** */
    public ColorKey()
    {
        super();
    }

    /** */
    public ColorKey(int value)
    {
        super(value);
    }

    /**
     * Creates an key instance from its string representation.
     *
     * @param keyRepresentation java.lang.String
     */
    public ColorKey(String keyRepresentation)
    {
        this(getValue(keyRepresentation, getKeyList()));
    }

    /** */
    public static String[] getKeyList()
    {
        return keyList;
    }

    /** */
    public String getString()
    {
        if (value < 0)
        {
            return UNKNOWN_STRING;
        }

        return keyList[value];
    }
}
