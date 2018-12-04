package de.upb.testify.efsm;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import de.upb.testify.efsm.eefsm.EEFSM;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.primitives.Primitives;
import com.mxgraph.layout.mxEdgeLabelLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.sun.javafx.collections.ObservableListWrapper;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.swing.*;

import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.textfield.CustomTextField;
import org.jgrapht.ext.JGraphXAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/** @author Manuel Benz created on 01.02.18 */
public class EFSMDebugger<State, Transition extends de.upb.testify.efsm.Transition<State, ?, ?>> extends Application
    implements PropertyChangeListener {
  // region fields and constants
  public static final float STROKE_WIDTH_HIGHLIGHTED = 4f;
  public static final Dimension TOOLBAR_BUTTON_SIZE = new Dimension(20, 20);
  public static final Color COLOR_ROOT_VERTEX = Color.BLUE;
  public static final Color COLOR_LAST_VERTEX = Color.RED;
  public static final Color COLOR_CUR_VERTEX = Color.SPRINGGREEN;
  public static final Color COLOR_TARGET_VERTEX = Color.GREEN;
  private static final Logger logger = LoggerFactory.getLogger(EFSMDebugger.class);
  private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(1200, 800);
  private static EFSMDebugger instance;
  private Label statusPanel;
  private StatusBar detailsPanel;
  private mxGraphComponent graphComponent;
  private MyJGraphXAdapter<State, Transition> jgxAdapter;
  private mxICell lastState;
  private String savedStyleCurState, savedStyleLastState, savedStyleEdgeTaken;
  private Map<mxICell, String> haltingStates = new HashMap<>();
  private mxICell edgeTaken;
  private boolean controlMode, executeStep;
  private Button toCur;
  private Button toLast;
  private Button playButton;
  private Button pauseButton;
  private Button stepButton;
  private Stage primaryStage;
  private Function<Object, String> stateLabeler;
  private Function<Object, String> transitionLabeler;
  private mxICell curState;
  private boolean initialized;
  private EFSMPath<State, ?, ?, Transition> highlightedPath;
  private NotificationPane notificationPane;
  private TreeItem<Object> invisibleRoot;
  private EFSM<State, ?, ?, Transition> efsm;

  // endregion

  // region constructors

  public EFSMDebugger() {
    instance = this;
  }

  // endregion

  // region setup

  public static synchronized <State, Transition extends de.upb.testify.efsm.Transition<State, ?, ?>>
      EFSMDebugger<State, Transition> startDebugger(EFSM<State, ?, ?, Transition> efsm, boolean startInControlMode) {
    return startDebugger(efsm, startInControlMode, state -> state.toString(), transition -> transition.toString());
  }

  public static synchronized <State, Transition extends de.upb.testify.efsm.Transition<State, ?, ?>>
      EFSMDebugger<State, Transition> startDebugger(EFSM<State, ?, ?, Transition> efsm, boolean startInControlMode,
          Function<State, String> stateLabeler, Function<Transition, String> transitionLabeler) {
    new Thread(() -> {
      // Have to run in a thread because launch doesn't return
      Application.launch(EFSMDebugger.class);
    }).start();

    while (instance == null) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    Platform.runLater(() -> instance.init(efsm, startInControlMode, stateLabeler, transitionLabeler));

    // wait for initialization to finish
    while (!instance.initialized) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return instance;
  }

  private void init(EFSM<State, ?, ?, Transition> efsm, boolean startInControlMode, Function<State, String> stateLabeler,
      Function<Transition, String> transitionLabeler) {
    this.efsm = efsm;

    logger.debug("Starting up efsm debugger...");
    Stopwatch sw = Stopwatch.createStarted();

    BorderPane borderPane = new BorderPane();
    borderPane.setTop(initToolBar());
    borderPane.setBottom(initStatusBar());

    this.stateLabeler = o -> o instanceof String ? o.toString() : stateLabeler.apply((State) o);
    this.transitionLabeler = o -> o instanceof String ? o.toString() : transitionLabeler.apply((Transition) o);
    this.controlMode = startInControlMode;

    efsm.addPropertyChangeListener(this);
    jgxAdapter = new MyJGraphXAdapter(efsm);
    jgxAdapter.layout();
    graphComponent = new mxGraphComponent(jgxAdapter);
    graphComponent.setToolTips(true);
    graphComponent.setDragEnabled(false);
    setupScrolling();
    setupZooming();
    setupHaltOnNode();

    notificationPane = new NotificationPane(createSwingNode(graphComponent));
    setupSearchBar();
    SplitPane jSplitPane = new SplitPane(notificationPane, setupPropertiesPanel());
    jSplitPane.setOrientation(Orientation.VERTICAL);
    borderPane.setCenter(jSplitPane);
    Platform.runLater(() -> jSplitPane.setDividerPosition(0, 0.8));

    primaryStage.setTitle("EFSM debugger");
    primaryStage.setScene(new Scene(borderPane, DEFAULT_WINDOW_SIZE.width, DEFAULT_WINDOW_SIZE.height));
    primaryStage.sizeToScene();
    primaryStage.show();

    setCurrentConfig(efsm.getInitialConfiguration());
    graphComponent.scrollCellToVisible(curState, true);

    logger.debug("Creating debugger took {}", sw);
    initialized = true;
    status("Waiting for input");
    info("");
  }

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  // endregion

  // region state

  private Node createSwingNode(JComponent jComponent) {
    SwingNode sn = new SwingNode();
    SwingUtilities.invokeLater(() -> sn.setContent(jComponent));
    return sn;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(EEFSM.PROP_CONFIGURATION)) {
      // halt execution if we are in a debugging state
      if (haltingStates.containsKey(curState)) {
        controlMode = true;
      }

      // if we are in controlled mode, the play and step button have to be reenabled to allow to
      // control the next action
      if (controlMode) {
        playButton.setDisable(false);
        stepButton.setDisable(false);
        pauseButton.setDisable(true);
        status("Paused");
      } else {
        stepButton.setDisable(true);
        playButton.setDisable(true);
      }

      executeStep = false;

      haltExecution();

      mxIGraphModel model = jgxAdapter.getModel();
      model.beginUpdate();

      Configuration<State, Context> lastConfig = (Configuration<State, Context>) evt.getOldValue();
      Pair<Configuration<State, Context>, Transition> newValue
          = (Pair<Configuration<State, Context>, Transition>) evt.getNewValue();
      Configuration<State, Context> newConfig = newValue.getKey();

      if (lastState != null) {
        model.setStyle(lastState, new SB(lastState).setFrom(savedStyleLastState, mxConstants.STYLE_FILLCOLOR).build());
      }

      lastState = jgxAdapter.getVertexToCellMap().get(lastConfig.getState());

      if (lastState != null) {
        savedStyleLastState = savedStyleCurState;
        model.setStyle(lastState, new SB(lastState).set(mxConstants.STYLE_FILLCOLOR, COLOR_LAST_VERTEX).build());
        toLast.setDisable(false);
      }

      setCurrentConfig(newConfig);

      // also highlight edge

      if (edgeTaken != null) {
        model.setStyle(edgeTaken, new SB(edgeTaken).setFrom(savedStyleEdgeTaken, mxConstants.STYLE_STROKEWIDTH).build());
      }

      Transition transition = newValue.getValue();

      if (transition != null) {
        edgeTaken = jgxAdapter.getEdgeToCellMap().get(transition);
        savedStyleEdgeTaken = Strings.nullToEmpty(edgeTaken.getStyle());
        model.setStyle(edgeTaken,
            new SB(edgeTaken).set(mxConstants.STYLE_STROKEWIDTH, String.valueOf(STROKE_WIDTH_HIGHLIGHTED)).build());
      } else {
        info("Uncovered state change conducted!");
      }

      model.endUpdate();

      graphComponent.scrollCellToVisible(curState, true);

      info("");
    }
  }

  private void setCurrentConfig(Configuration<State, ?> newConfig) {
    curState = jgxAdapter.getVertexToCellMap().get(newConfig.getState());

    savedStyleCurState = curState.getStyle();
    jgxAdapter.setCellStyle(new SB(curState).set(mxConstants.STYLE_FILLCOLOR, COLOR_CUR_VERTEX).build(),
        new mxICell[] { curState });
    toCur.setDisable(false);

    // show the new context in the properties window
    Platform.runLater(() -> invisibleRoot.getChildren().set(0, createTreeItem(newConfig.getContext())));
  }

  private void haltExecution() {
    logger.trace("Putting thread to sleep.");
    while (controlMode && !executeStep) {
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    logger.trace("Resuming thread after pause.");
  }

  /**
   * Highlights the given path (resets the previous highlighting)
   * 
   * @param path
   *          The path to highlight or null remove path highlighting all together
   */
  public void highlightPath(EFSMPath<State, ?, ?, Transition> path) {
    HashMap<Transition, mxICell> edgeToCellMap = jgxAdapter.getEdgeToCellMap();

    mxIGraphModel model = jgxAdapter.getModel();

    model.beginUpdate();

    // reset highlighting
    if (highlightedPath != null) {
      for (Transition transition : highlightedPath) {
        mxICell mxICell = edgeToCellMap.get(transition);
        model.setStyle(mxICell, new SB(mxICell).set(mxConstants.STYLE_STROKECOLOR, Color.BLACK).build());
      }
    }

    highlightedPath = path;

    if (path != null) {
      for (Transition transition : path) {
        mxICell mxICell = edgeToCellMap.get(transition);
        model.setStyle(mxICell, new SB(mxICell).set(mxConstants.STYLE_STROKECOLOR, Color.BLUE).build());
      }
    }

    model.endUpdate();
  }

  /**
   * Can be used to highlight important states from client side. (No unhighlighting supported yet.)
   *
   * @param states
   */
  public void highlightStates(State... states) {
    HashMap<State, mxICell> vertexToCellMap = jgxAdapter.getVertexToCellMap();

    mxIGraphModel model = jgxAdapter.getModel();
    model.beginUpdate();
    for (State state : states) {
      mxICell mxICell = vertexToCellMap.get(state);
      model.setStyle(mxICell, new SB(mxICell).set(mxConstants.STYLE_STROKECOLOR, COLOR_TARGET_VERTEX)
          .set(mxConstants.STYLE_STROKEWIDTH, String.valueOf(STROKE_WIDTH_HIGHLIGHTED)).build());
    }

    model.endUpdate();
  }

  // endregion

  // region toolbar

  /*
   * public void acceptPath() { mxIGraphModel model = jgxAdapter.getModel(); model.beginUpdate();
   * 
   * for (Pair<mxICell, String> sp : shortestPathCells) { mxICell cell = sp.getLeft(); model.setStyle(cell, new
   * SB(cell).setFrom(sp.getRight(), mxConstants.STYLE_STROKEWIDTH).build()); }
   * 
   * shortestPathCells.clear();
   * 
   * for (ExplorationEvent event : shortestPath.getEdgeList()) { mxICell mxICell = jgxAdapter.getEdgeToCellMap().get(event);
   * shortestPathCells.add(Pair.of(mxICell, mxICell.getStyle()));
   * 
   * model.setStyle(mxICell, new SB(mxICell).set(mxConstants.STYLE_STROKEWIDTH, STROKE_WIDTH_HIGHLIGHTED).build()); }
   * model.endUpdate(); }
   */

  private ToolBar initToolBar() {
    ToolBar toolBar = new ToolBar();
    playButton
        = addButton(toolBar, event -> performPlay(), MaterialDesignIcon.PLAY, "(Re)starts the continuous exploration");
    pauseButton = addButton(toolBar, event -> performPause(), MaterialDesignIcon.PAUSE,
        "Pauses the exploration and starts the stepping mode");
    stepButton = addButton(toolBar, event -> performStep(), MaterialDesignIcon.STEP_FORWARD,
        "Executes the next state transition in stepping mode");
    toolBar.getItems().add(new Separator());
    addButton(toolBar, event -> {
    }, MaterialDesignIcon.UNDO, "Undoes the last transition visually (internal state is not affected)");
    addButton(toolBar, event -> {
    }, MaterialDesignIcon.REDO, "Redoes the last transition visually (internal state is not affected)");
    toolBar.getItems().add(new Separator());
    toCur = addButton(toolBar, "CS", event -> graphComponent.scrollCellToVisible(curState, true), COLOR_CUR_VERTEX,
        "Scrolls the current state into view");
    toLast = addButton(toolBar, "LS", event -> graphComponent.scrollCellToVisible(lastState, true), COLOR_LAST_VERTEX,
        "Scrolls the last state into view");
    toolBar.getItems().add(new Separator());
    addButton(toolBar, event -> showSearchBar(), MaterialDesignIcon.MAGNIFY, "Opens the search panel").setDisable(false);
    return toolBar;
  }

  private void showSearchBar() {
    if (notificationPane.isShowing()) {
      notificationPane.hide();
    } else {
      notificationPane.show();
    }
  }

  private Button addButton(ToolBar toolBar, String label, EventHandler<javafx.event.ActionEvent> handler,
      javafx.scene.paint.Color color, String toolTip) {
    Button button = new Button(label);
    button.setOnAction(handler);
    button.setMinSize(TOOLBAR_BUTTON_SIZE.width, TOOLBAR_BUTTON_SIZE.height);
    button.setStyle(String.format(
        "-fx-font: 21 arial; -fx-padding: 2; -fx-border-insets: 0; -fx-border-width: 1; -fx-font-weight: bold; -fx-base: %s;",
        color.toString().replace("0x", "#")));
    button.setDisable(true);
    button.setTooltip(new Tooltip(toolTip));
    toolBar.getItems().add(button);
    return button;
  }

  private Button addButton(ToolBar jToolBar, EventHandler<javafx.event.ActionEvent> actionCommand, MaterialDesignIcon icon,
      String toolTip) {
    Button but = new Button();
    but.setGraphic(new MaterialDesignIconView(icon, "" + TOOLBAR_BUTTON_SIZE.height));
    but.setOnAction(actionCommand);
    but.setTooltip(new Tooltip(toolTip));
    but.setMinSize(TOOLBAR_BUTTON_SIZE.width, TOOLBAR_BUTTON_SIZE.height);
    but.setDisable(true);
    jToolBar.getItems().add(but);
    return but;
  }

  private void performStep() {
    status("Performing step");
    executeStep = true;
    playButton.setDisable(true);
    stepButton.setDisable(true);
  }

  private void performPause() {
    status("Paused");
    controlMode = true;
    executeStep = false;
    playButton.setDisable(false);
    pauseButton.setDisable(true);
    stepButton.setDisable(false);
  }

  private void performPlay() {
    status("Running");
    controlMode = false;
    executeStep = false;
    playButton.setDisable(true);
    pauseButton.setDisable(false);
    stepButton.setDisable(true);
  }

  // endregion

  // region StatusBar

  private StatusBar initStatusBar() {
    // create the status bar panel and shove it down the bottom of the frame
    StatusBar statusBar = new StatusBar();
    // statusBar.setPrefSize(new Dimension(this..getWidth(), 24));
    detailsPanel = statusBar;
    this.statusPanel = new Label();
    statusBar.getRightItems().add(statusPanel);
    return statusBar;
  }

  private void status(String msg) {
    Platform.runLater(() -> statusPanel.setText(msg + "  "));
  }

  public void info(String msg) {
    Platform.runLater(() -> detailsPanel.setText(msg));
  }

  // endregion

  // region control

  private void setupHaltOnNode() {
    graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          Object cellAt = graphComponent.getCellAt(e.getX(), e.getY());
          // for now we just allow to halt on nodes. if we see need, it woult be easy to
          // extend to halt on a specific transition
          if (cellAt != null) {
            mxICell cell = (mxICell) cellAt;
            if (cell.isVertex()) {
              mxICell[] mxICells = { cell };
              if (haltingStates.containsKey(cellAt)) {
                // remove visual feedback
                jgxAdapter.setCellStyle(new SB(cell)
                    .setFrom(haltingStates.get(cellAt), mxConstants.STYLE_SHADOW, mxConstants.STYLE_SHAPE).build(),
                    mxICells);
                haltingStates.remove(cellAt);
              } else {
                // add visual feedback
                haltingStates.put(cell, cell.getStyle());
                jgxAdapter.setCellStyle(new SB(cell).set(mxConstants.STYLE_SHADOW, true)
                    .set(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_DOUBLE_ELLIPSE).build(), mxICells);
              }
            }
          }
        }
      }
    });
  }

  private void setupZooming() {
    graphComponent.getGraphControl().addMouseWheelListener(e -> {
      if (e.getWheelRotation() < 0) {
        graphComponent.zoomIn();
      } else {
        graphComponent.zoomOut();
      }
    });
  }

  private void setupScrolling() {
    graphComponent.setAutoscrolls(true);
    graphComponent.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

    MouseAdapter ma = new MouseAdapter() {

      private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
      private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
      private final Point pp = new Point();

      @Override
      public void mouseDragged(MouseEvent e) {
        final JComponent jc = (JComponent) e.getSource();
        Container c = jc.getParent();
        if (c instanceof JViewport) {
          JViewport vport = (JViewport) c;
          Point cp = SwingUtilities.convertPoint(jc, e.getPoint(), vport);
          Point vp = vport.getViewPosition();
          vp.translate(pp.x - cp.x, pp.y - cp.y);
          jc.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
          pp.setLocation(cp);
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {
        JComponent jc = (JComponent) e.getSource();
        Container c = jc.getParent();
        if (c instanceof JViewport) {
          jc.setCursor(hndCursor);
          JViewport vport = (JViewport) c;
          Point cp = SwingUtilities.convertPoint(jc, e.getPoint(), vport);
          pp.setLocation(cp);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        ((JComponent) e.getSource()).setCursor(defCursor);
      }
    };

    graphComponent.getGraphControl().addMouseListener(ma);
    graphComponent.getGraphControl().addMouseMotionListener(ma);
  }

  // endregion

  // region properties panel

  private Control setupPropertiesPanel() {
    TreeTableView<Object> treeTable = new TreeTableView<>();

    TreeTableColumn<Object, Object> property = column("Property", o -> {
      Object value = o.getValue();
      if (value == treeTable.getRoot().getValue()) {
        return String.valueOf(value.getClass());
      } else if (value instanceof Field) {
        return ((Field) value).getName();
      }
      return String.valueOf(value);
    });

    property.prefWidthProperty().bind(treeTable.widthProperty().multiply(0.3));

    TreeTableColumn<Object, Object> value = column("Value", new Function<TreeItem<Object>, Object>() {
      @Override
      public Object apply(TreeItem<Object> o) {
        Object val = o.getValue();
        if (val instanceof Field) {
          try {
            Field field = (Field) val;

            Object parentVal = getParentValue(o);

            Object fieldVal = field.get(parentVal);
            if (field.getType().isArray()) {
              return Arrays.toString(unpack(fieldVal));
            } else {
              return String.valueOf(fieldVal);
            }
          } catch (IllegalAccessException e) {
            return "-";
          }
        }
        return String.valueOf(val);
      }

      private Object[] unpack(final Object value) {
        if (value == null) {
          return null;
        }
        if (value.getClass().isArray()) {
          if (value instanceof Object[]) {
            return (Object[]) value;
          } else // box primitive arrays
          {
            final Object[] boxedArray = new Object[Array.getLength(value)];
            for (int index = 0; index < boxedArray.length; index++) {
              boxedArray[index] = Array.get(value, index); // automatic boxing
            }
            return boxedArray;
          }
        } else {
          throw new IllegalArgumentException("Not an array");
        }
      }
    });

    // width should probably grow and shrink with content length
    value.prefWidthProperty().bind(treeTable.widthProperty().multiply(2));

    treeTable.getColumns().addAll(property, value);

    invisibleRoot = new TreeItem<>();
    treeTable.setRoot(invisibleRoot);
    treeTable.setShowRoot(false);

    invisibleRoot.getChildren().add(0, createTreeItem(efsm.getInitialConfiguration().getContext()));
    final TreeItem<Object> placeHolder = new TreeItem<>("");
    invisibleRoot.getChildren().add(1, placeHolder);

    graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        Object cell = graphComponent.getCellAt(e.getX(), e.getY());

        if (cell != null) {
          mxICell mxCell = (mxICell) cell;
          final Object newItem;
          if (mxCell.isVertex()) {
            newItem = jgxAdapter.getCellToVertexMap().get(mxCell);
          } else if (mxCell.isEdge()) {
            newItem = jgxAdapter.getCellToEdgeMap().get(mxCell);
          } else {
            newItem = "";
          }
          final ObservableList<TreeItem<Object>> children = invisibleRoot.getChildren();
          Platform.runLater(() -> children.set(1, createTreeItem(newItem)));
        }
      }
    });


    return treeTable;
  }

  private TreeItem<Object> createTreeItem(Object value) {
    TreeItem<Object> item = new TreeItem<Object>(value) {

      private boolean childrenComputed = false;

      {
        expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
          if (!isNowExpanded) { // remove child nodes...
            super.getChildren().clear();
            childrenComputed = false;
          }
        });
      }

      @Override
      public ObservableList<TreeItem<Object>> getChildren() {
        List<TreeItem<Object>> children = new ArrayList<>();
        Object value = getValue();
        if (!childrenComputed) {
          Class<?> aClass;
          if (!(value instanceof Field)) {
            aClass = value.getClass();
          } else {
            try {
              Object parentVal = getParentValue(this);

              value = ((Field) value).get(parentVal);
              aClass = value.getClass();
            } catch (IllegalAccessException e) {
              throw new RuntimeException();
            }
          }

          while (aClass != null) {
            // we do not care about fields of collections and arrays but their contents
            if (Iterable.class.isAssignableFrom(aClass)) {
              for (Object o : Iterable.class.cast(value)) {
                children.add(createTreeItem(o));
              }
            } else if (aClass.isArray()) {
              int length = Array.getLength(value);
              for (int i = 0; i < length; i++) {
                Object arrayElement = Array.get(value, i);
                children.add(createTreeItem(arrayElement));
              }
            } else {
              for (Field field : aClass.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                  field.setAccessible(true);
                  children.add(createTreeItem(field));
                }
              }
            }
            aClass = aClass.getSuperclass();
          }
          childrenComputed = true;
          // we have to add all childrens here together after we set the childrenComputed flag
          // to prevent a stackoverflow execption on double clickig an item
          // it will recurse into the same items again but stop due to childrenComputed flag set
          super.getChildren().addAll(children);
        }
        return super.getChildren();
      }

      @Override
      public boolean isLeaf() {

        final Object value = getValue();
        try {
          return isLeaf(value);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
          return true;
        }

        // return !isValueOfInterest(getValue());
      }

      private boolean isLeaf(Object value) throws IllegalAccessException {
        if (value == null) {
          return true;
        }

        final Class<?> aClass = value.getClass();

        if (aClass.isPrimitive() || Primitives.isWrapperType(aClass)) {
          return true;
        }

        if (value instanceof Field) {
          final Object parentValue = getParentValue(this);

          final Object fieldVal = ((Field) value).get(parentValue);
          return isLeaf(fieldVal);
        }

        return false;
      }
    };

    return item;
  }

  private Object getParentValue(TreeItem item) throws IllegalAccessException {
    Object parentVal = item.getParent().getValue();
    if (parentVal instanceof Field) {
      final Object grandParentVal = getParentValue(item.getParent());
      parentVal = ((Field) parentVal).get(grandParentVal);
    }
    return parentVal;
  }

  private <S, T> TreeTableColumn<S, T> column(String title, Function<TreeItem<S>, T> property) {
    TreeTableColumn<S, T> column = new TreeTableColumn<>(title);
    column.setCellValueFactory(cellData -> new SimpleObjectProperty<T>(property.apply(cellData.getValue())));
    column.setPrefWidth(200);
    return column;
  }

  // endregion

  // region style helpers
  private static final class SB {
    private final Map<String, String> styleMap;
    private final boolean useDefaultMapping;

    public SB() {
      this(true);
    }

    public SB(boolean useDefaultMapping) {
      this.styleMap = new HashMap<>();
      this.useDefaultMapping = useDefaultMapping;
    }

    /**
     * Use an existing style to build a new one
     *
     * @param base
     */
    public SB(mxICell base) {
      String baseStyle = Strings.nullToEmpty(base.getStyle());
      this.styleMap = styleToMap(baseStyle);
      this.useDefaultMapping = !baseStyle.startsWith(";");
    }

    private static String colorToString(Color color) {
      return "#" + colorChanelToHex(color.getRed()) + colorChanelToHex(color.getGreen()) + colorChanelToHex(color.getBlue());
    }

    private static String colorChanelToHex(double chanelValue) {
      String rtn = Integer.toHexString((int) Math.min(Math.round(chanelValue * 255), 255));
      if (rtn.length() == 1) {
        rtn = "0" + rtn;
      }
      return rtn;
    }

    public SB set(String attribute, Object value) {
      styleMap.put(attribute, value.toString());
      return this;
    }

    public SB set(String attribute, Color value) {
      styleMap.put(attribute, colorToString(value));
      return this;
    }

    /**
     * Sets all given attributes from the given style string. If the given style does not contain a given attribute, it is
     * removed.
     *
     * @param baseStyle
     * @param attributes
     * @return
     */
    public SB setFrom(String baseStyle, String... attributes) {
      Map<String, String> baseStyleMap = styleToMap(baseStyle);

      for (String attribute : attributes) {
        String value = baseStyleMap.get(attribute);
        if (value == null) {
          styleMap.remove(attribute);
        } else {
          styleMap.put(attribute, value);
        }
      }

      return this;
    }

    public String build() {
      return (useDefaultMapping ? "" : ";") + mapToStyle(styleMap);
    }

    private String mapToStyle(Map<String, String> curStyleMap) {
      return curStyleMap.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(";"));
    }

    private Map<String, String> styleToMap(String style) {
      return Arrays.stream(style.split(";")).filter(s -> !s.isEmpty()).map(kv -> kv.split("="))
          .collect(Collectors.toMap(e -> e[0], e -> e[1]));
    }
  }
  // endregion

  // region JGraphXAdapter

  private final class MyJGraphXAdapter<State, Transition extends de.upb.testify.efsm.Transition<State, ?, ?>>
      extends JGraphXAdapter<State, Transition> {

    public MyJGraphXAdapter(EFSM<State, ?, ?, Transition> efsm) {
      super(efsm.getBaseGraph());

      ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
      setAutoSizeCells(true);
      setAutoOrigin(true);
      setCellsDeletable(false);
      setCellsEditable(false);

      initGraphStyle(efsm.getConfiguration().getState());
    }

    // region labeling

    @Override
    public String getToolTipForCell(Object cell) {
      // tool tipp is the full blown information

      // we need to wrap it in html to allow linebreaks
      return "<html>" + model.getValue(cell).toString().replaceAll("\n", "<br>") + "</html>";
    }

    @Override
    public String convertValueToString(Object cell) {
      mxICell mxCell = (mxICell) cell;
      if (mxCell.isVertex()) {
        return stateLabeler.apply(mxCell.getValue());
      } else {
        return transitionLabeler.apply(mxCell.getValue());
      }
    }
    // endregion

    // region graph style

    private void initGraphStyle(State root) {
      model.beginUpdate();

      // set default stylesheet
      setDefaultVertexStyle();
      setDefaultEdgeStyle();

      setSpecialVertexProperties(root);
      setSpecialEdgeProperties();

      model.endUpdate();
    }

    private void setDefaultEdgeStyle() {
      Map<String, Object> style = getStylesheet().getDefaultEdgeStyle();
      style.put(mxConstants.STYLE_STROKECOLOR, "black");
      style.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW);
      style.put(mxConstants.STYLE_ROUNDED, true);
      style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
      style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_TOP);
      style.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
      style.put(mxConstants.STYLE_VERTICAL_LABEL_POSITION, mxConstants.ALIGN_LEFT);
      style.put(mxConstants.STYLE_LABEL_POSITION, mxConstants.ALIGN_LEFT);
      style.put(mxConstants.STYLE_FONTSIZE, 6);
    }

    private void setDefaultVertexStyle() {
      Map<String, Object> style = getStylesheet().getDefaultVertexStyle();
      style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
      style.put(mxConstants.STYLE_FONTCOLOR, "black");
      style.put(mxConstants.STYLE_STROKECOLOR, "black");
      style.put(mxConstants.STYLE_FILLCOLOR, "white");
      style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
    }

    private void setSpecialEdgeProperties() {
      for (Map.Entry<Transition, mxICell> edge : getEdgeToCellMap().entrySet()) {
        Transition mapEdge = edge.getKey();
        mxICell graphEdge = edge.getValue();
        setSpecialEdgeProperty(mapEdge, graphEdge);
      }
    }

    private void setSpecialEdgeProperty(Transition mapEdge, mxICell graphEdge) {
      // SB sb = new SB();
      //
      // if (!mapEdge.isActive()) {
      // sb.set(mxConstants.STYLE_DASHED, true);
      // }
      //
      // if (EventUtils.getTrigger(mapEdge.getBaseEvent(), BackPressed.class).isPresent()) {
      // sb.set(mxConstants.STYLE_STROKECOLOR, "#a0522d");
      // } else if (EventUtils.getTrigger(mapEdge.getBaseEvent(),
      // UiInteraction.class).isPresent()) {
      // sb.set(mxConstants.STYLE_STROKECOLOR, "blue");
      // } else if (mapEdge.getBaseEvent() instanceof TautologicEvent) {
      // sb.set(mxConstants.STYLE_STROKECOLOR, "green");
      // } else if (mapEdge.getBaseEvent() instanceof ImpossibleEvent) {
      // sb.set(mxConstants.STYLE_STROKECOLOR, "red");
      // }
      //
      // model.setStyle(graphEdge, sb.build());
    }

    private void setSpecialVertexProperties(State root) {
      for (Map.Entry<State, mxICell> node : getVertexToCellMap().entrySet()) {
        State mapNode = node.getKey();
        mxICell graphNode = node.getValue();
        setSpecialVertexProperty(mapNode, graphNode);
      }

      model.setStyle(getVertexToCellMap().get(root), new SB().set(mxConstants.STYLE_STROKECOLOR, COLOR_ROOT_VERTEX)
          .set(mxConstants.STYLE_STROKEWIDTH, STROKE_WIDTH_HIGHLIGHTED).build());
    }

    private void setSpecialVertexProperty(State mapNode, mxICell graphNode) {
      SB sb = new SB();

      // if (!(mapNode.getLoi() instanceof WayPoint)) {
      // // this is a target loi
      // sb.set(mxConstants.STYLE_STROKECOLOR, COLOR_LOI_VERTEX);
      // sb.set(mxConstants.STYLE_STROKEWIDTH, String.valueOf(STROKE_WIDTH_HIGHLIGHTED));
      // }

      model.setStyle(graphNode, sb.build());

      updateCellSize(graphNode);
      adjustCellHeight(graphNode);
    }

    private void adjustCellHeight(mxICell cell) {
      mxGeometry g = (mxGeometry) cell.getGeometry().clone();
      mxRectangle bounds = getView().getState(cell).getLabelBounds();
      g.setHeight(bounds.getHeight() + 25); // 15 is for padding
      cellsResized(new Object[] { cell }, new mxRectangle[] { g });
    }

    private void layout() {
      Stopwatch sw = Stopwatch.createStarted();

      /*
       * mxHierarchicalLayout mxHierarchicalLayout = new mxHierarchicalLayout(this);
       * mxHierarchicalLayout.setDisableEdgeStyle(false); mxHierarchicalLayout.setFineTuning(true);
       * mxHierarchicalLayout.execute(getDefaultParent());
       */

      // define layout
      mxFastOrganicLayout layout = new mxFastOrganicLayout(this);

      // set some properties
      final int maxLabelLength
          = getVertexToCellMap().values().stream().mapToInt(c -> convertValueToString(c).length()).max().orElse(0);
      final int forceConst = Math.max(200, maxLabelLength * 2);

      layout.setForceConstant(forceConst); // the higher, the more separated
      layout.setResetEdges(true);
      layout.setDisableEdgeStyle(true); // true transforms the edges and makes them direct lines
      final double maxIterations = 200.0d * Math.sqrt(getVertexToCellMap().size());
      layout.setMaxIterations(maxIterations);

      logger.trace("Setting force constant to {}", forceConst);
      logger.trace("Setting max iterations to {}", maxIterations);

      // layout graph
      layout.execute(this.getDefaultParent());

      mxParallelEdgeLayout parallelEdgeLayout = new mxParallelEdgeLayout(this);
      parallelEdgeLayout.execute(this.getDefaultParent());

      mxEdgeLabelLayout labelLayout = new mxEdgeLabelLayout(this);
      labelLayout.execute(this.getDefaultParent());

      logger.trace("Layouting graph took {}", sw);
    }
    // endregion
  }
  // endregion

  // region Searchbar

  private void setupSearchBar() {
    CustomTextField searchField = new CustomTextField();
    searchField.setLeft(new MaterialDesignIconView(MaterialDesignIcon.MAGNIFY, "20"));
    final Button lastEntry = new Button();
    lastEntry.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.CHEVRON_UP, "20"));
    lastEntry.setBorder(Border.EMPTY);
    lastEntry.setDisable(true);
    final Button nextEntry = new Button();
    nextEntry.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.CHEVRON_DOWN, "20"));
    nextEntry.setBorder(Border.EMPTY);
    nextEntry.setDisable(true);
    final Label resultLabel = new Label();
    resultLabel.setFont(new Font("arial", 15));
    final HBox hBox = new HBox(searchField, lastEntry, nextEntry, resultLabel);
    hBox.setAlignment(Pos.CENTER_LEFT);
    hBox.setPadding(new Insets(0, 2, 0, 7));
    notificationPane.setGraphic(hBox);
    notificationPane.setCloseButtonVisible(true);

    final EFSMDebugger.SearchModel searchModel = new EFSMDebugger.SearchModel();
    searchModel.searchTerm().bind(searchField.textProperty());
    searchField.onActionProperty().bind(searchModel.conductSearch());
    resultLabel.textProperty().bind(searchModel.result());
    lastEntry.disableProperty().bind(searchModel.hasPreviousEntry().not());
    lastEntry.onActionProperty().bind(searchModel.showPrevious());
    nextEntry.disableProperty().bind(searchModel.hasNextEntry().not());
    nextEntry.onActionProperty().bind(searchModel.showNext());
    notificationPane.setOnHidden(e -> searchModel.clearSearch());
  }

  private class SearchModel {

    public final Color COLOR_FOUND_CELL = Color.MAGENTA;
    private final Object[] allCells;
    private ObservableList<mxICell> found = new ObservableListWrapper<>(new ArrayList<>());
    private IntegerProperty index = new SimpleIntegerProperty(0);
    private SimpleStringProperty resultText;
    private SimpleStringProperty searchTerm;
    private String oldStyle;
    private mxICell foundCell;

    public SearchModel() {
      allCells = mxGraphModel.getChildCells(jgxAdapter.getModel(), jgxAdapter.getDefaultParent(), true, true);
    }

    public SimpleStringProperty result() {
      resultText = new SimpleStringProperty();
      return resultText;
    }

    public SimpleStringProperty searchTerm() {
      searchTerm = new SimpleStringProperty();
      return searchTerm;
    }

    public BooleanProperty hasPreviousEntry() {
      final BooleanProperty booleanSimpleObjectProperty = new SimpleBooleanProperty(false);
      booleanSimpleObjectProperty.bind(index.greaterThan(0));
      return booleanSimpleObjectProperty;
    }

    public ObservableValue<? extends EventHandler<ActionEvent>> showPrevious() {
      EventHandler<ActionEvent> prev = e -> {
        index.setValue(index.getValue() - 1);
        resultText.set(String.format("%d of %d matches", index.intValue() + 1, found.size()));
        showFound();
      };
      return new SimpleObjectProperty<>(prev);
    }

    public BooleanProperty hasNextEntry() {
      final BooleanProperty booleanSimpleObjectProperty = new SimpleBooleanProperty(false);
      booleanSimpleObjectProperty.bind(Bindings.size(found).subtract(1).greaterThan(index));
      return booleanSimpleObjectProperty;
    }

    public ObservableValue<? extends EventHandler<ActionEvent>> showNext() {
      EventHandler<ActionEvent> next = e -> {
        index.set(index.getValue() + 1);
        resultText.set(String.format("%d of %d matches", index.intValue() + 1, found.size()));
        showFound();
      };
      return new SimpleObjectProperty<>(next);
    }

    protected void showFound() {
      clearFocus();

      foundCell = found.get(index.get());
      oldStyle = Strings.nullToEmpty(foundCell.getStyle());
      jgxAdapter.getModel().setStyle(foundCell,
          new EFSMDebugger.SB(foundCell).set(mxConstants.STYLE_STROKEWIDTH, String.valueOf(STROKE_WIDTH_HIGHLIGHTED))
              .set(mxConstants.STYLE_STROKECOLOR, COLOR_FOUND_CELL).build());
      graphComponent.scrollCellToVisible(foundCell, true);
    }

    public ObservableValue<? extends EventHandler<ActionEvent>> conductSearch() {
      EventHandler<ActionEvent> search = e -> {
        clearSearch();

        final String term = searchTerm.getValue().toLowerCase();

        if (Strings.isNullOrEmpty(term)) {
          resultText.set("No search term given");
          return;
        }

        for (Object cell : allCells) {
          final mxICell mxCell = (mxICell) cell;
          if (mxCell.getValue().toString().toLowerCase().contains(term)) {
            found.add(mxCell);
          }
        }

        if (found.isEmpty()) {
          resultText.set("No matches found");
          return;
        }

        resultText.set(String.format("%d of %d matches", index.intValue() + 1, found.size()));
        showFound();
      };
      return new SimpleObjectProperty<>(search);
    }

    public void clearSearch() {
      clearFocus();
      found.clear();
      index.set(0);
      resultText.setValue("");
    }

    private void clearFocus() {
      if (foundCell != null) {
        jgxAdapter.getModel().setStyle(foundCell, oldStyle);
      }
    }
  }
  // endregion
}
