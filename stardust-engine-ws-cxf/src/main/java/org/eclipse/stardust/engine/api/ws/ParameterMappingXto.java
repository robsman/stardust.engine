
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		    The ParameterMapping represents a mapping between a trigger access point and a workflow data.
 * 		    
 * 
 * <p>Java class for ParameterMapping complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ParameterMapping">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="dataId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parameterPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parameter" type="{http://eclipse.org/stardust/ws/v2012a/api}AccessPoint" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterMapping", propOrder = {
    "dataId",
    "parameterPath",
    "parameter"
})
public class ParameterMappingXto
    extends ModelElementXto
{

    protected String dataId;
    protected String parameterPath;
    protected AccessPointXto parameter;

    /**
     * Gets the value of the dataId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataId() {
        return dataId;
    }

    /**
     * Sets the value of the dataId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataId(String value) {
        this.dataId = value;
    }

    /**
     * Gets the value of the parameterPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParameterPath() {
        return parameterPath;
    }

    /**
     * Sets the value of the parameterPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParameterPath(String value) {
        this.parameterPath = value;
    }

    /**
     * Gets the value of the parameter property.
     * 
     * @return
     *     possible object is
     *     {@link AccessPointXto }
     *     
     */
    public AccessPointXto getParameter() {
        return parameter;
    }

    /**
     * Sets the value of the parameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessPointXto }
     *     
     */
    public void setParameter(AccessPointXto value) {
        this.parameter = value;
    }

}
