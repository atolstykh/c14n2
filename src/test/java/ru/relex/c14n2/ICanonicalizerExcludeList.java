package ru.relex.c14n2;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class ICanonicalizerExcludeList {
  public List<Node> getExcludeList(Document doc) {
    return null;
  }

  public List<Node> getIncludeList(Document doc) {
    return null;
  }

  public abstract String getExcludeListName();
}
