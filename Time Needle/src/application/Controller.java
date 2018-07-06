package application;

import application.Main;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Scanner;

import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class Controller {
	//panes
	@FXML private StackPane displayPane;
	//labels and textfeilds
	@FXML private Label label1;
	@FXML private TextField fileNametext;
	@FXML private TextField timelineTitleText;
	//buttons
	@FXML private Button loadCsvBtn;
	@FXML private Button GenerateBtn;
	//table view
	@FXML private TableView<TimeEvent> timeEvent;
	@FXML private TableColumn<TimeEvent,Integer> date;
	@FXML private TableColumn<TimeEvent,String> event;
	
	public File selectedFile;
	public static final int TIMELINE_MIN_WIDTH = 1000;
	public static final int TIMELINE_MAX_HEIGHT = 300;
	
	
	public void LoadCsvBtnAction(ActionEvent e) {
		//open file chooser
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().addAll(new ExtensionFilter("CSV files", "*.csv"));
		selectedFile = fc.showOpenDialog(null);
		
		if(selectedFile != null) {
			label1.setText("File:");
			fileNametext.setVisible(true);
			fileNametext.setText(selectedFile.toString());
		}
		//populate the table view
		date.setCellValueFactory(new PropertyValueFactory<>("date"));
		event.setCellValueFactory(new PropertyValueFactory<>("event"));
		timeEvent.getItems().setAll(readCSV(selectedFile.toString()));
		
	}

	/**
	 * Reads the CSV file and put all items as TimeEvent objects into a list
	 */
	public ArrayList<TimeEvent> readCSV (String filename){
	    ArrayList<TimeEvent> timeEvents = new ArrayList<TimeEvent> ();
	    try{
			Scanner scan = new Scanner(Paths.get(filename)); 
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				String[] lineArray = line.split(",");
				int date = Integer.parseInt(lineArray[0]);
				String event = lineArray[1].toString();
				
	        //int,String
			timeEvents.add(new TimeEvent(date,event));
			}

	    }
	    catch(IOException e){
			System.out.println("IO exception");
		}
		return timeEvents;
	}
	

	public void GenerateBtnAction(ActionEvent e) {
		displayPane.getChildren().clear();//clear the displayPane
		drawTimeline();
	}
	
	/**
	 * draws the timeline on the displayPane, uses JavaFX scatter chart
	 */
	public void drawTimeline() {
        ArrayList<TimeEvent> timeEvent = readCSV(selectedFile.toString());
		Collections.sort(timeEvent);
		
        final NumberAxis xAxis = new NumberAxis();//x axis
        xAxis.setForceZeroInRange(false); //
        final NumberAxis yAxis = new NumberAxis(0,8,0.5); //y axis(lowerbound, upperbound, ticks)
        
        XYChart.Series series = new XYChart.Series();
        
        //add labels to the timeline (plot points to scatter chart)
        for(int i = 0; i < timeEvent.size(); i++) {
        	if(i % 2 == 0) {
        		series.getData().add(createLabels(timeEvent.get(i).getEvent(),timeEvent.get(i).getDate() ,4));
        	}
        	else {
        		series.getData().add(createLabels(timeEvent.get(i).getEvent(), timeEvent.get(i).getDate() ,2));
        	}
        }
        
        //draw lines from the event labels to the x axis (https://stackoverflow.com/questions/32639882/conditionally-color-background-javafx-linechart)
        final ScatterChart<Number,Number> sc = 
                new ScatterChart<Number,Number>(xAxis,yAxis) {
            	protected void layoutPlotChildren() {
            		super.layoutPlotChildren();
            		 ObservableList<Data<Number,Number>> listOfData = series.getData();
            	        for(int i = 0; i < timeEvent.size(); i++) {
            	        	double x0  = getXAxis().getDisplayPosition(listOfData.get(i).getXValue());
            	        	double y0 = getYAxis().getDisplayPosition(listOfData.get(i).getYValue()) + 15;
            	        	//System.out.println(y0);
            	        	double x1 = x0;
            	        	double y1 = getYAxis().getDisplayPosition(0);
            	        	
            	        	Line branch = new Line(x0,y0,x1,y1);
            	        	getPlotChildren().add(branch);
            	       
            	        }
            	}
            };
            
            sc.setTitle(timelineTitleText.getText());//user defined title for timeline
            sc.setMaxHeight(TIMELINE_MAX_HEIGHT);
            sc.setMinWidth(TIMELINE_MIN_WIDTH);
            sc.getYAxis().setTickLabelsVisible(false); //make y axis invisible
            sc.getYAxis().setOpacity(0);
            sc.setLegendVisible(false);
            sc.getStylesheets().add(getClass().getResource("/css/scatterChart.css").toExternalForm()); //stylesheet for scatter chart
      
            sc.getData().add(series);
            displayPane.getChildren().add(sc);//add the scatter chart to the displayPane
    }
	
	/**
	 * the event labels/timestamps of the timeline, uses labels on the scatter chart
	 */
	public XYChart.Data createLabels(String event, Integer date, double height) {
        XYChart.Data data =  new XYChart.Data(date, height);

        StackPane node = new StackPane();
        Label label = new Label(date.toString() + "\n" + event);

        label.setTextAlignment(TextAlignment.CENTER);
        label.setWrapText(true);
        label.setTextFill(Color.BLUE);
        //label.setStyle("-fx-background-color: rgba(0, 255, 0 ,0.5);");

        node.getChildren().add(label);
        data.setNode(node);
		return data;

    }
}

