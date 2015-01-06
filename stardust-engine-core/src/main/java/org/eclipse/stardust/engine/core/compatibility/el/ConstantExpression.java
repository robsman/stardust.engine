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
package org.eclipse.stardust.engine.core.compatibility.el;

import org.eclipse.stardust.common.log.Logger;

/**
 * @author fherinean
 * @version $Revision$
 */
public class ConstantExpression implements ValueExpression
{
   private Object value;

   public ConstantExpression(Object value)
   {
      this.value = value;
   }

   public void debug(Logger ps, String indent)
   {
      ps.debug(indent + value);
   }

   public Object evaluate(SymbolTable symbolTable) throws EvaluationError
   {
      return value;
   }

   public Object getValue()
   {
      return value;
   }
}
