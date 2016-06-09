# Rent'o'matic

Movie rental application

## Usage

      sbt clean run
      
      sbt clean test
      
      sbt clean coverage test coverageReport

## Documentation

Swagger UI accessible through: [http://localhost:9000/api/docs](http://localhost:9000/api/docs)

## Example

      curl "http://localhost:9000/api/popular"
      
      curl "http://localhost:9000/api/search?query=matrix"

      curl "http://localhost:9000/api/pricing?id=603&days=2"
      
      curl -X POST \
           -H "Content-Type: application/json" \
           -d '[{ "id": 603, "days": 1 }, { "id": 606, "days": 7 }]' \
          http://localhost:9000/api/invoice
          
      curl "http://localhost:9000/api/invoice"