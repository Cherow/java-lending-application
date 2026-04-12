package com.example.main.service.serviceImpl;

import com.example.main.service.TemplateRenderingService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TemplateRenderingServiceImpl implements TemplateRenderingService {

    @Override
    public String render(String template, Map<String, String> variables) {
        String rendered = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return rendered;
    }
}
