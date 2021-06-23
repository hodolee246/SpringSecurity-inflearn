package io.security.basicsecurity.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeon InWoo
 */
@RestController
public class SecurityController {

    @GetMapping
    public String index(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContext context = (SecurityContext)session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        Authentication authentication11 = context.getAuthentication();

        return "home";
    }

    @GetMapping("/thread")
    public String thread() {
        new Thread(
                () -> {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                }
        ).start();

        return "thread";
    }

}
