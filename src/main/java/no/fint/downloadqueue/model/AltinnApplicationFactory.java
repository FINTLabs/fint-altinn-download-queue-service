package no.fint.downloadqueue.model;

import no.altinn.downloadqueue.wsdl.*;
import no.fint.downloadqueue.client.AttachmentDataStreamedClient;
import no.fint.downloadqueue.client.DownloadQueueClient;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AltinnApplicationFactory {
    private final String requestor = "fylkesnummer";
    private final String requestorName = "fylke";
    private final String subjectName = "innsender";
    private final String languageCode = "language";
    private final Integer defaultLanguageCode = 1044;

    private final DownloadQueueClient downloadQueueClient;
    private final AttachmentDataStreamedClient attachmentDataStreamedClient;

    public AltinnApplicationFactory(DownloadQueueClient downloadQueueClient, AttachmentDataStreamedClient attachmentDataStreamedClient) {
        this.downloadQueueClient = downloadQueueClient;
        this.attachmentDataStreamedClient = attachmentDataStreamedClient;
    }

    public AltinnApplication of(DownloadQueueItemBE item, ArchivedFormTaskDQBE archivedFormTask) {
        AltinnApplication altinnApplication = new AltinnApplication();

        addMetadata(item)
                .andThen(addForms(archivedFormTask))
                .andThen(addAttachments(archivedFormTask))
                .accept(altinnApplication);

        altinnApplication.setStatus(AltinnApplicationStatus.NEW);

        return altinnApplication;
    }

    private Consumer<AltinnApplication> addMetadata(DownloadQueueItemBE item) {
        return application -> {
            application.setArchiveReference(item.getArchiveReference().getValue());
            application.setSubject(item.getReporteeID().getValue());
            application.setServiceCode(item.getServiceCode().getValue());

            ArchivedShipmentMetadataList metadata = item.getShipmentMetadataList().getValue();

            getMetadata(metadata, requestor).map(countyNumberMapping::get).ifPresent(application::setRequestor);
            getMetadata(metadata, requestorName).ifPresent(application::setRequestorName);
            getMetadata(metadata, subjectName).ifPresent(application::setSubjectName);

            application.setLanguageCode(getMetadata(metadata, languageCode).map(Integer::parseInt).orElse(defaultLanguageCode));
        };
    }

    private Consumer<AltinnApplication> addForms(ArchivedFormTaskDQBE archivedFormTask) {
        return altinnApplication -> Optional.ofNullable(archivedFormTask)
                .map(ArchivedFormTaskDQBE::getForms)
                .map(JAXBElement::getValue)
                .map(ArchivedFormListDQBE::getArchivedFormDQBE)
                .orElseGet(Collections::emptyList)
                .stream()
                .findFirst()
                .ifPresent(archivedForm -> {
                    AltinnApplication.Form form = new AltinnApplication.Form();

                    form.setFormData(archivedForm.getFormData().getValue());

                    byte[] formDataPdf;

                    if (altinnApplication.getLanguageCode() == null) {
                        formDataPdf = downloadQueueClient.getFormSetPdf(altinnApplication.getArchiveReference(), defaultLanguageCode);
                    } else {
                        formDataPdf = downloadQueueClient.getFormSetPdf(altinnApplication.getArchiveReference(), altinnApplication.getLanguageCode());
                    }

                    form.setFormDataPdf(formDataPdf);

                    altinnApplication.setForm(form);
                });
    }

    private Consumer<AltinnApplication> addAttachments(ArchivedFormTaskDQBE archivedFormTask) {
        return altinnApplication -> Optional.ofNullable(archivedFormTask)
                .map(ArchivedFormTaskDQBE::getAttachments)
                .map(JAXBElement::getValue)
                .map(ArchivedAttachmentExternalListDQBE::getArchivedAttachmentDQBE)
                .orElseGet(Collections::emptyList)
                .forEach(archivedAttachment -> {
                    AltinnApplication.Attachment attachment = new AltinnApplication.Attachment();

                    byte[] attachmentData;

                    if (archivedAttachment.getAttachmentData() == null) {
                        attachmentData = attachmentDataStreamedClient.getAttachmentDataStreamed(archivedAttachment.getAttachmentId());
                    } else {
                        attachmentData = archivedAttachment.getAttachmentData().getValue();
                    }

                    attachment.setAttachmentData(attachmentData);

                    attachment.setAttachmentId(archivedAttachment.getAttachmentId());
                    attachment.setAttachmentType(archivedAttachment.getAttachmentType().getValue());
                    attachment.setAttachmentTypeName(archivedAttachment.getAttachmentTypeName().getValue());
                    attachment.setAttachmentTypeNameLanguage(archivedAttachment.getAttachmentTypeNameLanguage().getValue());

                    altinnApplication.getAttachments().add(attachment);
                });
    }

    private Optional<String> getMetadata(ArchivedShipmentMetadataList metadataList, String key) {
        return Optional.ofNullable(metadataList)
                .map(ArchivedShipmentMetadataList::getArchivedShipmentMetadata)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(metadata -> metadata.getKey().getValue().equals(key))
                .map(ArchivedShipmentMetadata::getValue)
                .map(JAXBElement::getValue)
                .findAny();
    }

    private Optional<LocalDateTime> getDateTime(XMLGregorianCalendar dateTime) {
        return Optional.ofNullable(dateTime)
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .map(GregorianCalendar::toZonedDateTime)
                .map(ZonedDateTime::toLocalDateTime);
    }

    private final Map<String, String> countyNumberMapping = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>("3000", "921693230"), //Viken
            new AbstractMap.SimpleImmutableEntry<>("0300", "958935420"), //Oslo
            new AbstractMap.SimpleImmutableEntry<>("3400", "920717152"), //Innlandet
            new AbstractMap.SimpleImmutableEntry<>("3800", "821227062"), //Vestfold og Telemark
            new AbstractMap.SimpleImmutableEntry<>("4200", "921707134"), //Agder
            new AbstractMap.SimpleImmutableEntry<>("1100", "971045698"), //Rogaland
            new AbstractMap.SimpleImmutableEntry<>("4600", "821311632"), //Vestland
            new AbstractMap.SimpleImmutableEntry<>("1500", "944183779"), //Møre og Romsdal
            new AbstractMap.SimpleImmutableEntry<>("5000", "817920632"), //Trøndelang
            new AbstractMap.SimpleImmutableEntry<>("1800", "964982953"), //Nordland
            new AbstractMap.SimpleImmutableEntry<>("5400", "922420866")) //Troms og Finnmark
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
}
