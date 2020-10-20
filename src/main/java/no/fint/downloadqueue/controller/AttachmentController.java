package no.fint.downloadqueue.controller;

import no.fint.downloadqueue.client.AttachmentDataStreamedClient;
import no.fint.downloadqueue.exception.AltinnFaultException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {
    private final AttachmentDataStreamedClient attachmentDataStreamedClient;

    public AttachmentController(AttachmentDataStreamedClient attachmentDataStreamedClient) {
        this.attachmentDataStreamedClient = attachmentDataStreamedClient;
    }

    @GetMapping("/{attachmentId}")
    public @ResponseBody byte[] getApplication(@PathVariable Integer attachmentId) {
        return attachmentDataStreamedClient.getAttachmentDataStreamed(attachmentId);
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