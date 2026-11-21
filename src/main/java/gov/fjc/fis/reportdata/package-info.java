/**
 * Report parameter objects used by FIS.
 * <p>
 * This package contains classes that encapsulate the data required to execute
 * reports. Report data objects provide a single, strongly typed parameter that
 * can be supplied to Jmix reporting services, simplifying report invocation
 * and reducing the need to pass numerous individual parameters.
 * <p>
 * Classes in this package are commonly used with Jmix reporting components
 * such as:
 * <ul>
 *     <li>{@code ReportRunner}</li>
 *     <li>{@code UiReportRunner}</li>
 *     <li>{@code FluentUiReportRunner}</li>
 * </ul>
 * <p>
 * A report data object may contain report criteria, filtering options,
 * reporting periods, entity references, user selections, calculated values,
 * and other information required to generate a report. By gathering all
 * report inputs into a single object, report execution code becomes more
 * maintainable, extensible, and easier to understand.
 * <p>
 * Classes in this package are not persisted and typically serve as temporary
 * data containers that transfer report parameters between user interface
 * components, services, and reporting engines.
 *
 * @author Doug Mitchell
 */
package gov.fjc.fis.reportdata;