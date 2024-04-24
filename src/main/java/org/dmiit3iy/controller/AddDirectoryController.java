package org.dmiit3iy.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.dmiit3iy.App;

import java.io.File;

public class AddDirectoryController implements ControllerData<File>  {
    public TextField nameTextField;
    private File file;
    private String path;
    public void createDirButton(ActionEvent actionEvent) {
        String name = nameTextField.getText();
        if(!name.isEmpty()){
            File file = new File(path+name);
            if(file.mkdir()){
                App.showMessage("Success","Directory create", Alert.AlertType.INFORMATION);
                nameTextField.clear();
            }else {
                App.showMessage("Fail","Directory not created", Alert.AlertType.ERROR);
            }
        } else {
            App.showMessage("alyrma","enter directory name", Alert.AlertType.INFORMATION);
        }

    }

    @Override
    public void initData(File value) {
        this.file=value;
       path=file.getAbsolutePath()+"\\";
    }

}
