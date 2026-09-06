package in.kanchuk.service;

import in.kanchuk.dto.response.PageMeta;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility helpers shared by admin service implementations.
 */
public abstract class GenericAdminService {

    protected PageRequest pageRequest(int page, int limit) {
        return PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
    }

    protected PageMeta buildMeta(Page<?> pg, int page, int limit) {
        return new PageMeta(pg.getTotalElements(), page, limit, pg.getTotalPages());
    }

    protected <T> T findOrThrow(JpaRepository<T, UUID> repo, UUID id, String entity) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(entity + " not found: " + id));
    }

    /** Applies a map of field→value using reflection (camelCase field names). */
    protected <T> void applyPatch(T entity, Map<String, Object> fields) {
        Class<?> cls = entity.getClass();
        fields.forEach((field, value) -> {
            try {
                if (!tryInvokeSetter(entity, cls, "set" + Character.toUpperCase(field.charAt(0)) + field.substring(1), value)) {
                    // Lombok strips the 'is' prefix from boolean field setters (isActive → setActive)
                    if (field.startsWith("is") && field.length() > 2 && Character.isUpperCase(field.charAt(2))) {
                        tryInvokeSetter(entity, cls, "set" + field.substring(2), value);
                    }
                }
            } catch (Exception ignored) {}
        });
    }

    private <T> boolean tryInvokeSetter(T entity, Class<?> cls, String setterName, Object value) {
        for (Method m : cls.getMethods()) {
            if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                try { m.invoke(entity, coerce(value, m.getParameterTypes()[0])); } catch (Exception ignored) {}
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Object coerce(Object value, Class<?> target) {
        if (value == null || target.isInstance(value)) return value;
        if (target == Boolean.class || target == boolean.class) return Boolean.valueOf(value.toString());
        if (target == Integer.class || target == int.class) return Integer.valueOf(value.toString());
        if (target == Long.class || target == long.class) return Long.valueOf(value.toString());
        if (target == java.math.BigDecimal.class) return new java.math.BigDecimal(value.toString());
        if (target == UUID.class) return UUID.fromString(value.toString());
        if (target == OffsetDateTime.class) return OffsetDateTime.parse(value.toString());
        if (target == List.class) return value;
        return value.toString();
    }
}
