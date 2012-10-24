package ru.relex.c14n2;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface ICanonicalizerExcludeList {
  public List<Node> getExcludeList(Document doc);

  public String getExcludeListName();
}
