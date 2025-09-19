package byehunger.view

import scalafx.Includes.*
import javafx.event.ActionEvent
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.web.WebView
import javafx.fxml.{FXML, Initializable}
import java.net.URL
import java.util.ResourceBundle

class NearbyController extends Initializable {

  // FXML fields linked via fx:id
  @FXML
  var locationField: TextField = _

  @FXML
  var goButton: Button = _

  @FXML
  var webView: WebView = _

  // This is the method for handling button clicks and text field actions
  @FXML
  def handleGoButton(action: ActionEvent): Unit = {
    // The loadAction logic from your original code
    val url = locationField.text()
    webView.engine.load(validUrl(url))
  }

  // A helper function from your original code
  def validUrl(url: String): String =
    if (url.startsWith("http://")) url else "http://" + url

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    // The initialization logic from your original start method
    val defaultURL = "https://www.google.com/maps/@3.0670489,101.603924,1344m/data=!3m1!1e3?entry=ttu&g_ep=EgoyMDI1MDgwNC4wIKXMDSoASAFQAw%3D%3D"

    // Set the initial value of the text field
    locationField.text = defaultURL

    // Load the default page
    webView.engine.load(defaultURL)

    // Listen for changes in the WebView's location and update the text field
    webView.engine.location.onChange { (_, _, newValue) =>
      locationField.text = newValue
    }

    // Set up the event handlers. The @FXML on the method links the button,
    // but you need to manually link the text field's onAction
    locationField.onAction = handleGoButton _
  }
}