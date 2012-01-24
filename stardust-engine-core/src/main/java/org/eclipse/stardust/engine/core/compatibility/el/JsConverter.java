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

import java.util.StringTokenizer;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;


public class JsConverter
{
   private DataTypeResolver resolver;
   
   private boolean successfull;
   private Exception failReason;
   
   public JsConverter(DataTypeResolver resolver)
   {
      this.resolver = resolver;
   }

   public String convert(String source)
   {
      successfull = true;
      failReason = null;
      try
      {
         BooleanExpression expr = Interpreter.parse(source);
         return convert(expr);
      }
      catch (Exception ex)
      {
         successfull = false;
         failReason = ex;
         return PredefinedConstants.CARNOT_EL_PREFIX + source;
      }
   }

   public boolean isSuccessfull()
   {
      return successfull;
   }

   public Exception getFailReason()
   {
      return failReason;
   }

   private String convert(BooleanExpression expr)
   {
      String result = null;
      if (expr instanceof ConstantBooleanExpression)
      {
         result = toJS((ConstantBooleanExpression) expr);
      }
      else if (expr instanceof ComparisonOperation)
      {
         result = toJS((ComparisonOperation) expr);
      }
      else if (expr instanceof CombineOperation)
      {
         result = toJS((CombineOperation) expr);
      }
      // else throw exception
      return result;
   }

   private String toJS(CombineOperation expr)
   {
      int combineOperation = expr.getOperation();
      if (combineOperation == CombineOperation.NOT)
      {
         return toJSCombineOperation(expr.getOperation()) + "(" + convert(expr.getLhsExpression()) + ")";
      }
      else
      {
         return "(" + convert(expr.getLhsExpression()) + ")" + toJSCombineOperation(expr.getOperation()) + "(" + convert(expr.getRhsExpression()) + ")";
      }
   }

   private String toJSCombineOperation(int operation)
   {
      switch (operation)
      {
      case CombineOperation.AND: return " && ";
      case CombineOperation.EQUAL: return " == ";
      case CombineOperation.NOT: return " !";
      case CombineOperation.NOT_EQUAL: return " != ";
      case CombineOperation.OR: return " || ";
      // default: throw exception
      }
      return "";
   }

   private String toJS(ComparisonOperation expr)
   {
      return convert(expr.getLhsValue()) + toJSComparisonOperation(expr.getOperation()) + convert(expr.getRhsValue());
   }

   private String convert(ValueExpression expr)
   {
      String result = null;
      if (expr instanceof ConstantExpression)
      {
         result = toJS((ConstantExpression) expr);
      }
      else if (expr instanceof DereferencePath)
      {
         result = toJS((DereferencePath) expr);
      }
      // else throw exception
      return result;
   }

   private String toJS(DereferencePath expr)
   {
      String id = expr.getBaseReference();
      if (id == null)
      {
         return "null";
      }
      String base = resolver.resolveDataType(id);
      if (PredefinedConstants.PRIMITIVE_DATA.equals(base)
       || PredefinedConstants.SERIALIZABLE_DATA.equals(base)
       || PredefinedConstants.ENTITY_BEAN_DATA.equals(base)
       || PredefinedConstants.HIBERNATE_DATA.equals(base)
       || PredefinedConstants.STRUCTURED_DATA.equals(base))
      {
         String expression = expr.toString();         
         return expression.replaceAll("/", ".");
      }
      if (PredefinedConstants.PLAIN_XML_DATA.equals(base))
      {
         String accessPath = expr.getAccessPath();
         if (accessPath != null)
         {
            accessPath = accessPath.trim();
            if (accessPath.length() == 0)
            {
               accessPath = null;
            }
            else
            {
               StringTokenizer tokenizer = new StringTokenizer(accessPath, "\"", true);
               StringBuffer buffer = new StringBuffer();
               while (tokenizer.hasMoreTokens())
               {
                  if (buffer.length() > 0)
                  {
                     buffer.append(" + ");
                  }
                  String token = tokenizer.nextToken();
                  char sep = "\"".equals(token) ? '\'' : '"';
                  buffer.append(sep);
                  buffer.append(token);
                  buffer.append(sep);
               }
               accessPath = buffer.toString();
            }
         }
         return accessPath == null ? id : id + "[" + accessPath + "]";
      }
      throw new IllegalArgumentException("Unsupported data type: " + base);
   }

   private String toJS(ConstantExpression expr)
   {
      Object result = expr.getValue();
      if (result instanceof Character)
      {
         return "'" + result + "'";
      }
      else if (result instanceof String)
      {
         return "\"" + result + "\"";
      }
      // if null throw exception
      return result.toString();
   }

   private String toJSComparisonOperation(int operation)
   {
      switch (operation)
      {
      case ComparisonOperation.EQUAL: return " == ";
      case ComparisonOperation.NOT_EQUAL: return " != ";
      case ComparisonOperation.GREATER: return " > ";
      case ComparisonOperation.GREATER_EQUAL: return " >= ";
      case ComparisonOperation.LESS: return " < ";
      case ComparisonOperation.LESS_EQUAL: return " <= ";
      // default: throw exception
      }
      return "";
   }

   private String toJS(ConstantBooleanExpression expr)
   {
      String result = "";
      Result rs = expr.getResult();
      if (rs == Result.TRUE)
      {
         result = "true";
      }
      else if (rs == Result.FALSE)
      {
         result = "false";
      }
      // else throw exception
      return result;
   }
}
