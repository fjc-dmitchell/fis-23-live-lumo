/**
 * Provides the core financial and administrative JPA entity model for FIS.
 *
 * <p>This package contains the persistent domain entities that represent the
 * financial, budgetary, organizational, and operational data managed by FIS.
 * These entities form the primary business data model of the application and
 * are mapped to the underlying database using Jakarta Persistence (JPA)
 * through the Jmix framework.</p>
 *
 * <p>Entities in this package model key business concepts such as
 * appropriations, funds, fund control notices, activities, obligations,
 * invoices, vendors, organizational structures, documents, projections,
 * reimbursements, and related audit records. Together, these entities define
 * the relationships, validation rules, and persistence behavior required to
 * support budgeting, financial management, reporting, and application
 * workflows.</p>
 *
 * <p>Personnel-related entities are maintained in the
 * {@code gov.fjc.fis.entity.personnel} package to provide a clear separation
 * between financial and human resources domains.</p>
 *
 * <p>Most entities participate in Jmix platform features such as data access,
 * security, auditing, entity lifecycle events, and UI data binding.</p>
 *
 * @since 2.0
 * @author Doug Mitchell
 */
package gov.fjc.fis.entity;