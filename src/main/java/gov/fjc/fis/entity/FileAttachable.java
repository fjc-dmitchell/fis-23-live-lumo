package gov.fjc.fis.entity;

/**
 * interface should be implemented by any entity class that allows file attachments.
 * Currently used by Activity, Obligation, Invoice, and FundControlNotice.
 * <p>
 * Created to solve an issue with Obligation. If an attachment is added to an
 * Invoice or FCN from the Obligation detail view, the list of attachments
 * on the obligation view should be <em>immediately</em> refreshed using a load delegate
 * in the file attachment fragment. This issue is unique to FileAttachment
 * as it has associations to Activity and Obligation even when attached to
 * Invoice or FundControlNotice.
 * <p>
 * This also avoids "Object smell" that would otherwise occur in FileAttachmentFragment's
 * setHostEntity and FileAttachmentService if Object was allowed to be
 * passed a method parameters.
 */
public interface FileAttachable { }
