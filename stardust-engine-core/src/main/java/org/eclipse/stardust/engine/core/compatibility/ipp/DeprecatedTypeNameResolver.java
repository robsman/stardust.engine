package org.eclipse.stardust.engine.core.compatibility.ipp;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.spi.ITypeNameResolver;

public class DeprecatedTypeNameResolver implements ITypeNameResolver
{
   private static Logger trace = LogManager.getLogger(DeprecatedTypeNameResolver.class);
   
   private static String UNKNOWN_TYPE_NAME = null;
   
   private Map<String, DeprecatedType> typeMappings;
   
   public DeprecatedTypeNameResolver()
   {
      Map<String, DeprecatedType> typeMappings;
      try
      {
         Properties deprTypesProps = new Properties();
         deprTypesProps.load(DeprecatedTypeNameResolver.class.getResourceAsStream("deprecated-types.properties"));
         typeMappings = CollectionUtils.newHashMap(deprTypesProps.size());
         String appIds = deprTypesProps.getProperty("DeprecatedApplicationsIds");
         for(Iterator<String> appIter = StringUtils.split(appIds, ','); appIter.hasNext(); )
         {
            String appId = appIter.next();
            DeprecatedType type = new DeprecatedType(appId, deprTypesProps.getProperty(appId + ".name"), 
               deprTypesProps.getProperty(appId + ".validator"),
               deprTypesProps.getProperty(appId + ".applicationInstance"),
               deprTypesProps.getProperty(appId + ".accessPointProvider"));
            addTypeMapping(typeMappings, type);
         }
      }
      catch (IOException ioe)
      {
         trace.error("Failed loading deprecated compatibility type mappings.", ioe);
         typeMappings = Collections.emptyMap();
      }
      
      this.typeMappings = typeMappings;
   }
   
   private void addTypeMapping(Map<String, DeprecatedType> typeMappings, DeprecatedType type)
   {
      if(StringUtils.isNotEmpty(type.accessPointProviderClass))
      {
         typeMappings.put(type.accessPointProviderClass, type);
      }
      if(StringUtils.isNotEmpty(type.applicationInstanceClass))
      {
         typeMappings.put(type.applicationInstanceClass, type);
      }
      if(StringUtils.isNotEmpty(type.validatorClass))
      {
         typeMappings.put(type.validatorClass, type);
      }
   }

   @Override
   public String resolveTypeName(String typeName)
   {
      if(typeMappings.containsKey(typeName))
      {
         return typeMappings.get(typeName).getEquivalentClassType(typeName);
      }
      return null;
   }
   
   private static class DeprecatedType
   {
      String applicationId;
      String applicationName;
      String validatorClass;
      String applicationInstanceClass;
      String accessPointProviderClass;
      
      private static String WARN_MSG = "You''re using the deprecated class ''{0}'' which was part of the application ''{1}''";
      
      public DeprecatedType(String id, String name, String validatorClass,
            String applicationInstanceClass, String accessPointProviderClass)
      {
         this.applicationId = id;
         this.applicationName = name;
         this.validatorClass = validatorClass;
         this.applicationInstanceClass = applicationInstanceClass;
         this.accessPointProviderClass = accessPointProviderClass;
      }

      public String getEquivalentClassType(String typeName)
      {
         if(StringUtils.isEmpty(typeName))
         {
            return null;
         }
         String resolvedTypeName = null;
         if(typeName.compareTo(validatorClass) == 0)
         {
            resolvedTypeName = DeprecatedValidator.class.getName();
         } 
         else if (typeName.compareTo(applicationInstanceClass) == 0)
         {
            resolvedTypeName =  DeprecatedApplicationInstance.class.getName();
         }
         else if (typeName.compareTo(accessPointProviderClass) == 0)
         {
            resolvedTypeName =  DeprecatedAccessPointProvider.class.getName();
         }
         else
         {
            resolvedTypeName = UNKNOWN_TYPE_NAME;
         }
         
         if(resolvedTypeName != UNKNOWN_TYPE_NAME)
         {
            trace.warn(MessageFormat.format(WARN_MSG, typeName, applicationName)); 
         }
         return resolvedTypeName;
      }
   }

}
