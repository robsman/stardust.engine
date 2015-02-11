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
	private Long selfProcessingTime;
	@Expose
	private Long exchangesCompleted;
	@Expose
	private Long exchangesFailed;
	@Expose
	private Long failuresHandled;
	@Expose
	private Long redeliveries;
	@Expose
	private Long externalRedeliveries;
	@Expose
	private Long minProcessingTime;
	@Expose
	private Long maxProcessingTime;
	@Expose
	private Long totalProcessingTime;
	@Expose
	private Long lastProcessingTime;
	@Expose
	private Long deltaProcessingTime;
	@Expose
	private Long meanProcessingTime;
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
	@Expose
	private String description;

	void initResponseValues() {
		this.id = statisticElement.getAttributeValue("id");
		this.selfProcessingTime = Long.parseLong(statisticElement
				.getAttributeValue("selfProcessingTime"));
		this.exchangesCompleted = Long.parseLong(statisticElement
				.getAttributeValue("exchangesCompleted"));
		this.exchangesFailed = Long.parseLong(statisticElement
				.getAttributeValue("exchangesFailed"));
		this.failuresHandled = Long.parseLong(statisticElement
				.getAttributeValue("failuresHandled"));
		this.failuresHandled = Long.parseLong(statisticElement
				.getAttributeValue("failuresHandled"));
		this.redeliveries = Long.parseLong(statisticElement.getAttributeValue("redeliveries"));
		this.externalRedeliveries = Long.parseLong(statisticElement
				.getAttributeValue("externalRedeliveries"));
		this.minProcessingTime = Long.parseLong(statisticElement
				.getAttributeValue("minProcessingTime"));
		this.maxProcessingTime = Long.parseLong(statisticElement
				.getAttributeValue("maxProcessingTime"));
		this.totalProcessingTime = Long.parseLong(statisticElement
				.getAttributeValue("totalProcessingTime"));
		this.lastProcessingTime = Long.parseLong(statisticElement
				.getAttributeValue("lastProcessingTime"));
		this.deltaProcessingTime = Long.parseLong(statisticElement
				.getAttributeValue("deltaProcessingTime"));
		this.meanProcessingTime = Long.parseLong(statisticElement
				.getAttributeValue("meanProcessingTime"));
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
		if ( route.getDescription() != null){
			this.description = route.getDescription().getText();
	}
		else{
			this.description = "";
		}

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getSelfProcessingTime() {
		return selfProcessingTime;
	}

	public void setSelfProcessingTime(Long selfProcessingTime) {
		this.selfProcessingTime = selfProcessingTime;
	}

	public Long getExchangesCompleted() {
		return exchangesCompleted;
	}

	public void setExchangesCompleted(Long exchangesCompleted) {
		this.exchangesCompleted = exchangesCompleted;
	}

	public Long getExchangesFailed() {
		return exchangesFailed;
	}

	public void setExchangesFailed(Long exchangesFailed) {
		this.exchangesFailed = exchangesFailed;
	}

	public Long getFailuresHandled() {
		return failuresHandled;
	}

	public void setFailuresHandled(Long failuresHandled) {
		this.failuresHandled = failuresHandled;
	}

	public Long getRedeliveries() {
		return redeliveries;
	}

	public void setRedeliveries(Long redeliveries) {
		this.redeliveries = redeliveries;
	}

	public Long getExternalRedeliveries() {
		return externalRedeliveries;
	}

	public void setExternalRedeliveries(Long externalRedeliveries) {
		this.externalRedeliveries = externalRedeliveries;
	}

	public Long getMinProcessingTime() {
		return minProcessingTime;
	}

	public void setMinProcessingTime(Long minProcessingTime) {
		this.minProcessingTime = minProcessingTime;
	}

	public Long getMaxProcessingTime() {
		return maxProcessingTime;
	}

	public void setMaxProcessingTime(Long maxProcessingTime) {
		this.maxProcessingTime = maxProcessingTime;
	}

	public Long getTotalProcessingTime() {
		return totalProcessingTime;
	}

	public void setTotalProcessingTime(Long totalProcessingTime) {
		this.totalProcessingTime = totalProcessingTime;
	}

	public Long getLastProcessingTime() {
		return lastProcessingTime;
	}

	public void setLastProcessingTime(Long lastProcessingTime) {
		this.lastProcessingTime = lastProcessingTime;
	}

	public Long getDeltaProcessingTime() {
		return deltaProcessingTime;
	}

	public void setDeltaProcessingTime(Long deltaProcessingTime) {
		this.deltaProcessingTime = deltaProcessingTime;
	}

	public Long getMeanProcessingTime() {
		return meanProcessingTime;
	}

	public void setMeanProcessingTime(Long meanProcessingTime) {
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
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
