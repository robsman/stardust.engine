package org.eclipse.stardust.engine.extensions.camel.common;

import java.util.Map;

public class TestBean {

	@SuppressWarnings("unchecked")
	public Map<String, Object> complete(Map<String, Object> person)
	{
		return (Map<String, Object>) person.get("address");
	}
}
