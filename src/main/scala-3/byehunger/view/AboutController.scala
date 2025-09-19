package byehunger.view

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.stage.Stage

@FXML
class AboutController():
  //MODEL PROPERTY

  //STAGE PROPERTY
  var stage: Option[Stage] = None
  
  //RETURN PROPERTY
  var onClicked = false
  
  @FXML
  def handleClose(action: ActionEvent): Unit =
    onClicked = true
    stage.foreach(x => x.close())