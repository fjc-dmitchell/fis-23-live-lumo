/**
 * Contains Quartz jobs and supporting classes used to import personnel data
 * from HRMIS.
 * <p>
 * The HRMIS jobs process employee feed files received from external personnel
 * systems, load employee data into FIS, and synchronize imported
 * {@link gov.fjc.fis.entity.personnel.Employee} records with
 * {@link gov.fjc.fis.entity.personnel.Position} records used for budgeting,
 * personnel planning, salary projections, and workforce management.
 * </p>
 *
 * <p>
 * Package classes are responsible for reading and validating HRMIS feed data,
 * importing employee records, creating and updating positions, and maintaining
 * audit records that document changes resulting from synchronization.
 * </p>
 *
 * @author Doug Mitchell
 */
package gov.fjc.fis.job.hrmis;
