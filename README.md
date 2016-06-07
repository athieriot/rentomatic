# Rent'o'matic

Movie rental application

## Usage

      sbt run
      
      sbt test
      
      sbt clean coverage test coverageReport

## Documentation

Swagger UI accessible through: [http://localhost:9000/docs](http://localhost:9000/docs)

## Example

      curl "http://localhost:9000/api/movies"

      curl "http://localhost:9000/api/pricing?title=Matrix 11&days=2"
      
      curl -X POST \
           -H "Content-Type: application/json" \
           -d '[{ "title": "Matrix 11", "days": 1 }, { "title": "Out of Africa", "days": 7 }]' \
          http://localhost:9000/api/invoice