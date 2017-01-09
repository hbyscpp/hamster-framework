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

  public static void main(String[] args) {
    String nodes[] = new String[10];
    boolean adjMatrix[][] = new boolean[10][10];

    for (int i = 0; i < 10; i++) {
      nodes[i] = "Node " + i;
    }

    /*
     * adjMatrix[0][1] = true; adjMatrix[1][2] = true; adjMatrix[2][0] = true; adjMatrix[2][4] =
     * true; adjMatrix[1][3] = true; adjMatrix[3][6] = true; adjMatrix[6][5] = true; adjMatrix[5][3]
     * = true; adjMatrix[6][7] = true; adjMatrix[7][8] = true; adjMatrix[7][9] = true;
     * adjMatrix[9][6] = true;
     */

    adjMatrix[0][1] = true;
    adjMatrix[1][2] = true;
    adjMatrix[2][0] = true;
    adjMatrix[2][6] = true;
    adjMatrix[3][4] = true;
    adjMatrix[4][5] = true;
    adjMatrix[4][6] = true;
    adjMatrix[5][3] = true;
    adjMatrix[6][7] = true;
    adjMatrix[7][8] = true;
    adjMatrix[8][6] = true;

    adjMatrix[6][1] = true;

    ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(adjMatrix, nodes);
    List cycles = ecs.getElementaryCycles();
    for (int i = 0; i < cycles.size(); i++) {
      List cycle = (List) cycles.get(i);
      for (int j = 0; j < cycle.size(); j++) {
        String node = (String) cycle.get(j);
        if (j < cycle.size() - 1) {
          System.out.print(node + " -> ");
        } else {
          System.out.print(node);
        }
      }
      System.out.print("\n");
    }
  }
}
