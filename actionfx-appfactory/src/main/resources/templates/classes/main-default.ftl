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
public class ${mainAppClassName} {

	public static void main(final String[] argv) {
		Application.launch(${mainAppClassName}Application.class);
	}

	@AFXApplication(mainViewId = "${mainViewId}", scanPackage = "${packageName}")
	public static class ${mainAppClassName}Application extends AbstractAFXApplication {

	}
}
