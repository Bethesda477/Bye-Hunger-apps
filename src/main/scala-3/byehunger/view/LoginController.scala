package byehunger.view

import javafx.fxml.FXML
import byehunger.MainApp
import javafx.event.ActionEvent
import javafx.scene.control.{TextField, PasswordField, Label, Alert, ButtonType}
import javafx.scene.media.{Media, MediaPlayer, MediaView}
import javafx.stage.Stage

@FXML
class LoginController():

  @FXML private var loginBackgroundVideo: MediaView = _
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
  def handleLogin(): Unit =
    if validateLogin() then
      // Validation passed, proceed to main window
      MainApp.showMainWindow()
    else
      // Show error message
      showError("Please enter valid username and password")

  @FXML
  def handleSignUp(): Unit =
    MainApp.showSignUp()

  @FXML
  def handleMerchantLoginPage(): Unit =
    MainApp.showMerchantLogin()

  @FXML
  def handleBackToChoice(): Unit =
    MainApp.backToFirstPage()

  private def validateLogin(): Boolean =
    val username = Option(usernameField).map(_.getText.trim).getOrElse("")
    val password = Option(passwordField).map(_.getText.trim).getOrElse("")
    
    // Basic validation
    username.nonEmpty && password.nonEmpty && 
    username.length >= 3 && password.length >= 6

  private def showError(message: String): Unit =
    if errorLabel != null then
      errorLabel.setText(message)
      errorLabel.setVisible(true)
    else
      // Fallback to alert if error label is not available
      val alert = new Alert(Alert.AlertType.ERROR)
      alert.setTitle("Login Error")
      alert.setHeaderText("Validation Failed")
      alert.setContentText(message)
      alert.showAndWait()