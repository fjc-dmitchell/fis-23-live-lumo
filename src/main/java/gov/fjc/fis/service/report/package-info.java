/**
 * Report services for the FIS.
 * <p>
 * This package contains Spring-managed service components responsible for
 * assembling, calculating, and transforming data used by reports. Report
 * services coordinate information from multiple business domains and apply
 * report-specific business rules to produce the data structures required for
 * reporting.
 * <p>
 * Unlike entity-oriented services, report services typically do not perform
 * direct database queries. Instead, they rely on services in the
 * {@code gov.fjc.fis.service} package to retrieve and validate data. This
 * separation promotes reuse of business logic and provides a clear distinction
 * between data access operations and report generation.
 * <p>
 * Report services commonly perform tasks such as:
 * <ul>
 *     <li>Aggregating data from multiple entities and services</li>
 *     <li>Applying reporting calculations and business rules</li>
 *     <li>Generating report-specific summaries and projections</li>
 *     <li>Populating DTOs and report data objects</li>
 *     <li>Preparing data for Jmix reporting components and custom reports</li>
 * </ul>
 * <p>
 * Classes in this package form the reporting business layer of the
 * application and provide a centralized location for report generation
 * logic that would otherwise be duplicated across reports, views, and
 * scheduled processes.
 *
 * @author Doug Mitchell
 */
package gov.fjc.fis.service.report;