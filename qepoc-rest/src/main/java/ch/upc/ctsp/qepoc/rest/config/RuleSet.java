/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * TODO: add type comment.
 * 
 */

@Data
@EqualsAndHashCode(
        callSuper = true)
@ToString(
        callSuper = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
        name = "rule-set", namespace = "http://www.upc-cablecom.ch/query-engine/config")
@XmlType(
        name = "rule-set", namespace = "http://www.upc-cablecom.ch/query-engine/config")
public class RuleSet extends RuleCollection {
    @XmlAttribute(
            required = true)
    private String            path;
    @XmlElement(
            namespace = "http://www.upc-cablecom.ch/query-engine/config")
    private List<Conditional> conditional = new ArrayList<Conditional>();

}
