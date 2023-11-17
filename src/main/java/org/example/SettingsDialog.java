package org.example;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsDialog {

    private static boolean isTextSelected = true;
    private static boolean isPhotoSelected = false;
    private static boolean isOtherSelected = false;

    public void showSettingsDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Настройки");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        CheckBox textCheckBox = new CheckBox("Текст");
        textCheckBox.setSelected(isTextSelected);

        CheckBox photoCheckBox = new CheckBox("Фото");
        photoCheckBox.setSelected(isPhotoSelected);

        CheckBox otherCheckBox = new CheckBox("Другое");
        otherCheckBox.setSelected(isOtherSelected);

        Button doneButton = new Button("Готово");
        doneButton.setOnAction(event -> {
            isTextSelected = textCheckBox.isSelected();
            isPhotoSelected = photoCheckBox.isSelected();
            isOtherSelected = otherCheckBox.isSelected();
            dialogStage.close();
        });

        vbox.getChildren().addAll(textCheckBox, photoCheckBox, otherCheckBox, doneButton);

        Scene scene = new Scene(vbox, 200, 150);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    public static boolean isTextSelected() {
        return isTextSelected;
    }

    public static boolean isPhotoSelected() {
        return isPhotoSelected;
    }

    public static boolean isOtherSelected() {
        return isOtherSelected;
    }
}
