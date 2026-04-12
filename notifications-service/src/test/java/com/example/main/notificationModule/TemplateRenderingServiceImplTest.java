package com.example.main.notificationModule;

import com.example.main.service.serviceImpl.TemplateRenderingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemplateRenderingServiceImplTest {

    private TemplateRenderingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TemplateRenderingServiceImpl();
    }

    @Test
    void render_shouldReplaceSingleVariable() {
        String template = "Hello {name}";
        Map<String, String> variables = Map.of("name", "Mercy");

        String result = service.render(template, variables);

        assertEquals("Hello Mercy", result);
    }

    @Test
    void render_shouldReplaceMultipleVariables() {
        String template = "Hello {name}, your loan is {amount}";
        Map<String, String> variables = Map.of(
                "name", "Mercy",
                "amount", "5000"
        );

        String result = service.render(template, variables);

        assertEquals("Hello Mercy, your loan is 5000", result);
    }

    @Test
    void render_shouldReturnSameTemplateWhenNoVariables() {
        String template = "Hello {name}";

        String result = service.render(template, Map.of());

        assertEquals("Hello {name}", result);
    }

    @Test
    void render_shouldIgnoreVariablesNotInTemplate() {
        String template = "Hello {name}";
        Map<String, String> variables = Map.of(
                "name", "Mercy",
                "amount", "5000"
        );

        String result = service.render(template, variables);

        assertEquals("Hello Mercy", result);
    }

    @Test
    void render_shouldHandleMultipleOccurrencesOfSameVariable() {
        String template = "{name} sent money to {name}";
        Map<String, String> variables = Map.of("name", "Mercy");

        String result = service.render(template, variables);

        assertEquals("Mercy sent money to Mercy", result);
    }

    @Test
    void render_shouldHandleNullValue() {
        String template = "Hello {name}";
        Map<String, String> variables = Map.of("name", "null");

        String result = service.render(template, variables);

        assertEquals("Hello null", result);
    }
}