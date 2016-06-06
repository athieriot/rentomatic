import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import services.RentEngine

class Module extends AbstractModule with ScalaModule {

  override def configure() {
    bind[RentEngine]
  }
}
