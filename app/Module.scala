import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import services.TMDBApi

class Module extends AbstractModule with ScalaModule {
  def configure(): Unit = {
    bind[TMDBApi]
  }
}
