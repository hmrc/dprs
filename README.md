# Backend Service For DPRS (Digital Platforms Reporting  Service)

## About

This is the only backend of DPRS; it mostly acts as a conduit between the frontend and the integration layer: EIS (
Enterprise Integration Services).

By default, this service runs on port `20001`.

## Running

In order to run the following examples, ensure you first have [dprs-stubs](https://github.com/hmrc/dprs-stubs) running;
if using
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
  "type": "CorporateBody",
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

To get a sense of the various scenarios, you could look at the integration tests: one
for [individuals](it/test/uk/gov/hmrc/dprs/registration/withId/RegistrationWithIdForAnIndividualSpec.scala) and one
for [organisations](it/test/uk/gov/hmrc/dprs/registration/withId/RegistrationWithIdForAnOrganisationSpec.scala).

### Registration (Without ID)

Here's an example of a successful call, firstly with an individual:

``` 
curl 'http://localhost:20001/dprs/registrations/withoutId/individual' \
--header 'Content-Type: application/json' \
--data-raw '{
    "firstName": "Patrick",
    "middleName": "John",
    "lastName": "Dyson",
    "dateOfBirth": "1970-10-04",
    "address": {
        "lineOne": "34 Park Lane",
        "lineTwo": "Building A",
        "lineThree": "Suite 100",
        "lineFour": "Manchester",
        "postalCode": "M54 1MQ",
        "countryCode": "GB"
    },
    "contactDetails": {
        "landline": "747663966",
        "mobile": "38390756243",
        "fax": "58371813020",
        "emailAddress": "Patrick.Dyson@example.com"
    }
}'
```

```json
{
  "ids": [
    {
      "type": "ARN",
      "value": "ZARN5574814"
    },
    {
      "type": "SAFE",
      "value": "XE2986131148578"
    },
    {
      "type": "SAP",
      "value": "5094800652"
    }
  ]
}
```

And here's one for an organisation:

``` 
curl 'http://localhost:20001/dprs/registrations/withoutId/organisation' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "Dyson",
    "address": {
        "lineOne": "78 Rue Marie De Médicis",
        "lineTwo": "Cambrai",
        "lineThree": "Nord-Pas-de-Calais",
        "countryCode": "FR"
    },
    "contactDetails": {
        "landline": "747663966",
        "mobile": "38390756243",
        "fax": "58371813020",
        "emailAddress": "dyson@example.com"
    }
}'
```

```json
{
  "ids": [
    {
      "type": "ARN",
      "value": "ZARN5574814"
    },
    {
      "type": "SAFE",
      "value": "XE2986131148578"
    },
    {
      "type": "SAP",
      "value": "5094800652"
    }
  ]
}
```

To get a sense of the various scenarios, you could look at the integration tests: one
for [individuals](it/test/uk/gov/hmrc/dprs/registration/withoutId/RegistrationWithoutIdForAnIndividualSpec.scala) and
one
for [organisations](it/test/uk/gov/hmrc/dprs/registration/withoutId/RegistrationWithoutIdForAnOrganisationSpec.scala).

### Create Subscription

Here's an example of a successful call:

``` 
curl 'http://localhost:20001/dprs/subscriptions' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": {
        "type": "NINO",
        "value": "AA000000A"
    },
    "name": "Harold Winter",
    "contacts": [
        {
            "type": "I",
            "firstName": "Patrick",
            "middleName": "John",
            "lastName": "Dyson",
            "landline": "747663966",
            "mobile": "38390756243",
            "emailAddress": "Patrick.Dyson@example.com"
        },
        {
            "type": "O",
            "name": "Dyson",
            "landline": "847663966",
            "mobile": "48390756243",
            "emailAddress": "info@example.com"
        }
    ]
}'
```

```json 
{
  "id": "XSP1234567890"
}
```

To get a sense of the various scenarios, you could look
at [this integration test](it/test/uk/gov/hmrc/dprs/subscription/CreateSubscriptionSpec.scala).

### Update Subscription

Here's an example of a successful call:

``` 
curl -v 'http://localhost:20001/dprs/subscriptions/a7405c8d-06ee-46a3-b5a0-5d65176360ec' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "Harold Winter",
    "contacts": [
        {
            "type": "I",
            "firstName": "Patrick",
            "middleName": "John",
            "lastName": "Dyson",
            "landline": "747663966",
            "mobile": "38390756243",
            "emailAddress": "Patrick.Dyson@example.com"
        },
        {
            "type": "O",
            "name": "Dyson",
            "landline": "847663966",
            "mobile": "48390756243",
            "emailAddress": "info@example.com"
        }
    ]
}'
```

``` 
< HTTP/1.1 204 No Content
```

To get a sense of the various scenarios, you could look
at [this integration test](it/test/uk/gov/hmrc/dprs/subscription/UpdateSubscriptionSpec.scala).

### Read Subscription

Here's an example of a successful call:

``` 
curl 'http://localhost:20001/dprs/subscriptions/a7405c8d-06ee-46a3-b5a0-5d65176360ec'
```

```json 
{
  "id": "XLD1234567891",
  "name": "James Hank",
  "contacts": [
    {
      "type": "I",
      "firstName": "Mark",
      "middleName": "Jacob",
      "lastName": "Robinson",
      "landline": "0202731454",
      "mobile": "07896543333",
      "emailAddress": "markrobinson@hmrc.gov.uk"
    },
    {
      "type": "I",
      "firstName": "Leo",
      "middleName": "Jason",
      "lastName": "Smith",
      "landline": "0209231454",
      "mobile": "07896547777",
      "emailAddress": "leosmith@hmrc.gov.uk"
    }
  ]
}
```

To get a sense of the various scenarios, you could look
at [this integration test](it/test/uk/gov/hmrc/dprs/subscription/ReadSubscriptionSpec.scala).

## Demo ([Postman](https://www.postman.com/downloads/))

You can explore [the various Postman collections](etc/postman).

If you want to run them on the CLI
via [Newman](https://learning.postman.com/docs/collections/using-newman-cli/installing-running-newman/):

``` 
npm install -g newman
```

Assuming that `DPRS_STUBS` is already running, as well as this application. you can run any collections with, for
example:

``` 
newman run ./etc/postman/RegistrationWithID.json
```

Finally, to verify that all assertions for all collections are passing:

``` 
./run_postman.sh
```

It will stop at the first failure.

## Dev

Before pushing, you can run [verify.sh](./verify.sh) which will run all the tests, as well as check the format.

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

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").