/**
 * Reusable search fragments for FIS entity list views.
 * <p>
 * This package provides a configurable search framework built on Jmix Flow UI
 * fragments and property filters. The framework enables a consistent search
 * experience across FIS entity views while allowing individual entity types
 * to define additional search criteria and behavior.
 * </p>
 * <p>
 * {@link gov.fjc.fis.view.search.CustomSearchFragment} provides common search
 * functionality shared across entities, including budget fiscal year, fund,
 * division, object category, object class, branch, group, file attachment
 * category, quick searches, saved search criteria, and searches based on
 * selected records. The fragment dynamically loads an
 * {@link gov.fjc.fis.view.search.EntitySearchFragment} implementation based on
 * the entity being viewed and combines entity-specific criteria with the
 * common search conditions before executing the underlying data loader.
 * </p>
 * <p>
 * Entity-specific search fragments extend
 * {@link gov.fjc.fis.view.search.EntitySearchFragment} to provide additional
 * search fields, restore saved filter values and operations, and optionally
 * incorporate shared search controls into custom layouts. Examples include
 * search fragments for activities, obligations, invoices, Fund Control
 * Notices (FCNs), projections, audits, and file attachments.
 * </p>
 * <p>
 * Search criteria are persisted in the user session and restored when a view
 * is reopened, allowing users to maintain search context across navigation
 * within the application.
 * </p>
 * @author Doug Mitchell
 */
package gov.fjc.fis.view.search;