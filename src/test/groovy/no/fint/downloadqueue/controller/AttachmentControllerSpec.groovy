package no.fint.downloadqueue.controller

import no.fint.downloadqueue.client.AttachmentDataStreamedClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

class AttachmentControllerSpec extends Specification {
    MockMvc mockMvc

    AttachmentDataStreamedClient client = Mock()

    AttachmentController controller = new AttachmentController(client)

    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build()
    }

    def "Get attachment returns byte array"() {
        given:
        def bytes = [0, 1, 2] as byte[]

        when:
        def response = mockMvc.perform(MockMvcRequestBuilders.get('/attachments/123'))

        then:
        1 * client.getAttachmentDataStreamed(_ as Integer) >> bytes
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().bytes(bytes))
    }
}
