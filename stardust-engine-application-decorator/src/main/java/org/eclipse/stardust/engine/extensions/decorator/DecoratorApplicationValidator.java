package org.eclipse.stardust.engine.extensions.decorator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidatorEx;

public class DecoratorApplicationValidator
      implements ApplicationValidator, ApplicationValidatorEx
{
   @SuppressWarnings("rawtypes")
   public List validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      // TODO: implement
      List inconsistencies = CollectionUtils.newList();
      return inconsistencies;
   }

   @SuppressWarnings("rawtypes")
   public List validate(IApplication application)
   {
      // TODO: implement
      List inconsistencies = CollectionUtils.newList();
      return inconsistencies;
   }
}
