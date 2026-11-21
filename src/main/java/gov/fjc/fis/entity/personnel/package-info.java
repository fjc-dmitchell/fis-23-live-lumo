/**
 * Provides the personnel and human resources JPA entity model for the
 * Financial Information System (FIS).
 *
 * <p>This package contains the persistent domain entities that represent
 * employees, positions, compensation structures, personnel actions, and
 * related workforce planning data. These entities are mapped to the underlying
 * database using Jakarta Persistence (JPA) through the Jmix framework and
 * support personnel budgeting, staffing analysis, compensation projections,
 * and workforce management processes.</p>
 *
 * <p>Entities in this package model key human resources concepts such as
 * employees, positions, pay grades, pay grade rates, personnel actions,
 * and associated audit records. Employee data is synchronized from external
 * HRMIS interfaces, while position and planning entities support budgeting
 * and forecasting activities within FIS.</p>
 *
 * <p>The personnel domain integrates with the financial domain to support
 * compensation and benefits projections, workforce cost planning, and budget
 * reporting. Financial entities and budget execution records are maintained
 * separately in the {@code gov.fjc.fis.entity} package.</p>
 *
 * <p>Most entities participate in Jmix platform features such as data access,
 * security, auditing, entity lifecycle events, and UI data binding.</p>
 *
 * @since 2.0
 * @author Doug Mitchell
 */
package gov.fjc.fis.entity.personnel;