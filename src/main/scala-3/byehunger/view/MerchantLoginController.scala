package byehunger.view

import javafx.fxml.FXML
import byehunger.MainApp
import javafx.event.ActionEvent
import javafx.scene.control.{TextField, PasswordField, Label, Alert, ButtonType}

@FXML
class MerchantLoginController():
  @FXML private var usernameField: TextField = _
  @FXML private var passwordField: PasswordField = _
  @FXML private var errorLabel: Label = _

  @FXML
  def initialize(): Unit =
    // Clear error label on initialization
    if errorLabel != null then
      errorLabel.setText("")
      errorLabel.setVisible(false)

  @FXML
  def handleMerchantLogin(): Unit = 
    if validateMerchantLogin() then
      // Validation passed, proceed to merchant main window
      MainApp.showMerchantMainWindow()
    else
      // Show error message
      showError("Please enter valid merchant credentials")

  @FXML
  def handleMerchantSignUp(): Unit =
    MainApp.showMerchantSignUp()

  @FXML
  def handleUserPage(): Unit =
    MainApp.backToFirstPage()

  private def validateMerchantLogin(): Boolean =
    val username = Option(usernameField).map(_.getText.trim).getOrElse("")
    val password = Option(passwordField).map(_.getText.trim).getOrElse("")
    
    // Basic validation for merchant login
    username.nonEmpty && password.nonEmpty && 
    username.length >= 3 && password.length >= 6

  private def showError(message: String): Unit =
    if errorLabel != null then
      errorLabel.setText(message)
      errorLabel.setVisible(true)
    else
      // Fallback to alert if error label is not available
      val alert = new Alert(Alert.AlertType.ERROR)
      alert.setTitle("Merchant Login Error")
      alert.setHeaderText("Validation Failed")
      alert.setContentText(message)
      alert.showAndWait()

