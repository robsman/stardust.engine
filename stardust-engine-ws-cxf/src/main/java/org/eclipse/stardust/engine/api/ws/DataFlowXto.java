
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import org.eclipse.stardust.common.Direction;


/**
 * 
 * 			The DataFlow defines the mapping between an activity and a data.
 * 			
 * 
 * <p>Java class for DataFlow complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataFlow">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *         &lt;element name="direction" type="{http://eclipse.org/stardust/ws/v2012a/api}Direction"/>
 *         &lt;element name="activityId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="processDefinitionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="context" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dataId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dataPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="applicationAccessPointId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="applicationPath" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mappedJavaType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataFlow", propOrder = {
    "type",
    "direction",
    "activityId",
    "processDefinitionId",
    "context",
    "dataId",
    "dataPath",
    "applicationAccessPointId",
    "applicationPath",
    "mappedJavaType"
})
public class DataFlowXto
    extends ModelElementXto
{

    @XmlElement(required = true)
    protected QName type;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    protected Direction direction;
    @XmlElement(required = true)
    protected String activityId;
    @XmlElement(required = true)
    protected String processDefinitionId;
    @XmlElement(required = true)
    protected String context;
    @XmlElement(required = true)
    protected String dataId;
    protected String dataPath;
    @XmlElement(required = true)
    protected String applicationAccessPointId;
    protected String applicationPath;
    @XmlElement(required = true)
    protected String mappedJavaType;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setType(QName value) {
        this.type = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirection(Direction value) {
        this.direction = value;
    }

    /**
     * Gets the value of the activityId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * Sets the value of the activityId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivityId(String value) {
        this.activityId = value;
    }

    /**
     * Gets the value of the processDefinitionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    /**
     * Sets the value of the processDefinitionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessDefinitionId(String value) {
        this.processDefinitionId = value;
    }

    /**
     * Gets the value of the context property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets the value of the context property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContext(String value) {
        this.context = value;
    }

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
     * Gets the value of the dataPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataPath() {
        return dataPath;
    }

    /**
     * Sets the value of the dataPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataPath(String value) {
        this.dataPath = value;
    }

    /**
     * Gets the value of the applicationAccessPointId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApplicationAccessPointId() {
        return applicationAccessPointId;
    }

    /**
     * Sets the value of the applicationAccessPointId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApplicationAccessPointId(String value) {
        this.applicationAccessPointId = value;
    }

    /**
     * Gets the value of the applicationPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApplicationPath() {
        return applicationPath;
    }

    /**
     * Sets the value of the applicationPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApplicationPath(String value) {
        this.applicationPath = value;
    }

    /**
     * Gets the value of the mappedJavaType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMappedJavaType() {
        return mappedJavaType;
    }

    /**
     * Sets the value of the mappedJavaType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMappedJavaType(String value) {
        this.mappedJavaType = value;
    }

}
