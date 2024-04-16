package org.dmiit3iy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import net.lingala.zip4j.ZipFile;
import org.dmiit3iy.App;
import org.dmiit3iy.model.FolderChangeEvent;
import org.dmiit3iy.retorfit.DirectoryRepository;
import org.dmiit3iy.retorfit.FileUploadRepository;
import org.dmiit3iy.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainController {
    private ObjectMapper objectMapper = new ObjectMapper();

    {
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @FXML
    public TreeTableView treeTVServer;
    @FXML
    public TreeTableView<File> treeTVClient = new TreeTableView<File>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DirectoryRepository directoryRepository = new DirectoryRepository();
    private List<File> filesToSend = new ArrayList<>();

    private List<File> filesToLoad = new ArrayList<>();
    private FileUploadRepository fileUploadRepository= new FileUploadRepository();

    @FXML
    void initialize() {

        executorService.execute(() -> {
            try {
                while (true) {
                    System.out.println("Initialize event source");

                    String url = "http://localhost:8080/folder-watch";
                    EventSource.Builder builder = new EventSource.Builder(new EventHandler() {
                        @Override
                        public void onOpen() {
                            System.out.println("onOpen");
                        }

                        @Override
                        public void onClosed() {
                            System.out.println("onClosed");
                        }

                        @Override
                        public void onMessage(String event, MessageEvent messageEvent) {
                            Platform.runLater(() -> {
                                try {
                                    System.out.println(messageEvent.getData());
                                    FolderChangeEvent folderChangeEvent = objectMapper.readValue(messageEvent.getData(), FolderChangeEvent.class);
                                    System.out.println(folderChangeEvent.getAction());
                                    String action = folderChangeEvent.getAction();
                                    if (action.equals("ENTRY_DELETE") || action.equals("ENTRY_CREATE")) {
                                        File file = directoryRepository.get();
                                        treeTVServer.setRoot(getNodesForDirectoryForTreeTable(file));
                                    }
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }

                        @Override
                        public void onComment(String comment) {
                            System.out.println("onComment");
                        }

                        @Override
                        public void onError(Throwable t) {
                            System.out.println("onError: " + t);
                        }

                    }, URI.create(url));

                    try (EventSource eventSource = builder.build()) {
                        eventSource.start();
                        TimeUnit.MINUTES.sleep(1);
                    }
                }
            } catch (InterruptedException ignored) {
            }
        });


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
                                CheckBox checkBox = new CheckBox();

                                @Override
                                public void updateItem(Boolean item, boolean empty) {
                                    super.updateItem(item, empty);
                                    if (empty) {
                                        setGraphic(null);
                                        setText(null);
                                    } else {

                                        checkBox.setOnAction(event -> {
                                            if (checkBox.isSelected()) {
                                                File checkFile = getTreeTableView().getTreeItem(getIndex()).getValue();
                                                filesToSend.add(checkFile);
                                                System.out.println("В массиве для отправки:" + filesToSend.toString());
                                            }
                                            if (!checkBox.isSelected()) {
                                                File checkFile = getTreeTableView().getTreeItem(getIndex()).getValue();
                                                filesToSend.remove(checkFile);
                                                System.out.println("В массиве для отправки:" + filesToSend.toString());
                                            }
                                        });
                                        setGraphic(checkBox);
                                        setText(null);
                                    }
                                }
                            };
                            return cell;
                        }
                    };

            Callback<TreeTableColumn<File, Boolean>, TreeTableCell<File, Boolean>> cellFactoryServer =
                    new Callback<TreeTableColumn<File, Boolean>, TreeTableCell<File, Boolean>>() {
                        @Override
                        public TreeTableCell call(final TreeTableColumn<File, Boolean> param) {
                            final TreeTableCell<File, Boolean> cell = new TreeTableCell<File, Boolean>() {
                                CheckBox checkBox = new CheckBox();

                                @Override
                                public void updateItem(Boolean item, boolean empty) {
                                    super.updateItem(item, empty);
                                    if (empty) {
                                        setGraphic(null);
                                        setText(null);
                                    } else {

                                        checkBox.setOnAction(event -> {
                                            if (checkBox.isSelected()) {
                                                File checkFile = getTreeTableView().getTreeItem(getIndex()).getValue();
                                                filesToLoad.add(checkFile);
                                                System.out.println("В массиве для отправки:" +  filesToLoad.toString());
                                            }
                                            if (!checkBox.isSelected()) {
                                                File checkFile = getTreeTableView().getTreeItem(getIndex()).getValue();
                                                filesToLoad .remove(checkFile);
                                                System.out.println("В массиве для отправки:" +  filesToLoad.toString());
                                            }
                                        });
                                        setGraphic(checkBox);
                                        setText(null);
                                    }
                                }
                            };
                            return cell;
                        }
                    };




            actionColClient.setCellFactory(cellFactory);
            //TODO написать свою фабрику для сервера
            actionColServer.setCellFactory(cellFactoryServer);


            treeTVClient.setRoot(getNodesForDirectoryForTreeTable(new File(path)));
            treeTVClient.getColumns().addAll(fileNameColClient, actionColClient);

            treeTVServer.setRoot(getNodesForDirectoryForTreeTable(files));
            treeTVServer.getColumns().addAll(fileNameColServer, actionColServer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private javafx.event.EventHandler<WindowEvent> closeEventHandler = new javafx.event.EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
            executorService.shutdownNow();
        }
    };

    public javafx.event.EventHandler<WindowEvent> getCloseEventHandler() {
        return closeEventHandler;
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

    public void downloadButton(ActionEvent actionEvent) {
    }

    //TODO написать метод для добавления файлов в zip и сделать отправку на сервер
    public void loadButton(ActionEvent actionEvent) {
        if (!filesToSend.isEmpty()) {
            ZipFile zipFile = Util.addZip(filesToSend);
            try {
                fileUploadRepository.uploadFile(zipFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            App.showMessage("Уведомление","Нужно выбрать файлы для отправки", Alert.AlertType.INFORMATION);
        }
    }
}
