# Rent'o'matic

Movie rental application

## Usage

      sbt clean run
      
      sbt clean test
      
      sbt clean coverage test coverageReport

## Documentation

Swagger UI accessible through: [http://localhost:9000/api/docs](http://localhost:9000/api/docs)

## Examples

      curl "http://localhost:9000/api/popular" | python -m json.tool
      
      curl "http://localhost:9000/api/search?query=matrix" | python -m json.tool
      
      curl "http://localhost:9000/api/search?query=out%20of%20africa" | python -m json.tool

      curl "http://localhost:9000/api/pricing?id=603&days=2" | python -m json.tool
      
      curl -X POST \
           -H "Content-Type: application/json" \
           -d '[{ "id": 603, "days": 1 }, { "id": 606, "days": 7 }]' \
          http://localhost:9000/api/invoice | python -m json.tool
          
      curl "http://localhost:9000/api/invoice" | python -m json.tool
      
      curl -X POST \
           -H "Content-Type: application/json" \
           -d '[603, 606]' \
           http://localhost:9000/api/returns?returnDate=2016-07-19T00:00:00Z | python -m json.tool
           
       curl "http://localhost:9000/api/bonus" | python -m json.tool