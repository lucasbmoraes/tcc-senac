package com.tcc.aplicacao.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.tcc.aplicacao.dto.CadastroDTO;
import com.tcc.aplicacao.services.UsuarioService;

@Controller
public class UsuarioController {

    @Autowired
    UsuarioService service;

    @GetMapping("/cadastro")
    public String telaCadastro() {
        return "cadastro";
    }

    @PostMapping("/cadastroUsuario")
    public String cadastroUsuario(@Validated CadastroDTO cadastroDTO) {
        service.salvar(cadastroDTO);
        return "redirect:/login";
    }
}
