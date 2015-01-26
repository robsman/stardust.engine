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
public class LineKey extends Key
{
    public static final LineKey UNKNOWN = new LineKey(-1);
    public static final LineKey NORMAL = new LineKey(0);
    public static final LineKey SHORT_STROKES = new LineKey(1);
    public static final LineKey LONG_STROKES = new LineKey(2);

    static String[] keyList = {"normal line"
                               , "short strokes"
                               , "long strokes"};

    /** */
    public LineKey()
    {
        super();
    }

    /** */
    public LineKey(int value)
    {
        super(value);
    }

    /**
     * Creates an key instance from its string representation.
     *
     * @param keyRepresentation java.lang.String
     */
    public LineKey(String keyRepresentation)
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
