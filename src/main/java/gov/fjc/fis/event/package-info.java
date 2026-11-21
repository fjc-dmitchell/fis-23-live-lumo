/**
 * Application events used throughout FIS.
 * <p>
 * This package contains events that are published and consumed using
 * Spring's standard {@link org.springframework.context.ApplicationEvent}
 * infrastructure. Events provide a loosely coupled mechanism for notifying
 * interested components when significant application, business, or user
 * interface actions occur.
 * <p>
 * Events in this package are commonly used to:
 * <ul>
 *     <li>Broadcast application-wide state changes</li>
 *     <li>Notify user interface components of updates that require a refresh</li>
 *     <li>Coordinate processing between independent application modules</li>
 *     <li>Communicate lifecycle events for business entities and services</li>
 *     <li>Trigger background processing and asynchronous workflows</li>
 * </ul>
 * <p>
 * Typical examples include changes to fiscal year configuration, updates to
 * user session settings, modifications to system-wide messages, and other
 * events that must be communicated to multiple listeners throughout the
 * application.
 * <p>
 * By leveraging Spring's event framework, publishers remain independent of
 * event consumers, promoting a modular and maintainable architecture within
 * the FIS application.
 *
 * @author Doug Mitchell
 */
package gov.fjc.fis.event;