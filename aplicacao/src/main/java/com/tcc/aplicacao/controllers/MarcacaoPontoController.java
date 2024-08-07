package com.tcc.aplicacao.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tcc.aplicacao.entities.BancoHoras;
import com.tcc.aplicacao.entities.MarcacaoPonto;
import com.tcc.aplicacao.entities.Usuario;
import com.tcc.aplicacao.repository.BancoHorasRepository;
import com.tcc.aplicacao.services.BancoHorasService;
import com.tcc.aplicacao.services.MarcacaoPontoService;

@Controller
@RequestMapping("/ponto/")
@SessionAttributes("usuario")
public class MarcacaoPontoController {

    @Autowired
    private MarcacaoPontoService marcacaoPontoService;

    @Autowired
    private BancoHorasService bancoHorasService;

    @Autowired
    private BancoHorasRepository bancoHorasRepository;

    @PostMapping("cadastroPonto")
    public ModelAndView cadastroPonto(MarcacaoPonto marcacaoPonto, RedirectAttributes redirectAttributes) {
        ModelAndView mv = new ModelAndView("redirect:/home");
        List<MarcacaoPonto> marcacaoList = marcacaoPontoService.cadastroPonto(marcacaoPonto);
        bancoHorasService.atualizarBancoHoras(marcacaoPonto.getIdUsuario());
        if (marcacaoList.isEmpty()) {
            redirectAttributes.addFlashAttribute("alerta", "limiteAtingido");
        } else {
            redirectAttributes.addFlashAttribute("pontoConfirm", "pontoRegistrado");
        }
        mv.addObject("listaHoraMarcada", marcacaoList);
        return mv;
    }

    @GetMapping("listaPonto/{id}")
    public String listaPonto(@PathVariable int id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute("usuario") Usuario usuario,
            Model model) {
        Page<MarcacaoPonto> listaPonto = marcacaoPontoService.findAllByIdUsuario(id, PageRequest.of(page, size));
        model.addAttribute("listaPonto", listaPonto);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", listaPonto.getTotalPages());
        model.addAttribute("id", id);
        model.addAttribute("size", size);

        // Adicione logs
        System.out.println("currentPage: " + page);
        System.out.println("totalPages: " + listaPonto.getTotalPages());
        System.out.println("size: " + size);

        return "listaPonto";
    }

    @GetMapping("deletarPonto/{id}")
    public String deletarPonto(@PathVariable(name = "id") int id,
            @RequestParam int idUsuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            RedirectAttributes redirectAttributes) {
        MarcacaoPonto marcacaoPonto = marcacaoPontoService.buscaMarcacaoPontoPorId(id);

        // Verifica se a horaSaida é nula
        if (marcacaoPonto.getHoraSaida() != null) {
            long millisEntrada = marcacaoPonto.getHoraEntrada().getTime();
            long millisSaida = marcacaoPonto.getHoraSaida().getTime();
            long diff = millisSaida - millisEntrada;

            BancoHoras bancoHoras = bancoHorasService.buscaBancosHorasPorUsuario(idUsuario);
            bancoHoras.setSaldoAtual(bancoHoras.getSaldoAtual() - diff);
            bancoHorasRepository.save(bancoHoras);
        }

        marcacaoPontoService.deletarPonto(id);
        redirectAttributes.addFlashAttribute("message", "Ponto deletado com sucesso");
        return "redirect:/ponto/listaPonto/" + idUsuario + "?page=" + page + "&size=" + size;
    }

}
