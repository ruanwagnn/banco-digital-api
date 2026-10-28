package br.com.portfolio.bancodigital.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BancoDigitalIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    @Test
    void fluxoCompleto_registrar_login_depositar_e_consultarSaldo() throws Exception {
        // 1. registrar
        String registro = """
                {"nome":"Maria Teste","cpf":"98765432100","email":"maria@teste.com","senha":"senha12345"}
                """;
        mockMvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titular", is("Maria Teste")))
                .andExpect(jsonPath("$.saldo", is(0)));

        // 2. login -> token
        String login = """
                {"cpf":"98765432100","senha":"senha12345"}
                """;
        String resp = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(resp).get("token").asText();

        // 3. depositar
        mockMvc.perform(post("/transacoes/deposito")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":250.75}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo", is("DEPOSITO")))
                .andExpect(jsonPath("$.saldoApos", is(250.75)));

        // 4. consultar saldo
        mockMvc.perform(get("/contas/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo", is(250.75)));
    }

    @Test
    void acessoSemToken_eRejeitado() throws Exception {
        mockMvc.perform(get("/contas/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void saqueSemSaldo_retorna422() throws Exception {
        String registro = """
                {"nome":"Pedro Teste","cpf":"12312312399","email":"pedro@teste.com","senha":"senha12345"}
                """;
        mockMvc.perform(post("/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON).content(registro));
        String resp = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"12312312399\",\"senha\":\"senha12345\"}"))
                .andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(resp).get("token").asText();

        mockMvc.perform(post("/transacoes/saque")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\":500.00}"))
                .andExpect(status().isUnprocessableEntity());
    }
}
