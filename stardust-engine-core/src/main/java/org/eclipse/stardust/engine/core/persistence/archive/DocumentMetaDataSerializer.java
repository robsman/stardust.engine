package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryAuditTrailUtils;

public class DocumentMetaDataSerializer implements JsonSerializer<DocumentMetaData>, JsonDeserializer<DocumentMetaData>
{
   private static final Logger LOGGER = LogManager
         .getLogger(DocumentMetaDataSerializer.class);

   @Override
   public JsonElement serialize(final DocumentMetaData documentMetaData,
         final Type typeOfSrc, final JsonSerializationContext context)
   {
      final JsonObject jsonObject = new JsonObject();
      jsonObject.add("revisions", context.serialize(documentMetaData.getRevisions()));
      jsonObject.add("dataPathId", context.serialize(documentMetaData.getDataPathId()));
      String vfs;
      try
      {
         vfs = RepositoryAuditTrailUtils.serialize(documentMetaData.getVfsResource());
         jsonObject.addProperty("vfsResource", vfs);
      }
      catch (IOException e)
      {
         LOGGER.error("Failed to serialize documentMetaData", e);
      }

      return jsonObject;
   }

   @Override
   public DocumentMetaData deserialize(JsonElement json, Type typeOfT,
         JsonDeserializationContext context) throws JsonParseException
   {
      TypeToken<List<String>> listType = new TypeToken<List<String>>(){};
      
      DocumentMetaData meta = new DocumentMetaData();
      final JsonObject jsonObject = json.getAsJsonObject();
      JsonElement revisionsElement = jsonObject.get("revisions");
      if (revisionsElement != null)
      {
         meta.setRevisions((List)context.deserialize(revisionsElement, listType.getType()));
      }
      JsonElement pathElement = jsonObject.get("dataPathId");
      if (pathElement != null)
      {
         meta.setDataPathId((String)context.deserialize(pathElement, String.class));
      }
      JsonElement vfsElement = jsonObject.get("vfsResource");
      if (vfsElement != null)
      {
         try
         {
            Map vfsResource = RepositoryAuditTrailUtils.deserialize(vfsElement.getAsString());
            meta.setVfsResource(vfsResource);
         }
         catch (Exception e)
         {
            LOGGER.error("Failed to deserialize documentMetaData", e);
         }
      }
      return meta;
   }
   
   
}