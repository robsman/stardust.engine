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
package org.eclipse.stardust.engine.core.model.beans;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.compatibility.el.BooleanExpression;
import org.eclipse.stardust.engine.core.compatibility.el.EvaluationError;
import org.eclipse.stardust.engine.core.compatibility.el.Interpreter;
import org.eclipse.stardust.engine.core.compatibility.el.Result;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.compatibility.el.SyntaxError;
import org.eclipse.stardust.engine.core.javascript.TransitionConditionEvaluator;
import org.eclipse.stardust.engine.core.model.utils.ConnectionBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailTransitionBean;



public class TransitionBean extends ConnectionBean implements ITransition
{
   private static final Logger trace = LogManager.getLogger(TransitionBean.class);

   public static final String ON_BOUNDARY_EVENT_PREDICATE = "ON_BOUNDARY_EVENT";

   /* package-private */ static final Pattern ON_BOUNDARY_EVENT_CONDITION = Pattern.compile(ON_BOUNDARY_EVENT_PREDICATE + "\\(.+\\)");

   private static final String PARSED_EL_EXPRESSION = "ParsedElExpression";

   private static final String ID_ATT = "Id";
   private String id;

   private static final String NAME_ATT = "Name";
   private String name;

   private static final String FORK_ON_TRAVERSAL_ATT = "Fork on traversal";
   private boolean forkOnTraversal;

   private static final String CONDITION_ATT = "Condition";
   private String condition = "true";

   TransitionBean()
   {
   }

   public TransitionBean(String id, String name, String description,
         IActivity fromActivity, IActivity toActivity)
   {
      super(fromActivity, toActivity);
      this.id = id;
      this.name = name;
      setDescription(description);
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      if (!getId().equals(id))
      {
         if (getProcessDefinition().findTransition(id) != null)
         {
            throw new PublicException(
                  BpmRuntimeError.MDL_TRANSITION_WITH_ID_ALREADY_EXISTS.raise(id));
         }

         markModified();

         this.id = id;
      }
   }

   public String getUniqueId()
   {
      return getClass().getName() + ":" + getId();
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      markModified();

      this.name = name;
   }

   public String getCondition()
   {
      return condition;
   }

   public boolean getForkOnTraversal()
   {
      return forkOnTraversal;
   }

   public void setForkOnTraversal(boolean forkOnTraversal)
   {
      this.forkOnTraversal = forkOnTraversal;
   }

   public void setCondition(String condition)
   {
      this.condition = condition;
   }

   public IProcessDefinition getProcessDefinition()
   {
      return (IProcessDefinition) parent;
   }

   public IActivity getFromActivity()
   {
      return (IActivity) getFirst();
   }

   public IActivity getToActivity()
   {
      return (IActivity) getSecond();
   }

   /**
    * Evaluates the transition condition against the data instances of a
    * process instance
    */
   public boolean isEnabled(SymbolTable symbolTable)
   {
      IModel model = (IModel) getModel();
      String type = model.getScripting().getType();
      String condition = getCondition();
      if ("text/ecmascript".equals(type) || "text/javascript".equals(type))
      {
         if (condition.startsWith(PredefinedConstants.CARNOT_EL_PREFIX))
         {
            // fallback to carnotEL evaluation.
            condition = condition.substring(PredefinedConstants.CARNOT_EL_PREFIX.length());
         }
         else if (XMLConstants.CONDITION_OTHERWISE_VALUE.equals(condition))
         {
            return false;
         }
         else
         {
            // pure ecmascript
            return TransitionConditionEvaluator.isEnabled(this, symbolTable);
         }
      }
      // unsupported scripting languages will throw an exception here.
      try
      {
         if ("true".equals(condition))
         {
            // immediately return "true" (for transition conditions containing "true")
            return true;
         }
         BooleanExpression expression = (BooleanExpression) getRuntimeAttribute(PARSED_EL_EXPRESSION);
         if (null == expression)
         {
            // for compatibility, correct the new default "true" to the old default "TRUE"
            if ("true".equals(condition))
            {
               condition = "TRUE";
            }
            expression = Interpreter.parse(condition);
            setRuntimeAttribute(PARSED_EL_EXPRESSION, expression);
         }
         return Result.TRUE.equals(Interpreter.evaluate(expression, symbolTable));
      }
      catch (SyntaxError x)
      {
         throw new InternalException(x);
      }
      catch (EvaluationError x)
      {
         throw new InternalException(x);
      }
   }

