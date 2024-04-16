package org.dmiit3iy.controller;

import javafx.event.EventHandler;
import javafx.stage.WindowEvent;

public interface CloseEvent {
   EventHandler<WindowEvent> getCloseEventHandler();

}
