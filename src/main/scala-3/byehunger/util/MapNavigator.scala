package byehunger.util

import java.awt.Desktop
import java.net.{URI, URLEncoder}
import java.nio.charset.StandardCharsets
import scala.util.Try

object MapNavigator:

  final case class Coordinates(lat: Double, lon: Double):
    def isValid: Boolean = lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180

  enum MapProvider:
    case Auto, AppleMaps, GoogleMaps, OpenStreetMap

  def openLocation(
                    coords: Option[Coordinates],
                    address: Option[String],
                    preferred: MapProvider = MapProvider.Auto
                  ): Boolean =
    val url = buildUrl(coords, address, preferred)
      .orElse(buildUrl(coords, address, MapProvider.GoogleMaps))
      .orElse(buildUrl(coords, address, MapProvider.OpenStreetMap))

    url.exists(openUrl)

  private def buildUrl(
                        coords: Option[Coordinates],
                        address: Option[String],
                        provider: MapProvider
                      ): Option[String] =
    val useProvider = provider match
      case MapProvider.Auto =>
        if (isMac) MapProvider.AppleMaps else MapProvider.GoogleMaps
      case p => p

    val query: Option[String] =
      coords.filter(_.isValid).map(c => s"${c.lat},${c.lon}")
        .orElse(address.map(encode))

    query.map { q =>
      useProvider match
        case MapProvider.AppleMaps =>
          // Apple Maps: coordinates -> ll=lat,lon; address -> q=address
          coords.filter(_.isValid)
            .map(c => s"http://maps.apple.com/?ll=${c.lat},${c.lon}")
            .getOrElse(s"http://maps.apple.com/?q=$q")

        case MapProvider.GoogleMaps =>
          // Google Maps: https://www.google.com/maps/search/?api=1&query=...
          s"https://www.google.com/maps/search/?api=1&query=$q"

        case MapProvider.OpenStreetMap =>
          coords.filter(_.isValid)
            .map(c => s"https://www.openstreetmap.org/?mlat=${c.lat}&mlon=${c.lon}#map=17/${c.lat}/${c.lon}")
            .getOrElse(s"https://www.openstreetmap.org/search?query=$q")
    }

  def openUrl(url: String): Boolean =
    val uri = URI.create(url)
    // Prefer Desktop if supported
    if (Desktop.isDesktopSupported && Desktop.getDesktop.isSupported(Desktop.Action.BROWSE))
      Try(Desktop.getDesktop.browse(uri)).isSuccess
    else
      // Linux fallback
      if (isLinux) Try(new ProcessBuilder("xdg-open", url).start()).isSuccess
      else false
    
      
  

  private def encode(s: String): String =
    URLEncoder.encode(s, StandardCharsets.UTF_8)

  private def isMac: Boolean =
    System.getProperty("os.name", "").toLowerCase.contains("mac")

  private def isLinux: Boolean =
    val os = System.getProperty("os.name", "").toLowerCase
    os.contains("nix") || os.contains("nux") || os.contains("aix")