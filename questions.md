# Graphite Design Questions - Complete Answers

Generated: January 2025

---

## Section 1: Project Vision & Goals

### 1.1 Who is the primary target audience?
**Answer:** a) Backend/Microservices developers

### 1.2 What's the primary use case you envision?
**Answer:** d) All of the above (third-party APIs, internal microservices, BFF pattern)

### 1.3 Should Graphite compete with or complement existing libraries?
**Answer:** b) Be lightweight and focused (do one thing well)

### 1.4 What's your priority order? (rank 1-4)
**Answer:**
1. Type safety (compile-time guarantees)
2. Developer experience (ease of use)
3. Performance (speed, memory)
4. Flexibility (customization options)

### 1.5 Should Graphite be opinionated or flexible?
**Answer:** c) Opinionated defaults, flexible overrides

---

## Section 2: Client Architecture

### 2.1 What level of abstraction for the client API?
**Answer:** a) High-level only (simple execute method)

### 2.2 Should the client be thread-safe by default?
**Answer:** a) Yes, always thread-safe

### 2.3 Should we support multiple GraphQL endpoints in one client?
**Answer:** a) One client = one endpoint

### 2.4 Should clients be immutable or mutable?
**Answer:** c) Immutable client, mutable request context

### 2.5 How should client instances be created?
**Answer:** a) Builder pattern only

### 2.6 Should there be a global default client?
**Answer:** a) No, always explicit

---

## Section 3: HTTP & Networking

### 3.1 Should we support alternative HTTP clients beyond java.net.http?
**Answer:** a) No, java.net.http only

### 3.2 How should connection pooling be handled?
**Answer:** b) Configurable pool settings

### 3.3 What timeout configurations should be exposed?
**Answer:** d) All of the above (single, connect/read/write separately, per-operation overrides)

### 3.4 Should Graphite handle HTTP/2 specifically?
**Answer:** a) Transparent (whatever HttpClient does)

### 3.5 How should proxy configuration work?
**Answer:** c) Both with override capability

### 3.6 Should we support request compression?
**Answer:** c) Configurable compression

### 3.7 Should response decompression be handled?
**Answer:** a) Automatic (GZIP, deflate)

---

## Section 4: Authentication & Security

### 4.1 What authentication methods should be built-in?
**Answer:** e) All common methods (Bearer token, API key, Basic auth)

### 4.2 Should there be OAuth/OIDC integration?
**Answer:** b) Token refresh helper only

### 4.3 How should authentication tokens be provided?
**Answer:** c) Both options (static header + Supplier/lambda for dynamic tokens)

### 4.4 Should we support mutual TLS (mTLS)?
**Answer:** b) Yes, via Java's SSLContext

### 4.5 Should we validate SSL certificates by default?
**Answer:** b) Configurable (with warnings for disabled)

### 4.6 Should sensitive data (tokens, variables) be masked in logs?
**Answer:** c) Always mask by default

---

## Section 5: Error Handling

### 5.1 How should GraphQL errors be exposed?
**Answer:** c) Configurable behavior (response object + getDataOrThrow())

### 5.2 Should partial responses (data + errors) be supported?
**Answer:** a) Yes, return both data and errors

### 5.3 What exception hierarchy should we use?
**Answer:** b) Hierarchy (NetworkError, GraphQLError, etc.) - unchecked exceptions

### 5.4 Should we include error codes/categories?
**Answer:** c) Custom error classification support

### 5.5 How should network failures be handled?
**Answer:** b) Wrap in GraphiteException

### 5.6 Should there be automatic retry support?
**Answer:** c) Advanced retry (exponential backoff, jitter)

### 5.7 Which errors should be retryable by default?
**Answer:** c) Configurable retry conditions

---

## Section 6: Serialization

### 6.1 Should Jackson be the only JSON library?
**Answer:** a) Yes, Jackson only

### 6.2 Should ObjectMapper be shared or created per client?
**Answer:** c) Shared default, overridable

### 6.3 How should unknown JSON fields be handled?
**Answer:** a) Ignore by default

### 6.4 How should null values be serialized?
**Answer:** c) Configurable per-field

### 6.5 Should we support JSON field name customization?
**Answer:** a) Exact field names from schema

---

## Section 7: Code Generation - Types

### 7.1 What should be generated for GraphQL types?
**Answer:** a) Java records only

