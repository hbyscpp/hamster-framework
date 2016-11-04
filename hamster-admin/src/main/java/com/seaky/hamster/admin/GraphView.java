package com.seaky.hamster.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GraphView {


  private List<NodeView> nodes = new ArrayList<>();

  private List<LinkView> links = new ArrayList<>();

  public static class NodeView {

    private String id;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
  }

  public static class LinkView {

    private String target;

    private String source;

    private Set<String> serviceName;

    public String getTarget() {
      return target;
    }

    public void setTarget(String target) {
      this.target = target;
    }

    public String getSource() {
      return source;
    }

    public void setSource(String source) {
      this.source = source;
    }

    public Set<String> getServiceName() {
      return serviceName;
    }

    public void setServiceName(Set<String> serviceName) {
      this.serviceName = serviceName;
    }
  }

  public List<NodeView> getNodes() {
    return nodes;
  }

  public void setNodes(List<NodeView> nodes) {
    this.nodes = nodes;
  }

  public List<LinkView> getLinks() {
    return links;
  }

  public void setLinks(List<LinkView> links) {
    this.links = links;
  }
}
