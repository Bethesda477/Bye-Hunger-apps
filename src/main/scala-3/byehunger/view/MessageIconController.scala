package byehunger.view

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.stage.Stage

@FXML
class MessageIconController():
  //MODEL PROPERTY

  //STAGE PROPERTY
  var stage: Option[Stage] = None

  //RETURN PROPERTY
  var onCliked = false

  @FXML
  def handleOpenMessage(action: ActionEvent) = ???
    //pop out a message window for that specific dialogue