### 7.2 Should generated types have equals/hashCode/toString?
**Answer:** a) Yes, auto-generated (records do this)

### 7.3 Should generated types implement Serializable?
**Answer:** a) No

### 7.4 How should optional fields (nullable) be represented?
**Answer:** a) Direct type (String, can be null) with @Nullable annotation

### 7.5 Should we generate copy/with methods for records?
**Answer:** b) Yes, for all types

### 7.6 How should Lists be represented?
**Answer:** a) java.util.List

### 7.7 Should collections be made unmodifiable?
**Answer:** b) Yes, always unmodifiable

### 7.8 Should generated types have JavaDoc?
**Answer:** b) From GraphQL descriptions

### 7.9 How should deprecated fields be handled?
**Answer:** b) Add @Deprecated annotation (with option to skip deprecated fields)

### 7.10 Should we generate constant for field names?
**Answer:** a) No

---

## Section 8: Code Generation - Inputs

### 8.1 Should input types be mutable or immutable?
**Answer:** a) Immutable (records)

### 8.2 What builder style for input types?
**Answer:** d) Both builder and constructor

### 8.3 Should builders validate required fields?
**Answer:** b) Validate on build()

### 8.4 Should builders be reusable after build()?
**Answer:** a) Yes, can build multiple

### 8.5 Should input builders support "with" methods for modification?
**Answer:** b) Yes, return new builder

---

## Section 9: Code Generation - Enums

### 9.1 How should GraphQL enums map to Java?
**Answer:** b) Enum with String value

### 9.2 How should unknown enum values be handled?
**Answer:** a) Throw exception (strict enums)

### 9.3 Should enums include the GraphQL description?
**Answer:** b) As JavaDoc

### 9.4 Should enum values preserve original casing?
**Answer:** a) Convert to UPPER_SNAKE_CASE

---

## Section 10: Code Generation - Interfaces & Unions

### 10.1 How should GraphQL interfaces be represented?
**Answer:** a) Java sealed interface

### 10.2 How should GraphQL unions be represented?
**Answer:** a) Sealed interface

### 10.3 Should interface implementations share code?
**Answer:** c) Composition over inheritance

### 10.4 How should __typename be handled?
**Answer:** c) Both available (Jackson discriminator + explicit field)

### 10.5 Should we generate visitor pattern for unions?
**Answer:** a) No (sealed interfaces + pattern matching make visitors unnecessary)

---

## Section 11: Code Generation - Operations

### 11.1 What should operation class names be based on?
**Answer:** a) Operation name + suffix (GetUserQuery)

### 11.2 Should variables be a nested class or separate?
**Answer:** a) Nested record

### 11.3 Should operations support optional variables?
**Answer:** c) Always builder

### 11.4 How should selection set types be generated?
**Answer:** a) Operation-specific types (GetUserQuery.User)

### 11.5 Should operation documents be embedded as String or resource?
**Answer:** a) Embedded String constant (generated by type-safe builder)

### 11.6 Should we generate operation ID/hash for persisted queries?
**Answer:** b) SHA256 hash

### 11.7 Should operations be groupable by feature/domain?
**Answer:** b) Subdirectories = subpackages

---

## Section 12: Code Generation - Fragments

### 12.1 Should fragments generate separate interfaces?
**Answer:** a) Yes, fragment interface

### 12.2 How should fragment spreads be handled in types?
**Answer:** a) Implement fragment interface

### 12.3 Should inline fragments generate types?
**Answer:** a) Yes, nested type

---

## Section 13: Code Generation - Scalars

### 13.1 What default mappings for built-in scalars?
**Answer:**
- ID: `String`
- Int: `Integer`
- Float: `Double`
- Boolean: `Boolean`

### 13.2 How should custom scalars be configured?
**Answer:** a) Global mapping in plugin config

### 13.3 Should common scalars have built-in support?
**Answer:** Yes, all of the following:
- DateTime → OffsetDateTime
- Date → LocalDate
- Time → LocalTime
- BigDecimal → BigDecimal
- Long → Long
- JSON → JsonNode
- UUID → UUID
- URL → URI

### 13.4 How should unknown scalars be handled?
**Answer:** c) Generate as String

---

## Section 14: Validation

### 14.1 Should generated code validate non-null fields?
**Answer:** d) Both validation and annotations (Objects.requireNonNull + @NonNull)

