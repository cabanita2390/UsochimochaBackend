package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;

/** Cómo se muestra un usuario en las respuestas: nombre completo, o su username si no lo tiene. */
final class Nombres {

    private Nombres() {
    }

    static String de(UserEntity usuario) {
        if (usuario == null) {
            return null;
        }
        return usuario.getFullName() != null && !usuario.getFullName().isBlank()
                ? usuario.getFullName()
                : usuario.getUsername();
    }
}
