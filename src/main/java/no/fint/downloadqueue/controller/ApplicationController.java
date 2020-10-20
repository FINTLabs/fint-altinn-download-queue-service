package no.fint.downloadqueue.controller;

import no.fint.downloadqueue.client.DownloadQueueClient;
import no.fint.downloadqueue.exception.AltinnFaultException;
import org.springframework.http.HttpStatus;
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

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(AltinnFaultException.class)
    public void notFound() {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public void badRequest() {
    }
}
