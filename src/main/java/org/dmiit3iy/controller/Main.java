package org.dmiit3iy.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.Callback;
import org.dmiit3iy.retorfit.DirectoryRepository;

import java.io.File;
import java.io.IOException;

public class Main {
    @FXML
    public TreeTableView treeTVServer;
    @FXML
    public TreeTableView<File> treeTVClient = new TreeTableView<File>();
    public TreeView<String> treeView = new TreeView<String>();

    private DirectoryRepository directoryRepository = new DirectoryRepository();

    @FXML
    void initialize() {
        try {

            File files = directoryRepository.get();
            String path = System.getProperty("user.home") + "/Desktop";

            TreeTableColumn<File, String> fileNameColClient = new TreeTableColumn<File, String>("name");
            fileNameColClient.setCellValueFactory(new TreeItemPropertyValueFactory<File, String>("name"));
            TreeTableColumn<File, Boolean> actionColClient = new TreeTableColumn<File, Boolean>("check");

            TreeTableColumn<File, String> fileNameColServer = new TreeTableColumn<File, String>("name");
            fileNameColServer.setCellValueFactory(new TreeItemPropertyValueFactory<File, String>("name"));
            TreeTableColumn<File, Boolean> actionColServer = new TreeTableColumn<File, Boolean>("check");

            fileNameColClient.prefWidthProperty().bind(treeTVClient.widthProperty().multiply(0.8));
            actionColClient.prefWidthProperty().bind(treeTVClient.widthProperty().multiply(0.2));

            fileNameColServer.prefWidthProperty().bind(treeTVServer.widthProperty().multiply(0.8));
            actionColServer.prefWidthProperty().bind(treeTVServer.widthProperty().multiply(0.2));
            fileNameColClient.setResizable(false);
            actionColClient.setResizable(false);
            fileNameColServer.setResizable(false);
            actionColServer.setResizable(false);


            Callback<TreeTableColumn<File, Boolean>, TreeTableCell<File, Boolean>> cellFactory =
                    new Callback<TreeTableColumn<File, Boolean>, TreeTableCell<File, Boolean>>() {
                        @Override
                        public TreeTableCell call(final TreeTableColumn<File, Boolean> param) {
                            final TreeTableCell<File, Boolean> cell = new TreeTableCell<File, Boolean>() {
                                CheckBox btn = new CheckBox();

                                @Override
                                public void updateItem(Boolean item, boolean empty) {
                                    super.updateItem(item, empty);
                                    if (empty) {
                                        setGraphic(null);
                                        setText(null);
                                    } else {

                                        btn.setOnAction(event -> {
                                            File person = getTreeTableView().getTreeItem(getIndex()).getValue();

                                            System.out.println(person);
                                        });
                                        setGraphic(btn);
                                        setText(null);
                                    }
                                }
                            };
                            return cell;
                        }
                    };
            actionColClient.setCellFactory(cellFactory);
            actionColServer.setCellFactory(cellFactory);


            treeTVClient.setRoot(getNodesForDirectoryForTreeTable(new File(path)));
            treeTVClient.getColumns().addAll(fileNameColClient, actionColClient);

            treeTVServer.setRoot(getNodesForDirectoryForTreeTable(files));
            treeTVServer.getColumns().addAll(fileNameColServer,actionColServer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TreeItem<String> getNodesForDirectory(File directory) {
        TreeItem<String> root = new TreeItem<String>(directory.getName());
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) //если каталог идем на рекурсию
                root.getChildren().add(getNodesForDirectory(f));
            else //если просто файл заполняем только имя
                root.getChildren().add(new TreeItem<String>(f.getName()));
        }
        return root;
    }

    public TreeItem<File> getNodesForDirectoryForTreeTable(File directory) {
        TreeItem<File> root = new TreeItem<File>(directory);
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) //если каталог идем на рекурсию
                root.getChildren().add(getNodesForDirectoryForTreeTable(f));
            else //если просто файл заполняем только имя
                root.getChildren().add(new TreeItem<File>(f));
        }
        return root;
    }

    public void print(File f) {
        for (File x : f.listFiles()) {
            if (x.isDirectory()) {
                System.out.println(x.getName() + "  каталог");
                print(x);
                //как написать "если есть каталог, то прочитать его содержимое"?
            } else {
                System.out.println(x.getName() + " файл");
            }
        }
    }
}
