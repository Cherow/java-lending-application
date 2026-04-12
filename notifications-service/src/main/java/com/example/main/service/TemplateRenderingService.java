package com.example.main.service;

import java.util.Map;

public interface TemplateRenderingService {
    String render(String template, Map<String, String> variables);
}
