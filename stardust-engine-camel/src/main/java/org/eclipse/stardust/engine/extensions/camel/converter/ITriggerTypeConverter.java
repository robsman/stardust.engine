package org.eclipse.stardust.engine.extensions.camel.converter;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.extensions.camel.trigger.AccessPointProperties;

public interface ITriggerTypeConverter extends IBpmTypeConverter
{
   void unmarshal(IModel iModel,AccessPointProperties accessPoint);
}
