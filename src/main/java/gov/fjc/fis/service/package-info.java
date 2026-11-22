/**
 * Business services for FIS.
 * <p>
 * This package contains Spring-managed service components that implement
 * business logic, data access operations, and other reusable application
 * functionality. Services are typically annotated with {@code @Component}
 * and are intended to encapsulate logic that should be shared across
 * multiple views, jobs, listeners, and other application components.
 * <p>
 * Most services are associated with a specific domain entity and provide a
 * centralized location for reusable database queries, entity-related
 * business rules, validation support, and conversions between entities and
 * Data Transfer Objects (DTOs). By consolidating this functionality into
 * services, the application promotes consistency and avoids duplication of
 * logic across the codebase.
 * <p>
 * Services in this package form the primary business layer of the
 * application, providing a clear separation between the presentation layer,
 * persistence layer, and domain model.
 * <p>
 * Report-specific services are located in the
 * {@code gov.fjc.fis.service.report} subpackage.
 *
 * @author Doug Mitchell
 */
package gov.fjc.fis.service;