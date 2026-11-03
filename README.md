# BMMS — Bill Master Payment Gateway

**Bill Master (BMMS)** is a bank-grade billing and payments hub. Agents (banks, wallets, kiosks) use it to pay
thousands of service providers (mobile operators, utilities, internet providers, taxis, government services) and
to make card-to-card (P2P) transfers across Uzbekistan's national card networks.

> **Context.** Production system used in the Davrbank environment, built by the bank's integration team.
> I include it as a reference for the domain and integrations I work with. Published with all confidential data
> removed — see [Security & sanitization](#security--sanitization).

## Modules

| Module | Type | Purpose |
|---|---|---|
| [`bmms-core`](bmms-core) | JHipster microservice | Billing engine: bills, transactions, holds/cancellations, reconciliation, bookkeeping, deposits and virtual accounts, P2P transfers, commissions, fraud checks |
| [`bill-master-gateway`](bill-master-gateway) | JHipster gateway + Angular UI | API gateway (Spring Cloud Gateway), OAuth2/Keycloak security and admin dashboard (users, groups, cash-back, dashboards) |

## Integrations (adapter / gateway pattern)

- **Card networks:** Humo, Uzcard, Visa/Mastercard (P2P, holds, cancellations, balance)
- **Aggregators and providers:** Paynet, Munis (government payments, SOAP/WSDL), mobile operators, internet and taxi providers
- **Core banking:** NCI / ABS bookkeeping, OFD fiscal receipts
- **Reconciliation:** automatic daily matching with every provider, plus discrepancy reports

Each provider is described declaratively (JSON request templates and response-field mapping), so new
recipients can be added without code changes.

## Tech stack

**Java 11 · Spring Boot 2.4 · JHipster 7.1 · Spring Cloud (Gateway, OpenFeign, Consul) · PostgreSQL · Liquibase ·
Hazelcast · Keycloak (OAuth2/JWT) · SOAP/WSDL · MapStruct · Micrometer/Prometheus · Testcontainers ·
Angular · Docker · Maven**

The core module contains about 1,300 Java classes and more than 300 Liquibase changelogs.

## Running locally

```bash
cd bmms-core
docker-compose -f src/main/docker/postgresql.yml up -d
./mvnw
```

Configuration is externalized as `${ENV_VAR}` placeholders (see `.env.example`).

## Security & sanitization

- Private keys, certificates and keystores, gateway credentials, environment profiles, CI pipelines and
  real API dumps were removed.
- Internal hosts/IPs, card numbers and personal data were masked. Git history is not included.
