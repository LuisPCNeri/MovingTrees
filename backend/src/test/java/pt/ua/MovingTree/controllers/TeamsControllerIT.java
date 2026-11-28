package pt.ua.MovingTree.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pt.ua.MovingTree.support.IdentityTestData.municipality;
import static pt.ua.MovingTree.support.IdentityTestData.team;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.security.HttpSessionCallerContext;
import pt.ua.MovingTree.support.PostgresIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TeamsControllerIT extends PostgresIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbc;

    //GET /teams devolve só as equipas do município da sessão, ordenadas por nome
    @Test
    void listsOnlyCallerMunicipalityTeamsSortedByName() throws Exception {
        long municipalityId = municipality(jdbc, "teams-api-a");
        long otherMunicipalityId = municipality(jdbc, "teams-api-b");
        long pruning = team(jdbc, municipalityId, "Pruning");
        long cleaning = team(jdbc, municipalityId, "Cleaning");
        team(jdbc, otherMunicipalityId, "Elsewhere");

        mockMvc.perform(get("/teams").session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, municipalityId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(cleaning))
                .andExpect(jsonPath("$[0].name").value("Cleaning"))
                .andExpect(jsonPath("$[1].id").value(pruning))
                .andExpect(jsonPath("$[1].name").value("Pruning"));
    }

    //município sem equipas devolve lista vazia
    @Test
    void returnsEmptyListWhenMunicipalityHasNoTeams() throws Exception {
        long municipalityId = municipality(jdbc, "teams-api-empty");

        mockMvc.perform(get("/teams").session(session(AppUserRole.MUNICIPAL_ADMINISTRATOR, municipalityId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    //falha, sessão sem role de administrador municipal deve receber 403
    @Test
    void rejectsNonAdministrator() throws Exception {
        long municipalityId = municipality(jdbc, "teams-api-forbidden");

        mockMvc.perform(get("/teams").session(session(AppUserRole.FIELD_WORKER, municipalityId)))
                .andExpect(status().isForbidden());
    }

    //falha, pedido sem sessão deve receber 403
    @Test
    void rejectsMissingSession() throws Exception {
        mockMvc.perform(get("/teams"))
                .andExpect(status().isForbidden());
    }

    private MockHttpSession session(AppUserRole role, long municipalityId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionCallerContext.ROLE_ATTRIBUTE, role);
        session.setAttribute(HttpSessionCallerContext.MUNICIPALITY_ID_ATTRIBUTE, municipalityId);
        return session;
    }
}
