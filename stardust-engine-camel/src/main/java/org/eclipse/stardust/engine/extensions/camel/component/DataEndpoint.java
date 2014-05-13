package org.eclipse.stardust.engine.extensions.camel.component;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CSV_DELIMITER_KEY;
import java.util.Map;

import org.apache.camel.Consumer;

import org.apache.camel.Processor;
import org.apache.camel.Producer;

public class DataEndpoint extends AbstractIppEndpoint
{
   public DataEndpoint(String uri, IppComponent component)
   {
      super(uri, component);
   }

   @Override
   public Producer createProducer() throws Exception
   {
	   Map<String, Object> parameters = ((DataEndpointConfiguration) this.getEndpointConfiguration()).getParams();
	   if(this.subCommand.equals("fromCSV"))
      {
    	 if(parameters.size() > 1)
    	 {
    		 throw new IllegalArgumentException("This Endpoint accept only one option > delimiter");
    	 }
    	 if(!isValidDelimiter((String) parameters.get(CSV_DELIMITER_KEY)))
    	 {
    		 throw new IllegalArgumentException("Delimiter must have a length of one");
    	 }
      }
      
      if(this.subCommand.equals("toCSV"))
      {
    	  if(parameters.size() > 2)
     	 {
     		 throw new IllegalArgumentException("This Endpoint accept only two option > delimiter , autogenHeaders");
     	 }
     	 if(!isValidDelimiter((String) parameters.get(CSV_DELIMITER_KEY)))
     	 {
     		 throw new IllegalArgumentException("Delimiter must have a length of one");
     	 }
      }
	   return new DataEndpointProducer(this);
   }

   @Override
   public Consumer createConsumer(Processor processor) throws Exception
   {
      throw new UnsupportedOperationException("This endpoint cannot be used as a consumer:" + getEndpointUri());
   }

   @Override
   public boolean isSingleton()
   {
      return true;
   }

   /**
    * return true because the endpoint allows additional unknown options to be passed to
    * it
    */
   public boolean isLenientProperties()
   {
      return true;
   }
   
   private static boolean isValidDelimiter(String delimiter)
   {
	   return delimiter == null || delimiter.toCharArray().length == 1 ? true : false;
   }
}
