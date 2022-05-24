package ${packageName};

import com.github.actionfx.core.annotation.AFXController;
<#if actionMethods?size != 0>
import com.github.actionfx.core.annotation.AFXOnAction;
import javafx.event.ActionEvent;
</#if>
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
	<#if actionMethod.useAFXOnActionAnnotation>
	@AFXOnAction(nodeId = "${actionMethod.nodeId}", async = false)
	public void ${actionMethod.name}(ActionEvent event) {
	<#else>
	public void ${actionMethod.name}(ActionEvent event) {
	</#if>
		// TODO: implement action method
	}
	
</#list>

}
