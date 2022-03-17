package ${packageName};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.actionfx.core.ActionFX;
import com.github.actionfx.core.annotation.AFXApplication;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main class for application ${applicationName} using Spring Boot.
 *
 * @author actionfx-appfactory
 *
 */
@SpringBootApplication
public class ${mainClassName} {

	public static void main(final String[] argv) {
		ActionFX.builder().configurationClass(${mainClassName}Application.class).build();
		Application.launch(${mainClassName}Application.class);
	}

	@AFXApplication(mainViewId = "${mainViewId}", scanPackage = "${packageName}", enableBeanContainerAutodetection = true)
	public static class ${mainClassName}Application extends Application {

		@Override
		public void init() throws Exception {
			SpringApplication.run(BookstoreAppWithSpringBeanContainer.class);
		}

		@Override
		public void start(final Stage primaryStage) throws Exception {
			ActionFX.getInstance().showMainView(primaryStage);
		}
	}
}
