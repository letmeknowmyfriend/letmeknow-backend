package com.letmeknow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/support")
public class SupportController {
    @GetMapping
    public String getSupportPage() {
        return "support/support";
    }
}
