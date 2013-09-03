package org.eclipse.stardust.engine.extensions.camel;

import java.io.Serializable;


import org.apache.camel.impl.DefaultExchange;

public class ExchangeWrapper implements Serializable{

	 /**
	 *
	 */
	private static final long serialVersionUID = -5398091272032130883L;
	DefaultExchange exchange;

	public ExchangeWrapper(Object exchange) {
	    this.exchange=(DefaultExchange) exchange;
	}
	public Object getContent(){
	    return exchange.getIn().getBody();
	}
//	public DefaultExchange getExchange() {
//		return exchange;
//	}
//
//	public void setExchange(DefaultExchange exchange) {
//		this.exchange = exchange;
//	}

	@Override
	public String toString() {
	   
	return exchange.getIn().getBody().toString();
	}
}
