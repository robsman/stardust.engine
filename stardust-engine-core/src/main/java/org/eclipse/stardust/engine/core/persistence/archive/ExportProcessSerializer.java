package org.eclipse.stardust.engine.core.persistence.archive;

import java.lang.reflect.Type;

import com.google.gson.*;

public class ExportProcessSerializer
      implements JsonSerializer<ExportProcess>, JsonDeserializer<ExportProcess>
{
   @Override
   public ExportProcess deserialize(JsonElement je, Type t, JsonDeserializationContext ctx)
         throws JsonParseException
   {
      ExportProcess rv;
      JsonObject jo;

      if (je.isJsonObject())
      {
         jo = je.getAsJsonObject();
         rv = new ExportProcess(jo.get("oid").getAsLong(), jo.get("uuid").getAsString());
      }
      else
      {
         String js = je.getAsString();
         String[] s = js.split(":", 2);
         rv = new ExportProcess(Long.valueOf(s[0]), s[1]);
      }
      return rv;
   }

   @Override
   public JsonElement serialize(ExportProcess data, Type type,
         JsonSerializationContext jsonSerializationContext)
   {
      JsonObject jo = new JsonObject();
      jo.addProperty("oid", data.getOid());
      jo.addProperty("uuid", data.getUuid());
      return jo;
   }
}
