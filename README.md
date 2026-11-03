# BMMS (Bill Master)

Billing and payment hub. Banks, wallets and kiosks use it to pay service providers (mobile operators,
utilities, internet, taxi, government services) and to make card to card transfers.

This is a cleaned copy of the real project. Keys, passwords, server addresses and personal data were removed.

## Modules

- bmms-core - billing engine: bills, transactions, holds and cancels, reconciliation, bookkeeping,
  deposits, virtual accounts, P2P transfers, commissions, fraud checks
- bill-master-gateway - API gateway with Keycloak login and an Angular admin panel

## Integrations

- cards: Humo, Uzcard, Visa/Mastercard
- providers: Paynet, Munis (SOAP), mobile operators, internet and taxi providers
- core banking: NCI/ABS, OFD fiscal receipts
- daily reconciliation with each provider

Providers are described in JSON (request templates and response fields), so a new one can be added
without changing code.

## Stack

Java 11, Spring Boot 2.4, JHipster 7.1, Spring Cloud (Gateway, OpenFeign, Consul), PostgreSQL, Liquibase,
Hazelcast, Keycloak, SOAP, MapStruct, Prometheus, Testcontainers, Angular, Docker, Maven.

## How to run

```bash
cd bmms-core
docker-compose -f src/main/docker/postgresql.yml up -d
./mvnw
```

Settings come from environment variables, see `.env.example`.

## What was removed

Private keys, certificates, provider passwords, config profiles, CI files, real API dumps, internal hosts,
card numbers, personal data and git history.
