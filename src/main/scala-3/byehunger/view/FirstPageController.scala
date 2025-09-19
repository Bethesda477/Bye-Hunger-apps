package byehunger.view

import javafx.fxml.FXML
import javafx.scene.control.Button
import byehunger.MainApp

class FirstPageController:
  
  @FXML private var userButton: Button = _
  @FXML private var merchantButton: Button = _

  @FXML
  def initialize(): Unit =
    // Initialize any necessary setup
    println("UserChoiceController initialized successfully")

  /** Handle user choice - navigate to user login */
  @FXML
  def handleUserChoice(): Unit =
    MainApp.showLogin()

  /** Handle merchant choice - navigate to merchant login */
  @FXML
  def handleMerchantChoice(): Unit =
    MainApp.showMerchantLogin()
