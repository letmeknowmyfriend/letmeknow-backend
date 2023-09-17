package com.security.options;

import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

public class Options <T> {
    private List<T> list;

    public Options(List<T> input) {
        this.list = input;
    }

    @ModelAttribute("options")
    public List<T> getOptionList() {
        return list;
    }
}
