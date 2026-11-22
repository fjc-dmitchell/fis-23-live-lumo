/**
 * Provides Quartz job implementations that execute scheduled business
 * processes within FIS.
 *
 * <p>The subpackages in this package organize jobs by functional area and
 * external system integration. These jobs automate recurring operations such
 * as importing data, validating records, processing transactions, generating
 * reports, sending notifications, and synchronizing information with external
 * systems.
 *
 * <p>Job packages typically encapsulate all business logic, services,
 * processing pipelines, and supporting classes required for a particular
 * scheduled process. Examples include loading and processing data received
 * from external systems such as HRMIS and JIFMS, as well as generating
 * reports and performing related post-processing activities.
 *
 * <p>Most jobs are executed by the Quartz scheduler and are designed to run
 * without user interaction, allowing FIS to perform routine operational and
 * integration tasks automatically.
 *
 * @author Doug Mitchell
 */
package gov.fjc.fis.job;