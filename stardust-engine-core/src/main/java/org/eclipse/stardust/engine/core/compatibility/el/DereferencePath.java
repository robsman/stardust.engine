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

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;


public class DereferencePath implements ValueExpression
{
   public static final Logger trace = LogManager.getLogger(DereferencePath.class);

   private static final String DOT = ".";
   private static final String NULL_REFERENCE = "null";

   private String baseReference;
   private String accessPath;

   public DereferencePath(String baseReference)
   {
      this.baseReference = baseReference == null ? NULL_REFERENCE : baseReference;
   }

   public DereferencePath(String baseReference, String accessPath)
   {
      this.baseReference = baseReference == null ? NULL_REFERENCE : baseReference;
      this.accessPath = accessPath;
   }

   public String toString()
   {
      return StringUtils.join(baseReference, accessPath, DOT);
   }

   public void debug(Logger ps, String indent)
   {
      ps.debug(indent + toString());
   }

   public Object evaluate(SymbolTable symbolTable) throws EvaluationError
   {
      try
      {
         // check for NULL pointer constant
         if (NULL_REFERENCE.equalsIgnoreCase(baseReference))
         {
            return null;
         }

         AccessPoint data = symbolTable.lookupSymbolType(baseReference);

         Object value = symbolTable.lookupSymbol(baseReference);

         if (null != value && null != data && null != data.getType())
         {
            ExtendedAccessPathEvaluator evaluator = SpiUtils.createExtendedAccessPathEvaluator(data, accessPath);
            
            AccessPathEvaluationContext evaluationContext;
            // try to pass process instance in the context
            evaluationContext = new AccessPathEvaluationContext(symbolTable, null);
            return evaluator.evaluate(data, value, accessPath, evaluationContext);
         }

         return null;
      }
      catch (Exception x)
      {
         trace.warn("", x);
         throw new EvaluationError(x.getMessage());
      }
   }

   public String getBaseReference()
   {
      return NULL_REFERENCE.equalsIgnoreCase(baseReference) ? null : baseReference;
   }

   public String getAccessPath()
   {
      return accessPath;
   }
}
