package cz.stavbau.backend.common.i18n;

import org.springframework.stereotype.Component;

@Component
public class EnumLabeler {
    private final MessageService messages;
    public EnumLabeler(MessageService messages) { this.messages = messages; }

    public String label(String enumNamespace, Enum<?> v) {
        return messages.get(enumNamespace + "." + v.name()); // např. "enum.project.status.IN_PROGRESS"
    }
}
