
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BenchmarkResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BenchmarkResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="benchmarkOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="category" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="properties" type="{http://eclipse.org/stardust/ws/v2012a/api}Map"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BenchmarkResult", propOrder = {
    "benchmarkOid",
    "category",
    "properties"
})
public class BenchmarkResultXto {

    protected long benchmarkOid;
    protected int category;
    @XmlElement(required = true)
    protected MapXto properties;

    /**
     * Gets the value of the benchmarkOid property.
     * 
     */
    public long getBenchmarkOid() {
        return benchmarkOid;
    }

    /**
     * Sets the value of the benchmarkOid property.
     * 
     */
    public void setBenchmarkOid(long value) {
        this.benchmarkOid = value;
    }

    /**
     * Gets the value of the category property.
     * 
     */
    public int getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     * 
     */
    public void setCategory(int value) {
        this.category = value;
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link MapXto }
     *     
     */
    public MapXto getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link MapXto }
     *     
     */
    public void setProperties(MapXto value) {
        this.properties = value;
    }

}
