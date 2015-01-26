package org.eclipse.stardust.engine.extensions.camel.splitter;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;

/**
 * This class can be used to implement the Splitter EIP in a Camel route for messages that contain
 * {@link ProcessInstances} or {@link ActivityInstances} in the header of the Camel exchange.<br/>
 * This is typically the case after executing a "ipp:[process|activity]:find" command on the Camel route.
 * The OID of the activity or process will be populated in the header as the new context for the split
 * message. The fields of this class allow to control other runtime behavior, e.g. to retain the body
 * of the original message (default) or any or all of the original headers.<br/><br/>
 * Please note that any objects retained from the original message are by reference, so changes to them
 * would affect parallel split messages!
 * 
 * @author JanHendrik.Scheufen
 */
public class InstancesSplitter
{
   private boolean retainHeaders = false;
   private boolean retainBody = true;
   private List<String> retainHeadersList;
   
   public InstancesSplitter() {}
   
   public InstancesSplitter(boolean retainBody, boolean retainHeaders) {
      this.retainBody = retainBody;
      this.retainHeaders = retainHeaders;
   }
   
   public InstancesSplitter(boolean retainBody, List<String> retainHeadersList) {
      this.retainBody = retainBody;
      setRetainHeadersList(retainHeadersList);
   }
   
   public InstancesSplitter(boolean retainBody, String retainHeadersString) {
      this.retainBody = retainBody;
      setRetainHeadersList(Arrays.asList(retainHeadersString.split(",")));
   }

   public List<Message> splitProcessInstances(@Headers Map<String, Object> headers, @Body Object body)
   {
      List<Message> answer = new ArrayList<Message>();
      ProcessInstances instances = (ProcessInstances) headers.get(PROCESS_INSTANCES);
      for (ProcessInstance pi : instances)
      {
         Message message = createMessage(headers, body);
         message.setHeader(PROCESS_INSTANCE_OID, pi.getOID());
         answer.add(message);
      }
      return answer;
   }

   public List<Message> splitActivityInstances(@Headers Map<String, Object> headers, @Body Object body)
   {
      List<Message> answer = new ArrayList<Message>();
      ActivityInstances instances = (ActivityInstances) headers.get(ACTIVITY_INSTANCES);
      for (ActivityInstance pi : instances)
      {
         Message message = createMessage(headers, body);
         message.setHeader(ACTIVITY_INSTANCE_OID, pi.getOID());
         answer.add(message);
      }
      return answer;
   }

   private Message createMessage(Map<String, Object> headers, Object body)
   {
      Message msg = new DefaultMessage();
      if(retainBody)
      {
         msg.setBody(body);
      }
      if(retainHeaders)
      {
         if( null != retainHeadersList )
         {
            for( String headerKey : retainHeadersList )
            {
               msg.setHeader(headerKey, headers.get(headerKey));
            }
         }
         else
         {
            msg.getHeaders().putAll(headers);
         }
      }
      return msg;
   }

   public boolean isRetainBody()
   {
      return retainBody;
   }

   public void setRetainBody(boolean retainBody)
   {
      this.retainBody = retainBody;
   }

   public boolean isRetainHeaders()
   {
      return retainHeaders;
   }

   public void setRetainHeaders(boolean retainHeaders)
   {
      this.retainHeaders = retainHeaders;
   }

   public void setRetainHeadersList(List<String> retainHeadersList)
   {
      if( null != retainHeadersList )
      {
         this.retainHeaders = true;
         this.retainHeadersList = retainHeadersList;
      }
   }
}
