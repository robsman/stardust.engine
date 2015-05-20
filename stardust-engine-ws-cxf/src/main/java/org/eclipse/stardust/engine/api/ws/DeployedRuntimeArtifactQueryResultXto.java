
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *          Result of an DeployedRuntimeArtifactQuery execution.
 *          
 * 
 * <p>Java class for DeployedRuntimeArtifactQueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeployedRuntimeArtifactQueryResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *       &lt;sequence>
 *         &lt;element name="deployedRuntimeArtifacts" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="deployedRuntimeArtifact" type="{http://eclipse.org/stardust/ws/v2012a/api}DeployedRuntimeArtifact" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeployedRuntimeArtifactQueryResult", propOrder = {
    "deployedRuntimeArtifacts"
})
public class DeployedRuntimeArtifactQueryResultXto
    extends QueryResultXto
{

    protected DeployedRuntimeArtifactQueryResultXto.DeployedRuntimeArtifactsXto deployedRuntimeArtifacts;

    /**
     * Gets the value of the deployedRuntimeArtifacts property.
     * 
     * @return
     *     possible object is
     *     {@link DeployedRuntimeArtifactQueryResultXto.DeployedRuntimeArtifactsXto }
     *     
     */
    public DeployedRuntimeArtifactQueryResultXto.DeployedRuntimeArtifactsXto getDeployedRuntimeArtifacts() {
        return deployedRuntimeArtifacts;
    }

    /**
     * Sets the value of the deployedRuntimeArtifacts property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeployedRuntimeArtifactQueryResultXto.DeployedRuntimeArtifactsXto }
     *     
     */
    public void setDeployedRuntimeArtifacts(DeployedRuntimeArtifactQueryResultXto.DeployedRuntimeArtifactsXto value) {
        this.deployedRuntimeArtifacts = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="deployedRuntimeArtifact" type="{http://eclipse.org/stardust/ws/v2012a/api}DeployedRuntimeArtifact" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "deployedRuntimeArtifact"
    })
    public static class DeployedRuntimeArtifactsXto {

        protected List<DeployedRuntimeArtifactXto> deployedRuntimeArtifact;

        /**
         * Gets the value of the deployedRuntimeArtifact property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the deployedRuntimeArtifact property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDeployedRuntimeArtifact().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link DeployedRuntimeArtifactXto }
         * 
         * 
         */
        public List<DeployedRuntimeArtifactXto> getDeployedRuntimeArtifact() {
            if (deployedRuntimeArtifact == null) {
                deployedRuntimeArtifact = new ArrayList<DeployedRuntimeArtifactXto>();
            }
            return this.deployedRuntimeArtifact;
        }

    }

}
