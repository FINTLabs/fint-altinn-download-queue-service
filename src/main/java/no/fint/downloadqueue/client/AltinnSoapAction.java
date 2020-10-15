package no.fint.downloadqueue.client;

public final class AltinnSoapAction {
    public static final String GET_DOWNLOAD_QUEUE_ITEMS = "http://www.altinn.no/services/Archive/DownloadQueue/2012/08/IDownloadQueueExternalBasic/GetDownloadQueueItems";
    public static final String PURGE_ITEM = "http://www.altinn.no/services/Archive/DownloadQueue/2012/08/IDownloadQueueExternalBasic/PurgeItem";
    public static final String GET_ARCHIVED_FORM_TASK = "http://www.altinn.no/services/Archive/DownloadQueue/2012/08/IDownloadQueueExternalBasic/GetArchivedFormTaskBasicDQ";
    public static final String GET_FORM_SET_PDF = "http://www.altinn.no/services/Archive/DownloadQueue/2012/08/IDownloadQueueExternalBasic/GetFormSetPdfBasic";
    public static final String GET_ATTACHMENT_DATA_STREAMED = "http://www.altinn.no/services/Archive/ServiceOwnerArchive/2013/06/IServiceOwnerArchiveExternalStreamedBasic/GetAttachmentDataStreamedBasic";

    private AltinnSoapAction() {
    }
}
