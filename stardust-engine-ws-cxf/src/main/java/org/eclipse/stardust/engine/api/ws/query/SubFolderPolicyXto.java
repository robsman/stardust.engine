
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *          Evaluation Policy for limiting a DocumentQuery to a specific subfolder path.
 *          
 * 
 * <p>Java class for SubFolderPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SubFolderPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="limitSubFolder" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="recursive" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubFolderPolicy", propOrder = {
    "limitSubFolder",
    "recursive"
})
public class SubFolderPolicyXto
    extends EvaluationPolicyXto
{

    @XmlElement(required = true)
    protected String limitSubFolder;
    protected boolean recursive;

    /**
     * Gets the value of the limitSubFolder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLimitSubFolder() {
        return limitSubFolder;
    }

    /**
     * Sets the value of the limitSubFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLimitSubFolder(String value) {
        this.limitSubFolder = value;
    }

    /**
     * Gets the value of the recursive property.
     * 
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Sets the value of the recursive property.
     * 
     */
    public void setRecursive(boolean value) {
        this.recursive = value;
    }

}
