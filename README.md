
# disa-registration-stubs
App Description to be confirmed

### Before running the app

This repository relies on having mongodb running locally. You can start it with:

```bash
# first check to see if mongo is already running
docker ps | grep mongodb

# if not, start it
docker run --restart unless-stopped --name mongodb -p 27017:27017 -d percona/percona-server-mongodb:7.0 --replSet rs0
```

Reference instructions for [setting up docker](https://docs.tax.service.gov.uk/mdtp-handbook/documentation/developer-set-up/install-docker.html) and [running mongodb](https://docs.tax.service.gov.uk/mdtp-handbook/documentation/developer-set-up/set-up-mongodb.html#install-mongodb-applesilicon-mac).

### Running the app

```bash
sbt run
```

You can then query the app to ensure it is working with the following command:

```bash
# other useful commands
sbt clean

sbt reload

sbt compile
```

### Running the test suite

To run the unit tests:

```bash
sbt test
```

To run the integration tests:

```bash
sbt it/test
```

### Before you commit

This service leverages scalaFmt to ensure that the code is formatted correctly.

Before you commit, please run the following commands to check that the code is formatted correctly:

```bash
# checks all source and sbt files are correctly formatted
sbt prePrChecks

# if checks fail, you can format with the following commands

# formats all source files
sbt scalafmtAll

# formats all sbt files
sbt scalafmtSbt

# formats just the main source files (excludes test and configuration files)
sbt scalafmt
```

## Endpoints

## Endpoints

### POST /incorporated-entity-identification/api/limited-company-journey
Simulates the GRS create limited company journey endpoint.

The response is driven by the `credId` returned from Auth.

| Scenario                | `credId`                            | Response                      |
|-------------------------|-------------------------------------|-------------------------------|
| Unauthorized            | `grs-create-journey-unauthorised`   | `401 Unauthorized`            |
| Upstream Error          | `grs-create-journey-upstream-error` | `500 Internal Server Error`   |
| Invalid JSON (stubbed)  | `grs-create-journey-invalid-json`   | `400 Bad Request`             |
| Invalid URLs (stubbed)  | `grs-create-journey-invalid-urls`   | `400 Bad Request`             |
| Success                 | `grs-create-journey-success`        | `201 Created`                 |
| Success (default)       | any other value                     | `201 Created`                 |

For successful responses, the body will be:

```json
{
  "journeyStartUrl": "/obligations/enrolment/isa/incorporated-identity-callback?journeyId=<credId>"
}
```
Where `<credId>` is reused as the journeyId for subsequent calls to the journey data retrieval endpoint (see below).

### GET /journey/:journeyId
Simulates the GRS/BV journey data retrieval endpoint.

This can be triggered directly with calls, or by using the create journey endpoint with one of the following retrieval journey IDs as the Auth `credId`.

| Scenario                   | `journeyId` or `credId`             | Response           | Description                                                                 |
|----------------------------|-------------------------------------|--------------------|-----------------------------------------------------------------------------|
| Success                    | `grs-retrieval-success`             | `200 OK`           | Typical success case with user going through GRS and BV                     |
| Success (CT Enrolled)      | `grs-retrieval-success-ct-enrolled` | `200 OK`           | Success case for users with IR-CT enrolment, fast-tracked through BV        |
| Business Verification Fail | `grs-retrieval-bv-fail`             | `200 OK`           | Failure in BV journey resulting in lockout                                  |
| Registration Failed        | `grs-retrieval-registration-failed` | `200 OK`           | Successful verification but failure to register user with ETMP              |
| Absent UTR                 | `grs-retrieval-absent-utr`          | `200 OK`           | Edge case that can occur with Registered Societies                          |
| Not Found                  | `grs-retrieval-data-not-found`      | `404 Not Found`    | No journey data found for the given ID                                      |
| Unauthorized (stubbed)     | `grs-retrieval-unauthorised`        | `401 Unauthorized` | Explicit stubbed unauthorized response                                      |
| Unauthorized (real)        | auth fails                          | `401 Unauthorized` | Real authorization failure (e.g. missing or invalid credentials)            |
| Success (default)          | any other value                     | `200 OK`           | Defaults to typical success response                                        |

### PUT /tax-enrolments/subscriptions/:subscriptionId/subscriber

| Scenario     | `credId`                             | Response           |
| ------------ | ------------------------------------ | ------------------ |
| Success      | anything except specific test values | `204 No Content`   |
| Bad Request  | `tax-enrolment-bad-request`          | `400 Bad Request`  |
| Unauthorized | auth fails or no credentials         | `401 Unauthorized` |

### POST /address-lookup/lookup

Simulates the Address Lookup lookup-by-postcode endpoint.

The response is driven by the `postcode` submitted in the request. The optional `filter` field performs a substring match against address lines.

| Scenario                 | `postcode`  | `filter`      | Response           | Description                                      |
|--------------------------|-------------|---------------|--------------------|--------------------------------------------------|
| No results               | `ZZ00 1ZZ`  | not supplied  | `200 OK`           | Returns an empty JSON array                      |
| Single result            | `ZZ11 1ZZ`  | not supplied  | `200 OK`           | Returns one address record                       |
| Multiple results         | `ZZ22 2ZZ`  | not supplied  | `200 OK`           | Returns multiple address records                 |
| Filtered single result   | `ZZ22 2ZZ`  | `10`          | `200 OK`           | Returns only addresses containing `10`           |
| Filtered no results      | `ZZ22 2ZZ`  | no match      | `200 OK`           | Returns an empty JSON array                      |
| No results default       | any other value | any value | `200 OK`           | Defaults to an empty JSON array                  |

#### Request body

```json
{
  "postcode": "ZZ11 1ZZ",
  "filter": "10"
}
```

### Further documentation

You can view further information regarding this service via our [service guide](#).

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
