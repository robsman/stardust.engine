
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für TimeoutPolicy complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TimeoutPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="timeout" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimeoutPolicy", propOrder = {
    "timeout"
})
public class TimeoutPolicyXto
    extends EvaluationPolicyXto
{

    protected int timeout;

    /**
     * Ruft den Wert der timeout-Eigenschaft ab.
     * 
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Legt den Wert der timeout-Eigenschaft fest.
     * 
     */
    public void setTimeout(int value) {
        this.timeout = value;
    }

}
