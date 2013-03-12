
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		    A client side view of a process trigger.
 *  			A trigger is responsible for starting the process instance corresponding to the
 *  			process definition containing the trigger.
 * 		    
 * 
 * <p>Java class for Trigger complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Trigger">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="runtimeElementOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="synchronous" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="accessPoints" type="{http://eclipse.org/stardust/ws/v2012a/api}AccessPoints" minOccurs="0"/>
 *         &lt;element name="parameterMappings" type="{http://eclipse.org/stardust/ws/v2012a/api}ParameterMappings" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Trigger", propOrder = {
    "runtimeElementOid",
    "type",
    "synchronous",
    "accessPoints",
    "parameterMappings"
})
public class TriggerXto
    extends ModelElementXto
{

    protected long runtimeElementOid;
    protected String type;
    protected boolean synchronous;
    protected AccessPointsXto accessPoints;
    protected ParameterMappingsXto parameterMappings;

    /**
     * Gets the value of the runtimeElementOid property.
     * 
     */
    public long getRuntimeElementOid() {
        return runtimeElementOid;
    }

    /**
     * Sets the value of the runtimeElementOid property.
     * 
     */
    public void setRuntimeElementOid(long value) {
        this.runtimeElementOid = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the synchronous property.
     * 
     */
    public boolean isSynchronous() {
        return synchronous;
    }

    /**
     * Sets the value of the synchronous property.
     * 
     */
    public void setSynchronous(boolean value) {
        this.synchronous = value;
    }

    /**
     * Gets the value of the accessPoints property.
     * 
     * @return
     *     possible object is
     *     {@link AccessPointsXto }
     *     
     */
    public AccessPointsXto getAccessPoints() {
        return accessPoints;
    }

    /**
     * Sets the value of the accessPoints property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessPointsXto }
     *     
     */
    public void setAccessPoints(AccessPointsXto value) {
        this.accessPoints = value;
    }

    /**
     * Gets the value of the parameterMappings property.
     * 
     * @return
     *     possible object is
     *     {@link ParameterMappingsXto }
     *     
     */
    public ParameterMappingsXto getParameterMappings() {
        return parameterMappings;
    }

    /**
     * Sets the value of the parameterMappings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParameterMappingsXto }
     *     
     */
    public void setParameterMappings(ParameterMappingsXto value) {
        this.parameterMappings = value;
    }

}
