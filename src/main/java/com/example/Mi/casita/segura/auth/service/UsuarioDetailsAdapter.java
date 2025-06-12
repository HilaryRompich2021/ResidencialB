package com.example.Mi.casita.segura.auth.service;

import com.example.Mi.casita.segura.usuarios.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

//Constructor recibe una instancia de usuaris y extra de ella:
@RequiredArgsConstructor
public class UsuarioDetailsAdapter implements UserDetails {
    private final Usuario usuario;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + usuario.getRol().name());
    }

    @Override
    public String getPassword() {
        return usuario.getContrasena();
    }

    @Override
    public String getUsername() {
        return usuario.getUsuario();
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.isEstado();
    }


    public List<String> getRoles() {
        return List.of(usuario.getRol().name());
    }

    // Método opcional para acceder al objeto original
    public Usuario getUsuarioOriginal() {
        return usuario;
    }

    //private final int numeroCasa;


    public int getNumeroCasa() {
        return usuario.getNumeroCasa();
    }

    public String getCui() {
        return usuario.getCui();
    }


}
