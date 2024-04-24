package org.dmiit3iy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainController {
    @FXML
    public ListView<File> listViewServer;
    @FXML
    public ListView<File> listViewClient;
    private ObjectMapper objectMapper = new ObjectMapper();

    {
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DirectoryRepository directoryRepository = new DirectoryRepository();

    private FileUploadRepository fileUploadRepository = new FileUploadRepository();

    private String path = System.getProperty("user.home") + "\\Desktop";
    private ObservableList<File> observableListServer;
    private ObservableList<File> observableListClient;
    private File currentServerDirectory;
    private File currentClientDirectory;

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
                                        Thread.sleep(1000);

                                        List<File> list = directoryRepository.get();
                                        observableListServer = FXCollections.observableArrayList(list);
                                        listViewServer.setItems(observableListServer);
                                        System.out.println(list);

                                    }
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                } catch (InterruptedException e) {
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
            // List<File> serverFiles = directoryRepository.get();
            currentServerDirectory = directoryRepository.getRoot();
            List<File> serverFiles = List.of(currentServerDirectory.listFiles());
            observableListServer = FXCollections.observableArrayList(serverFiles);

            //List<File> files = getFilesForListView(new File(path));
            currentClientDirectory = new File(path);
            List<File> files = getFilesForListView(currentClientDirectory);
            observableListClient = FXCollections.observableArrayList(files);

            listViewClient.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
                @Override
                public ListCell<File> call(ListView<File> param) {
                    ListCell<File> cell = new ListCell<File>() {
                        @Override
                        protected void updateItem(File file, boolean empty) {
                            super.updateItem(file, empty);
                            if (empty || file == null) {
                                setText(null);
                            } else {
                                setText(file.getName());
                            }
                        }
                    };

                    cell.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) {
                            File selectedItem = cell.getItem();
                            if (selectedItem.isDirectory() && selectedItem.list().length != 0) {
                                currentClientDirectory=selectedItem;
                                System.out.println(Arrays.toString(selectedItem.list()));
                                List<File> list = Arrays.asList(selectedItem.listFiles());
                                ObservableList<File> observableList = FXCollections.observableArrayList(list);
                                listViewClient.setItems(observableList);
                            }

                        }
                    });

                    return cell;
                }
            });


            listViewServer.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
                @Override
                public ListCell<File> call(ListView<File> param) {
                    ListCell<File> cell = new ListCell<File>() {
                        @Override
                        protected void updateItem(File file, boolean empty) {
                            super.updateItem(file, empty);
                            if (empty || file == null) {
                                setText(null);
                            } else {
                                setText(file.getName());
                            }
                        }
                    };

                    cell.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) {
                            File selectedItem = cell.getItem();
                            // if (selectedItem.isDirectory() && selectedItem.list().length != 0) {
                            if (selectedItem.isDirectory()) {
                                currentServerDirectory = selectedItem;
                                System.out.println(Arrays.toString(selectedItem.list()));
                                List<File> list = Arrays.asList(selectedItem.listFiles());
                                observableListServer = FXCollections.observableArrayList(list);
                                listViewServer.setItems(observableListServer);
                            }

                        }
                    });

                    return cell;
                }
            });
            listViewServer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listViewClient.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listViewClient.setItems(observableListClient);
            listViewServer.setItems(observableListServer);


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


    public List<File> getFilesForListView(File directory) {
        List<File> root = new ArrayList<>();
        for (File f : directory.listFiles()) {
            root.add(f);
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


    public void loadButton(ActionEvent actionEvent) {
        ObservableList<File> observableList = listViewClient.getSelectionModel().getSelectedItems();
        System.out.println(observableList);
        if (!observableList.isEmpty()) {
            ZipFile zipFile = Util.addZip(observableList);
            try {
                String path = listViewServer.getItems().get(0).getParent();
                fileUploadRepository.uploadFile(zipFile, listViewServer.getItems().get(0).getParent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            zipFile.getFile().delete();
        } else {
            App.showMessage("Уведомление", "Нужно выбрать файлы для отправки", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void upButtonServer(ActionEvent actionEvent) {
        File up = new File(currentServerDirectory.getParent());
        if (up.exists() && up.toPath().startsWith("C:\\UploaderStorage")) {
            List<File> list = Arrays.asList(up.listFiles());
            ObservableList<File> observableList = FXCollections.observableArrayList(list);
            listViewServer.setItems(observableList);
            currentServerDirectory = up;
        }
    }

    @FXML
    public void upButtonClient(ActionEvent actionEvent) {

        File up = new File(currentClientDirectory.getParent());
        if (up.exists()) {
            List<File> list = Arrays.asList(up.listFiles());
            ObservableList<File> observableList = FXCollections.observableArrayList(list);
            listViewClient.setItems(observableList);
            currentClientDirectory=up;
        }
    }

    public void createDirectoryButton(ActionEvent actionEvent) {
        try {
            App.openWindowAndWait("AddDir.fxml", "Create dir", currentServerDirectory);
            List<File> list = Arrays.asList(currentServerDirectory.listFiles());
            observableListServer = FXCollections.observableArrayList(list);
            listViewServer.setItems(observableListServer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
