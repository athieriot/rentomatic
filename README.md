# Rent'o'matic

Movie rental application

## Usage

      sbt run
      
      sbt test
      
      sbt clean coverage test coverageReport

## Example

      curl "http://localhost:9000/api/movies"

      curl "http://localhost:9000/api/invoice?movies=Matrix&days=2"