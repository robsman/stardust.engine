package org.eclipse.stardust.engine.core.persistence.archive;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class ExportProcessSerializer
      implements JsonSerializer<ExportProcess>, JsonDeserializer<ExportProcess>
{
   private Gson gson;
   public static final String DELIMETER = ";";
   
   @Override
   public ExportProcess deserialize(JsonElement je, Type t, JsonDeserializationContext ctx)
         throws JsonParseException
   {
      ExportProcess rv;
      JsonObject jo;

      Type typeOfT = new TypeToken<Map<String, String>>() { }.getType();
      if (je.isJsonObject())
      {
         jo = je.getAsJsonObject();
         HashMap descr = ctx.deserialize(jo.get("descr"), typeOfT);
         rv = new ExportProcess(jo.get("oid").getAsLong(), jo.get("startDate").getAsString(), jo.get("endDate").getAsString(), jo.get("uuid").getAsString(), descr);
      }
      else
      {
         String js = je.getAsString();
         String[] s = js.split(ExportProcessSerializer.DELIMETER, 5);
         if (gson == null)
         {
            gson = ExportImportSupport.getGson();
         }
         HashMap descr = gson.fromJson(s[4], typeOfT);
         rv = new ExportProcess(Long.valueOf(s[0]), s[1], s[2], s[3], descr);
      }
      return rv;
   }

   @Override
   public JsonElement serialize(ExportProcess data, Type type,
         JsonSerializationContext jsonSerializationContext)
   {
      JsonObject jo = new JsonObject();
      jo.addProperty("oid", data.getOid());
      jo.addProperty("startDate", data.getStartDate());
      jo.addProperty("endDate", data.getEndDate());
      jo.addProperty("uuid", data.getUuid());
      JsonElement descr = jsonSerializationContext.serialize(data.getDescriptors());
      jo.add("descr", descr);
      return jo;
   }

   public void setGson(Gson gson)
   {
     this.gson = gson;
   }
}
