package io.security.basicsecurity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeon InWoo
 */
@RestController
public class SecurityController {

    @GetMapping("/")
    public String index() {
        return "home";
    }

}
