package org.eclipse.stardust.engine.extensions.camel.core.app;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.*;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.*;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IAccessPoint;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.emfxsd.XPathFinder;
import org.eclipse.stardust.engine.extensions.camel.Util;
import org.eclipse.stardust.engine.extensions.camel.core.ProducerRouteContext;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ScriptingApplicationRouteContext extends ProducerRouteContext
{
   public static final Logger logger = LogManager
         .getLogger(ScriptingApplicationRouteContext.class);

   public ScriptingApplicationRouteContext(IApplication application, String partitionId,
         String camelContextId)
   {
      super(application, partitionId, camelContextId);
   }

   /**
    * Contains the internal logic to generate a Camel route for each Scripting language.
    * 
    * @param application
    * @return
    */
   protected String generateRoute(IApplication application)
   {
      if (Util.getScriptingLanguge(application).equalsIgnoreCase(JAVASCRIPT))
      {
         return buildRouteForJavaScriptApp(application);
      }
      else if (Util.getScriptingLanguge(application).equalsIgnoreCase(PYTHON))
      {
         return buildRouteForPythonApp(application);
      }
      else if (Util.getScriptingLanguge(application).equalsIgnoreCase(GROOVY))
      {
         String scriptCode = Util.getScriptCode(application);
         return buildRouteForGroovyApp(scriptCode);
      }
      return null;
   }

   /**
    * This will produce a simple Camel route where groovy will be used as a language. this
    * language is not yet included in the product.
    * 
    * @param scriptCode
    * @return
    */
   private String buildRouteForGroovyApp(String scriptCode)
   {
      StringBuilder route = new StringBuilder();
      route.append(setHeader("CamelLanguageScript", buildConstantExpression(scriptCode)));
      route.append(NEW_LINE);
      route.append(to("language:groovy"));
      return route.toString();
   }

   /**
    * Produce a camel Route to run the configuration provided in a Python Scripting App.
    * 
    * @param application
    * @return
    */
   private String buildRouteForPythonApp(IApplication application)
   {
      StringBuilder route = new StringBuilder();
      route.append(to("bean:bpmTypeConverter?method=toJSON"));
      route.append(setHeader("CamelLanguageScript",
            buildConstantExpression(buildConstantExpressionForPythonApp(application))));
      route.append(to("language:python"));
      return route.toString();
   }

   /**
    * Produce a camel Route to run the configuration provided in a JavaScript Scripting
    * App.
    * 
    * @param application
    * @return
    */
   private String buildRouteForJavaScriptApp(IApplication application)
   {
      StringBuilder route = new StringBuilder();
      route.append(to("bean:bpmTypeConverter?method=toNativeObject"));
      route.append(setHeader("CamelLanguageScript",
            buildConstantExpression(buildConstantExpressionForJavaScriptApp(application))));
      route.append(to("language:rhino-nonjdk"));
      route.append(to("bean:bpmTypeConverter?method=fromNativeObject"));

      return route.toString();
   }

   /**
    * produce the full Python script that will set in CamelLanguageScript Header
    * 
    * @param application
    * @return
    */
   private String buildConstantExpressionForPythonApp(IApplication application)
   {
      String scriptCode = Util.getScriptCode(application);
      Gson gson=getGsonInstance(application);
      StringBuilder header = new StringBuilder();
      header.append("import json\n");
      header.append("import pprint\n");
      for (Iterator< ? > iter = application.getAllInAccessPoints(); iter.hasNext();)
      {
         IAccessPoint ap = (IAccessPoint) iter.next();
         if (ap.getType().getId().equalsIgnoreCase("primitive"))
         {
            header.append(ap.getId() + "= exchange.getIn().getHeader('" + ap.getId()
                  + "')\n");
         }
         else if (ap.getType().getId()
               .equalsIgnoreCase(StructuredDataConstants.STRUCTURED_DATA))
         {
            header.append(ap.getId() + "JSON= exchange.getIn().getHeader('" + ap.getId()
                  + "')\n");
            header.append(ap.getId() + " = json.loads(" + ap.getId() + "JSON)\n");
         }
      }
      for (Iterator< ? > iter = application.getAllOutAccessPoints(); iter.hasNext();)
      {
         IAccessPoint ap = (IAccessPoint) iter.next();
         ITypeDeclaration typeDeclaration = StructuredTypeRtUtils.getTypeDeclaration(ap);

         if (typeDeclaration != null
               && ap.getType().getId()
                     .equalsIgnoreCase(StructuredDataConstants.STRUCTURED_DATA))
         {
            XSDSchema xsdSchema = StructuredTypeRtUtils.getSchema(
                  (ModelBean) application.getModel(), typeDeclaration);
            XSDNamedComponent component = StructuredTypeRtUtils
                  .findElementOrTypeDeclaration(xsdSchema, typeDeclaration.getId(), false);
            Set<TypedXPath> xPathSet = XPathFinder.findAllXPaths(xsdSchema, component);
            IXPathMap xPathMap = new ClientXPathMap(xPathSet);
            Object instance = StructuredDataXPathUtils.createInitialValue(xPathMap, "",
                  true);
            header.append("if exchange.getIn().getHeader('" + ap.getId()
                  + "')is None: \n");
            header.append("\t" + ap.getId() + " = " + gson.toJson(instance) + "\n");
            header.append("else :\n");
            header.append("\t" + ap.getId() + " = exchange.getIn().getHeader('"
                  + ap.getId() + "')\n");
         }
      }
      header.append("<![CDATA[\n" + scriptCode + "\n]]>");
      for (Iterator< ? > iter = application.getAllOutAccessPoints(); iter.hasNext();)
      {
         IAccessPoint ap = (IAccessPoint) iter.next();
         header.append("\nexchange.getOut().setHeader('" + ap.getId() + "'," + ap.getId()
               + ");");
      }
      return header.toString();
   }
   
   private Gson getGsonInstance(IApplication application){
	   GsonBuilder gsonBuilder = new GsonBuilder();
	   String language=application.getAttribute(SCRIPTING_LANGUAGE_EA_KEY);
	   gsonBuilder.registerTypeAdapter(Date.class, new JsonSerializer<Date>()
		      {
		         @Override
		         public JsonElement serialize(Date date, java.lang.reflect.Type typeOfSrc,
		               JsonSerializationContext context)
		         {
		            return date != null
		                  ? new JsonPrimitive("/Date(" + date.getTime() + ")/")
		                  : null;
		         }
		      });
	   if(StringUtils.isNotEmpty(language) &&language.equals(PYTHON)){
    	   gsonBuilder.registerTypeAdapter(Boolean.class, new JsonSerializer<Boolean>()
                   {
					@Override
					public JsonElement serialize(Boolean val,
							java.lang.reflect.Type typeOfSrc,
							JsonSerializationContext context) {
						 return val ? new JsonPrimitive("True") : new JsonPrimitive("False");
					}
                   });
       }
	   
	   return gsonBuilder.create();
   }
   

   /**
    * produce the full Javascript code that will be set in CamelLanguageScript Header
    * 
    * @param application
    * @return
    */
   private String buildConstantExpressionForJavaScriptApp(IApplication application)
   {
      StringBuilder script = new StringBuilder();
      script.append("<![CDATA[\n");
      script.append(buildCoreJavascriptFunctions());
      script.append(buildInputParametersInitializationScript(application) + "\n");
      String scriptCode = Util.getScriptCode(application);
      script.append(scriptCode + "\n");
      script.append(buildOutputParametersInitializationScript(application) + "\n");
      script.append("]]>\n");
      return script.toString();
   }

   /**
    * 
    * @param application
    * @return
    */
   private String buildInputParametersInitializationScript(IApplication application)
   {
      StringBuilder script = new StringBuilder();
      Gson gson=getGsonInstance(application);
      for (Iterator< ? > iter = application.getAllInAccessPoints(); iter.hasNext();)
      {
         IAccessPoint ap = (IAccessPoint) iter.next();
         Type type = ap.getAttribute("carnot:engine:type");
         script.append("var " + ap.getId() + ";\n");
         script.append("if(request.headers.get('" + ap.getId() + "')!=null){\n");
         if (ap.getType().getId().equalsIgnoreCase("primitive"))
         {
            if (type != null
                  && (type.getId().equalsIgnoreCase(Type.Integer.getId())
                        || type.getId().equalsIgnoreCase(Type.Float.getId())
                        || type.getId().equalsIgnoreCase(Type.Double.getId()) || type
                        .getId().equalsIgnoreCase(Type.Long.getId())))
            {
               script.append(ap.getId() + " = new Number( request.headers.get('"
                     + ap.getId() + "'));\n");
            }
            else if (type != null
                  && (type.getId().equalsIgnoreCase(Type.Boolean.getId()) || type.getId()
                        .equals("boolean")))
            {
               // In ECMA-262, all nonempty strings convert to true
               script.append("if(request.headers.get('" + ap.getId()
                     + "')=='true' ||request.headers.get('" + ap.getId()
                     + "')=='True' ||request.headers.get('" + ap.getId()
                     + "')=='y'||request.headers.get('" + ap.getId() + "')==1){\n");
               script.append(ap.getId() + "= true;\n");
               script.append("}else if(request.headers.get('" + ap.getId()
                     + "')=='false' ||request.headers.get('" + ap.getId()
                     + "')=='False' ||request.headers.get('" + ap.getId()
                     + "')=='n'||request.headers.get('" + ap.getId() + "')==0){");
               script.append(ap.getId() + "= false;\n");
               script.append("}else{");
               script.append(ap.getId() + " =  request.headers.get('" + ap.getId()
                     + "');");
               script.append("}");
            }
            else
            {
               script.append(ap.getId() + " =  request.headers.get('" + ap.getId()
                     + "');");
            }
         }
         else if (ap.getType().getId()
               .equalsIgnoreCase(StructuredDataConstants.STRUCTURED_DATA))
         {
            script.append(ap.getId() + " =  eval('(' + request.headers.get('"
                  + ap.getId() + "')+ ')');\n");
            script.append(ap.getId() + "=visitMembers(" + ap.getId()
                  + ", recursiveFunction);\n");
         }
         script.append("}else{\n");
         if (type != null && (type.getId().equalsIgnoreCase(Type.String.getId())))
         {
            script.append(ap.getId() + " = \"\";\n");
         }
         if (type != null && (type.getId().equalsIgnoreCase(Type.Double.getId())))
         {
            script.append(ap.getId() + " = new Number(0);\n");
         }
         if (type != null && (type.getId().equalsIgnoreCase(Type.Timestamp.getId())))
         {
            script.append(ap.getId() + " = new java.util.Date();\n");
         }
         if (type == null)
         {
            script.append(ap.getId() + " = {};\n");
         }
         script.append("}");
      }

      for (Iterator< ? > iter = application.getAllOutAccessPoints(); iter.hasNext();)
      {
         IAccessPoint ap = (IAccessPoint) iter.next();
         ITypeDeclaration typeDeclaration = StructuredTypeRtUtils.getTypeDeclaration(ap);
         script.append("var " + ap.getId() + ";\n");
         if (typeDeclaration != null
               && ap.getType().getId()
                     .equalsIgnoreCase(StructuredDataConstants.STRUCTURED_DATA))
         {
            XSDSchema xsdSchema = StructuredTypeRtUtils.getSchema(
                  (ModelBean) application.getModel(), typeDeclaration);
            XSDNamedComponent component = StructuredTypeRtUtils
                  .findElementOrTypeDeclaration(xsdSchema, typeDeclaration.getId(), false);
            Set<TypedXPath> xPathSet = XPathFinder.findAllXPaths(xsdSchema, component);
            IXPathMap xPathMap = new ClientXPathMap(xPathSet);
            Object instance = StructuredDataXPathUtils.createInitialValue(xPathMap, "",
                  true);
            script.append("if(request.headers.get('" + ap.getId() + "')==null){\n");
            script.append("var " + ap.getId() + "Structure = ("
                  + gson.toJson(instance) + ");\n");
            script.append(ap.getId() + " =  eval(" + ap.getId() + "Structure);\n");
            script.append(ap.getId() + "=visitMembers(" + ap.getId()
                  + ", recursiveFunction);\n");
            script.append("}else {\n");
            script.append("\t" + ap.getId() + " = exchange.getIn().getHeader('"
                  + ap.getId() + "');\n");
            script.append("}\n");
         }
         else
         {
            // script.append("if(request.headers.get('" + ap.getId() + "')!=null){\n");
            // script.append(ap.getId() + "= request.headers.get('" + ap.getId() + "');");
            // script.append("}");
         }
      }

      return script.toString();
   }

   private String buildOutputParametersInitializationScript(IApplication application)
   {
      StringBuilder script = new StringBuilder();
      for (Iterator< ? > iter = application.getAllOutAccessPoints(); iter.hasNext();)
      {
         IAccessPoint ap = (IAccessPoint) iter.next();
         script.append("\nsetOutHeader('" + ap.getId() + "'," + ap.getId() + ");");
      }
      return script.toString();
   }

   /**
    * Common Javascript functions used by the internal javascript processing logic to
    * traverse SDTs and replace custom JSON date by java.util.Date
    * 
    * @return
    */
   private String buildCoreJavascriptFunctions()
   {
      StringBuilder functions = new StringBuilder();
      functions
            .append("function setOutHeader(key, output){\nexchange.out.headers.put(key,output);}\n");
      functions
            .append("function isArray(obj) {\n\tif (Array.isArray) {\n\t\treturn Array.isArray(obj);\n\t} else {\n\treturn Object.prototype.toString.call(obj) === '[object Array]';\n\t}\n}\n");
      functions
            .append("function visitMembers(obj, callback) {\n\tvar i = 0, length = obj.length;\n\tif (isArray(obj)) {\n\t\t");
      functions
            .append("for(; i < length; i++) {\n\t\tobj[i]= callback(i, obj[i]);\n\t\t}\n");
      functions
            .append("} else {\n\t\tfor (i in obj) {\n\t\tobj[i]=  callback(i, obj[i]);}\n\t}\n\treturn obj;\n}\n");
      functions.append("function recursiveFunction(key, val) {\n");
      functions.append("\tif (val instanceof Object || isArray(val)) {\n");
      functions.append("\t\treturn visitMembers(val, recursiveFunction);\n");
      functions.append("\t} else {\n");
      functions.append("\t\treturn actualFunction(val, typeof val);\n");
      functions.append("\t}\n");
      functions.append("}\n");

      functions.append("function actualFunction(value, type) {\n");
      functions.append("\tvar dataAsLong;\n");
      functions.append("\tif (type === 'string') {\n");
      functions
            .append("\t\tdataAsLong =new RegExp(/\\/Date\\((-?\\d*)\\)\\//).exec(value);\n");
      functions.append("\tif (dataAsLong) {\n");
      functions.append("\t\treturn new java.util.Date(+dataAsLong[1]);\n");
      functions.append("\t}\n");
      functions.append("}\n");
      functions.append("return value;\n");
      functions.append("}\n");
      return functions.toString();
   }

   
   @Override
   public List<Inconsistency> validate()
   {
      List<Inconsistency> inconsistencies = CollectionUtils.newList();
      inconsistencies=super.validate();
      String scriptingLanguague=Util.getScriptingLanguge(application);
      if ( StringUtils.isNotEmpty(scriptingLanguague) && scriptingLanguague.equalsIgnoreCase(PYTHON))
      {
         inconsistencies.addAll(validatePythonApplication());
      }
      else if (StringUtils.isNotEmpty(scriptingLanguague) && scriptingLanguague.equalsIgnoreCase(GROOVY))
      {
         inconsistencies.addAll(validateGroovyApplication());
      }
      inconsistencies.addAll(validateJavascriptApplication()); 
      
      return inconsistencies;
   }
   
   /**
    * Contains the logic to validate a Javascript Application
    * @return
    */
   private List<Inconsistency> validateJavascriptApplication(){
      List<Inconsistency> inconsistencies = CollectionUtils.newList();
      if( StringUtils.isEmpty(Util.getScriptCode(application)))
      {
         inconsistencies.add(new Inconsistency("Please provide a valid script for application " + application.getId(), application, Inconsistency.ERROR));
      }
      return inconsistencies;
   }
   /**
    * Contains the logic to validate a Python Application
    * @return
    */
   private List<Inconsistency> validatePythonApplication(){
      List<Inconsistency> inconsistencies = CollectionUtils.newList();
      if( StringUtils.isEmpty(Util.getScriptCode(application)))
      {
         inconsistencies.add(new Inconsistency("Please provide a valid python script for application" + application.getId(), application, Inconsistency.ERROR));
      }
      return inconsistencies;
   }
   
   /**
    * Contains the logic to validate a Groovy Application
    * @return
    */
   private List<Inconsistency> validateGroovyApplication(){
      List<Inconsistency> inconsistencies = CollectionUtils.newList();
      if( StringUtils.isEmpty(Util.getScriptCode(application)))
      {
         inconsistencies.add(new Inconsistency("Please provide a valid groovy script for application" + application.getId(), application, Inconsistency.ERROR));
      }
      return inconsistencies;
   }
}
