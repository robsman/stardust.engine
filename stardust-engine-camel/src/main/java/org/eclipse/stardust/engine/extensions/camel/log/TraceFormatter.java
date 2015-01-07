package org.eclipse.stardust.engine.extensions.camel.log;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PASSWORD;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.interceptor.DefaultTraceFormatter;
import org.apache.camel.processor.interceptor.TraceInterceptor;
import org.apache.camel.util.MessageHelper;
import org.springframework.util.StringUtils;

public class TraceFormatter extends DefaultTraceFormatter
{
   private String filterHeaders;

   public void setFilterHeaders(String filterHeaders)
   {
      this.filterHeaders = filterHeaders;
   }

   @Override
   public Object format(final TraceInterceptor interceptor,
         final ProcessorDefinition< ? > node, final Exchange exchange)
   {
      Message in = exchange.getIn();
      Message out = null;
      if (exchange.hasOut())
      {
         out = exchange.getOut();
      }

      StringBuilder sb = new StringBuilder();
      sb.append(extractBreadCrumb(interceptor, node, exchange));

      if (isShowExchangePattern())
      {
         sb.append(", Pattern:").append(exchange.getPattern());
      }
      // only show properties if we have any
      if (isShowProperties() && !exchange.getProperties().isEmpty())
      {
         sb.append(", Properties:").append(filter(exchange.getProperties()));
      }
      // only show headers if we have any
      if (isShowHeaders() && !in.getHeaders().isEmpty())
      {
         sb.append(", Headers:").append(filter(in.getHeaders()));
      }
      if (isShowBodyType())
      {
         sb.append(", BodyType:").append(MessageHelper.getBodyTypeName(in));
      }
      if (isShowBody())
      {
         sb.append(", Body:").append(MessageHelper.extractBodyForLogging(in, ""));
      }
      if (isShowOutHeaders() && out != null)
      {
         sb.append(", OutHeaders:").append(filter(out.getHeaders()));
      }
      if (isShowOutBodyType() && out != null)
      {
         sb.append(", OutBodyType:").append(MessageHelper.getBodyTypeName(out));
      }
      if (isShowOutBody() && out != null)
      {
         sb.append(", OutBody:").append(MessageHelper.extractBodyForLogging(out, ""));
      }
      if (isShowException() && exchange.getException() != null)
      {
         sb.append(", Exception:").append(exchange.getException());
      }

      // replace ugly <<<, with <<<
      String s = sb.toString();
      s = s.replaceFirst("<<<,", "<<<");

      if (getMaxChars() > 0)
      {
         if (s.length() > getMaxChars())
         {
            s = s.substring(0, getMaxChars()) + "...";
         }
         return s;
      }
      else
      {
         return s;
      }
   }

   private Map<String, Object> filter(Map<String, Object> input)
   {
      Map<String, Object> response = new HashMap<String, Object>();
      for (String key : input.keySet())
         if (!shouldFilterKey(key))
            response.put(key, input.get(key));
         else
            response.put(key, "xxxxxx");
      return response;
   }

   /**
    * returns true if the header should not be visible.
    * 
    * @param key
    * @return
    */
   private boolean shouldFilterKey(String key)
   {
      if (key.equalsIgnoreCase(PASSWORD))
      {
         return true;
      }
      if (!StringUtils.isEmpty(this.filterHeaders))
      {
         String[] keys = this.filterHeaders.split(",");
         for (String filter : keys)
         {
            if (key.equalsIgnoreCase(filter))
            {
               return true;
            }
         }
      }
      return false;
   }
}
