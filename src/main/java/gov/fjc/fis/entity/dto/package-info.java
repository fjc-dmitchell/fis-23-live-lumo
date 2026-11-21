/**
 * Data Transfer Objects (DTOs) used by FIS.
 * <p>
 * This package contains lightweight data structures that transfer information
 * between application layers, reports, integrations, and external clients
 * without exposing the underlying domain entities.
 * <p>
 * DTOs in this package are commonly used for:
 * <ul>
 *     <li>Providing data to reports and report-generation processes</li>
 *     <li>Serving request and response payloads for REST services</li>
 *     <li>Representing projections, summaries, and aggregated financial data</li>
 *     <li>Exchanging data between application modules and integrations</li>
 *     <li>Reducing payload size by exposing only the information required by
 *         a specific use case</li>
 * </ul>
 * <p>
 * DTOs may combine data from multiple entities, services, or business
 * processes into a single object. This allows clients to retrieve related
 * information through a single service call without needing to understand
 * the application's internal domain model or perform multiple requests.
 * <p>
 * Classes in this package are not persisted and typically serve as
 * serializable representations of business data tailored to specific
 * reporting, presentation, or integration requirements.
 *
 * @author Doug Mitchell
 */
package gov.fjc.fis.entity.dto;