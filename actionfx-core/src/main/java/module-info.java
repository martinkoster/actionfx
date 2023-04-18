module actionfx.actionfx.core.main {
    requires java.annotation;
    requires java.datatransfer;
    requires java.instrument;
    requires javax.inject;
    requires com.github.spotbugs.annotations;
    requires commons.beanutils;
    requires io.github.classgraph;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires net.bytebuddy;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires net.bytebuddy.agent;
    requires java.desktop;

    exports com.github.actionfx.core;
    exports com.github.actionfx.core.annotation;
    exports com.github.actionfx.core.app;
    exports com.github.actionfx.core.collections;
    exports com.github.actionfx.core.container;
    exports com.github.actionfx.core.container.instantiation;
    exports com.github.actionfx.core.extension;
    exports com.github.actionfx.core.instrumentation;
    exports com.github.actionfx.core.utils;
    exports com.github.actionfx.core.validation;
    exports com.github.actionfx.core.view;
    exports com.github.actionfx.core.view.graph;
}