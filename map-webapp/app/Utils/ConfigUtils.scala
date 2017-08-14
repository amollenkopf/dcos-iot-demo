package utils

import com.typesafe.config.ConfigFactory

object ConfigUtils {

  val INTERNAL_HOST_PATH = "proxy.internalHttpHost"
  val INTERNAL_PORT_PATH = "proxy.internalHttpPort"
  val DEFAULT_INTERNAL_HOST = "coordinator.elastic.l4lb.thisdcos.directory"
  val DEFAULT_INTERNAL_PORT = 9200

  lazy val conf: com.typesafe.config.Config = ConfigFactory.load()

  // get the version number from the config
  lazy val VERSION: String = {
    conf.getString("application.version")
  }

  // get the proxy's internal host mame from the config
  def internalHost: String = {
    conf.getStringWithDefault(INTERNAL_HOST_PATH, DEFAULT_INTERNAL_HOST)
  }

  // get the proxy's internal port number from the config
  def internalPort: Int = {
    conf.getIntWithDefault(INTERNAL_PORT_PATH, DEFAULT_INTERNAL_PORT)
  }

  /**
    * Helper class to help check optional config parameters
    *
    * @param underlying the Config
    */
  implicit class RichConfig(val underlying: com.typesafe.config.Config) extends AnyVal {

    def getStringWithDefault(path: String, defaultValue: String): String = {
      if (underlying.hasPath(path)) {
        underlying.getString(path)
      } else {
        defaultValue
      }
    }

    def getBooleanWithDefault(path: String, defaultValue: Boolean): Boolean = {
      if (underlying.hasPath(path)) {
        underlying.getBoolean(path)
      } else {
        defaultValue
      }
    }

    def getIntWithDefault(path: String, defaultValue: Int): Int = {
      if (underlying.hasPath(path)) {
        underlying.getInt(path)
      } else {
        defaultValue
      }
    }
  }

}


