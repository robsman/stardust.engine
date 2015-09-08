package org.eclipse.stardust.engine.extensions.template;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidatorEx;

public class TemplateApplicationValidator implements ApplicationValidator,
ApplicationValidatorEx{

	@Override
	public List validate(Map attributes, Map typeAttributes,
			Iterator accessPoints) {

	   List inconsistencies = CollectionUtils.newList();
		return inconsistencies;
	}

	@Override
	public List validate(IApplication application) {
	   List inconsistencies = CollectionUtils.newList();
      return inconsistencies;
	}

}
