/**
 * Quartz jobs, import models, validators, processors, and supporting classes
 * used to integrate FIS with JIFMS.
 * <p>
 * This package implements the automated workflow for importing purchase order
 * and travel authorization data received from JIFMS feed files. Import jobs
 * read and validate source records, then create or update
 * {@link gov.fjc.fis.entity.Document} entities that represent JIFMS financial
 * transactions within FIS.
 * </p>
 * <p>
 * Processing jobs subsequently evaluate imported documents, apply business
 * rules, resolve related FIS entities, and create corresponding
 * {@link gov.fjc.fis.entity.DocumentAudit} records to document processing
 * outcomes. Depending on the transaction data and existing system state,
 * processing may insert or update {@link gov.fjc.fis.entity.Obligation}
 * entities and related records such as activities, projections, allocations,
 * vendors, and Fund Control Notices (FCNs).
 * </p>
 * <p>
 * The package uses composable validation and processing pipelines to perform
 * document validation, obligation creation and maintenance, audit logging,
 * and synchronization between JIFMS source data and FIS financial records.
 * </p>
 * @author Doug Mitchell
 */
package gov.fjc.fis.job.jifms;