package gal.usc.etse.inbest;

import com.jayway.jsonpath.JsonPath;
import gal.usc.etse.inbest.model.dto.CreateUserRequest;
import gal.usc.etse.inbest.model.entity.CashAccount;
import gal.usc.etse.inbest.model.entity.User;
import gal.usc.etse.inbest.repository.CashAccountRepository;
import gal.usc.etse.inbest.repository.UserRepository;
import gal.usc.etse.inbest.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.ott.InMemoryOneTimeTokenService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class AuthenticationIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired UserService userService;
    @Autowired UserRepository users;
    @Autowired JdbcTemplate jdbc;
    @Autowired InMemoryOneTimeTokenService tokens;
    @MockitoSpyBean CashAccountRepository cashAccounts;
    @MockitoBean JavaMailSender mailSender;

    private String username;
    private String email;
    private RequestPostProcessor client;

    @BeforeEach
    void prepare() {
        jdbc.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
        tokens.setClock(Clock.systemUTC());
        username = "user" + UUID.randomUUID().toString().substring(0, 12);
        email = username + "@example.test";
        String address = "client-" + UUID.randomUUID();
        client = request -> {
            request.setRemoteAddr(address);
            return request;
        };
    }

    @Test
    void registrationNormalizesInputAndCreatesAnEmptyEuroAccount() throws Exception {
        mvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(registration("  " + username + "  ", "  " + email.toUpperCase() + "  ")))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/users/1"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.createdAt").isNotEmpty());
        var account = cashAccounts.findByUserId(1L).orElseThrow();
        assertThat(account.getCurrency()).isEqualTo("EUR");
        assertThat(account.getAvailableCash()).isZero();
        assertThat(account.getReservedCash()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{}",
        "{\"username\":\"   \",\"email\":\"user@example.test\"}",
        "{\"username\":\"user\",\"email\":\"invalid\"}",
        "{\"username\":\"user\",\"email\":\"in valid@example.test\"}",
        "{\"username\":\"1234567890123456789012345678901\",\"email\":\"u@example.test\"}"
    })
    void invalidRegistrationDoesNotCreateRecords(String body) throws Exception {
        mvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.errors").isMap());
        assertThat(users.count()).isZero();
        assertThat(cashAccounts.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"username", "email"})
    void duplicateRegistrationReturnsConflictWithoutAnotherAccount(String duplicate) throws Exception {
        register();
        String nextUsername = duplicate.equals("username") ? username : "another";
        String nextEmail = duplicate.equals("email") ? email.toUpperCase() : "another@example.test";
        mvc.perform(post("/api/users").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(registration(nextUsername, nextEmail)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
        assertThat(users.count()).isEqualTo(1);
        assertThat(cashAccounts.count()).isEqualTo(1);
    }

    @Test
    void failedCashAccountCreationRollsBackTheUser() {
        doThrow(new DataIntegrityViolationException("Simulated cash account failure"))
            .when(cashAccounts).save(any(CashAccount.class));
        assertThatThrownBy(this::register).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(users.findByUsername(username)).isEmpty();
        assertThat(cashAccounts.count()).isZero();
    }

    @Test
    void publicUserResponseDoesNotExposePrivateFields() throws Exception {
        var user = register();
        mvc.perform(get("/api/users/{id}", user.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.id").doesNotExist())
            .andExpect(jsonPath("$.email").doesNotExist())
            .andExpect(jsonPath("$.createdAt").doesNotExist());
        mvc.perform(get("/api/users/999999")).andExpect(status().isNotFound());
    }

    @Test
    void privateApiRequiresAuthenticationAndDoesNotRedirectToHtml() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/1/orders")).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/users", "/ott/generate", "/login/ott", "/logout"})
    void modifyingRequestsRequireCsrf(String path) throws Exception {
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(registration(username, email)))
            .andExpect(status().isForbidden());
        assertThat(users.count()).isZero();
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void generatedLoginPageOnlyOffersOneTimeTokens() throws Exception {
        mvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/ott/generate")))
            .andExpect(content().string(not(containsString("name=\"password\""))));
    }

    @Test
    void browserCanRegisterLogInReadItsProfileAndLogOutWithRealCsrf() throws Exception {
        var csrf = csrfFor(null);
        var session = csrf.session();
        mvc.perform(post("/api/users").session(session)
                .header(csrf.header(), csrf.token())
                .contentType(MediaType.APPLICATION_JSON).content(registration(username, email)))
            .andExpect(status().isCreated());
        mvc.perform(post("/ott/generate").session(session).with(client)
                .header(csrf.header(), csrf.token()).param("username", "  " + username + "  "))
            .andExpect(redirectedUrl("/ott/sent"));
        var message = sentMessage();
        assertThat(message.getTo()).containsExactly(email);
        assertThat(message.getFrom()).isEqualTo("no-reply@inbest.local");
        String token = tokenFrom(message);

        // Abrir el enlace muestra la confirmación y no consume el token.
        mvc.perform(get("/login/ott").session(session).param("token", token))
            .andExpect(status().isOk()).andExpect(content().string(containsString(token)));
        mvc.perform(get("/api/me").session(session)).andExpect(status().isUnauthorized());
        String previousSessionId = session.getId();
        mvc.perform(post("/login/ott").session(session)
                .header(csrf.header(), csrf.token()).param("token", token))
            .andExpect(redirectedUrl("/api/me"))
            .andExpect(authenticated().withUsername(username));
        assertThat(session.getId()).isNotEqualTo(previousSessionId);
        mvc.perform(get("/api/me").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.createdAt").isNotEmpty());

        // La autenticación invalida el token CSRF anterior.
        mvc.perform(post("/logout").session(session).header(csrf.header(), csrf.token()))
            .andExpect(status().isForbidden());
        var authenticatedCsrf = csrfFor(session);
        mvc.perform(post("/logout").session(session)
                .header(authenticatedCsrf.header(), authenticatedCsrf.token()))
            .andExpect(redirectedUrl("/login?logout")).andExpect(unauthenticated());
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        assertThat(csrfFor(null).token()).isNotBlank();
    }

    @Test
    void aConsumedTokenCannotBeUsedAgain() throws Exception {
        register();
        String token = requestToken();
        mvc.perform(post("/login/ott").with(csrf()).param("token", token))
            .andExpect(redirectedUrl("/api/me")).andExpect(authenticated().withUsername(username));
        mvc.perform(post("/login/ott").with(csrf()).param("token", token))
            .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        register();
        tokens.setClock(Clock.offset(Clock.systemUTC(), Duration.ofMinutes(-10)));
        String token = requestToken();
        tokens.setClock(Clock.systemUTC());
        mvc.perform(post("/login/ott").with(csrf()).param("token", token))
            .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
    }

    @Test
    void nonexistentAndInvalidUsernamesGetTheSameConfirmationWithoutMail() throws Exception {
        for (String name : new String[] {username, "", " ", "x".repeat(31)}) {
            mvc.perform(post("/ott/generate").with(csrf()).with(client).param("username", name))
                .andExpect(redirectedUrl("/ott/sent"));
        }
        mvc.perform(post("/ott/generate").with(csrf()).with(client))
            .andExpect(redirectedUrl("/ott/sent"));
        mvc.perform(get("/ott/sent")).andExpect(status().isOk())
            .andExpect(content().string(containsString("Si la cuenta existe")));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void repeatedTokenRequestsAreLimitedWithoutDisclosingTheAccount() throws Exception {
        register();
        for (int i = 0; i < 4; i++) {
            mvc.perform(post("/ott/generate").with(csrf()).with(client).param("username", username))
                .andExpect(redirectedUrl("/ott/sent"));
        }
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    void failedMailDeliveryRevokesTheTokenAndKeepsTheGenericConfirmation() throws Exception {
        register();
        doThrow(new MailSendException("SMTP unavailable"))
            .when(mailSender).send(any(SimpleMailMessage.class));
        String token = requestToken();
        mvc.perform(post("/login/ott").with(csrf()).param("token", token))
            .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
    }

    private User register() {
        return userService.register(new CreateUserRequest(username, email));
    }

    private String requestToken() throws Exception {
        mvc.perform(post("/ott/generate").with(csrf()).with(client).param("username", username))
            .andExpect(redirectedUrl("/ott/sent"));
        return tokenFrom(sentMessage());
    }

    private SimpleMailMessage sentMessage() {
        var message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        return message.getValue();
    }

    private String tokenFrom(SimpleMailMessage message) {
        var match = Pattern.compile("http://localhost:8080/login/ott\\?token=\\S+")
            .matcher(message.getText());
        assertThat(match.find()).isTrue();
        return UriComponentsBuilder.fromUriString(match.group()).build()
            .getQueryParams().getFirst("token");
    }

    private Csrf csrfFor(MockHttpSession session) throws Exception {
        var request = get("/api/csrf");
        if (session != null) {
            request.session(session);
        }
        var result = mvc.perform(request).andExpect(status().isOk()).andReturn();
        var json = JsonPath.parse(result.getResponse().getContentAsString());
        return new Csrf(json.read("$.headerName"), json.read("$.token"),
            (MockHttpSession) result.getRequest().getSession(false));
    }

    private String registration(String name, String address) {
        return "{\"username\":\"%s\",\"email\":\"%s\"}".formatted(name, address);
    }

    private record Csrf(String header, String token, MockHttpSession session) {}
}
