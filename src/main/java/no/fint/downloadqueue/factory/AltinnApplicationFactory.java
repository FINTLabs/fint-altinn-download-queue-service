package no.fint.downloadqueue.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import no.altinn.downloadqueue.wsdl.*;
import no.fint.altinn.model.AltinnApplication;
import no.fint.altinn.model.AltinnApplicationStatus;
import no.fint.altinn.model.AltinnForm;
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

@Slf4j
public class AltinnApplicationFactory {
    private static final ObjectMapper objectMapper = new XmlMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final static Integer DEFAULT_LANGUAGE_CODE = 1044;

    public static AltinnApplication of(DownloadQueueItemBE item, ArchivedFormTaskDQBE archivedFormTask) {
        AltinnApplication altinnApplication = new AltinnApplication();

        addMetadata(item)
                .andThen(addForms(archivedFormTask))
                .andThen(addAttachments(archivedFormTask))
                .accept(altinnApplication);

        if (altinnApplication.getRequestor() == null) {
            altinnApplication.setStatus(AltinnApplicationStatus.REQUESTOR_MISSING);
        } else {
            altinnApplication.setStatus(AltinnApplicationStatus.NEW);
        }

        return altinnApplication;
    }

    private static Consumer<AltinnApplication> addMetadata(DownloadQueueItemBE item) {
        return application -> {
            application.setArchiveReference(item.getArchiveReference().getValue());
            application.setSubject(item.getReporteeID().getValue());
            application.setServiceCode(item.getServiceCode().getValue());

            getDateTime(item.getArchivedDate()).ifPresent(application::setArchivedDate);
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

                    String formData = archivedForm.getFormData().getValue();

                    form.setFormData(formData);

                    try {
                        AltinnForm altinnForm = objectMapper.readValue(archivedForm.getFormData().getValue(), AltinnForm.class);

                        Optional.ofNullable(altinnForm)
                                .map(AltinnForm::getSubmitter)
                                .map(submitter -> {
                                    Integer languageCode = Optional.ofNullable(submitter.getLanguage()).map(Integer::parseInt).orElse(DEFAULT_LANGUAGE_CODE);
                                    altinnApplication.setLanguageCode(languageCode);

                                    return submitter;
                                })
                                .map(AltinnForm.Submitter::getOrganisation)
                                .ifPresent(organisation -> {
                                    String requestor = countyNumberMapping.get(organisation.getCountyNumber());

                                    altinnApplication.setRequestor(requestor);
                                    altinnApplication.setRequestorName(organisation.getCounty());
                                    altinnApplication.setSubjectName(organisation.getName());
                                    altinnApplication.setPhone(organisation.getPhone());
                                    altinnApplication.setEmail(organisation.getEmail());

                                    AltinnApplication.Address businessAddress = new AltinnApplication.Address();
                                    Optional.ofNullable(organisation.getBusinessAddress())
                                            .ifPresent(address -> {
                                                businessAddress.setAddress(address.getAddress());
                                                businessAddress.setPostCode(address.getPostCode());
                                                businessAddress.setPostalArea(address.getPostalArea());
                                            });

                                    altinnApplication.setBusinessAddress(businessAddress);
                                });

                    } catch (JsonProcessingException e) {
                        log.warn("Error reading form data of archive reference {}", archivedForm.getReference());
                    }

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

                    if (fileExtension.isEmpty()) {
                        log.warn("Missing file extension for attachment {} of archive reference {}", attachment.getAttachmentId(), archivedAttachment.getArchiveReference().getValue());
                        fileExtension = "taxi";
                    }

                    attachment.setFileName(archivedAttachment.getAttachmentTypeName().getValue().concat("." + fileExtension));

                    altinnApplication.getAttachments().put(attachment.getAttachmentId(), attachment);
                });
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
