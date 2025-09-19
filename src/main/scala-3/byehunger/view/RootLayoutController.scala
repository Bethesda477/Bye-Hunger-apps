package byehunger.view

import byehunger.MainApp
import javafx.event.ActionEvent
import javafx.fxml.FXML

@FXML
class RootLayoutController():
  @FXML
  def handleClose(action: ActionEvent): Unit =
    System.exit(0)
    
  @FXML
  def handleAbout(action: ActionEvent): Unit =
    MainApp.showAbout()