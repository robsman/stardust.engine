package org.eclipse.stardust.engine.extensions.camel.integration.management.model;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.model.RouteDefinition;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.google.gson.annotations.Expose;

public class RouteCamelModel {

	private RouteDefinition route;
	CamelContext context;
	ProducerTemplate template;
	private Element statisticElement;

	@Expose
	private String id;
	@Expose
	private String selfProcessingTime;
	@Expose
	private String exchangesCompleted;
	@Expose
	private String exchangesFailed;
	@Expose
	private String failuresHandled;
	@Expose
	private String redeliveries;
	@Expose
	private String externalRedeliveries;
	@Expose
	private String minProcessingTime;
	@Expose
	private String maxProcessingTime;
	@Expose
	private String totalProcessingTime;
	@Expose
	private String lastProcessingTime;
	@Expose
	private String deltaProcessingTime;
	@Expose
	private String meanProcessingTime;
	@Expose
	private String resetTimestamp;
	@Expose
	private String firstExchangeCompletedTimestamp;
	@Expose
	private String firstExchangeCompletedExchangeId;
	@Expose
	private String firstExchangeFailureTimestamp;
	@Expose
	private String firstExchangeFailureExchangeId;
	@Expose
	private String lastExchangeCompletedTimestamp;
	@Expose
	private String lastExchangeCompletedExchangeId;
	@Expose
	private String lastExchangeFailureTimestamp;
	@Expose
	private String lastExchangeFailureExchangeId;
	@Expose
	private String status;

