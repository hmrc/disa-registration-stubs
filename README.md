
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

### POST /incorporated-entity-identification/api/limited-company-journey
Simulates the GRS create limited company journey endpoint.

The response is driven by the `credId` returned from Auth (derived from the bearer token in tests).

| Scenario                | `credId`                          | Response                      |
|------------------------|-----------------------------------|-------------------------------|
| Unauthorized           | `grs-create-journey-unauthorised` | `401 Unauthorized`            |
| Upstream Error         | `grs-create-journey-upstream-error` | `500 Internal Server Error` |
| Invalid JSON (stubbed) | `grs-create-journey-invalid-json` | `400 Bad Request`             |
| Invalid URLs (stubbed) | `grs-create-journey-invalid-urls` | `400 Bad Request`             |
| Success                | `grs-create-journey-success`      | `201 Created`                 |
| Success (default)      | any other value                   | `201 Created`                 |

For successful responses, the body will be:

```json
{
  "journeyStartUrl": "<disa-reg-frontend>/incorporated-identity-callback?journeyId=<credId>"
}
```
Where `<credId>` is reused as the journeyId for subsequent calls to the journey data retrieval endpoint (see below).

### GET /journey/:journeyId
Simulates the GRS/BV journey data retrieval endpoint.

This can be triggered directly with calls, or by using the create journey endpoint, using one of the following journeyIds as credId.

| Scenario                   | `journeyId` or `credId`    | Response           |
|----------------------------|---------------------------| ------------------ |
| Success                    | `success`                 | `200 OK`           |
| Identifiers Mismatch       | `identifiers-fail`        | `200 OK`           |
| Business Verification Fail | `bv-fail`                 | `200 OK`           |
| BV Not Called              | `bv-not-called`           | `200 OK`           |
| CT Enrolled                | `bv-ct-enrolled`          | `200 OK`           |
| Registration Failed        | `registration-failed`     | `200 OK`           |
| Registration Not Called    | `registration-not-called` | `200 OK`           |
| CT UTR Absent              | `ct-utr-absent`           | `200 OK`           |
| Not Found                  | `grs-data-not-found`      | `404 Not Found`    |
| Unauthorized               | auth fails                | `401 Unauthorized` |
| Success (default)          | any other value           | `200 OK`           |

### PUT /tax-enrolments/subscriptions/:subscriptionId/subscriber

| Scenario     | `credId`                             | Response           |
| ------------ | ------------------------------------ | ------------------ |
| Success      | anything except specific test values | `204 No Content`   |
| Bad Request  | `tax-enrolment-bad-request`          | `400 Bad Request`  |
| Unauthorized | auth fails or no credentials         | `401 Unauthorized` |

### Further documentation

You can view further information regarding this service via our [service guide](#).

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
