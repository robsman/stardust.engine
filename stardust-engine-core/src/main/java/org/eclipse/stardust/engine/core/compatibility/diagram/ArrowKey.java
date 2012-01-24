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
public class ArrowKey extends Key
{
    public static final ArrowKey UNKNOWN = new ArrowKey(-1);
    public static final ArrowKey NO_ARROW = new ArrowKey(0);
    public static final ArrowKey OPEN_TRIANGLE = new ArrowKey(1);
    public static final ArrowKey EMPTY_TRIANGLE = new ArrowKey(2);
    public static final ArrowKey FILLED_TRIANGLE = new ArrowKey(3);
    public static final ArrowKey EMPTY_RHOMBUS = new ArrowKey(4);
    public static final ArrowKey FILLED_RHOMBUS = new ArrowKey(5);

    static String[] keyList = {"without Symbol"
                               , "open Triangle"
                               , "empty Triangle"
                               , "filled Triangle"
                               , "empty Rhombus"
                               , "filled Rhombus"};

    /** */
    public ArrowKey()
    {
        super();
    }

    /** */
    public ArrowKey(int value)
    {
        super(value);
    }

    /**
     * Creates an key instance from its string representation.
     *
     * @param keyRepresentation java.lang.String
     */
    public ArrowKey(String keyRepresentation)
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
