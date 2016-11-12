package ru.relex.c14n2;

/**
 * The internal representation of the prefix declaration (xmlns attribure).
 */
class PrefixContextParams {
  private String uri = "";
  private int depth = 0;

  public PrefixContextParams(String uri, int depth) {
    this.uri = uri;
    this.depth = depth;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public int getDepth() {
    return depth;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

}