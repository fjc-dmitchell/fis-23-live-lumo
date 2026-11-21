package gov.fjc.fis.entity;

/**
 * Marker interface for entity types that support file attachments.
 *
 * <p>Implemented by entities that can serve as the host of one or more
 * file attachments, including {@link Activity}, {@link Obligation},
 * {@link Invoice}, and {@link FundControlNotice}.
 *
 * <p>This interface provides a common type for attachment-related
 * components and services, allowing them to operate on supported
 * entities without relying on {@code Object} parameters or
 * entity-specific logic.
 *
 * <p>The interface was introduced to support attachment refresh behavior
 * when attachments are added from related entity views. In particular,
 * attachments associated with Invoice or FundControlNotice records may
 * also need to be reflected immediately within the corresponding
 * Obligation view because {@code FileAttachment} maintains relationships
 * with both Activity and Obligation entities.
 *
 * <p>Using a dedicated marker interface improves type safety and avoids
 * the use of generic {@code Object} parameters in attachment-related
 * services and UI components.
 *
 * @author Doug Mitchell
 */
public interface FileAttachable { }
