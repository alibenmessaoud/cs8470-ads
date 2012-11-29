package org.w3c.rdf.model;

/**
 * An abstract RDF node. Can either be resource or literal, exclusively.
 */
public interface RDFNode {

  /**
   * The formal string label of the node.
   * URI in case of a resource, string in case of a literal.
   */
  public String getLabel() throws ModelException;
}

