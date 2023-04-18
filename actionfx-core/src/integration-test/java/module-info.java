module actionfx.actionfx.core.integrationTest {
    requires javax.inject;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires org.hamcrest;
    requires org.junit.jupiter.api;
    requires org.mockito;
    requires org.mockito.junit.jupiter;
    requires org.testfx;
    requires actionfx.actionfx.core.main;
    requires actionfx.actionfx.testing.main;

    exports com.github.actionfx.core;
    exports com.github.actionfx.core.container.instantiation;
}