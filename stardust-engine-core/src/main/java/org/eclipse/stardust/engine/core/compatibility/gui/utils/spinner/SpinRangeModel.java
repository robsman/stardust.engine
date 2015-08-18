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
package org.eclipse.stardust.engine.core.compatibility.gui.utils.spinner;

import javax.swing.event.ChangeListener;

/**
 * @author rsauer
 * @version $Revision$
 */
public interface SpinRangeModel
{
   public double getValue();

   public double getExtent();

   public double getMinimum();

   public double getMaximum();

   public boolean getWrap();

   public void setValue(double value);

   public void setExtent(double extent);

   public void setMinimum(double min);

   public void setMaximum(double max);

   public void setWrap(boolean wrap);

   public void setValueIsAdjusting(boolean adusting);

   public boolean getValueIsAdjusting();

   public void addChangeListener(ChangeListener listener);

   public void removeChangeListener(ChangeListener listener);
}