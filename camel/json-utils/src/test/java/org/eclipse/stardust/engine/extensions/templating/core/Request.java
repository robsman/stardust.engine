package org.eclipse.stardust.engine.extensions.templating.core;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Request
{

   @SerializedName("templateUri")
   private String templateUri;

   @SerializedName("template")
   private String template;

   private String format;

   @SerializedName("pdf")
   private boolean convertToPdf;

   private Map<String, Object> parameters;

   private Map<String, Object> output;

   public String getTemplateUri()
   {
      return templateUri;
   }

   public void setTemplateUri(String templateUri)
   {
      this.templateUri = templateUri;
   }

   public String getTemplate()
   {
      return template;
   }

   public void setTemplate(String template)
   {
      this.template = template;
   }

   public String getFormat()
   {
      return format;
   }

   public void setFormat(String format)
   {
      this.format = format;
   }

   public boolean isConvertToPdf()
   {
      return convertToPdf;
   }

   public void setConvertToPdf(boolean convertToPdf)
   {
      this.convertToPdf = convertToPdf;
   }

   public Map<String, Object> getParameters()
   {
      return parameters;
   }

   public void setParameters(Map<String, Object> parameters)
   {
      this.parameters = parameters;
   }

   public Map<String, Object> getOutput()
   {
      return output;
   }

   public void setOutput(Map<String, Object> output)
   {
      this.output = output;
   }

   @Override
   public String toString()
   {
      return "TemplatingRequest [templateUri=" + templateUri + ", template=" + template
            + ", format=" + format + ", convertToPdf=" + convertToPdf + ", parameters="
            + parameters + ", output=" + output + "]";
   }
   public void fromMap(Map<String, Object> input){
      if(input.containsKey("templateUri"))
         this.templateUri=(String) input.get("templateUri");
      if(input.containsKey("template"))
      this.template =(String) input.get("template");
      if(input.containsKey("format"))
         this.format=   (String) input.get("format");
      if(input.containsKey("pdf"))
         this.convertToPdf=(Boolean) input.get("pdf");
      if(input.containsKey("parameters"))
         this.parameters=(Map<String, Object>) input.get("parameters");
      if(input.containsKey("output"))
         this.output=(Map<String, Object>) input.get("output");
   }
   public Map<String,Object> toMap(){
      Map<String, Object> topLevel = new HashMap<String, Object>();
      topLevel.put("templateUri", this.templateUri);
      topLevel.put("template", this.template);
      topLevel.put("format", this.format);
      topLevel.put("pdf", this.convertToPdf);
      topLevel.put("parameters", this.parameters);
      topLevel.put("output", this.output);
      return topLevel;
   }

}
