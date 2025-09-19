package byehunger.view

import javafx.fxml.FXML
import byehunger.MainApp
import javafx.event.ActionEvent
import javafx.stage.Stage

@FXML
class ProfileIconController():

  @FXML
  def handleOpenMessage(action: ActionEvent) = ???

  var stage: Option[Stage] = None

  //RETURN PROPERTY
  var onCliked = false

  @FXML
  def handleClose(action: ActionEvent): Unit =
    onCliked = true
    stage.foreach(x => x.close())



