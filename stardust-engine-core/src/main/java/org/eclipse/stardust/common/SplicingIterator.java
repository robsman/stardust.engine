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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iterator which glues two Iterators together in a chain.
 *
 * @author  ubirkemeyer
 * @version $Revision$
 */
public class SplicingIterator<E> implements Iterator<E>
{
    private boolean turnAround = false;
    private Iterator<? extends E> i1;
    private Iterator<? extends E> i2;

    /**
     * @param i1 The first Iterator or null.
     * @param i2 The second Iterator or null.
     */
    public SplicingIterator(Iterator<? extends E> i1, Iterator<? extends E> i2)
    {
        this.i1 = i1;
        this.i2 = i2;
    }

    public boolean hasNext()
    {
        if (!turnAround)
        {
            if (i1 != null && i1.hasNext())
            {
                return true;
            }
            turnAround = true;
        }
        return i2 != null && i2.hasNext();
    }

    public E next()
    {
        if (!turnAround)
        {
            if (i1 != null && i1.hasNext())
            {
                return i1.next();
            }
            turnAround = true;
        }
        if (i2 == null)
        {
            throw new NoSuchElementException();
        }
        return i2.next();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
