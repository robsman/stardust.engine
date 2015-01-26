
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
 * <p>Java-Klasse für SubFolderPolicy complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der limitSubFolder-Eigenschaft ab.
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
     * Legt den Wert der limitSubFolder-Eigenschaft fest.
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
     * Ruft den Wert der recursive-Eigenschaft ab.
     * 
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Legt den Wert der recursive-Eigenschaft fest.
     * 
     */
    public void setRecursive(boolean value) {
        this.recursive = value;
    }

}
