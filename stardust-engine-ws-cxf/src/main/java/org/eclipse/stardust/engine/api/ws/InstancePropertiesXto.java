
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	        Contains descriptors.
 * 	        Descriptors can be included or excluded using the descriptor policy.
 * 	        
 * 
 * <p>Java class for InstanceProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InstanceProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="instanceProperty" type="{http://eclipse.org/stardust/ws/v2012a/api}Parameter" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InstanceProperties", propOrder = {
    "instanceProperty"
})
public class InstancePropertiesXto {

    protected List<ParameterXto> instanceProperty;

    /**
     * Gets the value of the instanceProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the instanceProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInstanceProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ParameterXto }
     * 
     * 
     */
    public List<ParameterXto> getInstanceProperty() {
        if (instanceProperty == null) {
            instanceProperty = new ArrayList<ParameterXto>();
        }
        return this.instanceProperty;
    }

}
