# Stub IDP
![Stub IDP logo](././stub-idp/src/main/resources/assets/images/providers/stub-idp-demo-one.png)

This microservice is a stub IDP that can be white-labelled to simulate any IDP, or used as a Stub eIDAS Proxy Service Node.

[![Build Status](https://travis-ci.org/stub-idp/stub-idp.svg?branch=master)](https://travis-ci.org/stub-idp/stub-idp)

## Running

Does not yet run, because app needs a run config :D

## Configuring the app

A yaml file containing IDPs needs to be created and the file location set as  `stubIdpsYmlFileLocation` in the app yml config file

The format is as follows:

```yaml
stubIdps:
  - friendlyId: stub-idp-one-for-rp1
    displayName: Stub IDP One
    assetId: stub-idp-one
    sendKeyInfo: true
    idpUserCredentials:
        - user: userName
          password: <password hash>
```

The `friendlyId` can be used to enable multiple IDPs but using the same `displayName` and `assetId` e.g. for different on-boarding relying parties.

Header images for IDPs should be placed into `ida-stub-idp/src/main/resources/assets/images/providers/` and are referenced as `assetId` e.g. stub-idp-one.png is referenced as `stub-idp-one`

## Connecting to a hub/using as an IDP

You need to use the entityId `http://stub_idp.acme.org/{friendlyId}/SSO/POST` or as the template configured in the main config file, with the shared key/cert configured in the main config file (`stub-idp.yml`)

The SSO URI for that IDP will be `http://localhost:50140/{friendlyId}/SAML2/SSO` or equivalent.

You need to set the hub (or service) entityId that messages will be received from/sent back to, as well as where the hub/service metadata can be received from -> see the `hubEntityId` and `metadata` configuration blocks.

# Using as a Stub eIDAS Proxy Service Node

Set the values in `europeanIdentity` including HUB_CONNECTOR_ENTITY_ID for the consuming relying party/service provider/hub.

Several countries can be dynamically stubbed at once - see the full list in [EidasScheme](src/main/java/uk/gov/ida/stub/idp/domain/EidasScheme.java) (use the values, not the enum keys).  Once the metadata is retrieved for each stub it can be used by the consuming relying party/service provider/hub.  Metadata is at http://localhost:50140/[scheme]/ServiceMetadata  Test users are all `stub-country*`.

## Test Users

Test users can be uploaded to the IDPs [docs](https://alphagov.github.io/rp-onboarding-tech-docs/pages/env/envEndToEndTests.html#createtestusers)

Basic auth for the `/{friendlyId}/users` endpoint is enabled by default and can be configured using `basicAuthEnabledForUserResource`

# Licence

[MIT Licence](LICENCE)