package ${packageName};

import com.github.actionfx.core.annotation.AFXApplication;
import com.github.actionfx.core.app.AbstractAFXApplication;

import javafx.application.Application;

/**
 * Main class for application ${applicationName}
 *
 * @author actionfx-appfactory
 *
 */
public class ${mainClassName} {

	public static void main(final String[] argv) {
		Application.launch(${mainClassName}Application.class);
	}

	@AFXApplication(mainViewId = "${mainViewId}", scanPackage = "${packageName}", enableBeanContainerAutodetection = false)
	public static class ${mainClassName}Application extends AbstractAFXApplication {

	}
}
