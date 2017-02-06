package com.seaky.hamster.admin.graph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SCCResult {

  private Set<Integer> nodeIDsOfSCC = null;
  private List<Integer>[] adjList = null;
  private int lowestNodeId = -1;

  public SCCResult(List<Integer>[] adjList, int lowestNodeId) {
    this.adjList = adjList;
    this.lowestNodeId = lowestNodeId;
    this.nodeIDsOfSCC = new HashSet<>();
    if (this.adjList != null) {
      for (int i = this.lowestNodeId; i < this.adjList.length; i++) {
        if (this.adjList[i].size() > 0) {
          this.nodeIDsOfSCC.add(new Integer(i));
        }
      }
    }
  }

  public List<Integer>[] getAdjList() {
    return adjList;
  }

  public int getLowestNodeId() {
    return lowestNodeId;
  }


}
