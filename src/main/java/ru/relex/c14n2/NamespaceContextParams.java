package ru.relex.c14n2;

/**
 * The internal representation of the namespace declaration (xmlns attribure).
 */
class NamespaceContextParams {
  private String uri = "";
  private String prefix = "";
  private int depth = 1;
  private String newPrefix = "";
  private Boolean hasOutput = null;

  /**
   * Constructor.
   * 
   * @param uri
   *          URI
   * @param hasOutput
   *          output flag
   * @param newPrefix
   *          new local name
   * @param depth
   *          depth of the node
   */
  public NamespaceContextParams(String uri, boolean hasOutput,
      String newPrefix, int depth) {
    setUri(uri);
    setHasOutput(hasOutput);
    setNewPrefix(newPrefix);
    setPrefix(newPrefix);
    setDepth(depth);
  }

  /**
   * Constructor.
   */
  public NamespaceContextParams() {
  }

  /**
   * Returns the URI of this attribute.
   * 
   * @return Returns the URI
   */
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  /**
   * Returns whether this declaration in the output.
   * 
   * @return Returns true if this declaration is used in output, false
   *         otherwise.
   */
  public Boolean isHasOutput() {
    return hasOutput;
  }

  public void setHasOutput(Boolean hasOutput) {
    this.hasOutput = hasOutput;
  }

  /**
   * Returns the new local name (in "Prefix rewrite" mode) of the qualified name
   * of this attribute.
   * 
   * @return Returns the new local name
   */
  public String getNewPrefix() {
    return newPrefix;
  }

  public void setNewPrefix(String newPrefix) {
    this.newPrefix = newPrefix;
  }

  /**
   * Returns the depth of the parent node in the DOM tree.
   * 
   * @return Returns the depth
   */
  public int getDepth() {
    return depth;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

  /**
   * Returns the local name of the qualified name of this attribute.
   * 
   * @return Returns the prefix
   */
  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  /**
   * {@inheritDoc}
   */
  public NamespaceContextParams clone() {
    NamespaceContextParams ncp = new NamespaceContextParams();
    ncp.depth = depth;
    ncp.hasOutput = hasOutput;
    ncp.newPrefix = newPrefix;
    ncp.prefix = prefix;
    ncp.uri = uri;
    return ncp;
  }
}