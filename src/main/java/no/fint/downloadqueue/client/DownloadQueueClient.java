package no.fint.downloadqueue.client;

import no.fint.downloadqueue.configuration.AltinnProperties;
import no.altinn.downloadqueue.wsdl.*;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import javax.xml.bind.JAXBElement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class DownloadQueueClient extends WebServiceGatewaySupport {
    private final ObjectFactory objectFactory = new ObjectFactory();
    private final AltinnProperties altinnProperties;

    public DownloadQueueClient(AltinnProperties altinnProperties) {
        this.altinnProperties = altinnProperties;

        setDefaultUri(altinnProperties.getDownloadQueueUri());
        setMarshaller(marshaller());
        setUnmarshaller(marshaller());

        ClientInterceptor[] interceptors = {new AltinnClientInterceptor()};
        setInterceptors(interceptors);
    }

    public List<DownloadQueueItemBE> getDownloadQueueItems() {
        GetDownloadQueueItems request = new GetDownloadQueueItems();

        request.setServiceCode(objectFactory.createGetDownloadQueueItemsServiceCode(altinnProperties.getServiceCode()));
        request.setSystemUserName(objectFactory.createGetDownloadQueueItemsSystemUserName(altinnProperties.getSystemUsername()));
        request.setSystemPassword(objectFactory.createGetDownloadQueueItemsSystemPassword(altinnProperties.getSystemPassword()));

        GetDownloadQueueItemsResponse response = (GetDownloadQueueItemsResponse) getResponse(request, AltinnSoapAction.GET_DOWNLOAD_QUEUE_ITEMS);

        return Optional.ofNullable(response)
                .map(GetDownloadQueueItemsResponse::getGetDownloadQueueItemsResult)
                .map(JAXBElement::getValue)
                .map(DownloadQueueItemBEList::getDownloadQueueItemBE)
                .orElseGet(Collections::emptyList);
    }

    public Optional<String> purgeItem(String archiveReference) {
        PurgeItem request = new PurgeItem();

        request.setArchiveReference(objectFactory.createPurgeItemArchiveReference(archiveReference));
        request.setSystemUserName(objectFactory.createGetDownloadQueueItemsSystemUserName(altinnProperties.getSystemUsername()));
        request.setSystemPassword(objectFactory.createGetDownloadQueueItemsSystemPassword(altinnProperties.getSystemPassword()));

        PurgeItemResponse response = (PurgeItemResponse) getResponse(request, AltinnSoapAction.PURGE_ITEM);

        return Optional.ofNullable(response)
                .map(PurgeItemResponse::getPurgeItemResult)
                .map(JAXBElement::getValue);
    }

    public Optional<ArchivedFormTaskDQBE> getArchivedFormTask(String archiveReference) {
        GetArchivedFormTaskBasicDQ request = new GetArchivedFormTaskBasicDQ();

        request.setArchiveReference(objectFactory.createPurgeItemArchiveReference(archiveReference));
        request.setSystemUserName(objectFactory.createGetDownloadQueueItemsSystemUserName(altinnProperties.getSystemUsername()));
        request.setSystemPassword(objectFactory.createGetDownloadQueueItemsSystemPassword(altinnProperties.getSystemPassword()));

        GetArchivedFormTaskBasicDQResponse response = (GetArchivedFormTaskBasicDQResponse) getResponse(request, AltinnSoapAction.GET_ARCHIVED_FORM_TASK);

        return Optional.ofNullable(response)
                .map(GetArchivedFormTaskBasicDQResponse::getGetArchivedFormTaskBasicDQResult)
                .map(JAXBElement::getValue);
    }

    public byte[] getFormSetPdf(String archiveReference, int languageId) {
        GetFormSetPdfBasic request = new GetFormSetPdfBasic();

        request.setArchiveReference(archiveReference);
        request.setSystemName(altinnProperties.getSystemUsername());
        request.setSystemPassword(altinnProperties.getSystemPassword());
        request.setLanguageId(languageId);

        /*
        request.setDataFormatId();
        request.setDataFormatVersion();
         */

        GetFormSetPdfBasicResponse response = (GetFormSetPdfBasicResponse) getResponse(request, AltinnSoapAction.GET_FORM_SET_PDF);

        return Optional.ofNullable(response)
                .map(GetFormSetPdfBasicResponse::getGetFormSetPdfBasicResult)
                .map(JAXBElement::getValue)
                .orElse(new byte[0]);
    }

    private Object getResponse(Object request, String soapAction) {
        return getWebServiceTemplate().marshalSendAndReceive(request, new SoapActionCallback(soapAction));
    }

    public static Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("no.altinn.downloadqueue.wsdl");
        marshaller.setMtomEnabled(true);
        return marshaller;
    }
}
