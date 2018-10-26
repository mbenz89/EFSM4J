package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.DirectedConnectivityInspector;
import de.upb.testify.efsm.EFSMPath;
import de.upb.testify.efsm.IFeasiblePathAlgo;
import de.upb.testify.efsm.JGraphBasedFPALgo;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.IntegerComponentNameProvider;
import org.jgrapht.io.StringComponentNameProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Computes feasible paths based on an exploded graph where ich node is a feasible configuration of the original EEFSM. Uses
 * Dijkstra's shortest path algorithm to compute the set of shortest paths for the exploded EEFSM.
 *
 * <p>
 * Note: This algorithm is complete.
 *
 * @author Manuel Benz created on 26.03.18
 */
public class GraphExplosionFeasiblePathAlgorithm<State, Parameter, Context>
    extends JGraphBasedFPALgo<State, Parameter, EEFSMContext<Context>, ETransition<State, Parameter, Context>>
    implements IEEFSMFeasiblePathAlgo<State, Parameter, Context> {

  private static final Logger logger = LoggerFactory.getLogger(GraphExplosionFeasiblePathAlgorithm.class);

  /** Maps a state of the EEFSM to all possible configurations in the exploded EEFSM */
  private final Multimap<State, Configuration<State, EEFSMContext<Context>>> stateToConfigs;

  private final DirectedConnectivityInspector<Configuration<State, EEFSMContext<Context>>,
      TransitionWrapper> connectivityInspector;

  private final ShortestPathAlgorithm<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> shortestPath;

  /** Exploded graph where ich node is a configuration of the original eefsm. */
  private DirectedPseudograph<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> explodedEEFSM
      = new DirectedPseudograph<>((s, t) -> {
        throw new UnsupportedOperationException();
      });

  public GraphExplosionFeasiblePathAlgorithm(EEFSM<State, Parameter, Context> eefsm) {
    super(eefsm);
    stateToConfigs = MultimapBuilder.hashKeys(baseGraph.vertexSet().size()).arrayListValues().build();
    Stopwatch sw = Stopwatch.createStarted();
    explode(eefsm);
    logger.debug("Exploding EEFSM took {}. Exploded EEFSM contains {} nodes and {} transitions.", sw,
        explodedEEFSM.vertexSet().size(), explodedEEFSM.edgeSet().size());
    connectivityInspector = new DirectedConnectivityInspector<>(explodedEEFSM);
    shortestPath = new DijkstraShortestPath<>(explodedEEFSM);
  }

  @Override
  /**
   * {@inheritDoc}
   *
   * @return The shortest feasible path from source to target.
   */
  public EEFSMPath<State, Parameter, Context> getPath(Configuration<State, EEFSMContext<Context>> config, State tgt) {
    List<EEFSMPath<State, Parameter, Context>> paths = getPaths(config, tgt);
    return paths == null ? null : Iterables.getFirst(paths, null);
  }

  /**
   * {@inheritDoc}
   *
   * @return The shortest feasible path from current to target.
   */
  @Override
  public EEFSMPath<State, Parameter, Context> getPath(State tgt) {
    return getPath(efsm.getConfiguration(), tgt);
  }

  /**
   * {@inheritDoc}
   *
   * @return A ascending sorted list of shortest feasible path through different configurations, i.e., every returned path is
   *         the shortest feasible path for its terminal configuration but leads to a different terminal configuration that
   *         contains the target state.
   */
  @Override
  public List<EEFSMPath<State, Parameter, Context>> getPaths(State tgt) {
    return getPaths(efsm.getConfiguration(), tgt);
  }

  /**
   * {@inheritDoc}
   *
   * @return A ascending sorted list of shortest feasible path through different configurations, i.e., every returned path is
   *         the shortest feasible path for its terminal configuration but leads to a different terminal configuration that
   *         contains the target state.
   */
  @Override
  public List<EEFSMPath<State, Parameter, Context>> getPaths(Configuration<State, EEFSMContext<Context>> config, State tgt) {
    // the given configuration might not exist in the exploded graph which means there is not even a
    // single path to it.
    if (!explodedEEFSM.containsVertex(config)) {
      return null;
    }

    ShortestPathAlgorithm.SingleSourcePaths<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> paths
        = shortestPath.getPaths(config);

    final List<EEFSMPath<State, Parameter, Context>> res = getPathsForSSSP(tgt, paths);

    return res;
  }

  /**
   * Returns a cached set of feasible path between the current state of the EEFSM and any target. If a path is feasible
   * depends on the semantics of the underlying efsm implementation. The algorithm will assume the current configuration of
   * the efsm as start configuration.
   *
   *
   * @return A {@link SingleSourceShortestPath} instance containing all feasible path from the current EEFSM configuration
   */
  public SingleSourceShortestPath getSingleSourceShortestPath() {
    return getSingleSourceShortestPath(efsm.getConfiguration());
  }

  /**
   * Returns a cached set of feasible path between the given state of the efsm and any target. If a path is feasible depends
   * on the semantics of the underlying efsm implementation. The algorithm will assume given configuration of the efsm as
   * start configuration.
   *
   * <p>
   * Note: The implementation of this interface has to decide which paths to returen, e.g., all feasible path or a subset
   *
   * @param config
   *          The configuration from which a path should be calculated The source configuration for which a single source
   *          shortest path set should be collected
   * @return A {@link SingleSourceShortestPath} instance containing all feasible path from the given EEFSM configuration
   */
  public SingleSourceShortestPath getSingleSourceShortestPath(Configuration<State, EEFSMContext<Context>> config) {
    // the given configuration might not exist in the exploded graph which means there is not even a
    // single path to it.
    if (!explodedEEFSM.containsVertex(config)) {
      return null;
    }

    return new SingleSourceShortestPath(shortestPath.getPaths(config));
  }

  /**
   * Returns a list of all configuration containing the given target state which are reachable by the given
   * {@link ShortestPathAlgorithm.SingleSourcePaths}
   * 
   * @param tgt
   * @param baseSSSP
   * @return
   */
  private List<EEFSMPath<State, Parameter, Context>> getPathsForSSSP(State tgt,
      ShortestPathAlgorithm.SingleSourcePaths<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> baseSSSP) {

    Stopwatch sw = null;
    if (logger.isTraceEnabled()) {
      sw = Stopwatch.createStarted();
    }

    // nodes are used multiple times
    Collection<Configuration<State, EEFSMContext<Context>>> tgtConfigs = stateToConfigs.get(tgt);
    List<EEFSMPath<State, Parameter, Context>> res = new ArrayList<>(tgtConfigs.size());

    for (Configuration<State, EEFSMContext<Context>> tgtConfig : tgtConfigs) {
      GraphPath<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> path = baseSSSP.getPath(tgtConfig);
      if (path != null) {
        res.add(toEFSMPath(path));
      }
    }

    logger.trace("Computing paths from {} to {} resulted in {} paths and took {}", baseSSSP.getSourceVertex(), tgt,
        res.size(), sw);

    if (res.isEmpty()) {
      return null;
    }

    res.sort(Comparator.comparingInt(EFSMPath::getLength));
    return res;
  }

  private EEFSMPath<State, Parameter, Context>
      toEFSMPath(GraphPath<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> path) {
    List<ETransition<State, Parameter, Context>> edges
        = path.getEdgeList().stream().map(e -> e.t).collect(Collectors.toList());
    return new EEFSMPath((EEFSM) efsm, edges);
  }

  @Override
  public boolean pathExists(Configuration<State, EEFSMContext<Context>> config, State tgt) {
    if (!explodedEEFSM.containsVertex(config)) {
      return false;
    }

    for (Configuration<State, EEFSMContext<Context>> tgtConfig : stateToConfigs.get(tgt)) {
      if (connectivityInspector.pathExists(config, tgtConfig)) {
        return true;
      }
    }
    return false;
  }

  private void explode(EEFSM<State, Parameter, Context> eefsm) {
    Set<State> rootNodes
        = baseGraph.vertexSet().stream().filter(v -> baseGraph.inDegreeOf(v) == 0).collect(Collectors.toSet());

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
        // if the graph already contains this node, we just closed a loop and can stop recursion
        // here
        if (newVertex) {
          dfsExplode(newConfig);
        }
      }
    }
  }

  public void explodedGraphToDot(Path outFile) {
    try {
      new DOTExporter(new IntegerComponentNameProvider(), new StringComponentNameProvider<>(),
          new StringComponentNameProvider<>()).exportGraph(explodedEEFSM, outFile.toFile());
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

  /**
   * Single source shortest path implementation for {@link GraphExplosionFeasiblePathAlgorithm}. Caches all shortest path for
   * a given source node and can be useful if multiple have to be queried without re-computation.
   */
  public class SingleSourceShortestPath implements IFeasiblePathAlgo.SingleSourceShortestPath<State, Parameter,
      EEFSMContext<Context>, ETransition<State, Parameter, Context>> {

    private final ShortestPathAlgorithm.SingleSourcePaths<Configuration<State, EEFSMContext<Context>>,
        TransitionWrapper> baseSSSP;

    private final LoadingCache<State, List<EEFSMPath<State, Parameter, Context>>> tgtToPaths
        = CacheBuilder.newBuilder().build(new CacheLoader<State, List<EEFSMPath<State, Parameter, Context>>>() {
          @Override
          public List<EEFSMPath<State, Parameter, Context>> load(State tgt) {
            final List<EEFSMPath<State, Parameter, Context>> pathsForSSSP = getPathsForSSSP(tgt, baseSSSP);

            if (pathsForSSSP == null || pathsForSSSP.isEmpty()) {
              throw new NoSuchElementException();
            }

            return pathsForSSSP;
          }
        });

    private SingleSourceShortestPath(
        ShortestPathAlgorithm.SingleSourcePaths<Configuration<State, EEFSMContext<Context>>, TransitionWrapper> baseSSSP) {
      this.baseSSSP = baseSSSP;
    }

    @Override
    public List<EEFSMPath<State, Parameter, Context>> getPaths(State tgt) {
      try {
        return tgtToPaths.get(tgt);
      } catch (ExecutionException e) {
        if (e.getCause() instanceof NoSuchElementException) {
          return null;
        }
        throw new RuntimeException(e);
      }
    }

    @Override
    public EEFSMPath<State, Parameter, Context> getPath(State tgt) {
      try {
        return Iterables.getFirst(tgtToPaths.get(tgt), null);
      } catch (ExecutionException e) {
        if (e.getCause() instanceof NoSuchElementException) {
          return null;
        }
        throw new RuntimeException(e);
      }
    }

    @Override
    public int getLength(State tgt) {
      final EEFSMPath<State, Parameter, Context> path = getPath(tgt);
      return path != null ? path.getLength() : -1;
    }

    @Override
    public Configuration<State, EEFSMContext<Context>> getSource() {
      return baseSSSP.getSourceVertex();
    }

  }
}
