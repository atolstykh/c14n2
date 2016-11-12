package ru.relex.c14n2;

/**
 * The internal representation of the namespace declaration (xmlns attribure).
 */
class NamespaceContextParams {
  private String prefix = "";
  private int definitionDepth = 1;
  private String newPrefix = "";
  private Boolean hasOutput = false;
  private Boolean toOutput =false;
  private int outputDepth = -1;

  /**
   * Constructor.
   * @param hasOutput
   *          output flag
   * @param newPrefix
   *          new local name
   * @param definitionDepth
   *          definitionDepth of the node
   */
  public NamespaceContextParams(boolean hasOutput,
      String newPrefix, int definitionDepth) {
    setHasOutput(hasOutput);
    setNewPrefix(newPrefix);
    setPrefix(newPrefix);
    setDefinitionDepth(definitionDepth);
  }

  /**
   * Constructor.
   */
  public NamespaceContextParams() {
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
   * Returns the definitionDepth of the parent node in the DOM tree.
   * 
   * @return Returns the definitionDepth
   */
  public int getDefinitionDepth() {
    return definitionDepth;
  }

  public void setDefinitionDepth(int definitionDepth) {
    this.definitionDepth = definitionDepth;
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


  public Boolean isToOutput() {
    return toOutput;
  }

  public void setToOutput(Boolean toOutput) {
    this.toOutput = toOutput;
  }
}