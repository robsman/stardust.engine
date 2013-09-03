package org.eclipse.stardust.engine.extensions.camel.trigger;

import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.extensions.camel.runtime.Endpoint;

public class AccessPointProperties {

	private Endpoint endPoint;

	private String accessPointType;

	private String accessPointPath;

	private String accessPointLocation;

	private IData data;

	private String dataPath;

	private String xsdName;

	public Endpoint getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(Endpoint endPoint) {
		this.endPoint = endPoint;
	}

	public String getAccessPointType() {
		return accessPointType;
	}

	public void setAccessPointType(String accessPointType) {
		this.accessPointType = accessPointType;
	}

	public String getAccessPointLocation() {
		return accessPointLocation;
	}

	public void setAccessPointLocation(String accessPointLocation) {
		this.accessPointLocation = accessPointLocation;
	}

	public String getAccessPointPath() {
		return accessPointPath;
	}

	public void setAccessPointPath(String accessPointPath) {
		this.accessPointPath = accessPointPath;
	}

	public IData getData() {
		return data;
	}

	public void setData(IData data) {
		this.data = data;
	}

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public String getXsdName() {
		return xsdName;
	}

	public void setXsdName(String xsdName) {
		this.xsdName = xsdName;
	}
}
