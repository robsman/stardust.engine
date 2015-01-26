
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ProcessCumulationPolicy complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ProcessCumulationPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="cumulateWithRootPi" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="cumulateWithScopePi" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessCumulationPolicy", propOrder = {
    "cumulateWithRootPi",
    "cumulateWithScopePi"
})
public class ProcessCumulationPolicyXto
    extends EvaluationPolicyXto
{

    protected boolean cumulateWithRootPi;
    protected boolean cumulateWithScopePi;

    /**
     * Ruft den Wert der cumulateWithRootPi-Eigenschaft ab.
     * 
     */
    public boolean isCumulateWithRootPi() {
        return cumulateWithRootPi;
    }

    /**
     * Legt den Wert der cumulateWithRootPi-Eigenschaft fest.
     * 
     */
    public void setCumulateWithRootPi(boolean value) {
        this.cumulateWithRootPi = value;
    }

    /**
     * Ruft den Wert der cumulateWithScopePi-Eigenschaft ab.
     * 
     */
    public boolean isCumulateWithScopePi() {
        return cumulateWithScopePi;
    }

    /**
     * Legt den Wert der cumulateWithScopePi-Eigenschaft fest.
     * 
     */
    public void setCumulateWithScopePi(boolean value) {
        this.cumulateWithScopePi = value;
    }

}