### 14.2 Should inputs validate constraints (length, range)?
**Answer:** b) If defined in schema directives

### 14.3 Should operations be validated against schema at generation time?
**Answer:** a) Yes, fail on invalid operations

### 14.4 Should we validate the schema itself?
**Answer:** b) Basic validation (parseable)

---

## Section 15: Interceptors & Middleware

### 15.1 What interceptor model?
**Answer:** b) Request + Response interceptors separately

### 15.2 Should interceptors be async-aware?
**Answer:** c) Both sync and async interceptors

### 15.3 What should interceptors have access to?
**Answer:** b) GraphQL operation context too

### 15.4 Should we provide built-in interceptors?
**Answer:** All of the following:
- Logging interceptor
- Retry interceptor
- Metrics interceptor
- Auth header interceptor
- Request ID interceptor

### 15.5 Should interceptor order be configurable?
**Answer:** a) Fixed order (add order)

---

## Section 16: Caching

### 16.1 Should Graphite include caching support?
**Answer:** a) No caching (client is stateless)

### 16.2 - 16.5
**Answer:** N/A (no caching)

---

## Section 17: Batching & DataLoader

### 17.1 Should Graphite support request batching?
**Answer:** a) No batching (post-MVP)

### 17.2 - 17.3
**Answer:** N/A (no batching)

---

## Section 18: Observability

### 18.1 What logging framework should be used?
**Answer:** a) SLF4J

### 18.2 What should be logged by default?
**Answer:** d) Full request/response bodies (debug level)

### 18.3 Should we support metrics?
**Answer:** b) Micrometer integration

### 18.4 What metrics should be collected?
**Answer:** e) All of the above (request count, response times, error rates)

### 18.5 Should we support distributed tracing?
**Answer:** b) OpenTelemetry integration (via Micrometer Tracing)

### 18.6 Should trace context be propagated to GraphQL server?
**Answer:** b) Via headers (configurable)

---

## Section 19: Testing Support

### 19.1 Should we provide a mock client for testing?
**Answer:** c) Full mock server (GraphiteMockServer)

### 19.2 How should mock responses be defined?
**Answer:** c) Both (programmatic + JSON files)

### 19.3 Should we support response recording/playback?
**Answer:** b) Record to files

### 19.4 Should there be test assertions?
**Answer:** b) Custom assertions (assertThat(response).hasNoErrors())

### 19.5 Should we generate test fixtures/factories?
**Answer:** b) Test data builders

---

## Section 20: Maven Plugin Specifics

### 20.1 What Maven lifecycle phase?
**Answer:** a) generate-sources (default)

### 20.2 Should the plugin support incremental compilation?
**Answer:** b) Yes, hash-based

### 20.3 Should schema be downloadable from URL?
**Answer:** c) Yes, with caching

### 20.4 Should there be a "validate only" goal?
**Answer:** b) Yes, for CI validation

### 20.5 Should there be an "introspect" goal to download schema?
**Answer:** b) Yes

---

## Section 21: Gradle Plugin Specifics

### 21.1 Should the task support Gradle's build cache?
**Answer:** b) Yes

### 21.2 Should there be Kotlin DSL support?
**Answer:** c) Both

### 21.3 Should the plugin support Gradle's configuration cache?
**Answer:** b) Yes

### 21.4 Should there be a watch mode for development?
**Answer:** b) Yes, regenerate on file changes

---

## Section 22: IDE Integration

### 22.1 Should generated sources be marked as generated?
**Answer:** c) Both (@Generated annotation + file header comment)

### 22.2 Should we provide IntelliJ plugin?
**Answer:** c) Future consideration

### 22.3 Should generated code be formatted?
**Answer:** b) Google Java Style

---

## Section 23: Documentation

### 23.1 What documentation should be generated?
**Answer:** b) JavaDoc from GraphQL descriptions

### 23.2 Should we generate example usage code?
**Answer:** b) In JavaDoc

### 23.3 How should generated code be documented internally?
**Answer:** c) Link back to schema locations

---

## Section 24: Framework Integrations

### 24.1 Should there be Spring Boot integration?
**Answer:** b) Auto-configuration starter

### 24.2 Should there be Micronaut integration?
**Answer:** a) No

### 24.3 Should there be Quarkus integration?
**Answer:** a) No

### 24.4 Should the client be CDI/injection-ready?
**Answer:** b) @Inject support