   public boolean isOtherwiseEnabled(SymbolTable symbolTable)
   {
      String elType = ((IModel) getModel()).getScripting().getType();
      if ("text/ecmascript".equals(elType) || "text/javascript".equals(elType))
      {
         return XMLConstants.CONDITION_OTHERWISE_VALUE.equals(getCondition());
      }
      else
      {
         try
         {
            BooleanExpression expression = (BooleanExpression) getRuntimeAttribute(PARSED_EL_EXPRESSION);
            if (null == expression)
            {
               expression = Interpreter.parse(getCondition());
               setRuntimeAttribute(PARSED_EL_EXPRESSION, expression);
            }
            return Result.OTHERWISE.equals(Interpreter.evaluate(expression, symbolTable));
         }
         catch (SyntaxError x)
         {
            //throw new InternalException(x);
            return false;
         }
         catch (EvaluationError x)
         {
            throw new InternalException(x);
         }
      }
   }

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies of the transition.
    */
   public void checkConsistency(List inconsistencies)
   {
      if (null == getId())
      {
      }
      else
      {
         checkForVariables(inconsistencies, id, "ID");

         // check id to fit in maximum length
         if (getId().length() > AuditTrailTransitionBean.getMaxIdLength())
         {
            BpmValidationError error = BpmValidationError.TRAN_ID_EXCEEDS_MAXIMUM_LENGTH.raise(
                  getName(), AuditTrailTransitionBean.getMaxIdLength());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }
      }

      IModel model = (IModel) getModel();
      String type = model.getScripting().getType();
      if (ON_BOUNDARY_EVENT_CONDITION.matcher(getCondition()).matches())
      {
         final String eventHandlerId = getCondition().substring(ON_BOUNDARY_EVENT_PREDICATE.length() + 1, getCondition().length() - 1);
         final IEventHandler eventHandler = getFromActivity().findHandlerById(eventHandlerId);
         if (eventHandler == null)
         {
            BpmValidationError error = BpmValidationError.TRAN_NO_BOUNDARY_EVENT_HANDLER_WITH_ID_FOR_EXCEPTION_TRANSITION_FOUND.raise(
                  eventHandlerId, getId());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
         }
      }
      else if ("text/carnotEL".equals(type))
      {
         validateELScript(inconsistencies);
      }
      else if ("text/ecmascript".equals(type) || "text/javascript".equals(type))
      {
         if (getCondition().startsWith(PredefinedConstants.CARNOT_EL_PREFIX))
         {
            BpmValidationError error = BpmValidationError.TRAN_EXPRESSION_SCRIPTING_LANGUAGE_DO_NOT_MATCH.raise(type);
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
         }
         else
         {
            validateECMAScript(inconsistencies);
         }
      }
      else
      {
         BpmValidationError error = BpmValidationError.TRAN_UNSUPPORTED_SCRIPTING_LANGUAGE.raise(type);
         inconsistencies.add(new Inconsistency(error, this, Inconsistency.WARNING));
      }
   }

   private void validateECMAScript(List inconsistencies)
   {
      TransitionConditionEvaluator.checkConsistency(this, inconsistencies);
   }

   private void validateELScript(List inconsistencies)
   {
      try
      {
         Interpreter.evaluate(getCondition(),
               ((IModel) getProcessDefinition().getModel()));
      }
      catch (SyntaxError e)
      {
         inconsistencies.add(new Inconsistency(
               "Syntactically invalid transition condition: " + e.getMessage(),
               this, Inconsistency.WARNING));
      }
      catch (EvaluationError e)
      {
         inconsistencies.add(new Inconsistency(
               "Semantically invalid transition condition: " + e.getMessage(),
               this, Inconsistency.WARNING));
      }
   }

   public String toString()
   {
      return "Transition: " + id;
   }
}
