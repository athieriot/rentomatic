# Rent'o'matic

[![Build Status](https://travis-ci.org/athieriot/rentomatic.svg?branch=master)](https://travis-ci.org/athieriot/rentomatic) [![Coverage Status](https://coveralls.io/repos/github/athieriot/rentomatic/badge.svg?branch=master)](https://coveralls.io/github/athieriot/rentomatic?branch=master)

Movie rental application

You can visit online: [https://rent-o-matic.herokuapp.com](https://rent-o-matic.herokuapp.com)

## Usage

      sbt clean run
      
      sbt clean test
      
      sbt clean coverage test coverageReport

## Documentation

Swagger UI accessible through: [http://localhost:9000/api/docs](http://localhost:9000/api/docs)

## Examples

### Find movies

      curl "http://localhost:9000/api/movie/popular" | python -m json.tool
      
      curl "http://localhost:9000/api/movie/search?query=matrix" | python -m json.tool

      curl "http://localhost:9000/api/movie/search?query=out%20of%20africa" | python -m json.tool

### Preview the price of a rental

      curl "http://localhost:9000/api/rental/pricing?id=603&days=2" | python -m json.tool
      
### Rent movies

      curl -X POST \
           -H "Content-Type: application/json" \
           -d '[{ "id": 603, "days": 1 }, { "id": 606, "days": 7 }]' \
          http://localhost:9000/api/rental/invoice | python -m json.tool
          
### Return movies

      curl -X POST \
           -H "Content-Type: application/json" \
           -d '[603, 606]' \
           http://localhost:9000/api/rental/returns?returnDate=2016-07-19T00:00:00Z | python -m json.tool

### See your invoice history

      curl "http://localhost:9000/api/rental/invoice" | python -m json.tool

### See your bonus points
           
    curl "http://localhost:9000/api/profile/bonus" | python -m json.tool

## Next ?

- The next step would be to add an Authentication mecanism (Silhouette is a great framework for that).
- Then providing a GUI for it (Web app or Mobile app) would be great (Posters !) and it might highlight some issues with the data returned by the API (Like need for more data or a different schema)

## Deployment

      git push heroku master
