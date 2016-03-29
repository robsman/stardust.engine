package org.eclipse.stardust.engine.extensions.camel.core.app;

import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.to;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.extensions.camel.Util;
import org.eclipse.stardust.engine.extensions.camel.core.ProducerRouteContext;

public class SqlApplicationRouteContext extends ProducerRouteContext
{
   private static final Logger logger = LogManager.getLogger(SqlApplicationRouteContext.class);
   public SqlApplicationRouteContext(IApplication application, String partitionId,
         String camelContextId)
   {
      super(application, partitionId, camelContextId);
   }

   @Override
   protected String generateRoute(IApplication application)
   {
      return buildRoute(application);
   }
   private String buildRoute(IApplication application)
   {
      StringBuilder route = new StringBuilder();
      String uri=buildEndpointUri(application);
      route.append(to("sql:"+uri));
      route.append(to("bean:bpmTypeConverter?method=fromList"));
      return route.toString();
   }
   
   private String buildEndpointUri(IApplication application){
      StringBuilder uri=new StringBuilder();
      String  parameters=concatenateParameters(extractRequestParameters(application));
      String query=getQuery(application);
      if(StringUtils.isNotEmpty(query) ){
         if(query.contains("?"))
            uri.append(query+"&amp;"+parameters);
         else
            uri.append(query+"?"+parameters);
      }
      
     return uri.toString();
   }
   
   private String concatenateParameters(Map<String,String> parameters){
      StringBuilder params=new StringBuilder();
      if(!parameters.isEmpty()){
         for(String param: parameters.keySet()){
            params.append(param+"="+parameters.get(param)+"&amp;");
         }
         
         params=new StringBuilder(params.substring(0, params.length() - 5) );
      }
      return params.toString();
   }
   
   private Map<String,String> extractRequestParameters(IApplication application){
      Map<String,String> parameters= new HashMap<String,String>();
      parameters.put("alwaysPopulateStatement", "true");
      parameters.put("prepareStatementStrategy", "#sqlPrepareStatementStrategy");
      parameters.put("dataSource", "#"+getDataSourceName(application));
      String outputType=getOutputTypegetQuery(application);
      if(StringUtils.isNotEmpty(outputType))
         parameters.put("outputType", outputType);
      
      return parameters;
   }
   private String getDataSourceName(IApplication application){
      return application.getId()+ "Ds";
   }
   private String getOutputTypegetQuery(IApplication application){
      String outputType= Util.getOutputType(application);
      if(StringUtils.isNotEmpty(outputType))
         return outputType;
      return null;
   }
   private String getQuery(IApplication application){
      String sqlQuery= Util.getSqlQuery(application);
      if(StringUtils.isNotEmpty(sqlQuery))
         sqlQuery=StringEscapeUtils.escapeSql(StringEscapeUtils.escapeXml(sqlQuery));
      return sqlQuery;
   }
}
