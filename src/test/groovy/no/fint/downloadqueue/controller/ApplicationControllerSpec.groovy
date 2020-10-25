package no.fint.downloadqueue.controller

import no.fint.downloadqueue.client.DownloadQueueClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

class ApplicationControllerSpec extends Specification {
    MockMvc mockMvc

    DownloadQueueClient client = Mock()

    ApplicationController controller = new ApplicationController(client)

    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build()
    }

    def "Get application returns byte array"() {
        given:
        def bytes = [0, 1, 2] as byte[]

        when:
        def response = mockMvc.perform(MockMvcRequestBuilders.get('/applications/123?languageCode=456'))

        then:
        1 * client.getFormSetPdf(_ as String, _ as Integer) >> bytes
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().bytes(bytes))
    }
}
