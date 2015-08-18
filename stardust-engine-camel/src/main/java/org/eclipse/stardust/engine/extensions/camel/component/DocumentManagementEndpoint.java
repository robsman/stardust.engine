package org.eclipse.stardust.engine.extensions.camel.component;

import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.parseSimpleExpression;
import org.apache.camel.*;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;

public class DocumentManagementEndpoint extends AbstractIppEndpoint
{
   protected Expression documentId;
   protected Expression processInstanceOid;
   protected Expression targetPath;

   public DocumentManagementEndpoint(String uri, IppComponent component)
   {
      super(uri, component);
   }

   @Override
   public Producer createProducer() throws Exception
   {
      return new DocumentManagementProducer(this);
   }

   @Override
   public Consumer createConsumer(Processor processor) throws Exception
   {
      throw new UnsupportedOperationException("This endpoint cannot be used as a consumer:" + getEndpointUri());
   }

   private Expression executeExpression(String expression)
   {
      return parseSimpleExpression(expression);
   }

   /**
    * 
    * @param exchange
    * @param strict
    *           flag
    * @return user account
    */
   public String evaluateDocumentId(Exchange exchange)
   {
      if (null != this.documentId)
      {
         return this.documentId.evaluate(exchange, String.class);
      }
      else
      {
         String documentId = exchange.getIn().getHeader(CamelConstants.MessageProperty.DOCUMENT_ID, String.class);
         if (documentId==null)
         {
            throw new IllegalStateException("Missing required DocumentID.");
         }
         return documentId;
      }
   }
   
   /**
    * 
    * @param exchange
    * @param strict
    *           flag
    * @return user account
    */
   public String evaluateTargetPath(Exchange exchange)
   {
      if (null != this.targetPath)
      {
         return this.targetPath.evaluate(exchange, String.class);
      }
      else
      {
         return exchange.getIn().getHeader(CamelConstants.MessageProperty.TARGET_PATH, String.class);
      }
   }

   public void setDocumentId(String documentId)
   {
      this.documentId = executeExpression(documentId);
   }

   public void setTargetPath(String targetPath)
   {
      this.targetPath = executeExpression(targetPath);
   }

   public Expression getDocumentId()
   {
      return documentId;
   }

   public void setDocumentId(Expression documentId)
   {
      this.documentId = documentId;
   }

   
   public Expression getProcessInstanceOid()
   {
      return processInstanceOid;
   }
   @Override
   public void setProcessInstanceOid(String processInstanceOid)
   {
      super.setProcessInstanceOid(processInstanceOid);
   }
}
