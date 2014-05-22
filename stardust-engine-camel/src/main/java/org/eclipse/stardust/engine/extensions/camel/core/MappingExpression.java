package org.eclipse.stardust.engine.extensions.camel.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingExpression
{
   private StringBuffer bodyExpression = new StringBuffer();

   private Boolean includeMoveEndpoint = false;
   private boolean includeConversionStrategy = false;

   private List<String> beanExpression = new ArrayList<String>();

   private List<String> postExpression = new ArrayList<String>();

   private Map<String, String> headerExpression = new HashMap<String, String>();

   private List<String> postHeadersExpression = new ArrayList<String>();

   public StringBuffer getBodyExpression()
   {
      return bodyExpression;
   }

   public List<String> getBeanExpression()
   {
      return beanExpression;
   }

   public List<String> getPostExpression()
   {
      return postExpression;
   }

   public Map<String, String> getHeaderExpression()
   {
      return headerExpression;
   }

   public List<String> getPostHeadersExpression()
   {
      return postHeadersExpression;
   }

   public void setPostHeadersExpression(List<String> postHeadersExpression)
   {
      this.postHeadersExpression = postHeadersExpression;
   }

   public Boolean getIncludeMoveEndpoint()
   {
      return includeMoveEndpoint;
   }

   public void setIncludeMoveEndpoint(Boolean includeMoveEndpoint)
   {
      this.includeMoveEndpoint = includeMoveEndpoint;
   }

   public boolean isIncludeConversionStrategy()
   {
      return includeConversionStrategy;
   }

   public void setIncludeConversionStrategy(boolean includeConversionStrategy)
   {
      this.includeConversionStrategy = includeConversionStrategy;
   }

   
}