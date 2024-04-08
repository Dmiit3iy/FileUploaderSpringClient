package org.dmiit3iy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.Callback;
import org.dmiit3iy.model.FolderChangeEvent;
import org.dmiit3iy.retorfit.DirectoryRepository;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private ObjectMapper objectMapper = new ObjectMapper();

    {
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    @FXML
    public TreeTableView treeTVServer;
    @FXML
    public TreeTableView<File> treeTVClient = new TreeTableView<File>();
    public TreeView<String> treeView = new TreeView<String>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DirectoryRepository directoryRepository = new DirectoryRepository();

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
                                        if (action.equals("ENTRY_DELETE")||action.equals("ENTRY_CREATE")){
                                            File file=directoryRepository.get();
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
                } catch (InterruptedException ignored) {}
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
