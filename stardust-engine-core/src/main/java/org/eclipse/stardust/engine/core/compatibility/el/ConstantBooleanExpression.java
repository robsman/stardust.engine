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
 * @author sauer
 * @version $Revision$
 */
public class ConstantBooleanExpression implements BooleanExpression
{

   public static final ConstantBooleanExpression TRUE = new ConstantBooleanExpression(
         Result.TRUE);

   public static final ConstantBooleanExpression FALSE = new ConstantBooleanExpression(
         Result.FALSE);

   public static final ConstantBooleanExpression OTHERWISE = new ConstantBooleanExpression(
         Result.OTHERWISE);

   private final Result result;

   public ConstantBooleanExpression(Result result)
   {
      this.result = result;
   }

   public Result getResult()
   {
      return result;
   }

   public Result evaluate(SymbolTable symbolTable) throws EvaluationError
   {
      return result;
   }

   public void debug(Logger logger, String indent)
   {
      logger.debug(indent + result.toString());
   }
}