	void initResponseValues() {
		this.id = statisticElement.getAttributeValue("id");
		this.selfProcessingTime = statisticElement
				.getAttributeValue("selfProcessingTime");
		this.exchangesCompleted = statisticElement
				.getAttributeValue("exchangesCompleted");
		this.exchangesFailed = statisticElement
				.getAttributeValue("exchangesFailed");
		this.failuresHandled = statisticElement
				.getAttributeValue("failuresHandled");
		this.failuresHandled = statisticElement
				.getAttributeValue("failuresHandled");
		this.redeliveries = statisticElement.getAttributeValue("redeliveries");
		this.externalRedeliveries = statisticElement
				.getAttributeValue("externalRedeliveries");
		this.minProcessingTime = statisticElement
				.getAttributeValue("minProcessingTime");
		this.maxProcessingTime = statisticElement
				.getAttributeValue("maxProcessingTime");
		this.totalProcessingTime = statisticElement
				.getAttributeValue("totalProcessingTime");
		this.lastProcessingTime = statisticElement
				.getAttributeValue("lastProcessingTime");
		this.deltaProcessingTime = statisticElement
				.getAttributeValue("deltaProcessingTime");
		this.meanProcessingTime = statisticElement
				.getAttributeValue("meanProcessingTime");
		this.resetTimestamp = statisticElement
				.getAttributeValue("resetTimestamp");
		this.firstExchangeCompletedTimestamp = statisticElement
				.getAttributeValue("firstExchangeCompletedTimestamp");
		this.firstExchangeCompletedExchangeId = statisticElement
				.getAttributeValue("firstExchangeCompletedExchangeId");
		this.firstExchangeFailureTimestamp = statisticElement
				.getAttributeValue("firstExchangeFailureTimestamp");
		this.firstExchangeFailureExchangeId = statisticElement
				.getAttributeValue("firstExchangeFailureExchangeId");
		this.lastExchangeCompletedTimestamp = statisticElement
				.getAttributeValue("lastExchangeCompletedTimestamp");
		this.lastExchangeCompletedExchangeId = statisticElement
				.getAttributeValue("lastExchangeCompletedExchangeId");
		this.lastExchangeFailureTimestamp = statisticElement
				.getAttributeValue("lastExchangeFailureTimestamp");
		this.lastExchangeFailureExchangeId = statisticElement
				.getAttributeValue("lastExchangeFailureExchangeId");
		this.status = template.requestBody("controlbus:route?routeId=" + id
				+ "&action=status", null, String.class);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSelfProcessingTime() {
		return selfProcessingTime;
	}

	public void setSelfProcessingTime(String selfProcessingTime) {
		this.selfProcessingTime = selfProcessingTime;
	}

	public String getExchangesCompleted() {
		return exchangesCompleted;
	}

	public void setExchangesCompleted(String exchangesCompleted) {
		this.exchangesCompleted = exchangesCompleted;
	}

	public String getExchangesFailed() {
		return exchangesFailed;
	}

	public void setExchangesFailed(String exchangesFailed) {
		this.exchangesFailed = exchangesFailed;
	}

	public String getFailuresHandled() {
		return failuresHandled;
	}

	public void setFailuresHandled(String failuresHandled) {
		this.failuresHandled = failuresHandled;
	}

	public String getRedeliveries() {
		return redeliveries;
	}

	public void setRedeliveries(String redeliveries) {
		this.redeliveries = redeliveries;
	}

	public String getExternalRedeliveries() {
		return externalRedeliveries;
	}

	public void setExternalRedeliveries(String externalRedeliveries) {
		this.externalRedeliveries = externalRedeliveries;
	}

	public String getMinProcessingTime() {
		return minProcessingTime;
	}

	public void setMinProcessingTime(String minProcessingTime) {
		this.minProcessingTime = minProcessingTime;
	}

	public String getMaxProcessingTime() {
		return maxProcessingTime;
	}

	public void setMaxProcessingTime(String maxProcessingTime) {
		this.maxProcessingTime = maxProcessingTime;
	}

	public String getTotalProcessingTime() {
		return totalProcessingTime;
	}

	public void setTotalProcessingTime(String totalProcessingTime) {
		this.totalProcessingTime = totalProcessingTime;
	}

	public String getLastProcessingTime() {
		return lastProcessingTime;
	}

	public void setLastProcessingTime(String lastProcessingTime) {
		this.lastProcessingTime = lastProcessingTime;
	}

	public String getDeltaProcessingTime() {
		return deltaProcessingTime;
	}

	public void setDeltaProcessingTime(String deltaProcessingTime) {
		this.deltaProcessingTime = deltaProcessingTime;
	}

	public String getMeanProcessingTime() {
		return meanProcessingTime;
	}

	public void setMeanProcessingTime(String meanProcessingTime) {
		this.meanProcessingTime = meanProcessingTime;
	}

	public String getResetTimestamp() {
		return resetTimestamp;
	}

	public void setResetTimestamp(String resetTimestamp) {
		this.resetTimestamp = resetTimestamp;
	}

	public String getFirstExchangeCompletedTimestamp() {
		return firstExchangeCompletedTimestamp;
	}

	public void setFirstExchangeCompletedTimestamp(
			String firstExchangeCompletedTimestamp) {
		this.firstExchangeCompletedTimestamp = firstExchangeCompletedTimestamp;
	}

	public String getFirstExchangeCompletedExchangeId() {
		return firstExchangeCompletedExchangeId;
	}

	public void setFirstExchangeCompletedExchangeId(
			String firstExchangeCompletedExchangeId) {
		this.firstExchangeCompletedExchangeId = firstExchangeCompletedExchangeId;
	}

	public String getFirstExchangeFailureTimestamp() {
		return firstExchangeFailureTimestamp;
	}

	public void setFirstExchangeFailureTimestamp(
			String firstExchangeFailureTimestamp) {
		this.firstExchangeFailureTimestamp = firstExchangeFailureTimestamp;
	}

	public String getFirstExchangeFailureExchangeId() {
		return firstExchangeFailureExchangeId;
	}

	public void setFirstExchangeFailureExchangeId(
			String firstExchangeFailureExchangeId) {
		this.firstExchangeFailureExchangeId = firstExchangeFailureExchangeId;
	}

	public String getLastExchangeCompletedTimestamp() {
		return lastExchangeCompletedTimestamp;
	}

	public void setLastExchangeCompletedTimestamp(
			String lastExchangeCompletedTimestamp) {
		this.lastExchangeCompletedTimestamp = lastExchangeCompletedTimestamp;
	}

	public String getLastExchangeCompletedExchangeId() {
		return lastExchangeCompletedExchangeId;
	}

	public void setLastExchangeCompletedExchangeId(
			String lastExchangeCompletedExchangeId) {
		this.lastExchangeCompletedExchangeId = lastExchangeCompletedExchangeId;
	}

	public String getLastExchangeFailureTimestamp() {
		return lastExchangeFailureTimestamp;
	}

	public void setLastExchangeFailureTimestamp(
			String lastExchangeFailureTimestamp) {
		this.lastExchangeFailureTimestamp = lastExchangeFailureTimestamp;
	}

	public String getLastExchangeFailureExchangeId() {
		return lastExchangeFailureExchangeId;
	}

	public void setLastExchangeFailureExchangeId(
			String lastExchangeFailureExchangeId) {
		this.lastExchangeFailureExchangeId = lastExchangeFailureExchangeId;
	}
	
	public RouteDefinition getRoute() {
		return route;
	}

	public void setRoute(RouteDefinition route) {
		this.route = route;
	}
	
	public RouteCamelModel(CamelContext context, RouteDefinition route) {
		this.route = route;
		this.context = context;
		template = context.createProducerTemplate();
		String xml = template.requestBody(
				"controlbus:route?routeId=" + route.getId() + "&action=stats",
				null, String.class);
		statisticElement = getRootElementFromXmlString(xml);
		initResponseValues();
	}

	public void startRoute() {
		template.sendBody("controlbus:route?routeId=" + route.getId()
				+ "&action=start", null);
	}

	public void stopRoute() {
		template.sendBody("controlbus:route?routeId=" + route.getId()
				+ "&action=stop", null);
	}

	private Element getRootElementFromXmlString(String xml) {
		SAXBuilder builder = new SAXBuilder();
		Reader in = new StringReader(xml);
		Document doc = null;
		Element root = null;
		try {
			doc = builder.build(in);
			root = doc.getRootElement();
		} catch (JDOMException e) {
		} catch (IOException e) {
		} catch (Exception e) {
		}
		return root;
	}
}
