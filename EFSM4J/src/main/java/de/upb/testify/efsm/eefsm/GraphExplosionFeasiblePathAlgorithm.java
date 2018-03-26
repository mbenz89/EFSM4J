package de.upb.testify.efsm.eefsm;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.EFSMPath;
import de.upb.testify.efsm.JGraphBasedFPALgo;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.IntegerComponentNameProvider;
import org.jgrapht.io.StringComponentNameProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes feasible paths based on an exploded graph where ich node is a feasible configuration of the original EEFSM.
 * Uses Dijkstra's shortest path algorithm to compute the set of shortest paths for the exploded EEFSM.
 * <p>
 * Note: This algorithm is complete.
 *
 * @author Manuel Benz
 * created on 26.03.18
 */
public class GraphExplosionFeasiblePathAlgorithm<State, Parameter, Context> extends JGraphBasedFPALgo<State, Parameter, EEFSMContext<Context>, ETransition<State, Parameter, Context>> {

  private static final Logger logger = LoggerFactory.getLogger(GraphExplosionFeasiblePathAlgorithm.class);

  /**
   * Maps a state of the EEFSM to all possible configurations in the exploded EEFSM
   */
  private final Multimap<State, Configuration<State, EEFSMContext<Context>>> stateToConfigs;
  private final ConnectivityInspector<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> connectivityInspector;
  private final ShortestPathAlgorithm<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> shortestPath;
  /**
   * Exploded graph where ich node is a configuration of the original eefsm.
   */
  private DirectedMultigraph<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> explodedEEFSM = new DirectedMultigraph<>((s, t) -> {
    throw new UnsupportedOperationException();
  });

  public GraphExplosionFeasiblePathAlgorithm(EEFSM<State, Parameter, Context> eefsm) {
    super(eefsm);
    stateToConfigs = MultimapBuilder.hashKeys(baseGraph.vertexSet().size()).arrayListValues().build();
    Stopwatch sw = Stopwatch.createStarted();
    explode(eefsm);
    logger.trace("Exploding EEFSM took {}", sw);
    connectivityInspector = new ConnectivityInspector<>(explodedEEFSM);
    shortestPath = new DijkstraShortestPath<>(explodedEEFSM);
    // explodedGraphToDot(Paths.get("target/exploded.dot"));
  }

  @Override
  /**
   * {@inheritDoc}
   *
   * @return The shortest feasible path from source to target.
   */
  public EFSMPath<State, Parameter, EEFSMContext<Context>, ETransition<State, Parameter, Context>> getPath(Configuration<State, EEFSMContext<Context>> config, State tgt) {
    List<EFSMPath<State, Parameter, EEFSMContext<Context>, ETransition<State, Parameter, Context>>> paths = getPaths(config, tgt);
    return paths == null ? null : Iterables.getFirst(paths, null);
  }

  /**
   * {@inheritDoc}
   *
   * @return The shortest feasible path from current to target.
   */
  @Override
  public EFSMPath<State, Parameter, EEFSMContext<Context>, ETransition<State, Parameter, Context>> getPath(State tgt) {
    return super.getPath(tgt);
  }

  /**
   * {@inheritDoc}
   *
   * @return A ascending sorted list of shortest feasible path through different configurations, i.e., every returned path is the shortest feasible path for its terminal configuration but leads to a different terminal configuration.
   */
  @Override
  public List<EFSMPath<State, Parameter, EEFSMContext<Context>, ETransition<State, Parameter, Context>>> getPaths(State tgt) {
    return super.getPaths(tgt);
  }

