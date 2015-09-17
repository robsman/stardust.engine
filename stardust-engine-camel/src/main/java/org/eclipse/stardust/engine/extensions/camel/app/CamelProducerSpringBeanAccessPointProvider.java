package org.eclipse.stardust.engine.extensions.camel.app;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SEND_RECEIVE_METHOD_WITH_HEADER;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SEND_METHOD_WITH_HEADER;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.app.PlainJavaAccessPointProvider;
import org.eclipse.stardust.engine.core.pojo.data.JavaAccessPoint;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;

public class CamelProducerSpringBeanAccessPointProvider extends
		PlainJavaAccessPointProvider {

	private static final Logger trace = LogManager
			.getLogger(CamelProducerSpringBeanAccessPointProvider.class
					.getName());
	
	Map<String, Object> computedAccessPoints = null;

	/**
	 * return an Iterator of computed access points values
	 * 
	 * @param applicationAttributes
	 * @param typeAttributes
	 * @return computedAccessPoints
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Iterator createIntrinsicAccessPoints(Map applicationAttributes,
			Map typeAttributes) {

		this.computedAccessPoints = new HashMap();

		boolean supportsMultipleAccessPoints = false;
		
		if (applicationAttributes
				.get(CamelConstants.SUPPORT_MULTIPLE_ACCESS_POINTS) != null)
		{
			supportsMultipleAccessPoints = (Boolean) applicationAttributes
					.get(CamelConstants.SUPPORT_MULTIPLE_ACCESS_POINTS);
		}

		if (!supportsMultipleAccessPoints) {

			String producerMethodName = (String) applicationAttributes
					.get(CamelConstants.PRODUCER_METHOD_NAME_ATT);
			
			if (producerMethodName == null)
			{
				producerMethodName = SEND_RECEIVE_METHOD_WITH_HEADER;
			}
			
			JavaAccessPoint oParamAccessPoint = new JavaAccessPoint("oParam1",
					"oParam1", Direction.IN);
			oParamAccessPoint.setAttribute(PredefinedConstants.CLASS_NAME_ATT,
					Object.class);
			this.computedAccessPoints.put("oParam1", oParamAccessPoint);

			JavaAccessPoint bodyAccessPoint = new JavaAccessPoint("body",
					"Body", Direction.IN);
			bodyAccessPoint.setAttribute(PredefinedConstants.CLASS_NAME_ATT,
					Object.class);
			computedAccessPoints.put("body", bodyAccessPoint);

			JavaAccessPoint oldHeaderAccessPoint = new JavaAccessPoint(
					"mParam2", "mParam2", Direction.IN);
			oldHeaderAccessPoint.setAttribute(
					PredefinedConstants.CLASS_NAME_ATT, Object.class);
			computedAccessPoints.put("mParam2", oldHeaderAccessPoint);

			JavaAccessPoint headerAccessPoint = new JavaAccessPoint("header",
					"Header", Direction.IN);
			headerAccessPoint.setAttribute(PredefinedConstants.CLASS_NAME_ATT,
					Object.class);
			computedAccessPoints.put("header", headerAccessPoint);

			if (!producerMethodName
					.equalsIgnoreCase(SEND_METHOD_WITH_HEADER)) {
				JavaAccessPoint returnValueAccessPoint = new JavaAccessPoint(
						"returnValue", "returnValue", Direction.OUT);
				returnValueAccessPoint.setAttribute(
						PredefinedConstants.CLASS_NAME_ATT, Object.class);
				computedAccessPoints.put("returnValue", returnValueAccessPoint);
			}
		}

		return computedAccessPoints.values().iterator();
	}
}
