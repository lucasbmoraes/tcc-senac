package com.tcc.aplicacao.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.tcc.aplicacao.repository.PessoaRepository;
import com.tcc.aplicacao.services.JasperService;
import java.io.IOException;
import java.util.*;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class JasperController {

    @Autowired
    private JasperService service;

    @Autowired
    private PessoaRepository pessoaRepository;

    @GetMapping("/relatorio/pdf/relatorio-docente")
    public void exibirRelatorio01(@RequestParam("code") String code,
            @RequestParam("acao") String acao,
            HttpServletResponse response) throws IOException {

        byte[] bytes = service.exportarPDF(code);
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        if (acao.equals("v")) {
            response.setHeader("attachment", "incline; filename=relatorio-" + code + ".pdf");
        } else {
            response.setHeader("Content-disposition", "incline; filename=relatorio-" + code + ".pdf");

        }
        response.getOutputStream().write(bytes);

    }

    @GetMapping("/relatorio/pdf/relatorio-horas/{code}")
    public void exibirRelatorio02(@PathVariable("code") String code,
            @RequestParam(name = "nomeCompleto", required = false) String nomeCompleto,
            HttpServletResponse response) throws IOException {
        service.addParams("NomeDocente", nomeCompleto.isEmpty() ? null : nomeCompleto);

        byte[] bytes = service.exportarPDF(code);
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-disposition", "incline; filename=relatorio-" + code + ".pdf");
        response.getOutputStream().write(bytes);

    }

    @ModelAttribute("pessoa")
    public List<String> getnomeCompleto() {
        return pessoaRepository.findNomes();
    }

}
