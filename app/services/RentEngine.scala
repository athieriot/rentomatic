package services

class RentEngine {

  private val PREMIUM_PRICE: Int = 40

  private val BASIC_PRICE: Int = 30

  def rentalPrice(movie: String, days: Int): Double = movie match {
    case _ if days == 0 => 0
    case "Matrix" => PREMIUM_PRICE * days
    case "Spider Man" => BASIC_PRICE + BASIC_PRICE * (if (days - 3 < 0) 0 else days - 3)
    case "Out of Africa" => BASIC_PRICE + BASIC_PRICE * (if (days - 5 < 0) 0 else days - 5)
  }
}
