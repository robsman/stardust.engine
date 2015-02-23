
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			 Result of a Query execution.
 * 			 Holds retrieved items and a flag if more items would be available beyond that subset.
 * 			
 * 
 * <p>Java-Klasse für QueryResult complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="QueryResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="totalCount" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="hasMore" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="totalCountThreshold" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryResult", propOrder = {
    "totalCount",
    "hasMore",
    "totalCountThreshold"
})
@XmlSeeAlso({
    UserGroupQueryResultXto.class,
    ProcessDefinitionQueryResultXto.class,
    VariableDefinitionQueryResultXto.class,
    org.eclipse.stardust.engine.api.ws.WorklistXto.UserWorklistXto.class,
    org.eclipse.stardust.engine.api.ws.WorklistXto.SharedWorklistsXto.SharedWorklistXto.class,
    ActivityQueryResultXto.class,
    ModelsQueryResultXto.class,
    UserQueryResultXto.class,
    ProcessInstanceQueryResultXto.class,
    LogEntryQueryResultXto.class,
    DocumentQueryResultXto.class
})
public class QueryResultXto {

    protected Long totalCount;
    protected boolean hasMore;
    protected long totalCountThreshold;

    /**
     * Ruft den Wert der totalCount-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTotalCount() {
        return totalCount;
    }

    /**
     * Legt den Wert der totalCount-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTotalCount(Long value) {
        this.totalCount = value;
    }

    /**
     * Ruft den Wert der hasMore-Eigenschaft ab.
     * 
     */
    public boolean isHasMore() {
        return hasMore;
    }

    /**
     * Legt den Wert der hasMore-Eigenschaft fest.
     * 
     */
    public void setHasMore(boolean value) {
        this.hasMore = value;
    }

    /**
     * Ruft den Wert der totalCountThreshold-Eigenschaft ab.
     * 
     */
    public long getTotalCountThreshold() {
        return totalCountThreshold;
    }

    /**
     * Legt den Wert der totalCountThreshold-Eigenschaft fest.
     * 
     */
    public void setTotalCountThreshold(long value) {
        this.totalCountThreshold = value;
    }

}
