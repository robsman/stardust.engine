package org.eclipse.stardust.engine.extensions.camel;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;

/**
 * Disable CaseSensitive Keys in exchange headers
 *
 *
 */
public class CamelMessage extends DefaultMessage
{
   private Map<String, Object> headers;

   @Override
   public void copyFrom(Message that)
   {
      if (that == this)
      {
         // the same instance so do not need to copy
         return;
      }

      // must initialize headers before we set the JmsMessage to avoid Camel
      // populating it before we do the copy
      getHeaders().clear();
      setMessageId(that.getMessageId());
      // copy body and fault flag
      setBody(that.getBody());
      setFault(that.isFault());

      // we have already cleared the headers
      if (that.hasHeaders())
      {
         getHeaders().putAll(that.getHeaders());
      }

      getAttachments().clear();
      if (that.hasAttachments())
      {
         getAttachments().putAll(that.getAttachments());
      }
   }

   @Override
   public boolean hasHeaders()
   {
      return (headers != null && !headers.isEmpty()) ? true : false;
   }

   @Override
   public boolean hasAttachments()
   {
      return super.hasAttachments();
   }

   @Override
   public void setBody(Object body)
   {
      super.setBody(body);
   }

   @Override
   public Object removeHeader(String name)
   {
      if (!hasHeaders())
      {
         return null;
      }
      return headers.remove(name);
   }

   @Override
   protected void populateInitialHeaders(Map<String, Object> map)
   {
      super.populateInitialHeaders(map);
   }

   @Override
   public DefaultMessage newInstance()
   {
      return new CamelMessage();
   }

   @Override
   public void setHeader(String name, Object value)
   {
      if (this.headers == null)
         this.headers = createHeaders();
      this.headers.put(name, value);
   }

   @Override
   public void setHeaders(Map<String, Object> headers)
   {
      this.headers = headers;
   }

   @Override
   public boolean removeHeaders(String pattern)
   {
      return super.removeHeaders(pattern);
   }

   @Override
   public Map<String, Object> getHeaders()
   {
      if (this.headers == null)
         this.headers = createHeaders();
      return this.headers;
   }

   @Override
   public Object getHeader(String name)
   {
      if (this.headers != null && !this.headers.isEmpty())
         return this.headers.get(name);
      return null;
   }

   @Override
   public <T> void setBody(Object value, Class<T> type)
   {
      super.setBody(value, type);
   }

   @Override
   protected Map<String, Object> createHeaders()
   {
      Map<String, Object> map = new HashMap<String, Object>();
      return map;
   }
}
