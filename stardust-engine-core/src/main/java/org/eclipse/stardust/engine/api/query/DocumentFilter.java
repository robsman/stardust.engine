package org.eclipse.stardust.engine.api.query;

public class DocumentFilter implements FilterCriterion
{

   private static final long serialVersionUID = 987920689518801181L;

   private String documentId;

   private final String modelId;

   public DocumentFilter(String documentId, String modelId)
   {
      this.documentId = documentId;
      this.modelId = modelId;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   public String getDocumentId()
   {
      return documentId;
   }

   public String getModelId()
   {
      return modelId;
   }

}
