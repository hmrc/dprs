
# Backend Service For DPRS (Digital Platforms Reporting Service)

## About

This is the only backend of DPRS; it mostly acts as a conduit between the frontend and the integration layer: EIS (Enterprise Integration Services).

By default, this service runs on port `20001`.

## Running

In order to run the following examples, ensure you first have [dprs-stubs](https://github.com/hmrc/dprs-stubs) running; if using
service manager, it would simply be:

```
sm -start DPRS_STUBS 
```
Note that you also need to have MongoDb running, in whichever way you wish.

You can then run this service in a variety of ways.

For sbt:

``` 
sbt run
```

For service manager:

``` 
sm -start DPRS 
```

## API

### Registration (With ID)

Here's an example of a successful call, firstly with an individual:

``` 
curl 'http://localhost:20001/dprs/registrations/withId/individual' \
--header 'Content-Type: application/json' \
--data '{
    "id": {
        "type": "NINO",
        "value": "AA000000A"
    },
    "firstName": "Patrick",
    "lastName": "Dyson",
    "dateOfBirth": "1970-10-04"
}'
```

```json
{
  "ids": [
    {
      "type": "ARN",
      "value": "WARN3849921"
    },
    {
      "type": "SAFE",
      "value": "XE0000200775706"
    },
    {
      "type": "SAP",
      "value": "1960629967"
    }
  ],
  "firstName": "Patrick",
  "middleName": "John",
  "lastName": "Dyson",
  "dateOfBirth": "1970-10-04",
  "address": {
    "lineOne": "26424 Cecelia Junction",
    "lineTwo": "Suite 858",
    "lineThree": "",
    "lineFour": "West Siobhanberg",
    "postalCode": "OX2 3HD",
    "countryCode": "AD"
  },
  "contactDetails": {
    "landline": "747663966",
    "mobile": "38390756243",
    "fax": "58371813020",
    "emailAddress": "Patrick.Dyson@example.com"
  }
}
```

And here's one for an organisation:

``` 
curl 'http://localhost:20001/dprs/registrations/withId/organisation' \
--header 'Content-Type: application/json' \
--data '{
    "id": {
        "type": "UTR",
        "value": "1234567890"
    },
    "name": "Dyson",
    "type": "CorporateBody"
}'
```

```json
{
  "ids": [
    {
      "type": "ARN",
      "value": "WARN1442450"
    },
    {
      "type": "SAFE",
      "value": "XE0000586571722"
    },
    {
      "type": "SAP",
      "value": "8231791429"
    }
  ],
  "name": "Dyson",
  "type": "UnincorporatedBody",
  "address": {
    "lineOne": "2627 Gus Hill",
    "lineTwo": "Apt. 898",
    "lineThree": "",
    "lineFour": "West Corrinamouth",
    "postalCode": "OX2 3HD",
    "countryCode": "AR"
  },
  "contactDetails": {
    "landline": "176905117",
    "mobile": "62281724761",
    "fax": "08959633679",
    "emailAddress": "edward.goodenough@example.com"
  }
}
```

To get a sense of the various scenarios, you could look at [this integration test](it/test/uk/gov/hmrc/dprs/RegistrationWithIdSpec.scala).

## Demo ([Postman](https://www.postman.com/downloads/))

You can explore [the various Postman collections](etc/postman) (currently only the one).

If you want to run them on the CLI via [Newman](https://learning.postman.com/docs/collections/using-newman-cli/installing-running-newman/):

``` 
npm install -g newman
```

Assuming that `DPRS_STUBS` is already running, as well as this application. you can run all the collections with:

``` 
newman run ./etc/postman/*.json
```

Or just a single one:

``` 
newman run ./etc/postman/RegistrationWithID.json
```

## Dev

Before pushing, you can run [verify.sh](./verify.sh) which will run all of the tests, as well as check the format.

### Tests

#### Unit

```
sbt test
```

#### Integration

Although we're not currently using MongoDb, it must be running beforehand.

``` 
sbt "it/test"
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").