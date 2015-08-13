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
package org.eclipse.stardust.engine.core.spi.extensions.model;

import java.util.List;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.ILoopCharacteristics;
import org.eclipse.stardust.engine.api.model.IMultiInstanceLoopCharacteristics;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;


/**
 * Provides an abstraction of a dereferenced {@link AccessPoint}. Contains information
 * about the data flow direction supported by this bridge and the type of the data.
 * <p />
 * Will be used for static type checking.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class BridgeObject
{
   private final Class endClass;
   private final Direction direction;

   /**
    * Creates a bridge object representing the dereferenced {@link AccessPoint}.
    *
    * @param ap The access point to be dereferenced.
    * @param path The path expression to be used for dereferencing. Path analysis may hint
    *             on the data flow direction of the resulting bridge.
    * @param direction The data flow direction, either {@link Direction#In}if a LHS bridge
    *             is requested or {@link Direction#Out}if a RHS bridge is requested.
    *
    * @return The bridge representing the dereference result.
    */
   public static BridgeObject getBridge(AccessPoint ap, String path, Direction direction,
         AccessPathEvaluationContext context)
   {
      String validatorClass = ap.getType().getStringAttribute(
            PredefinedConstants.VALIDATOR_CLASS_ATT);
      if (!StringUtils.isEmpty(validatorClass))
      {
         ExtendedDataValidator validator = SpiUtils.createExtendedDataValidator(validatorClass);
         return validator.getBridgeObject(ap, path, direction, context);
      }
      else
      {
         return new BridgeObject(Object.class, direction);
      }
   }

   public BridgeObject(Class endClass, Direction direction)
   {
      this.endClass = endClass;
      this.direction = direction;
   }

   /**
    * Retrieves the type of data supported by this bridge object. Will be {@link Object}
    * if no static type information is available.
    *
    * @return The Java type of the data supported by this bridge.
    */
   public Class getEndClass()
   {
      return endClass;
   }

   /**
    * Retrieves the data flow direction of the represented data. May be
    * {@link Direction#In} for a data sink or {@link Direction#Out} for a data source.
    *
    * @return The data flow direction of this bridge.
    */
   public Direction getDirection()
   {
      return direction;
   }

   /**
    * Performs a static check if this bridge is valid as a data sink for the data source
    * represented by <code>rhs</code>.
    * <p />
    * Basic validity requires compatible data flow directions and type compatibility (if
    * available).
    *
    * @param rhs The data source to check compatibility against.
    *
    * @return <code>true</code> if this bridge may accept assignments from the given data
    *         source, <code>false</code> if not.
    */
   public boolean acceptAssignmentFrom(BridgeObject rhs)
   {
      // direction must be in or inout or null
      if (getDirection() == Direction.OUT)
      {
         return false;
      }
      // rhs direction must be out, inout or null
      if (rhs.getDirection() == Direction.IN)
      {
         return false;
      }
      // @todo (france, fh): check if it's correctly working with primitive types
      return Reflect.isAssignable(getEndClass(), rhs.getEndClass());
   }

   /**
    * Performs a static check if the two given {@link AccessPoint} dereferences are
    * compatible regarding possible data flow from one to the other.
    *
    * @param direction The data flow direction.
    * @param lhsPoint The left access point.
    * @param lhsPath The left access point's dereference.
    * @param rhsPoint The right access point.
    * @param rhsPath The right access point's dereference.
    *
    * @return <code>true</code> if the two dereferences are compatible, <code>false</code>
    *         if not.
    *
    * @see #acceptAssignmentFrom(org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject)
    */
   public static boolean isValidMapping(String context, Direction direction, AccessPoint lhsPoint,
         String lhsPath, AccessPoint rhsPoint, String rhsPath, IActivity activity)
   {
      return isValidMapping(context, direction, lhsPoint.getId(), lhsPoint, lhsPath, rhsPoint, rhsPath, activity);
   }

   public static boolean isValidMapping(String context, Direction direction, String accessPointId,
         AccessPoint lhsPoint, String lhsPath, AccessPoint rhsPoint, String rhsPath, IActivity activity)
   {
      BridgeObject leftBridge;
      try
      {
         AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(null,
               rhsPoint, rhsPath, activity);
         leftBridge = getBridge(lhsPoint, lhsPath, Direction.IN.equals(direction)
               ? Direction.IN
               : Direction.OUT, evaluationContext);
      }
      catch (Exception e)
      {
         return false;
      }

      BridgeObject rightBridge;
      try
      {
         AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(null,
               lhsPoint, lhsPath, activity);
         rightBridge = getBridge(rhsPoint, rhsPath, Direction.OUT.equals(direction)
               ? Direction.IN
               : Direction.OUT, evaluationContext);
      }
      catch (Exception e)
      {
         return false;
      }

      if (activity != null)
      {
         ILoopCharacteristics loop = activity.getLoopCharacteristics();
         if (loop instanceof IMultiInstanceLoopCharacteristics)
         {
            String paramId = Direction.OUT.equals(direction)
               ? ((IMultiInstanceLoopCharacteristics) loop).getOutputParameterId()
               : ((IMultiInstanceLoopCharacteristics) loop).getInputParameterId();
            Class<?> endClass = rightBridge.getEndClass();
            if (paramId != null && paramId.equals(context + ':' + accessPointId) && (List.class.isAssignableFrom(endClass) || endClass.isArray()))
            {
               return true;
            }
         }
      }
      
      // validation will be skipped if a generic type is involved
      if (leftBridge == null || rightBridge == null)
      {
         return true;
      }

      return Direction.IN.equals(direction)
            ? leftBridge.acceptAssignmentFrom(rightBridge)
            : rightBridge.acceptAssignmentFrom(leftBridge);
   }
}
