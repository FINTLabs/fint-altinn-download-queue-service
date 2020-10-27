package no.fint.downloadqueue.model;

import no.altinn.downloadqueue.wsdl.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AltinnApplicationFactory {
    private final static String REQUESTOR = "fylkesnummer";
    private final static String REQUESTOR_NAME = "fylke";
    private final static String SUBJECT_NAME = "innsender";
    private final static String LANGUAGE_CODE = "language";
    private final static Integer DEFAULT_LANGUAGE_CODE = 1044;

    public AltinnApplicationFactory() {
    }

    public static AltinnApplication of(DownloadQueueItemBE item, ArchivedFormTaskDQBE archivedFormTask) {
        AltinnApplication altinnApplication = new AltinnApplication();

        addMetadata(item)
                .andThen(addForms(archivedFormTask))
                .andThen(addAttachments(archivedFormTask))
                .accept(altinnApplication);

        altinnApplication.setStatus(AltinnApplicationStatus.NEW);

        return altinnApplication;
    }

    private static Consumer<AltinnApplication> addMetadata(DownloadQueueItemBE item) {
        return application -> {
            application.setArchiveReference(item.getArchiveReference().getValue());
            application.setSubject(item.getReporteeID().getValue());
            application.setServiceCode(item.getServiceCode().getValue());

            getDateTime(item.getArchivedDate()).ifPresent(application::setArchivedDate);

            ArchivedShipmentMetadataList metadata = item.getShipmentMetadataList().getValue();

            getMetadata(metadata, REQUESTOR).map(countyNumberMapping::get).ifPresent(application::setRequestor);
            getMetadata(metadata, REQUESTOR_NAME).ifPresent(application::setRequestorName);
            getMetadata(metadata, SUBJECT_NAME).ifPresent(application::setSubjectName);

            application.setLanguageCode(getMetadata(metadata, LANGUAGE_CODE).map(Integer::parseInt).orElse(DEFAULT_LANGUAGE_CODE));
        };
    }

    private static Consumer<AltinnApplication> addForms(ArchivedFormTaskDQBE archivedFormTask) {
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

                    altinnApplication.setForm(form);
                });
    }

    private static Consumer<AltinnApplication> addAttachments(ArchivedFormTaskDQBE archivedFormTask) {
        return altinnApplication -> Optional.ofNullable(archivedFormTask)
                .map(ArchivedFormTaskDQBE::getAttachments)
                .map(JAXBElement::getValue)
                .map(ArchivedAttachmentExternalListDQBE::getArchivedAttachmentDQBE)
                .orElseGet(Collections::emptyList)
                .forEach(archivedAttachment -> {
                    AltinnApplication.Attachment attachment = new AltinnApplication.Attachment();

                    attachment.setAttachmentId(archivedAttachment.getAttachmentId());
                    attachment.setAttachmentTypeName(archivedAttachment.getAttachmentTypeName().getValue());
                    attachment.setAttachmentTypeNameLanguage(archivedAttachment.getAttachmentTypeNameLanguage().getValue());

                    String attachmentType = archivedAttachment.getAttachmentType().getValue().replaceFirst("_", "/");

                    try {
                        MediaType mediaType = MediaType.parseMediaType(attachmentType);
                        attachment.setAttachmentType(mediaType.toString());
                    } catch (InvalidMediaTypeException ex) {
                        attachment.setAttachmentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                    }

                    String fileExtension = StringUtils.substringAfterLast(archivedAttachment.getFileName().getValue(), ".");
                    attachment.setFileName(archivedAttachment.getAttachmentTypeName().getValue().concat("." + fileExtension));

                    altinnApplication.getAttachments().put(attachment.getAttachmentId(), attachment);
                });
    }

    private static Optional<String> getMetadata(ArchivedShipmentMetadataList metadataList, String key) {
        return Optional.ofNullable(metadataList)
                .map(ArchivedShipmentMetadataList::getArchivedShipmentMetadata)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(metadata -> metadata.getKey().getValue().equals(key))
                .map(ArchivedShipmentMetadata::getValue)
                .map(JAXBElement::getValue)
                .findAny();
    }

    private static Optional<LocalDateTime> getDateTime(XMLGregorianCalendar dateTime) {
        return Optional.ofNullable(dateTime)
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .map(GregorianCalendar::toZonedDateTime)
                .map(ZonedDateTime::toLocalDateTime);
    }

    private static final Map<String, String> countyNumberMapping = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>("30", "921693230"), //Viken
            new AbstractMap.SimpleImmutableEntry<>("03", "958935420"), //Oslo
            new AbstractMap.SimpleImmutableEntry<>("34", "920717152"), //Innlandet
            new AbstractMap.SimpleImmutableEntry<>("38", "821227062"), //Vestfold og Telemark
            new AbstractMap.SimpleImmutableEntry<>("42", "921707134"), //Agder
            new AbstractMap.SimpleImmutableEntry<>("11", "971045698"), //Rogaland
            new AbstractMap.SimpleImmutableEntry<>("46", "821311632"), //Vestland
            new AbstractMap.SimpleImmutableEntry<>("15", "944183779"), //Møre og Romsdal
            new AbstractMap.SimpleImmutableEntry<>("50", "817920632"), //Trøndelang
            new AbstractMap.SimpleImmutableEntry<>("18", "964982953"), //Nordland
            new AbstractMap.SimpleImmutableEntry<>("54", "922420866")) //Troms og Finnmark
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
}
