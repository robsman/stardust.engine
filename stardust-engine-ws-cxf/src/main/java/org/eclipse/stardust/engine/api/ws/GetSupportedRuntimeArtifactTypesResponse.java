
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="artifactTypes">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="artifactType" type="{http://eclipse.org/stardust/ws/v2012a/api}ArtifactType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "artifactTypes"
})
@XmlRootElement(name = "getSupportedRuntimeArtifactTypesResponse")
public class GetSupportedRuntimeArtifactTypesResponse {

    @XmlElement(required = true, nillable = true)
    protected GetSupportedRuntimeArtifactTypesResponse.ArtifactTypesXto artifactTypes;

    /**
     * Gets the value of the artifactTypes property.
     * 
     * @return
     *     possible object is
     *     {@link GetSupportedRuntimeArtifactTypesResponse.ArtifactTypesXto }
     *     
     */
    public GetSupportedRuntimeArtifactTypesResponse.ArtifactTypesXto getArtifactTypes() {
        return artifactTypes;
    }

    /**
     * Sets the value of the artifactTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetSupportedRuntimeArtifactTypesResponse.ArtifactTypesXto }
     *     
     */
    public void setArtifactTypes(GetSupportedRuntimeArtifactTypesResponse.ArtifactTypesXto value) {
        this.artifactTypes = value;
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
     *         &lt;element name="artifactType" type="{http://eclipse.org/stardust/ws/v2012a/api}ArtifactType" maxOccurs="unbounded" minOccurs="0"/>
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
        "artifactType"
    })
    public static class ArtifactTypesXto {

        protected List<ArtifactTypeXto> artifactType;

        /**
         * Gets the value of the artifactType property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the artifactType property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getArtifactType().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ArtifactTypeXto }
         * 
         * 
         */
        public List<ArtifactTypeXto> getArtifactType() {
            if (artifactType == null) {
                artifactType = new ArrayList<ArtifactTypeXto>();
            }
            return this.artifactType;
        }

    }

}
