package pt.ua.MovingTree.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pt.ua.MovingTree.support.IdentityTestData.municipality;
import static pt.ua.MovingTree.support.IdentityTestData.team;
import static pt.ua.MovingTree.support.IdentityTestData.user;

import tools.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.security.HttpSessionCallerContext;
import pt.ua.MovingTree.support.PostgresIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsersControllerIT extends PostgresIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //POST /users cria a linha com INACTIVE, team e município da sessão, password hashed e Location
    @Test
    void createsUserWithSessionMunicipalityAndPersistsRow() throws Exception {
        long municipalityId = municipality(jdbc, "api-register-a");
        long teamId = team(jdbc, municipalityId, "parks-api");
        String payload = json("Ana", "ana.api", "password123", "FIELD_WORKER", teamId, 999L);

        MvcResult result = mockMvc.perform(post("/users")
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, municipalityId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.municipalityId").value(municipalityId))
                .andExpect(jsonPath("$.teamId").value(teamId))
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);
        assertTrue(location.matches(".*/users/\\d+"));
        long userId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
        Map<String, Object> row = jdbc.queryForMap("""
                select municipality_id, team_id, status, password_hash
                from app_user where id = ?
                """, userId);
        assertEquals(municipalityId, ((Number) row.get("municipality_id")).longValue());
        assertEquals(teamId, ((Number) row.get("team_id")).longValue());
        assertEquals("INACTIVE", row.get("status"));
        assertNotEquals("password123", row.get("password_hash"));
        assertTrue(passwordEncoder.matches("password123", (String) row.get("password_hash")));
    }

    //falha, cada campo obrigatório inválido deve devolver 400
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPayloads")
    void rejectsInvalidFields(String description, String payload) throws Exception {
        mockMvc.perform(post("/users")
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, 1L))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    //falha, sessão sem role de administrador municipal deve receber 403
    @Test
    void rejectsNonAdministrator() throws Exception {
        mockMvc.perform(post("/users")
                .session(session(AppUserRole.FIELD_WORKER, 1L))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("Ana", "ana.forbidden", "password123", "FIELD_WORKER", 1L, null)))
                .andExpect(status().isForbidden());
    }

    //falha porque username já existente deve receber 409
    @Test
    void rejectsDuplicateUsername() throws Exception {
        long municipalityId = municipality(jdbc, "api-duplicate-a");
        long teamId = team(jdbc, municipalityId, "parks-duplicate-api");
        user(jdbc, municipalityId, teamId, "Existing", "duplicate.api", "FIELD_WORKER");

        mockMvc.perform(post("/users")
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, municipalityId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("Ana", "duplicate.api", "password123", "FIELD_WORKER", teamId, null)))
                .andExpect(status().isConflict());
    }

    //GET /users devolve página 200 com os utilizadores do município da sessão
    @Test
    void listsUsersWithDefaultPaging() throws Exception {
        long municipalityId = municipality(jdbc, "api-list-a");
        long teamId = team(jdbc, municipalityId, "parks-api-list");
        user(jdbc, municipalityId, teamId, "Ana", "ana.list", "FIELD_WORKER", "ACTIVE");
        user(jdbc, municipalityId, teamId, "Bruno", "bruno.list", "FLEET_OPERATOR", "INACTIVE");

        mockMvc.perform(get("/users")
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, municipalityId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].municipalityId").value(municipalityId));
    }

    //passa, município sem utilizadores devolve página vazia com 200
    @Test
    void returnsEmptyPage() throws Exception {
        long municipalityId = municipality(jdbc, "api-list-empty");

        mockMvc.perform(get("/users")
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, municipalityId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    //paginação inválida deve devolver 400
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPaging")
    void rejectsInvalidPaging(String description, String param, String value) throws Exception {
        mockMvc.perform(get("/users")
                .param(param, value)
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, 1L)))
                .andExpect(status().isBadRequest());
    }

    //falha, sessão sem role de administrador municipal deve receber 403
    @Test
    void rejectsListForNonAdministrator() throws Exception {
        mockMvc.perform(get("/users")
                .session(session(AppUserRole.FIELD_WORKER, 1L)))
                .andExpect(status().isForbidden());
    }

    //GET /users/{id} devolve os detalhes de um utilizador do município da sessão
    @Test
    void returnsUserDetailFromCallerMunicipality() throws Exception {
        long municipalityId = municipality(jdbc, "api-detail-a");
        long teamId = team(jdbc, municipalityId, "parks-api-detail");
        long userId = user(jdbc, municipalityId, teamId, "Ana", "ana.detail", "FIELD_WORKER", "ACTIVE");

        mockMvc.perform(get("/users/{id}", userId)
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, municipalityId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("ana.detail"))
                .andExpect(jsonPath("$.municipalityId").value(municipalityId))
                .andExpect(jsonPath("$.teamId").value(teamId));
    }

    //falha, id de outro município e id inexistente devolvem 404 com corpo idêntico
    @Test
    void foreignAndUnknownIdsReturnIdenticalNotFoundBody() throws Exception {
        long municipalityA = municipality(jdbc, "api-detail-404-a");
        long municipalityB = municipality(jdbc, "api-detail-404-b");
        long teamB = team(jdbc, municipalityB, "parks-api-detail-404");
        long foreignId = user(jdbc, municipalityB, teamB, "Bruno", "bruno.foreign-detail", "FIELD_WORKER");
        long unknownId = Long.MAX_VALUE;

        MvcResult foreign = mockMvc.perform(get("/users/{id}", foreignId)
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, municipalityA)))
                .andExpect(status().isNotFound())
                .andReturn();
        MvcResult unknown = mockMvc.perform(get("/users/{id}", unknownId)
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, municipalityA)))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals(foreign.getResponse().getContentAsString(), unknown.getResponse().getContentAsString());
        assertFalse(foreign.getResponse().getContentAsString().contains("bruno.foreign-detail"));
    }

    //falha, sessão sem role de administrador municipal deve receber 403
    @Test
    void rejectsDetailForNonAdministrator() throws Exception {
        mockMvc.perform(get("/users/{id}", 1L)
                .session(session(AppUserRole.FIELD_WORKER, 1L)))
                .andExpect(status().isForbidden());
    }

    //PUT /users/{id} altera role e team e devolve o utilizador atualizado
    @Test
    void updatesRoleAndTeam() throws Exception {
        long municipalityId = municipality(jdbc, "api-update-a");
        long oldTeamId = team(jdbc, municipalityId, "parks-api-update-old");
        long newTeamId = team(jdbc, municipalityId, "ops-api-update-new");
        long userId = user(jdbc, municipalityId, oldTeamId, "Ana", "ana.update-api", "FIELD_WORKER", "ACTIVE");

        mockMvc.perform(put("/users/{id}", userId)
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, municipalityId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role", "FLEET_OPERATOR", "teamId", newTeamId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.role").value("FLEET_OPERATOR"))
                .andExpect(jsonPath("$.teamId").value(newTeamId));
    }

    //falha, team de outro município deve devolver 400
    @Test
    void rejectsUpdateWithTeamFromAnotherMunicipality() throws Exception {
        long municipalityId = municipality(jdbc, "api-update-400-a");
        long teamA = team(jdbc, municipalityId, "parks-api-update-400-a");
        long teamB = team(jdbc, municipality(jdbc, "api-update-400-b"), "parks-api-update-400-b");
        long userId = user(jdbc, municipalityId, teamA, "Ana", "ana.update-400", "FIELD_WORKER");

        mockMvc.perform(put("/users/{id}", userId)
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, municipalityId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("teamId", teamB))))
                .andExpect(status().isBadRequest());
    }

    //falha, pedido sem role e sem team deve devolver 400
    @Test
    void rejectsUpdateWithoutChanges() throws Exception {
        mockMvc.perform(put("/users/{id}", 1L)
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, 1L))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    //falha, sessão sem role de administrador municipal deve receber 403
    @Test
    void rejectsUpdateForNonAdministrator() throws Exception {
        mockMvc.perform(put("/users/{id}", 1L)
                .session(session(AppUserRole.FIELD_WORKER, 1L))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role", "FLEET_OPERATOR"))))
                .andExpect(status().isForbidden());
    }

    //falha, id inexistente deve devolver 404
    @Test
    void rejectsUpdateForUnknownUser() throws Exception {
        mockMvc.perform(put("/users/{id}", Long.MAX_VALUE)
                .session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, 1L))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role", "FLEET_OPERATOR"))))
                .andExpect(status().isNotFound());
    }

    private static Stream<Arguments> invalidPaging() {
        return Stream.of(
                Arguments.of("negative page", "page", "-1"),
                Arguments.of("zero size", "size", "0"),
                Arguments.of("size above limit", "size", "101"),
                Arguments.of("non numeric page", "page", "abc"));
    }

    private static Stream<Arguments> invalidPayloads() {
        return Stream.of(
                Arguments.of("blank name", """
                        {"name":"","username":"ana.invalid","password":"password123","role":"FIELD_WORKER","teamId":1}
                        """),
                Arguments.of("blank username", """
                        {"name":"Ana","username":"","password":"password123","role":"FIELD_WORKER","teamId":1}
                        """),
                Arguments.of("blank password", """
                        {"name":"Ana","username":"ana.invalid","password":"","role":"FIELD_WORKER","teamId":1}
                        """),
                Arguments.of("unknown role", """
                        {"name":"Ana","username":"ana.invalid","password":"password123","role":"NOT_A_ROLE","teamId":1}
                        """),
                Arguments.of("null role", """
                        {"name":"Ana","username":"ana.invalid","password":"password123","role":null,"teamId":1}
                        """),
                Arguments.of("null teamId", """
                        {"name":"Ana","username":"ana.invalid","password":"password123","role":"FIELD_WORKER","teamId":null}
                        """));
    }

    private MockHttpSession session(AppUserRole role, long municipalityId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionCallerContext.ROLE_ATTRIBUTE, role);
        session.setAttribute(HttpSessionCallerContext.MUNICIPALITY_ID_ATTRIBUTE, municipalityId);
        return session;
    }

    private String json(Object name, Object username, Object password, Object role, Object teamId, Object municipalityId)
            throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", name);
        payload.put("username", username);
        payload.put("password", password);
        payload.put("role", role);
        payload.put("teamId", teamId);
        payload.put("municipalityId", municipalityId);
        return objectMapper.writeValueAsString(payload);
    }
}
