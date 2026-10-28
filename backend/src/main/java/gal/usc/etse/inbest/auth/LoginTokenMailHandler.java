package gal.usc.etse.inbest.auth;

import gal.usc.etse.inbest.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class LoginTokenMailHandler implements OneTimeTokenGenerationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(LoginTokenMailHandler.class);

    private final UserService userService;
    private final JavaMailSender mailSender;
    private final OneTimeTokenService tokenService;
    private final String baseUrl;
    private final String mailFrom;

    public LoginTokenMailHandler(UserService userService, JavaMailSender mailSender,
            OneTimeTokenService tokenService,
            @Value("${inbest.auth.base-url}") String baseUrl,
            @Value("${inbest.auth.mail-from}") String mailFrom) {
        this.userService = userService;
        this.mailSender = mailSender;
        this.tokenService = tokenService;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.mailFrom = mailFrom;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            OneTimeToken token) throws IOException {
        var user = userService.findByUsername(token.getUsername());
        if (user.isPresent()) {
            String link = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/login/ott")
                .queryParam("token", token.getTokenValue())
                .build().encode().toUriString();
            var message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(user.get().getEmail());
            message.setSubject("Tu enlace de acceso a Inbest");
            message.setText("Confirma el inicio de sesión en este enlace:\n" + link
                + "\n\nEl enlace caduca en cinco minutos y solo puede utilizarse una vez.");
            try {
                mailSender.send(message);
            } catch (MailException exception) {
                revoke(token);
                log.warn("No se ha podido enviar el enlace de acceso por SMTP.");
            }
        } else {
            revoke(token);
        }
        response.sendRedirect(request.getContextPath() + "/ott/sent");
    }

    private void revoke(OneTimeToken token) {
        tokenService.consume(new OneTimeTokenAuthenticationToken(token.getTokenValue()));
    }
}
