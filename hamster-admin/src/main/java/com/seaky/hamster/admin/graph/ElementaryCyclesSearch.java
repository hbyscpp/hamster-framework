package com.seaky.hamster.admin.graph;

import java.util.ArrayList;
import java.util.List;

public class ElementaryCyclesSearch {
  /** List of cycles */
  private List<List<Object>> cycles = null;

  /** Adjacency-list of graph */
  private int[][] adjList = null;

  /** Graphnodes */
  private Object[] graphNodes = null;

  /** Blocked nodes, used by the algorithm of Johnson */
  private boolean[] blocked = null;

  /** B-Lists, used by the algorithm of Johnson */
  private List<Integer>[] B = null;

  /** Stack for nodes, used by the algorithm of Johnson */
  private List<Integer> stack = null;

  /**
   * Constructor.
   *
   * @param matrix adjacency-matrix of the graph
   * @param graphNodes array of the graphnodes of the graph; this is used to build sets of the
   *        elementary cycles containing the objects of the original graph-representation
   */
  public ElementaryCyclesSearch(boolean[][] matrix, Object[] graphNodes) {
    this.graphNodes = graphNodes;
    this.adjList = AdjacencyList.getAdjacencyList(matrix);
  }

  /**
   * Returns List::List::Object with the Lists of nodes of all elementary cycles in the graph.
   *
   * @return List::List::Object with the Lists of the elementary cycles.
   */
  public List<List<Object>> getElementaryCycles() {
    this.cycles = new ArrayList<>();
    this.blocked = new boolean[this.adjList.length];
    this.B = new ArrayList[this.adjList.length];
    this.stack = new ArrayList<Integer>();
    StrongConnectedComponents sccs = new StrongConnectedComponents(this.adjList);
    int s = 0;

    while (true) {
      SCCResult sccResult = sccs.getAdjacencyList(s);
      if (sccResult != null && sccResult.getAdjList() != null) {
        List<Integer>[] scc = sccResult.getAdjList();
        s = sccResult.getLowestNodeId();
        for (int j = 0; j < scc.length; j++) {
          if ((scc[j] != null) && (scc[j].size() > 0)) {
            this.blocked[j] = false;
            this.B[j] = new ArrayList<>();
          }
        }

        this.findCycles(s, s, scc);
        s++;
      } else {
        break;
      }
    }

    return this.cycles;
  }

  /**
   * Calculates the cycles containing a given node in a strongly connected component. The method
   * calls itself recursivly.
   *
   * @param v
   * @param s
   * @param adjList adjacency-list with the subgraph of the strongly connected component s is part
   *        of.
   * @return true, if cycle found; false otherwise
   */
  private boolean findCycles(int v, int s, List<Integer>[] adjList) {
    boolean f = false;
    this.stack.add(new Integer(v));
    this.blocked[v] = true;

    for (int i = 0; i < adjList[v].size(); i++) {
      int w = ((Integer) adjList[v].get(i)).intValue();
      // found cycle
      if (w == s) {
        List<Object> cycle = new ArrayList<>();
        for (int j = 0; j < this.stack.size(); j++) {
          int index = ((Integer) this.stack.get(j)).intValue();
          cycle.add(this.graphNodes[index]);
        }
        this.cycles.add(cycle);
        f = true;
      } else if (!this.blocked[w]) {
        if (this.findCycles(w, s, adjList)) {
          f = true;
        }
      }
    }

    if (f) {
      this.unblock(v);
    } else {
      for (int i = 0; i < adjList[v].size(); i++) {
        int w = ((Integer) adjList[v].get(i)).intValue();
        if (!this.B[w].contains(new Integer(v))) {
          this.B[w].add(new Integer(v));
        }
      }
    }

    this.stack.remove(new Integer(v));
    return f;
  }

  /**
   * Unblocks recursivly all blocked nodes, starting with a given node.
   *
   * @param node node to unblock
   */
  private void unblock(int node) {
    this.blocked[node] = false;
    List<Integer> Bnode = this.B[node];
    while (Bnode.size() > 0) {
      Integer w = Bnode.get(0);
      Bnode.remove(0);
      if (this.blocked[w.intValue()]) {
        this.unblock(w.intValue());
      }
    }
  }
}
