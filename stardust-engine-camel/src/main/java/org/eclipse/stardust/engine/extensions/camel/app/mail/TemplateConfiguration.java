package org.eclipse.stardust.engine.extensions.camel.app.mail;

import com.google.gson.annotations.SerializedName;

public class TemplateConfiguration
{
   @SerializedName("tName")
   private String name;

   @SerializedName("tPath")
   private String path;

   @SerializedName("tFormat")
   private String format;

   @SerializedName("tSource")
   private String source;

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getPath()
   {
      return path;
   }

   public void setPath(String path)
   {
      this.path = path;
   }

   public String getFormat()
   {
      return format;
   }

   public void setFormat(String format)
   {
      this.format = format;
   }

   public String getSource()
   {
      return source;
   }

   public void setSource(String source)
   {
      this.source = source;
   }

}
