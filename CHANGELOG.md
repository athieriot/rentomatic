(2016-06-13) Provide a bit more insight on the code
============

 - Mostly added comments on the important bits on the API.

- Also adding a few footnotes about the next step I would probably
  continue with.

(2016-06-13) Worked on Error Handling
============

 - Most unknown Exception would not result in a Json readable
  Content error message.

- Changed the status code of the "invoice" endpoint as well to be 201
  instead of 200.

- Also renamed tmdbApi to a more generic MovieCatalogue as the
  underlying implementation can be anything. (Such as actual internal
  catalogue of owned movies)

(2016-06-11) Fix Swagger on Heroku
============

 - Heroku is using Https and provide (obviously) another domain name than
  localhost. And Swagger needs both scheme and host to be provided in
  the configuration.

(2016-06-10) Attempt to plug Travis CI
============

 - Probably to show off the code coverage but Travis CI is a must have for Open
  Source projects (I use Drone for private repository at work).

- Tools like Coveralls or Sonar are great investments as well.

(2016-06-10) Prepare deployment on Heroku
============

 - I do like my ops things to be nicely automated and Heroku provide a
  very good way of doing so. The database patches needed fix as well and
  the "hardest" thing to configure was the Postgresql driver. Heroku
  doesn't provide a JDBC formated URL by default and I had to add a
  special Env Variable up there.

(2016-06-10) Refactor Api paths
============

 - The Api Test class was getting very big and moving specific things in
  their own class made sense.

- Updated the endpoint paths slightly in an attempt to make them a
  little bit more understandable.

(2016-06-10) Merge branch 'the-returns'
============

 
(2016-06-10) Implement bonus points API
============

 - Statelessly look up at all the invoices and compute the bonus points.
  Won't perform for very big customer.

(2016-06-10) Implement Returns API
============

 - The calculation rules are in the complete method of an Invoice.
  There's not much to it really. Using today's date (Or an optional
  parameter to make tests easier) I get the total number of rental days
  and recompute the total price. As I know already what they paid, it's
  only a matter of charging the extra sum.

- There is no refund if they return their movie early.

(2016-06-09) Integrate Slick as a way to store customer rentals
============

 - Before being able to implement a return API, we need a way to store
  previous rentals accross requests. I could have start with a simple
  cache or a map but I had like to do it properly. Besides, Slick was
  not complicated to introduced and it made me think a little bit more
  about what I wanted to persist exactly.

- A new object was introduced for the occasion: Invoice. It represents
  what a customer actually paid for and when. This will allow to compute
  extra charge upon return and stay consistent with any change of
  pricing that might happen in the future.

- Currently the system currently assume that there is only one "customer"
  but that can come later.

- I used a Timestamp for dates mostly because of the lack of support for
  Instant by Slick.

- At the moment, a customer can rent several times the same movie. I am
  not quite sure what to do about that. I guess it might happen.

(2016-06-08) Implement The Movie Database search
============

 - Ok. That is quite a big commit. I should definitively have branch it
  and commit more regularly. Will be for next time.

- Objects are mostly the same but invoicing is done using a movie ID
  coming from TMDb.

- The main addition is the TMDBApi service which uses Play! WS to make
  API calls and parse JSON. This lead some decisions in the Movie object
  mapping such as Release Date being now an Option[LocalDate] as TMDb
  could have the field empty.

- I am aware that my API key has been commited to Git. Usually it should
  be set as an Env Variable but it was easier that way for other people
  to test the program.

- A great deal of effort (And a big part of the changes) were made in
  testing. Especially the API Controller and the TMDb services (Using
  relevant HTTP mocks).

(2016-06-07) Now able to get an invoice for several rentals
============

 - Movie pricing logic into specific model objects. Mainly to be able to
  acknowledge real release dates.

- Separate the endpoint to get a pricing and actually requesting an
  invoice (GET / POST), paving the path for rental persistence (In order
  to build return Movie extra charge).

- I noticed that running the build with code coverage lead to
  inconsistent test failures (That are normally fine). Need to
  investigate this.

(2016-06-06) Include Swagger documentation
============

 - Wanted to provide a way to easily try the API

(2016-06-06) Initial commit
============

 - Bootstrap a simple Play! application to lay the foundations of the
  project. The readme will give some explainations of how to run it.

- Baby steps to start with and going right to the core of the Rental app
  with only two endpoints: Movie listing and basic invoice computation
  on request.

- Introducing code coverage reporting for future improvements and a
  limited set of tests for existing code.

