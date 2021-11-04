package ${packageName};

import com.github.actionfx.core.annotation.AFXController;

import javafx.fxml.FXML;
<#list importStatements as importStatement>
import ${importStatement};
</#list>

/**
 * ActionFX controller for FXML view "${fxmlFile}"
 *
 * @author actionfx-appfactory
 *
 */
@AFXController(viewId = "${viewId}", fxml = "${fxmlFile}", title = "${title}")
public class ${controllerName} {

<#list nodes as node>
	@FXML
	protected ${node.type} ${node.id};
	
</#list>


<#list actionMethods as actionMethod>
	public void ${actionMethod.name}(ActionEvent event) {
		// TODO: implement action method
	}
	
</#list>

}
