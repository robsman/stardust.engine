package org.eclipse.stardust.engine.extensions.camel.sql;

import static org.eclipse.stardust.engine.extensions.camel.EndpointHelper.replaceHtmlCodeByCharacter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.RuntimeExchangeException;
import org.apache.camel.component.sql.DefaultSqlPrepareStatementStrategy;

import org.apache.camel.util.StringQuoteHelper;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class SqlPrepareStatementStrategy extends DefaultSqlPrepareStatementStrategy
{
   private final char separator;
   private static final Logger LOG = LogManager.getLogger(SqlPrepareStatementStrategy.class.getCanonicalName());

   public SqlPrepareStatementStrategy(char separator)
   {
      this.separator = separator;
   }

   public SqlPrepareStatementStrategy()
   {
      super(',');
      separator = ',';
   }

   /**
    * Custom implementation for prepareQuery Method; The html characters are replaced by
    * their corresponding special characters.
    */
   @Override
   public String prepareQuery(String query, boolean allowNamedParameters) throws SQLException
   {
      String answer = super.prepareQuery(query, allowNamedParameters);
      answer = replaceHtmlCodeByCharacter(answer);
      if (LOG.isDebugEnabled())
         LOG.debug("Prepared query: " + answer);
      return answer;
   }

   @Override
   public void populateStatement(PreparedStatement ps, Iterator< ? > iterator, int expectedParams) throws SQLException
   {
      super.populateStatement(ps, iterator, expectedParams);
   }

   @Override
   public Iterator< ? > createPopulateIterator(final String query, final String preparedQuery,
         final int expectedParams, final Exchange exchange, final Object value) throws SQLException
   {
      if (hasNamedParameters(query))
      {
         // create an iterator that returns the value in the named order
         try
         {
            // the body may be a map which we look at first
            final Map< ? , ? > bodyMap = exchange.getContext().getTypeConverter().tryConvertTo(Map.class, value);
            final Map< ? , ? > headerMap = exchange.getIn().hasHeaders() ? exchange.getIn().getHeaders() : null;

            return new Iterator<Object>()
            {
               private NamedQueryParser parser = new NamedQueryParser(query);

               private Object nextParam;

               private boolean done;

               @Override
               public boolean hasNext()
               {
                  if (done)
                  {
                     return false;
                  }

                  if (nextParam == null)
                  {
                     nextParam = parser.next();
                     if (nextParam == null)
                     {
                        done = true;
                     }
                  }
                  return nextParam != null;
               }

               @Override
               public Object next()
               {
                  if (!hasNext())
                  {
                     throw new NoSuchElementException();
                  }

                  boolean contains = bodyMap != null && bodyMap.containsKey(nextParam);
                  contains |= headerMap != null && headerMap.containsKey(nextParam);
                  if (!contains)
                  {
                     throw new RuntimeExchangeException("Cannot find key [" + nextParam
                           + "] in message body or headers to use when setting named parameter in query [" + query
                           + "]", exchange);
                  }

                  // get from body before header
                  Object next = bodyMap != null ? bodyMap.get(nextParam) : null;
                  // ISB:Customized the default behavior; if a headerId exists it will
                  // override the value provided in the body Map
                  if (next == null || headerMap.get(nextParam) != null)
                  {
                     next = headerMap != null ? headerMap.get(nextParam) : null;
                  }

                  nextParam = null;
                  return next;
               }

               @Override
               public void remove()
               {
                  // noop
               }
            };
         }
         catch (Exception e)
         {
            throw new SQLException(
                  "The message body must be a java.util.Map type when using named parameters in the query: " + query, e);
         }

      }
      else
      {
         // if only 1 parameter and the body is a String then use body as is
         if (expectedParams == 1 && value instanceof String)
         {
            return Collections.singletonList(value).iterator();
         }
         else
         {
            // is the body a String
            if (value instanceof String)
            {
               // if the body is a String then honor quotes etc.
               String[] tokens = StringQuoteHelper.splitSafeQuote((String) value, separator, true);
               List<String> list = Arrays.asList(tokens);
               return list.iterator();
            }
            else
            {
               // just use a regular iterator
               return exchange.getContext().getTypeConverter().convertTo(Iterator.class, value);
            }
         }
      }
   }

   private static final class NamedQueryParser
   {

      private static final Pattern PATTERN = Pattern.compile("\\:\\?(\\w+)");

      private final Matcher matcher;

      private NamedQueryParser(String query)
      {
         this.matcher = PATTERN.matcher(query);
      }

      public String next()
      {
         if (!matcher.find())
         {
            return null;
         }

         return matcher.group(1);
      }
   }
}
