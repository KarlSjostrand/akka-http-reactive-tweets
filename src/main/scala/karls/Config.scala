package karls

import com.typesafe.config.ConfigFactory

trait Config {

  private val config = ConfigFactory.load()
  protected val interface = config.getString("interface")
  protected val port = config.getInt("port")

  case class TwitterConfig(track: String = config.getString("twitter.track"),
                           host: String = config.getString("twitter.host"),
                           path: String = config.getString("twitter.path"),
                           consumerKey: String = config.getString("twitter.consumerKey"),
                           consumerSecret: String = config.getString("twitter.consumerSecret"),
                           accessToken: String = config.getString("twitter.accessToken"),
                           accessTokenSecret: String = config.getString("twitter.accessTokenSecret"))

  protected val twitterConfig = TwitterConfig()
}
