package ru.relex.c14n2;

/**
 * The internal representation of the attribute.
 */
class Attribute {
  private String prefix;
  private String newPrefix;
  private String localName;
  private String value;

  /**
   * Returns the prefix of the qualified name of this attribute.
   * 
   * @return Returns the new prefix
   */
  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  /**
   * Returns the new prefix (in "Prefix rewrite" mode) of the qualified name of
   * this attribute.
   * 
   * @return Returns the prefix
   */
  public String getNewPrefix() {
    return newPrefix;
  }

  public void setNewPrefix(String newPrefix) {
    this.newPrefix = newPrefix;
  }

  /**
   * Returns the local part of the qualified name of this attribute.
   * 
   * @return Returns the local name
   */
  public String getLocalName() {
    return localName;
  }

  public void setLocalName(String localName) {
    this.localName = localName;
  }

  /**
   * Returns the value of this attribute.
   * 
   * @return Returns the value
   */
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}