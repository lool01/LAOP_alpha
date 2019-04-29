package org.lrima.laop.ui.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import org.lrima.laop.ui.I18n;
import org.lrima.laop.utils.ImageDefilementTMP;

import javax.swing.*;
import javax.tools.Tool;
import java.awt.*;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class homeController implements Initializable {
    @FXML  Label text;
    @FXML ChoiceBox<String> choiceBox;
    @FXML Label selectLanguageLabel;
    @FXML private JFXButton viewScientificConceptsBtn;
    @FXML private JFXButton viewUserGuideBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(choiceBox);
        String englishLabel = "English";
        String frenchLabel = "Fran\u00e7ais";
        choiceBox.getItems().addAll(englishLabel, frenchLabel);

        choiceBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal.intValue() == 0){
                I18n.update(new Locale("en", "CA"));
            }else if(newVal.intValue() == 1){
                I18n.update(new Locale("fr", "CA"));
            }
        });

        if(I18n.getLocal().getLanguage().equals("fr")){
            choiceBox.getSelectionModel().select(frenchLabel);
        }else {
            choiceBox.getSelectionModel().select(englishLabel);
        }
        I18n.bind(text, selectLanguageLabel);

        //TMP
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        //Show a panel when the user clicks on the two required buttons
        this.viewScientificConceptsBtn.setOnMouseClicked((event) -> {
            Platform.runLater(() -> {
                JFrame frame = new JFrame();
                frame.setTitle("Concepts scientifiques");
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.setMaximizedBounds(new Rectangle(500, 600));

                JPanel panel = new JPanel();
                panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
                frame.setContentPane(panel);

                ImageDefilementTMP image = new ImageDefilementTMP();
                image.setBounds(0, 0, (int)(screenDim.getWidth() / 2), (int)(screenDim.getHeight() / 1.5));
                image.setFichierImage("images/concepts_scientifiques-full.jpg");
                panel.add(image);
                frame.pack();
                frame.setVisible(true);
            });

        });

        this.viewUserGuideBtn.setOnMouseClicked((event) -> {
            Platform.runLater(() -> {
                JFrame frame = new JFrame();
                frame.setTitle("Guide d'utilisation");
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.setMaximizedBounds(new Rectangle(500, 600));
                ImageDefilementTMP image = new ImageDefilementTMP();
                image.setBounds(0, 0, (int)(screenDim.getWidth() / 2), (int)(screenDim.getHeight() / 1.5));
                image.setFichierImage("images/guide_utilisation-1.jpg");
                frame.add(image);
                frame.pack();
                frame.setVisible(true);
            });

        });

        //END TMP

    }
}