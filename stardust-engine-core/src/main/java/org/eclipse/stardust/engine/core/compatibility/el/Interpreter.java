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

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 *
 */
public class Interpreter
{
   private static final Logger trace = LogManager.getLogger(Interpreter.class);

   protected static final String TRUE = "TRUE";
   protected static final String FALSE = "FALSE";
   protected static final String OTHERWISE = "OTHERWISE";
   
   /**
    *    This method creates of the given expressionString a result as a expression,
    *    for the result the content of the String will be examined.
    *
    *    For syntax see
    * @see #evaluate
    */
   public static BooleanExpression parse(String expressionString)
         throws SyntaxError
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("==> create Expression from: " + expressionString);
      }

      String trimmedString = expressionString.trim();

      BooleanExpression expression;
      if (TRUE.equalsIgnoreCase(trimmedString))
      {
         return ConstantBooleanExpression.TRUE;
      }
      else if (FALSE.equalsIgnoreCase(trimmedString))
      {
         return ConstantBooleanExpression.FALSE;
      }
      else if (OTHERWISE.equalsIgnoreCase(trimmedString))
      {
         return ConstantBooleanExpression.OTHERWISE;
      }
      else
      {
         ELTokenizer tok = new ELTokenizer(expressionString);
         expression = createBooleanExpression(tok);
         if (tok.nextToken() != ELTokenizer.TT_EOF)
         {
            throw new SyntaxError("Invalid token: " + tok);
         }
         if (trace.isDebugEnabled())
         {
            expression.debug(trace, "");
         }
      }

      return expression;
   }

   private static int getBooleanOperator(ELTokenizer tok) throws SyntaxError
   {
      int tt = tok.nextToken();
      if (tt == ELTokenizer.TT_EOF)
      {
         return 0;
      }
      else if (tt == ')')
      {
         tok.pushBack();
         return 0;
      }
      else if (tt == '=')
      {
         return CombineOperation.EQUAL;
      }
      else if (tt == '!')
      {
         if (isNextToken(tok, '='))
         {
            return CombineOperation.NOT_EQUAL;
         }
      }
      else if (tt == ELTokenizer.TT_WORD)
      {
         if ("AND".equalsIgnoreCase(tok.sval))
         {
            return CombineOperation.AND;
         }
         if ("OR".equalsIgnoreCase(tok.sval))
         {
            return CombineOperation.OR;
         }
      }
      throw new SyntaxError("Invalid token: " + tok);
   }

   private static BooleanExpression createBooleanExpression(ELTokenizer tok) throws SyntaxError
   {
      BooleanExpression lexpr = createBooleanOperand(tok);
      int op = getBooleanOperator(tok);
      while (CombineOperation.EQUAL == op || CombineOperation.NOT_EQUAL == op)
      {
         BooleanExpression rexpr = createBooleanOperand(tok);
         lexpr = new CombineOperation(op, lexpr, rexpr);
         op = getBooleanOperator(tok);
      }
      while (CombineOperation.AND == op)
      {
         BooleanExpression rexpr = createBooleanOperand(tok);
         lexpr = new CombineOperation(op, lexpr, rexpr);
         op = getBooleanOperator(tok);
      }
      if (CombineOperation.OR == op)
      {
         return new CombineOperation(op, lexpr, createBooleanExpression(tok));
      }
      return lexpr;
   }

   private static BooleanExpression createBooleanOperand(ELTokenizer tok) throws SyntaxError
   {
      int tt = tok.nextToken();
      if (tt == ELTokenizer.TT_EOF)
      {
         throw new SyntaxError("Unexpected end of stream.");
      }

      if (tt == '!')
      {
         return createNotExpression(tok);
      }
      else if (tt == '(')
      {
         return evaluateParanthesis(tok);
      }
      else
      {
         tok.pushBack();
         return createComparisonExpression(tok);
      }
   }

   private static BooleanExpression evaluateParanthesis(ELTokenizer tok) throws SyntaxError
   {
      BooleanExpression expr = createBooleanExpression(tok);
      if (tok.nextToken() != ')')
      {
         throw new SyntaxError("Invalid token: " + tok);
      }
      return expr;
   }

   private static BooleanExpression createNotExpression(ELTokenizer tok) throws SyntaxError
   {
      int tt = tok.nextToken();
      if (tt == ELTokenizer.TT_EOF)
      {
         throw new SyntaxError("Unexpected end of stream.");
      }
      if (tt == '(')
      {
         return new CombineOperation(CombineOperation.NOT, evaluateParanthesis(tok), null);
      }
      throw new SyntaxError("Invalid token: " + tok);
   }

   private static int readCompareOperator(ELTokenizer tok) throws SyntaxError
   {
      int tt = tok.nextToken();
      if (tt == ELTokenizer.TT_EOF)
      {
         throw new SyntaxError("Unexpected end of stream.");
      }
      if (tt == '=')
      {
         if (isNextToken(tok, '<'))
         {
            trace.warn("Deprecated comparison symbol used: '=<'. Correct symbol is: '<='." );
            return ComparisonOperation.LESS_EQUAL;
         }
         if (isNextToken(tok, '>'))
         {
            trace.warn("Deprecated comparison symbol used: '=>'. Correct symbol is: '>='." );
            return ComparisonOperation.GREATER_EQUAL;
         }
         return ComparisonOperation.EQUAL;
      }
      if (tt == '<')
      {
         return isNextToken(tok, '=') ? ComparisonOperation.LESS_EQUAL : ComparisonOperation.LESS;
      }
      if (tt == '>')
      {
         return isNextToken(tok, '=') ? ComparisonOperation.GREATER_EQUAL : ComparisonOperation.GREATER;
      }
      if (tt == '!')
      {
         if (isNextToken(tok, '='))
         {
            return ComparisonOperation.NOT_EQUAL;
         }
      }
      throw new SyntaxError("Invalid token: " + tok);
   }

   private static boolean isNextToken(ELTokenizer tok, int token)
   {
      int tt = tok.nextToken();
      if (tt == token)
      {
         return true;
      }
      tok.pushBack();
      return false;
   }

   private static BooleanExpression createComparisonExpression(ELTokenizer tok) throws SyntaxError
   {
      ValueExpression ldata = actualReadExpression(tok);
      int op = readCompareOperator(tok);
      ValueExpression rdata = actualReadExpression(tok);
      return op == 0 ? null : new ComparisonOperation(op, ldata, rdata);
   }

   private static ValueExpression actualReadExpression(ELTokenizer tok) throws SyntaxError
   {
      switch (tok.nextToken())
      {
         case ELTokenizer.TT_EOF:
            throw new SyntaxError("Premature end of stream " + tok);
         case ELTokenizer.TT_NUMBER:
            return new ConstantExpression(new Double(tok.nval));
         case '\'':
            if (tok.sval.length() != 1)
            {
               throw new SyntaxError("Invalid character " + tok);
            }
            return new ConstantExpression(new Character(tok.sval.charAt(0)));
         case '"':
            return new ConstantExpression(tok.sval);
         case ELTokenizer.TT_WORD:
            if ("TRUE".equalsIgnoreCase(tok.sval) || "FALSE".equalsIgnoreCase(tok.sval))
            {
               return new ConstantExpression(Boolean.valueOf(tok.sval));
            }
            else
            {
               return readDereferencePath(tok);
            }
         default:
            throw new SyntaxError("Unexpected character " + tok);
      }
   }

   private static ValueExpression readDereferencePath(ELTokenizer tok) throws SyntaxError
   {
      boolean shouldAppend = false;
      StringBuffer buf = new StringBuffer(tok.sval);
      if (!Character.isJavaIdentifierStart(buf.charAt(0)))
      {
         throw new SyntaxError("Invalid variable identifier " + tok);
      }
      while (true)
      {
         switch (tok.nextToken())
         {
            case '.':
               return new DereferencePath(buf.toString(), readJavaAccessPath(tok));
            case '[':
               return new DereferencePath(buf.toString(), readComplexAccessPath(tok));
            default:
               if (Character.isJavaIdentifierPart((char) tok.ttype))
               {
                  buf.append((char) tok.ttype);
                  shouldAppend = true;
               }
               else if (tok.ttype == ELTokenizer.TT_WORD && shouldAppend)
               {
                  buf.append(tok.sval);
                  shouldAppend = false;
               }
               else
               {
                  tok.pushBack();
                  for (int i = 0; i < buf.length(); i++)
                  {
                     if (buf.charAt(i) != '_')
                     {
                        return new DereferencePath(buf.toString());
                     }
                  }
                  throw new SyntaxError("Invalid variable identifier " + buf.toString());
               }
         }
      }
   }

   private static String readJavaAccessPath(ELTokenizer tok) throws SyntaxError
   {
      boolean needsSeparator = false;
      int level = 0;
      StringBuffer buf = new StringBuffer();
      while (tok.nextToken() != ELTokenizer.TT_EOF)
      {
         if (tok.ttype == '(')
         {
            level++;
         }
         else if (tok.ttype == ')')
         {
            if (level == 0)
            {
               tok.pushBack();
               break;
            }
            level--;
         }
         else
         {
            if (level == 0 && isSeparator(tok))
            {
               tok.pushBack();
               break;
            }
         }
         needsSeparator = appendToken(tok, buf, needsSeparator);
      }
      if (level != 0)
      {
         throw new SyntaxError("Missing ending ')' " + tok);
      }
      return buf.toString();
   }

   private static String readComplexAccessPath(ELTokenizer tok) throws SyntaxError
   {
      boolean needsSeparator = false;
      int level = 1;
      StringBuffer buf = new StringBuffer();
      while (tok.nextToken() != ELTokenizer.TT_EOF)
      {
         if (tok.ttype == '[')
         {
            level++;
         }
         else if (tok.ttype == ']')
         {
            level--;
            if (level == 0)
            {
               break;
            }
         }
         needsSeparator = appendToken(tok, buf, needsSeparator);
      }
      if (level != 0)
      {
         throw new SyntaxError("Missing ending ']' " + tok);
      }
      return buf.toString();
   }

   private static boolean appendToken(ELTokenizer tok, StringBuffer buf, boolean needsSeparator)
   {
      switch (tok.ttype)
      {
         case ELTokenizer.TT_NUMBER:
            if (needsSeparator)
            {
               buf.append(' ');
            }
            buf.append(tok.nval);
            needsSeparator = true;
            break;
         case ELTokenizer.TT_WORD:
            if (needsSeparator)
            {
               buf.append(' ');
            }
            buf.append(tok.sval);
            needsSeparator = true;
            break;
         case '\'':
            if (needsSeparator)
            {
               buf.append(' ');
            }
            buf.append('\'').append(tok.sval).append('\'');
            needsSeparator = true;
            break;
         case '"':
            if (needsSeparator)
            {
               buf.append(' ');
            }
            buf.append('"').append(tok.sval).append('"');
            needsSeparator = true;
            break;
         default:
            buf.append((char) tok.ttype);
            needsSeparator = false;
      }
      return needsSeparator;
   }

   private static boolean isSeparator(ELTokenizer tok)
   {
      if (tok.ttype == '=' || tok.ttype == '<' || tok.ttype == '>' || tok.ttype == '!')
      {
         return true;
      }
      if (tok.ttype == ELTokenizer.TT_WORD)
      {
         if ("AND".equalsIgnoreCase(tok.sval) || "OR".equalsIgnoreCase(tok.sval))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * This method evaluates the given expressionString.
    *
    * For the result the content of the String will be examined.
    * It identify braces, OR, AND and comparison Expression.
    * <p>
    * You can use the following operators:
    * <li> a AND operator <code>AND</code> </li>
    * <li> a OR operator <code>OR</code> </li>
    * <li> a GREATER operator <code>></code> </li>
    * <li> a LESS operator <code><</code> </li>
    * <li> a EQUALS operator <code>=</code> </li>
    * <li> a NOT_EQUAL operator <code>!=</code> </li>
    * <li> a GREATER_EQUAL operator <code>>=</code> </li>
    * <li> a LESS_EQUAL operator <code><=</code> </li>
    * <li> and you can use brace to separate AND and OR expressions </li>
    * <p>
    * You can use the following constant types:
    * <li> boolean constants (<code>TRUE</code> and <code>FALSE</code>) </li>
    * <li> string constants (like <code>"blue"</code> or <code>"closed"</code>) </li>
    * <li> character constants (like <code>'c'</code> or <code>'R'</code>) </li>
    * <li> numeric constants (like <code>1233</code> or <code>142323.65</code>) </li>
    * <li> nullpointer constant (like <code>NULL</code> or <code>null</code>) </li>
    * <p>
    * Additional you can use some variables whith must be found in the <code>symbolTable</code>.
    * <li>You can use simple variables (like <code>streetname</code>) </li>
    * <li>or variables with a DereferencePath (like <code>customer.getAdress().getStreetname()</code>) </li>
    * <p>
    * Attention! There are some minor limitations! You can't use:
    * <li> a NOT operator (like <code>!a.isEmpty()</code>) but you can use <code>a.isEmpty() = FALSE</code>) </li>
    * <li> a boolean symbol without a comparison (like <code>a.isEmpty()</code> but you can use <code>a.isEmpty() = TRUE</code> </li>
    * <li> a constants without a comparison (like boolean constant in <code>TRUE OR count>5</code>) </li>
    * <li> a comparison between brace expression (like <code>(a.isEmpty() AND b.isOpen()) = TRUE</code> )</li>
    * <li> mathematical operations (like <code>+ - / % *</code>) <li>
    * <li> don't mix different numeric types (like comparing Long and Double <code>3 < 4.55</code>) <li>
    * <li> space between a minussign and the number in negative numbers are not allowed (use <code>-3</code> and not <code>- 3</code>) <li>
    * <li> braceexpressions in a comparisonexpression are not allowed (use <code>(String1 = String2)</code> and not <code>FALSE = (String1 != String2)</code>) <li>
    * <p>
    * Here are some examples for correct expressions:
    * <li><code>TRUE</code> </li>
    * <li><code>a.isEmpty() = TRUE</code> </li>
    * <li><code>(a.isEmpty() = TRUE) AND (b.getWeight() < 123)</code> </li>
    * <li><code>(currentCustomer.getAdress().getStreetname() = "Broadway") OR (cost > 9999.99)</code> </li>
    */
   public static Result evaluate(String expressionString, SymbolTable symbolTable)
         throws SyntaxError, EvaluationError
   {
      Assert.isNotNull(expressionString, "Expression string is not null.");
      Assert.isNotEmpty(expressionString, "Expression string is not empty.");

      String trimmedString = expressionString.trim();

      if (TRUE.equalsIgnoreCase(trimmedString))
      {
         return Result.TRUE;
      }
      else if (FALSE.equalsIgnoreCase(trimmedString))
      {
         return Result.FALSE;
      }
      else if (OTHERWISE.equalsIgnoreCase(trimmedString))
      {
         return Result.OTHERWISE;
      }

      BooleanExpression rootExpression = parse(expressionString);
      return rootExpression.evaluate(symbolTable);
   }

   public static Result evaluate(BooleanExpression expression, SymbolTable symbolTable)
         throws SyntaxError, EvaluationError
   {
      Assert.isNotNull(expression, "Expression string is null.");

      return expression.evaluate(symbolTable);
   }

   /**
    * For syntax see
    * @see org.eclipse.stardust.engine.core.compatibility.el.Interpreter#evaluate
    */
   public static boolean validate(String expressionString) throws SyntaxError
   {
      Assert.isNotNull(expressionString, "Expression string is not null.");
      Assert.isNotEmpty(expressionString, "Expression string is not empty.");

      String trimmedString = expressionString.trim();

      if (TRUE.equalsIgnoreCase(trimmedString) || FALSE.equalsIgnoreCase(trimmedString)
            || OTHERWISE.equalsIgnoreCase(trimmedString))
      {
         return true;
      }

      return (null != parse(expressionString));
   }

   private Interpreter()
   {
      // utility class
   }
}