package io.github.dewanahmed.notify.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CoreApiTests {

    private static final Pattern TOKEN = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");

    @Autowired
    private MockMvc mvc;

    @Test
    void healthIsPublicAndEchoesCorrelationId() throws Exception {
        mvc.perform(get("/health").header("X-Correlation-Id", "corr-core-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "corr-core-1"));
    }

    @Test
    void createRequiresAuthentication() throws Exception {
        mvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipient\":\"user@example.com\",\"body\":\"hello\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsUnknownPassword() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"notify-admin\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid credentials"));
    }

    @Test
    void loginCreateAndFetchNotification() throws Exception {
        String token = login();

        String created = mvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipient\":\"user@example.com\",\"body\":\"hello\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("queued"))
                .andExpect(jsonPath("$.recipient").value("user@example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = created.replaceAll("(?s).*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mvc.perform(get("/api/notifications/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("hello"));
    }

    @Test
    void createRejectsBlankBody() throws Exception {
        mvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + login())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipient\":\"user@example.com\",\"body\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    private String login() throws Exception {
        String json = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"notify-admin\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Matcher matcher = TOKEN.matcher(json);
        if (!matcher.find()) {
            throw new AssertionError(json);
        }
        return matcher.group(1);
    }
}
