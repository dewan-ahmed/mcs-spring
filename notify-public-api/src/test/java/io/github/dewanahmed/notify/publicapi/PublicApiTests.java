package io.github.dewanahmed.notify.publicapi;

import io.github.dewanahmed.notify.Notification;
import io.github.dewanahmed.notify.NotificationStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private NotificationStore store;

    @Test
    void healthDoesNotRequireApiKey() throws Exception {
        mvc.perform(get("/health")).andExpect(status().isOk());
    }

    @Test
    void statusRequiresApiKey() throws Exception {
        mvc.perform(get("/api/notifications/" + UUID.randomUUID() + "/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void callbackUpdatesStatus() throws Exception {
        UUID id = UUID.randomUUID();
        store.add(new Notification(id, "user@example.com", "hello", "queued", Instant.now()));

        mvc.perform(post("/api/callbacks/status")
                        .header("X-Api-Key", "test-api-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + id + "\",\"status\":\"delivered\"}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/notifications/" + id + "/status").header("X-Api-Key", "test-api-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("delivered"));
    }

    @Test
    void callbackRejectsUnknownStatus() throws Exception {
        mvc.perform(post("/api/callbacks/status")
                        .header("X-Api-Key", "test-api-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + UUID.randomUUID() + "\",\"status\":\"exploded\"}"))
                .andExpect(status().isBadRequest());
    }
}
