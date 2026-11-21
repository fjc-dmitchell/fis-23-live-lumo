/**
 * Provides application event listeners for the FIS.
 *
 * <p>This package contains Spring and Jmix event listener implementations that
 * respond to entity lifecycle events, application events, user actions, and
 * system notifications. These listeners encapsulate business logic that must
 * be executed when specific events occur, helping to keep entity classes and
 * services focused on their primary responsibilities.</p>
 *
 * <p>Listeners in this package are used to perform tasks such as maintaining
 * audit records, synchronizing related data, enforcing business rules,
 * publishing application events, updating user session state, refreshing
 * cached information, and coordinating interactions between different areas
 * of the system.</p>
 *
 * <p>By leveraging the Jmix event framework and Spring's event infrastructure,
 * these listeners support a loosely coupled architecture in which business
 * processes can react to changes without introducing direct dependencies
 * between components.</p>
 *
 * @since 2.0
 * @author Doug Mitchell
 */
package gov.fjc.fis.listener;