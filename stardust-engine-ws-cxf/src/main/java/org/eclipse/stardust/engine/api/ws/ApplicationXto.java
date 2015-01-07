
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	        The client view of a workflow application.
 *  			Applications are software programs that interact with the Eclipse Process
 *  			Manager handling the processing required to support a particular activity in
 *  			whole or in part. Multiple activities may use the same application but only one
 *  			application may be executed within an activity.
 * 	        
 * 
 * <p>Java class for Application complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Application">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="accessPoints" type="{http://eclipse.org/stardust/ws/v2012a/api}AccessPoints" minOccurs="0"/>
 *         &lt;element name="typeAttributes" type="{http://eclipse.org/stardust/ws/v2012a/api}Attributes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Application", propOrder = {
    "accessPoints",
    "typeAttributes"
})
public class ApplicationXto
    extends ModelElementXto
{

    protected AccessPointsXto accessPoints;
    protected AttributesXto typeAttributes;

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
     * Gets the value of the typeAttributes property.
     * 
     * @return
     *     possible object is
     *     {@link AttributesXto }
     *     
     */
    public AttributesXto getTypeAttributes() {
        return typeAttributes;
    }

    /**
     * Sets the value of the typeAttributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributesXto }
     *     
     */
    public void setTypeAttributes(AttributesXto value) {
        this.typeAttributes = value;
    }

}
