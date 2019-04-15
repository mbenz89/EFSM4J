package de.upb.testify.efsm;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;

import java.lang.reflect.Field;

/** @author Manuel Benz created on 26.03.18 */
public class DirectedConnectivityInspector<V, E> extends ConnectivityInspector<V, E> {
  public DirectedConnectivityInspector(Graph<V, E> g) {
    super(g);
    // we have to overwrite the graph field to have a directed graph again
    try {
      Field graph = ConnectivityInspector.class.getDeclaredField("graph");
      graph.setAccessible(true);
      graph.set(this, g);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new UnsupportedOperationException("It seems the api has changed!", e);
    }
  }
}