---

## Section 25: Advanced Features

### 25.1 Should we support @defer directive?
**Answer:** c) Future consideration

### 25.2 Should we support @stream directive?
**Answer:** c) Future consideration

### 25.3 Should we support file uploads (multipart)?
**Answer:** c) Future consideration

### 25.4 Should we support persisted queries?
**Answer:** a) No (post-MVP)

### 25.5 Should we support schema extensions?
**Answer:** a) No

### 25.6 Should we support custom directives in codegen?
**Answer:** a) No

---

## Section 26: Naming Conventions

### 26.1 What naming pattern for generated packages?
**Answer:**
- `{basePackage}.type` for types
- `{basePackage}.query` for queries
- `{basePackage}.mutation` for mutations
- `{basePackage}.fragment` for fragments
- `{basePackage}.scalar` for custom scalars

### 26.2 How should type names be derived?
**Answer:** b) With configurable suffix (UserDTO, CreateUserInput, etc.)

### 26.3 How should operation names be derived?
**Answer:** From type-safe builder chain (no .graphql files)

### 26.4 Should field names be transformed?
**Answer:** b) To Java camelCase

---

## Section 27: Multi-Schema Support

### 27.1 Should Graphite support multiple schemas?
**Answer:** a) One schema per project (multi-schema is post-MVP)

### 27.2 How should cross-schema types be handled?
**Answer:** a) Completely separate

---

## Section 28: Backward Compatibility

### 28.1 How important is schema evolution support?
**Answer:** b) Support additive changes

### 28.2 Should there be a compatibility checking tool?
**Answer:** a) No (post-MVP consideration)

---

## Section 29: Performance Considerations

### 29.1 Should we pre-compile/cache operation documents?
**Answer:** c) Lazy parsing with caching

### 29.2 Should we support response streaming?
**Answer:** a) No, full response in memory

### 29.3 Should we minimize generated code size?
**Answer:** a) No, clarity over size

---

## Section 30: Miscellaneous

### 30.1 Should we support GraphQL over WebSocket (for queries/mutations)?
**Answer:** a) No, HTTP only

### 30.2 Should we support schema-first development helpers?
**Answer:** a) No (client library only)

### 30.3 Should we provide a CLI tool?
**Answer:** a) No

### 30.4 Should we support JSON Schema generation from GraphQL types?
**Answer:** a) No

### 30.5 What license should Graphite use?
**Answer:** a) Apache 2.0

### 30.6 Should we publish to Maven Central from day one?
**Answer:** a) Yes

### 30.7 What's the initial version number?
**Answer:** a) 0.1.0 (experimental)

---

## Section 31: Error Messages & Developer Experience

### 31.1 How verbose should error messages be?
**Answer:** c) Include suggestions

### 31.2 Should code generation errors include fix suggestions?
**Answer:** b) Yes, common issues only

### 31.3 Should we validate operation names are unique?
**Answer:** c) Error

---

## Section 32: Your Additional Requirements

### 32.1 Are there specific APIs or GraphQL servers you want to ensure compatibility with?
**Answer:** No specific APIs - general-purpose client

### 32.2 Are there features from other GraphQL clients you specifically want to include?
**Answer:** Type-safe query builder (like Nodes, genqlient, GraphQL Zeus)

### 32.3 Are there features you specifically want to AVOID?
**Answer:**
- .graphql files (use type-safe builder instead)
- Reactive complexity
- Plugin ecosystems
- Server-side features

### 32.4 What's your timeline expectation?
**Answer:** a) MVP quickly, iterate later

### 32.5 Any other requirements or preferences not covered above?
**Answer:** No additional requirements

---

## Summary: Confirmed Decisions

1. **Java 21** (latest LTS)
2. **Gradle build** with Maven Central publishing
3. **Java HttpClient** (built-in, no dependencies)
4. **Jackson** for serialization
5. **No subscriptions** (queries/mutations only)
6. **Group ID:** io.github.graphite
7. **Generate builders** for input types
8. **Type-safe query builder** (no .graphql files)
9. **Java Records** for DTOs
10. **Sealed interfaces** for unions/interfaces
11. **JetBrains annotations** for null safety
12. **SLF4J** for logging
13. **Micrometer** for metrics and tracing
14. **Apache 2.0** license
15. **0.1.0** initial version

---

*Document generated from interactive questionnaire session*