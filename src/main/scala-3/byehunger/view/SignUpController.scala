package byehunger.view

import javafx.fxml.FXML
import byehunger.MainApp
import javafx.event.ActionEvent
import javafx.stage.Stage

@FXML
class SignUpController():
  //MODEL PROPERTY

  //STAGE PROPERTY
  var stage: Option[Stage] = None

  //RETURN PROPERTY
  var onClicked = false

  @FXML
  def handleSignUp(action: ActionEvent): Unit =
    //record data in log in validation
    onClicked = true
    stage.foreach(x => x.close())
