module org.dmiit3iy {
    requires static lombok;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires retrofit2;
    requires okhttp3;
    requires retrofit2.converter.jackson;

    opens org.dmiit3iy to javafx.fxml;
    opens org.dmiit3iy.controller to javafx.fxml;

    exports org.dmiit3iy;
    exports org.dmiit3iy.dto to com.fasterxml.jackson.databind;
}
