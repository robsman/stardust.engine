package org.eclipse.stardust.engine.core.compatibility.ipp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidatorEx;

public class DeprecatedValidator implements ApplicationValidator,
   ApplicationValidatorEx
{
   private String applicationName;
   
   @Override
   public List validate(IApplication application)
   {
      ArrayList inconsistencies = new ArrayList();
      if(applicationName == null && application != null)
      {
         applicationName = application.getName();
      }
      inconsistencies.add(new Inconsistency("Application type of " + applicationName + " is no longer supported",
            application, Inconsistency.WARNING));
      return inconsistencies;
   }

   @Override
   public List validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      return validate(null);
   }

}
