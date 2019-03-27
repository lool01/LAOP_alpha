package org.lrima.laop.ui;

import org.lrima.laop.simulation.Simulation;
import org.lrima.laop.ui.components.PlayButton;
import org.lrima.laop.ui.panels.ChartPanel;
import org.lrima.laop.ui.panels.ConsolePanel;
import org.lrima.laop.ui.panels.inspector.InspectorPanel;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * Class that displays the simulation with the side panels
 *
 * @author Léonard
 */
public class SimulationStage extends Stage {
    private final Simulation simulation;
    private Canvas canvas;

    private SimulationDrawer simulationDrawer;
    private InspectorPanel inspector;
    private ConsolePanel consolePanel;
    private ChartPanel chartPanel;

    private final double WINDOW_WIDTH = 1280;
    private final double WINDOW_HEIGHT = 720;
    private CheckBox checkBoxRealTime;
    private JFXSlider sliderTimeLine;

    private MenuBar menuBar;
    private Button btnGenFinish;

    /**
     * Initialize a new simulation stage with a specific simulation buffer
     * @param simulation the simulation to initialize the simulation stage with
     */
    public SimulationStage(Simulation simulation){
        this.setTitle("LAOP : Simulation");
        this.simulation = simulation;

        this.canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.inspector = new InspectorPanel();
        this.consolePanel = new ConsolePanel();
        this.chartPanel = new ChartPanel(simulation);
        this.simulationDrawer = new SimulationDrawer(canvas, simulation, inspector);
        this.configureMenu();

        this.setOnCloseRequest(e->{
            Platform.exit();
            System.exit(0);
        });

        this.loadAllScenes();

        this.simulation.setAutoRun(false);
        this.simulation.setOnGenerationFinish(this::handleGenerationFinish);
        this.simulation.setMainScene(this);

        this.checkBoxRealTime.selectedProperty().setValue(true);

        this.simulationDrawer.start();
    }

    /**
     * Called when the generations finished
     *
     * @param simulation the simulation
     */
    private void handleGenerationFinish(Simulation simulation) {
        btnGenFinish.setDisable(false);
    }

    /**
     * Adds all the layouts with their components to root Pane
     */
    private void loadAllScenes() {
        Node timeLine = timeline();

        BorderPane rootPane = new BorderPane();

        Pane blankPane = new Pane();
        blankPane.setVisible(false);

        rootPane.setTop(this.menuBar);
        //rootPane.setCenter(blankPane);

        //Add the timeLine and the chart panel to the bottom
        VBox bottomPanelBox = new VBox();
        bottomPanelBox.getChildren().addAll(timeLine, this.chartPanel);
        
        rootPane.setBottom(bottomPanelBox);
        rootPane.setRight(inspector);

        rootPane.setLeft(this.consolePanel);
        canvas.setPickOnBounds(false);
        rootPane.setPickOnBounds(false);

        StackPane rootrootPane = new StackPane(canvas, rootPane);

        //CANVAS
        ChangeListener<Number> updateWidthHeight = (observable, oldValue, newValue) -> {
            canvas.setHeight(rootrootPane.getHeight());
            canvas.setWidth(rootrootPane.getWidth());
        };

        rootrootPane.widthProperty().addListener(updateWidthHeight);
        rootrootPane.heightProperty().addListener(updateWidthHeight);

        this.simulationDrawer.setSlider(sliderTimeLine);

        Scene scene = new Scene(rootrootPane);
        scene.getStylesheets().add("/css/general.css");

        this.setScene(scene);
    }

    /**
     * Cree le menu au dessus de la fenetre avec des boutons
     * @author Clement Bisaillon
     */
    private void configureMenu() {
    	this.menuBar = new MenuBar();

    	Menu windowMenu = new Menu("Window");
    	CheckMenuItem showConsole = new CheckMenuItem("Console");
    	CheckMenuItem showCharts = new CheckMenuItem("Charts");
    	CheckMenuItem showCarInfo = new CheckMenuItem("Car info");
    	showConsole.setSelected(true);
    	showCharts.setSelected(true);
    	showCarInfo.setSelected(true);

    	//Les actions quand nous cliquons sur les boutons
    	showConsole.selectedProperty().addListener((obs, oldVal, newVal) -> {
    		this.consolePanel.setVisible(newVal);
    		this.consolePanel.setManaged(newVal);
    	});

    	showCharts.selectedProperty().addListener((obs, oldVal, newVal) -> {
    		this.chartPanel.setVisible(newVal);
    		this.chartPanel.setManaged(newVal);
    	});

    	showCarInfo.selectedProperty().addListener((obs, oldVal, newVal) -> {
    		this.inspector.setVisible(newVal);
    		this.inspector.setManaged(newVal);
    	});

    	//Two way bind with the inspector panel (When you click a car)
    	this.inspector.visibleProperty().addListener((obs, oldVal, newVal) -> {
    		showCarInfo.setSelected(newVal);
    	});

    	windowMenu.getItems().addAll(showConsole, showCharts, showCarInfo);

    	Menu view = new Menu("View");

    	MenuItem resetView = new MenuItem("Reset View");
    	resetView.setOnAction(e -> simulationDrawer.resetView());

    	view.getItems().add(resetView);

    	this.menuBar.getMenus().addAll(windowMenu, view);
    }

    /**
     * Creates all the elements that compose the Timeline
     *
     * @return the elements of the timeline
     */
    private HBox timeline() {
        //TODO : Probablement que c'est bien de l'encapsuler, mais il faut penser à comment le faire si on veut qu'il soit réutilisable
        HBox root = new HBox();
        root.getStyleClass().add("panel");

        PlayButton button = new PlayButton(
                (b)-> {
                    if(b)
                        this.simulationDrawer.startAutodraw(2);
                    else
                        this.simulationDrawer.stopAutoDraw();
                });

        checkBoxRealTime = new JFXCheckBox("");
        checkBoxRealTime.selectedProperty().setValue(false);
        checkBoxRealTime.selectedProperty().addListener((obs, oldVal, newVal) -> {
            this.sliderTimeLine.setDisable(newVal);
            button.setDisable(newVal);
            button.setIsPlaying(false);
            simulationDrawer.setRealTime(newVal);
        });

        sliderTimeLine = new JFXSlider();

        sliderTimeLine.setMax(simulation.getBuffer().getSize());
        sliderTimeLine.setValue(0);
        sliderTimeLine.setMinorTickCount(1);
        sliderTimeLine.setMaxWidth(Integer.MAX_VALUE);
        sliderTimeLine.valueProperty().addListener((obs, oldVal, newVal) -> {
            int currentValue = (int)Math.round(newVal.doubleValue());
            int oldValue = (int)Math.round(oldVal.doubleValue());

            if(currentValue != oldValue)
                this.simulationDrawer.setCurrentStep(currentValue);
        });

        sliderTimeLine.setOnMousePressed(e -> this.simulationDrawer.stopAutoDraw());


        HBox.setMargin(sliderTimeLine, new Insets(7,0,7,0));

        btnGenFinish = new JFXButton("Next generation");
        btnGenFinish.getStyleClass().add("btn-light");
        btnGenFinish.setOnAction( e-> {
            btnGenFinish.setDisable(true);
            this.simulation.nextGen();
        });

        root.getChildren().addAll(button, sliderTimeLine, checkBoxRealTime, btnGenFinish);
        root.setSpacing(10);
        root.setPadding(new Insets(5));

        HBox.setHgrow(sliderTimeLine, Priority.ALWAYS);

        return root;
    }
}
