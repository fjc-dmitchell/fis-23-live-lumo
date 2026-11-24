/**
 * Provides application-wide utility classes and helper methods used throughout
 * FIS.
 * <p>
 * This package contains stateless utility classes that encapsulate common
 * functionality shared across multiple modules of the application. The methods
 * provided by these classes support tasks such as string manipulation,
 * date handling, numeric calculations, report navigation, data formatting,
 * framework integration, and other reusable operations that do not belong
 * to a specific business entity or service.
 * <p>
 * Examples of utilities in this package include:
 * <ul>
 *     <li>{@code ReportRouter} for report navigation and routing support</li>
 *     <li>{@code AsciiSanitizer} for normalizing and sanitizing text data</li>
 *     <li>{@code BigDecimalUtils} for financial and numeric calculations</li>
 *     <li>{@code DateUtils} for date-related helper methods</li>
 *     <li>{@code StringUtils} for string processing and validation</li>
 *     <li>{@code FrameworkUtils} for framework-specific convenience methods</li>
 *     <li>{@code FisConstants} for application constants (use judiciously)</li>
 * </ul>
 * <p>
 * Classes in this package are intended to be reusable throughout the FIS
 * application and generally provide static methods that can be invoked from
 * any layer of the system, including entities, services, views, reports,
 * jobs, listeners, and other application components.
 *
 * @author Doug Mitchell
 */
package gov.fjc.fis.app;
