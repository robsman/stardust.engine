
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProcessCumulationPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the cumulateWithRootPi property.
     * 
     */
    public boolean isCumulateWithRootPi() {
        return cumulateWithRootPi;
    }

    /**
     * Sets the value of the cumulateWithRootPi property.
     * 
     */
    public void setCumulateWithRootPi(boolean value) {
        this.cumulateWithRootPi = value;
    }

    /**
     * Gets the value of the cumulateWithScopePi property.
     * 
     */
    public boolean isCumulateWithScopePi() {
        return cumulateWithScopePi;
    }

    /**
     * Sets the value of the cumulateWithScopePi property.
     * 
     */
    public void setCumulateWithScopePi(boolean value) {
        this.cumulateWithScopePi = value;
    }

}
