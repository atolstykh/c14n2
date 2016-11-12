package ru.relex.c14n2;

/**
 * The internal representation of the attribute.
 */
class Attribute {
  private String uri;
  private String localName;
  private String value;
  private boolean attributeQualified=true;

  public String getOldPrefix() {
    return oldPrefix;
  }

  private String oldPrefix;

  /**
   * Returns the uri of the qualified name of this attribute.
   * 
   * @return Returns the uri
   */
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
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

  public void setAttributeQualified(boolean attributeQualified) {
    this.attributeQualified = attributeQualified;
  }

  public boolean isAttributeQualified() {
    return attributeQualified;
  }

  public void setOldPrefix(String oldPrefix) {
    this.oldPrefix = oldPrefix;
  }
}