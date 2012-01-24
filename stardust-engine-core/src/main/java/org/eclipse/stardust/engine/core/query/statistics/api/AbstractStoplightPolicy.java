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
package org.eclipse.stardust.engine.core.query.statistics.api;

import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.engine.api.query.EvaluationPolicy;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AbstractStoplightPolicy implements EvaluationPolicy
{
   public static final Status GREEN = new Status("green");

   public static final Status YELLOW = new Status("yellow");

   public static final Status RED = new Status("red");

   protected final float yellowPct;

   protected final float redPct;

   public AbstractStoplightPolicy(float yellowPct, float redPct)
   {
      this.yellowPct = yellowPct;
      this.redPct = redPct;
   }

   public static final class Status extends StringKey
   {
      static final long serialVersionUID = 5685998020704619981L;

      public Status(String key)
      {
         super(key, key);
      }
   }
}
