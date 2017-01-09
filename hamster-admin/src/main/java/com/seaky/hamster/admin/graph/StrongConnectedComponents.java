package com.seaky.hamster.admin.graph;

import java.util.ArrayList;
import java.util.List;

public class StrongConnectedComponents {

  /** Adjacency-list of original graph */
  private int[][] adjListOriginal = null;

  /** Adjacency-list of currently viewed subgraph */
  private int[][] adjList = null;

  /** Helpattribute for finding scc's */
  private boolean[] visited = null;

  /** Helpattribute for finding scc's */
  private List<Integer> stack = null;

  /** Helpattribute for finding scc's */
  private int[] lowlink = null;

  /** Helpattribute for finding scc's */
  private int[] number = null;

  /** Helpattribute for finding scc's */
  private int sccCounter = 0;

  /** Helpattribute for finding scc's */
  private List<List<Integer>> currentSCCs = null;

  /**
   * Constructor.
   *
   * @param adjList adjacency-list of the graph
   */
  public StrongConnectedComponents(int[][] adjList) {
    this.adjListOriginal = adjList;
  }

  /**
   * This method returns the adjacency-structure of the strong connected component with the least
   * vertex in a subgraph of the original graph induced by the nodes {s, s + 1, ..., n}, where s is
   * a given node. Note that trivial strong connected components with just one node will not be
   * returned.
   *
   * @param node node s
   * @return SCCResult with adjacency-structure of the strong connected component; null, if no such
   *         component exists
   */
  public SCCResult getAdjacencyList(int node) {
    this.visited = new boolean[this.adjListOriginal.length];
    this.lowlink = new int[this.adjListOriginal.length];
    this.number = new int[this.adjListOriginal.length];
    this.visited = new boolean[this.adjListOriginal.length];
    this.stack = new ArrayList<>();
    this.currentSCCs = new ArrayList<>();

    this.makeAdjListSubgraph(node);

    for (int i = node; i < this.adjListOriginal.length; i++) {
      if (!this.visited[i]) {
        this.getStrongConnectedComponents(i);
        List<Integer> nodes = this.getLowestIdComponent();
        if (nodes != null && !nodes.contains(new Integer(node))
            && !nodes.contains(new Integer(node + 1))) {
          return this.getAdjacencyList(node + 1);
        } else {
          List<Integer>[] adjacencyList = this.getAdjList(nodes);
          if (adjacencyList != null) {
            for (int j = 0; j < this.adjListOriginal.length; j++) {
              if (adjacencyList[j].size() > 0) {
                return new SCCResult(adjacencyList, j);
              }
            }
          }
        }
      }
    }

    return null;
  }

  /**
   * Builds the adjacency-list for a subgraph containing just nodes >= a given index.
   *
   * @param node Node with lowest index in the subgraph
   */
  private void makeAdjListSubgraph(int node) {
    this.adjList = new int[this.adjListOriginal.length][0];

    for (int i = node; i < this.adjList.length; i++) {
      List<Integer> successors = new ArrayList<>();
      for (int j = 0; j < this.adjListOriginal[i].length; j++) {
        if (this.adjListOriginal[i][j] >= node) {
          successors.add(new Integer(this.adjListOriginal[i][j]));
        }
      }
      if (successors.size() > 0) {
        this.adjList[i] = new int[successors.size()];
        for (int j = 0; j < successors.size(); j++) {
          Integer succ = successors.get(j);
          this.adjList[i][j] = succ.intValue();
        }
      }
    }
  }

  /**
   * Calculates the strong connected component out of a set of scc's, that contains the node with
   * the lowest index.
   *
   * @return Vector::Integer of the scc containing the lowest nodenumber
   */
  private List<Integer> getLowestIdComponent() {
    int min = this.adjList.length;
    List<Integer> currScc = null;

    for (int i = 0; i < this.currentSCCs.size(); i++) {
      List<Integer> scc = this.currentSCCs.get(i);
      for (int j = 0; j < scc.size(); j++) {
        Integer node = (Integer) scc.get(j);
        if (node.intValue() < min) {
          currScc = scc;
          min = node.intValue();
        }
      }
    }

    return currScc;
  }

  /**
   * @return Vector[]::Integer representing the adjacency-structure of the strong connected
   *         component with least vertex in the currently viewed subgraph
   */
  private List<Integer>[] getAdjList(List<Integer> nodes) {
    List<Integer>[] lowestIdAdjacencyList = null;

    if (nodes != null) {
      lowestIdAdjacencyList = new List[this.adjList.length];
      for (int i = 0; i < lowestIdAdjacencyList.length; i++) {
        lowestIdAdjacencyList[i] = new ArrayList<>();
      }
      for (int i = 0; i < nodes.size(); i++) {
        int node = ((Integer) nodes.get(i)).intValue();
        for (int j = 0; j < this.adjList[node].length; j++) {
          int succ = this.adjList[node][j];
          if (nodes.contains(new Integer(succ))) {
            lowestIdAdjacencyList[node].add(new Integer(succ));
          }
        }
      }
    }

    return lowestIdAdjacencyList;
  }

  /**
   * Searchs for strong connected components reachable from a given node.
   *
   * @param root node to start from.
   */
  private void getStrongConnectedComponents(int root) {
    this.sccCounter++;
    this.lowlink[root] = this.sccCounter;
    this.number[root] = this.sccCounter;
    this.visited[root] = true;
    this.stack.add(new Integer(root));

    for (int i = 0; i < this.adjList[root].length; i++) {
      int w = this.adjList[root][i];
      if (!this.visited[w]) {
        this.getStrongConnectedComponents(w);
        this.lowlink[root] = Math.min(lowlink[root], lowlink[w]);
      } else if (this.number[w] < this.number[root]) {
        if (this.stack.contains(new Integer(w))) {
          lowlink[root] = Math.min(this.lowlink[root], this.number[w]);
        }
      }
    }

    // found scc
    if ((lowlink[root] == number[root]) && (stack.size() > 0)) {
      int next = -1;
      List<Integer> scc = new ArrayList<>();

      do {
        next = (this.stack.get(stack.size() - 1)).intValue();
        this.stack.remove(stack.size() - 1);
        scc.add(new Integer(next));
      } while (this.number[next] > this.number[root]);

      // simple scc's with just one node will not be added
      if (scc.size() > 1) {
        this.currentSCCs.add(scc);
      }
    }
  }

}
