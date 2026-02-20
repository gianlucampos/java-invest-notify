# Java invest notify

A project to notify if a stock value is above the market value using **AWS Lambda**.

---

## Features

- Notify my email if a stock is above market value.
- Runs **once an hour** (or as configured in EventBridge cron).
- Fully **free-tier friendly** on AWS.

---

## Stack

- Java 21
- Jakarta Mail 2.0.1
- Google Auth Api
- AWS Lambda + EventBridge (cron)
- Lombok

---

## Build

```bash
./gradlew clean build

