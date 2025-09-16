package no.novari.downloadqueue.controller;

import no.novari.downloadqueue.client.DownloadQueueClient;
import no.novari.downloadqueue.exception.AltinnFaultException;
import no.novari.downloadqueue.util.AltinnFaultPretty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class ApplicationController {
    private final DownloadQueueClient downloadQueueClient;

    public ApplicationController(DownloadQueueClient downloadQueueClient) {
        this.downloadQueueClient = downloadQueueClient;
    }

    @GetMapping("/{archiveReference}")
    public @ResponseBody byte[] getApplication(@PathVariable String archiveReference, @RequestParam Integer languageCode) {
        return downloadQueueClient.getFormSetPdf(archiveReference, languageCode);
    }

    @ExceptionHandler(AltinnFaultException.class)
    public ResponseEntity<AltinnFaultPretty> altinnFaultException(AltinnFaultException altinnFaultException) {
        AltinnFaultPretty altinnFaultPretty = AltinnFaultPretty.of(altinnFaultException.getAltinnFault());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(altinnFaultPretty);
    }
}
