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
package org.eclipse.stardust.engine.core.extensions.data;

import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.spi.IStructuredDataTransformer;
import org.eclipse.stardust.engine.core.struct.spi.StructDataMappingUtils;
import org.eclipse.stardust.engine.core.struct.spi.StructDataTransformerKey;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataTransformation;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataXPathEvaluator;


/**
 * Adapts the old-style {@link AccessPathEvaluator} to the new interface
 * {@link ExtendedAccessPathEvaluator}
 */
public class AccessPathEvaluatorAdapter implements ExtendedAccessPathEvaluator, Stateless
{

   private AccessPathEvaluator oldStyleEvaluator;

   public boolean isStateless()
   {
      return (oldStyleEvaluator instanceof Stateless)
            && ((Stateless) oldStyleEvaluator).isStateless();
   }

   public AccessPathEvaluatorAdapter(AccessPathEvaluator oldStyleEvaluator)
   {
      this.oldStyleEvaluator = oldStyleEvaluator;
   }

   public Object createDefaultValue(AccessPoint accessPointDefinition, AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return this.oldStyleEvaluator.createDefaultValue(accessPointDefinition.getAllAttributes());
   }

   public Object createInitialValue(AccessPoint accessPointDefinition, AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return this.oldStyleEvaluator.createInitialValue(accessPointDefinition.getAllAttributes());
   }

   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance,
         String outPath, AccessPathEvaluationContext accessPathEvaluationContext)
   {
      boolean vizRulesApplication = StructDataMappingUtils
            .isVizRulesApplication(accessPathEvaluationContext.getActivity());
      AccessPoint targetAccessPointDefinition = accessPathEvaluationContext
            .getTargetAccessPointDefinition();
      if (null != targetAccessPointDefinition
            && PredefinedConstants.STRUCTURED_DATA.equals(targetAccessPointDefinition
                  .getType().getId()))
      {
         final IXPathMap xPathMap = DataXPathMap.getXPathMap(targetAccessPointDefinition);
         boolean needsToBeJaxbTransformed = vizRulesApplication
               && null != targetAccessPointDefinition
               && StructuredDataXPathUtils.returnsSingleComplex(
                     accessPathEvaluationContext.getTargetPath(), xPathMap);
         if (needsToBeJaxbTransformed)
         {
            targetAccessPointDefinition.setAttribute(
                  StructuredDataConstants.TRANSFORMATION_ATT,
                  StructDataTransformerKey.BEAN);

            StructuredDataTransformation transformation = StructuredDataTransformation
                  .valueOf(outPath, targetAccessPointDefinition);

            IStructuredDataTransformer transformator = StructuredDataXPathEvaluator.getTransformator(transformation);
            Object result = transformator.toStructData(accessPointDefinition,
                  accessPointInstance, outPath, accessPathEvaluationContext);

            return result;
         }
         else
         {
            return this.oldStyleEvaluator.evaluate(accessPointDefinition
                  .getAllAttributes(), accessPointInstance, outPath);
         }
      }
      else
      {
         return this.oldStyleEvaluator.evaluate(accessPointDefinition.getAllAttributes(),
               accessPointInstance, outPath);
      }
   }

   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance, String inPath,
         AccessPathEvaluationContext accessPathEvaluationContext, Object value)
   {
      return this.oldStyleEvaluator.evaluate(accessPointDefinition.getAllAttributes(), accessPointInstance, inPath, value);
   }
}
