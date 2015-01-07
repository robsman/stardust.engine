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

import java.util.Comparator;

/** */
public class SymbolComparator implements Comparator
{
    public static final int UNKNOWN_TYPE = -1;
    public static final int COMPARE_X = 0;
    public static final int COMPARE_Y = 1;
    public static final int COMPARE_HEIGHT = 2;
    public static final int COMPARE_WIDTH = 3;
    public static final int COMPARE_RIGHT_SIDE = 4;
    public static final int COMPARE_BOTTOM_SIDE = 5;

    protected static final String ERROR_UNKNOWN_TYPE = "Illegal comparator type id.";
    protected static final String ERROR_ARGUMENT_NULL = "Illegal nullpointer as argument.";

    private int comparatorType = UNKNOWN_TYPE;

    /** */
    private SymbolComparator()
    {
    }

    /** */
    protected SymbolComparator(int comparatorType)
    {
        this.comparatorType = comparatorType;
    }

    /** */
    protected int compareValues(int value1, int value2)
    {
        if (value1 == value2)
        {
            return 0;
        }
        else if (value1 < value2)
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }

    /** */
    static public Comparator createComparatorForXPosition()
    {
        return new SymbolComparator(COMPARE_X);
    }

    /** */
    static public Comparator createComparatorForYPosition()
    {
        return new SymbolComparator(COMPARE_Y);
    }

    /** */
    static public Comparator createComparatorForHeight()
    {
        return new SymbolComparator(COMPARE_HEIGHT);
    }

    /** */
    static public Comparator createComparatorForWidth()
    {
        return new SymbolComparator(COMPARE_WIDTH);
    }

    /** */
    static public Comparator createComparatorForRightSide()
    {
        return new SymbolComparator(COMPARE_RIGHT_SIDE);
    }

    /** */
    static public Comparator createComparatorForBottomSide()
    {
        return new SymbolComparator(COMPARE_BOTTOM_SIDE);
    }

    /** */
    protected int getComparatorType()
    {
        return this.comparatorType;
    }

    /** */
    protected void setComparatorType(int comperatorType)
    {
        switch (comperatorType)
        {
            case COMPARE_HEIGHT:
                {
                    this.comparatorType = COMPARE_HEIGHT;
                    break;
                }
            case COMPARE_WIDTH:
                {
                    this.comparatorType = COMPARE_WIDTH;
                    break;
                }
            case COMPARE_X:
                {
                    this.comparatorType = COMPARE_X;
                    break;
                }
            case COMPARE_Y:
                {
                    this.comparatorType = COMPARE_Y;
                    break;
                }
            case COMPARE_RIGHT_SIDE:
                {
                    this.comparatorType = COMPARE_RIGHT_SIDE;
                    break;
                }
            case COMPARE_BOTTOM_SIDE:
                {
                    this.comparatorType = COMPARE_BOTTOM_SIDE;
                    break;
                }
            default:
                throw new IllegalArgumentException(ERROR_UNKNOWN_TYPE);
        }

    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     *
     * The implementor must ensure that <tt>sgn(compare(x, y)) ==
     * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>compare(x, y)</tt> must throw an exception if and only
     * if <tt>compare(y, x)</tt> throws an exception.)<p>
     *
     * The implementor must also ensure that the relation is transitive:
     * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
     * <tt>compare(x, z)&gt;0</tt>.<p>
     *
     * Finally, the implementer must ensure that <tt>compare(x, y)==0</tt>
     * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
     * <tt>z</tt>.<p>
     *
     * It is generally the case, but <i>not</i> strictly required that
     * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
     * any comparator that violates this condition should clearly indicate
     * this fact.  The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."
     *
     * @return a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second.
     * @throws ClassCastException if the arguments' types prevent them from
     * 	       being compared by this Comparator.
     */
    public int compare(Object o1, Object o2)
    {
        if (o1 != null && o2 != null)
        {
            NodeSymbol _symbol1 = (NodeSymbol) o1;
            NodeSymbol _symbol2 = (NodeSymbol) o2;

            switch (comparatorType)
            {
                case COMPARE_BOTTOM_SIDE:
                    return compareValues(_symbol1.getBottom(), _symbol2.getBottom());
                case COMPARE_HEIGHT:
                    return compareValues(_symbol1.getHeight(), _symbol2.getHeight());
                case COMPARE_RIGHT_SIDE:
                    return compareValues(_symbol1.getRight(), _symbol2.getRight());
                case COMPARE_WIDTH:
                    return compareValues(_symbol1.getWidth(), _symbol2.getWidth());
                case COMPARE_X:
                    return compareValues(_symbol1.getX(), _symbol2.getX());
                case COMPARE_Y:
                    return compareValues(_symbol1.getY(), _symbol2.getY());

                default:
                    throw new IllegalStateException(ERROR_UNKNOWN_TYPE);
            }
        }
        else
        {
            throw new IllegalArgumentException(ERROR_ARGUMENT_NULL);
        }
    }

    /**
     * Indicates whether some other object is &quot;equal to&quot; this
     * Comparator.  This method must obey the general contract of
     * <tt>Object.equals(Object)</tt>.  Additionally, this method can return
     * <tt>true</tt> <i>only</i> if the specified Object is also a comparator
     * and it imposes the same ordering as this comparator.  Thus,
     * <code>comp1.equals(comp2)</code> implies that <tt>sgn(comp1.compare(o1,
     * o2))==sgn(comp2.compare(o1, o2))</tt> for every object reference
     * <tt>o1</tt> and <tt>o2</tt>.<p>
     *
     * Note that it is <i>always</i> safe <i>not</i> to override
     * <tt>Object.equals(Object)</tt>.  However, overriding this method may,
     * in some cases, improve performance by allowing programs to determine
     * that two distinct Comparators impose the same order.
     *
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> only if the specified object is also
     *		a comparator and it imposes the same ordering as this
     *		comparator.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see java.lang.Object#hashCode()
     */
    public boolean equals(Object obj)
    {
        return super.equals(obj)
                && (obj instanceof SymbolComparator)
                && (obj != null)
                && (this.comparatorType == ((SymbolComparator) obj).getComparatorType());
    }

}
