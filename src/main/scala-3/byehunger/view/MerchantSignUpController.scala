package byehunger.view

import javafx.fxml.FXML
import byehunger.MainApp
import javafx.event.ActionEvent
import javafx.scene.control.{TextField, PasswordField, Label, Alert, ButtonType}
import javafx.stage.Stage
import java.util.regex.Pattern

@FXML
class MerchantSignUpController():
  //MODEL PROPERTY

  //STAGE PROPERTY
  var stage: Option[Stage] = None

  //RETURN PROPERTY
  var onClicked = false

  // FXML fields for validation
  @FXML private var businessNameField: TextField = _
  @FXML private var usernameField: TextField = _
  @FXML private var emailField: TextField = _
  @FXML private var passwordField: PasswordField = _
  @FXML private var confirmPasswordField: PasswordField = _
  @FXML private var errorLabel: Label = _

  @FXML
  def initialize(): Unit =
    // Clear error label on initialization
    if errorLabel != null then
      errorLabel.setText("")
      errorLabel.setVisible(false)

  @FXML
  def handleMerchantSignUp(action: ActionEvent): Unit =
    if validateMerchantSignUp() then
      // Validation passed, record data and close
      onClicked = true
      stage.foreach(x => x.close())
    else
      // Show error message
      showError("Please check your input and try again")

  private def validateMerchantSignUp(): Boolean =
    val businessName = Option(businessNameField).map(_.getText.trim).getOrElse("")
    val username = Option(usernameField).map(_.getText.trim).getOrElse("")
    val email = Option(emailField).map(_.getText.trim).getOrElse("")
    val password = Option(passwordField).map(_.getText.trim).getOrElse("")
    val confirmPassword = Option(confirmPasswordField).map(_.getText.trim).getOrElse("")

    // Basic validation rules
    val businessNameValid = businessName.nonEmpty && businessName.length >= 2
    val usernameValid = username.nonEmpty && username.length >= 3 && username.matches("^[a-zA-Z0-9_]+$")
    val emailValid = email.nonEmpty && isValidEmail(email)
    val passwordValid = password.nonEmpty && password.length >= 6
    val confirmPasswordValid = password == confirmPassword

    // Check each validation rule and show specific errors
    if !businessNameValid then
      showError("Business name must be at least 2 characters long")
      false
    else if !usernameValid then
      showError("Username must be at least 3 characters and contain only letters, numbers, and underscores")
      false
    else if !emailValid then
      showError("Please enter a valid email address")
      false
    else if !passwordValid then
      showError("Password must be at least 6 characters long")
      false
    else if !confirmPasswordValid then
      showError("Passwords do not match")
      false
    else
      true

  private def isValidEmail(email: String): Boolean =
    val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$")
    emailPattern.matcher(email).matches()

  private def showError(message: String): Unit =
    if errorLabel != null then
      errorLabel.setText(message)
      errorLabel.setVisible(true)
    else
      // Fallback to alert if error label is not available
      val alert = new Alert(Alert.AlertType.ERROR)
      alert.setTitle("Merchant Sign Up Error")
      alert.setHeaderText("Validation Failed")
      alert.setContentText(message)
      alert.showAndWait()