  /**
   * {@inheritDoc}
   *
   * @return A ascending sorted list of shortest feasible path through different configurations, i.e., every returned path is the shortest feasible path for its terminal configuration but leads to a different terminal configuration.
   */
  @Override
  public List<EFSMPath<State, Parameter, EEFSMContext<Context>, ETransition<State, Parameter, Context>>> getPaths(Configuration<State, EEFSMContext<Context>> config, State tgt) {
    Collection<Configuration<State, EEFSMContext<Context>>> tgtConfigs = stateToConfigs.get(tgt);
    List<EFSMPath<State, Parameter, EEFSMContext<Context>, ETransition<State, Parameter, Context>>> res = new ArrayList<>(tgtConfigs.size());

    ShortestPathAlgorithm.SingleSourcePaths<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> paths = shortestPath.getPaths(config);

    for (Configuration<State, EEFSMContext<Context>> tgtConfig : tgtConfigs) {
      GraphPath<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> path = paths.getPath(tgtConfig);
      if (path != null) {
        res.add(toEFSMPath(path));
      }
    }

    if (res.isEmpty()) {
      return null;
    }

    res.sort(Comparator.comparingInt(EFSMPath::getLength));
    return res;
  }

  private EFSMPath<State, Parameter, EEFSMContext<Context>, ETransition<State, Parameter, Context>> toEFSMPath(GraphPath<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> path) {
    List<ETransition<State, Parameter, Context>> edges = path.getEdgeList().stream().map(e -> e.t).collect(Collectors.toList());
    return new EEFSMPath((EEFSM) efsm, edges);
  }

  @Override
  public boolean pathExists(Configuration<State, EEFSMContext<Context>> config, State tgt) {
    for (Configuration<State, EEFSMContext<Context>> tgtConfig : stateToConfigs.get(tgt)) {
      if (connectivityInspector.pathExists(config, tgtConfig)) {
        return true;
      }
    }
    return false;
  }

  private void explode(EEFSM<State, Parameter, Context> eefsm) {
    Set<State> rootNodes = baseGraph.vertexSet().stream().filter(v -> baseGraph.inDegreeOf(v) == 0).collect(Collectors.toSet());

    Configuration<State, EEFSMContext<Context>> initialConfig = eefsm.getInitialConfiguration();
    Sets.SetView<State> rootNodesWithInitialState = Sets.union(Collections.singleton(initialConfig.getState()), rootNodes);

    for (State root : rootNodesWithInitialState) {
      Configuration<State, EEFSMContext<Context>> newConfig = new Configuration<>(root, initialConfig.getContext());
      explodedEEFSM.addVertex(newConfig);
      dfsExplode(newConfig);
    }
  }

  private void dfsExplode(Configuration<State, EEFSMContext<Context>> curConfig) {
    State curState = curConfig.getState();
    stateToConfigs.put(curState, curConfig);

    for (ETransition<State, Parameter, Context> t : baseGraph.outgoingEdgesOf(curState)) {
      EEFSMContext<Context> curContext = curConfig.getContext();
      // only further explode if the transition is viable in the current context
      if (t.domainGuard(curContext)) {
        EEFSMContext<Context> contextSnapshot = curContext.snapshot();
        t.operation(t.getExpectedInput(), contextSnapshot);
        Configuration<State, EEFSMContext<Context>> newConfig = new Configuration<>(t.getTgt(), contextSnapshot);
        boolean newVertex = explodedEEFSM.addVertex(newConfig);
        explodedEEFSM.addEdge(curConfig, newConfig, new TransitionWrapper(t));
        // if the graph already contains this node, we just closed a loop and can stop recursion here
        if (newVertex) {
          dfsExplode(newConfig);
        }
      }
    }
  }

  public void explodedGraphToDot(Path outFile) {
    try {
      new DOTExporter(
          new IntegerComponentNameProvider(),
          new StringComponentNameProvider<>(),
          new StringComponentNameProvider<>()
      ).exportGraph(explodedEEFSM, outFile.toFile());
    } catch (ExportException e) {
      throw new RuntimeException(e);
    }
  }


  private final class TransitionWrapper {
    private final ETransition<State, Parameter, Context> t;

    public TransitionWrapper(ETransition<State, Parameter, Context> t) {
      this.t = t;
    }

    @Override
    public String toString() {
      return Objects.toString(t.getExpectedInput(), "-");
    }
  }
}
