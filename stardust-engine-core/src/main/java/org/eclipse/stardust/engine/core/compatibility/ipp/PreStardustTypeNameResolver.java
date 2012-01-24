package org.eclipse.stardust.engine.core.compatibility.ipp;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.spi.ITypeNameResolver;

public class PreStardustTypeNameResolver implements ITypeNameResolver
{
   private static final Logger trace = LogManager.getLogger(PreStardustTypeNameResolver.class);

   private final Map<String, String> typeMappings;
   
   public PreStardustTypeNameResolver()
   {
      Map<String, String> typeMappings;
      try
      {
         Properties compatibilityProps = new Properties();
         compatibilityProps.load(PreStardustTypeNameResolver.class.getResourceAsStream("pre-stardust-types.properties"));
         typeMappings = CollectionUtils.newHashMap(compatibilityProps.size());
         for (Map.Entry entry : compatibilityProps.entrySet())
         {
            typeMappings.put((String) entry.getKey(), (String) entry.getValue());
         }
      }
      catch (IOException ioe)
      {
         trace.error("Failed loading pre-Stardust compatibility type mappings.", ioe);
         typeMappings = Collections.emptyMap();
      }
      
      this.typeMappings = typeMappings;
   }
   
   public String resolveTypeName(String typeName)
   {
      if (typeMappings.containsKey(typeName))
      {
         String resolvedTypeName = typeMappings.get(typeName);
         if (trace.isDebugEnabled())
         {
            trace.debug("Translating pre-Stardust type " + typeName + " to " + resolvedTypeName);
         }
         return resolvedTypeName;
      }
      
      return null;
   }

}
