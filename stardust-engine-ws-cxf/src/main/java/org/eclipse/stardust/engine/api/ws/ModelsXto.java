
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Result of an DeployedModelQuery execution. Retrieved items are instances of DeployedModelDescription.
 * 			
 * 
 * <p>Java class for Models complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Models">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="deployedModelDescription" type="{http://eclipse.org/stardust/ws/v2012a/api}DeployedModelDescription" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Models", propOrder = {
    "deployedModelDescription"
})
public class ModelsXto {

    protected List<DeployedModelDescriptionXto> deployedModelDescription;

    /**
     * Gets the value of the deployedModelDescription property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deployedModelDescription property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeployedModelDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DeployedModelDescriptionXto }
     * 
     * 
     */
    public List<DeployedModelDescriptionXto> getDeployedModelDescription() {
        if (deployedModelDescription == null) {
            deployedModelDescription = new ArrayList<DeployedModelDescriptionXto>();
        }
        return this.deployedModelDescription;
    }

}
