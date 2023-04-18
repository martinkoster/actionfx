module actionfx.actionfx.spring.boot.main {
    requires org.slf4j;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires actionfx.actionfx.core.main;

    exports com.github.actionfx.spring.container;
}