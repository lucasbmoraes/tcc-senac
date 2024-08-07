package com.tcc.aplicacao.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tcc.aplicacao.dto.AjusteDto;
import com.tcc.aplicacao.entities.Ajuste;
import com.tcc.aplicacao.entities.BancoHoras;
import com.tcc.aplicacao.entities.MarcacaoPonto;
import com.tcc.aplicacao.entities.Usuario;
import com.tcc.aplicacao.exceptions.AjusteExistenteException;
import com.tcc.aplicacao.repository.AjusteRepository;
import com.tcc.aplicacao.repository.BancoHorasRepository;
import com.tcc.aplicacao.repository.MarcacaoPontoRepository;
import com.tcc.aplicacao.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class AjusteService {

    @Autowired
    AjusteRepository ajusteRepository;

    @Autowired
    BancoHorasRepository bancoHorasRepository;

    @Autowired
    private BancoHorasService bancoHorasService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MarcacaoPontoRepository marcacaoPontoRepository;

    public void salvarAjuste(Ajuste ajuste, MultipartFile arquivoNovo) {
        if (!arquivoNovo.isEmpty()) {
            String nomeArquivo = arquivoNovo.getOriginalFilename();

            try {
                String diretorioImg = "uploads/";
                File dir = new File(diretorioImg);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File serverFile = new File(dir.getAbsolutePath() + File.separator + nomeArquivo);
                ajuste.setArquivo(serverFile.getAbsolutePath());
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));

                stream.write(arquivoNovo.getBytes());
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Optional<Ajuste> existente = ajusteRepository.findFirstByMarcacaoPontoId(ajuste.getMarcacaoPonto().getId());
        if (existente.isPresent()) {
            throw new AjusteExistenteException("Ajuste com este marcacao_ponto_id já existe.");
        }

        ajusteRepository.save(ajuste);
    }

    public void salvarAjusteSemMarcacao(Ajuste ajuste, MultipartFile arquivoNovo, int usuarioId) {
        MarcacaoPonto marcacaoPonto = new MarcacaoPonto();
        marcacaoPonto.setIdUsuario(usuarioId);
        marcacaoPonto.setData(ajuste.getData());
        marcacaoPonto.setHoraEntrada(ajuste.getHoraEntrada());
        marcacaoPonto.setHoraSaida(ajuste.getHoraSaida());
        marcacaoPontoRepository.save(marcacaoPonto);

        ajuste.setMarcacaoPonto(marcacaoPonto);
        if (!arquivoNovo.isEmpty()) {
            String nomeArquivo = arquivoNovo.getOriginalFilename();

            try {
                String diretorioImg = "uploads/";
                File dir = new File(diretorioImg);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File serverFile = new File(dir.getAbsolutePath() + File.separator + nomeArquivo);
                ajuste.setArquivo(serverFile.getAbsolutePath());
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));

                stream.write(arquivoNovo.getBytes());
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ajusteRepository.save(ajuste);
    }

    public Ajuste buscarPorId(int id) {
        return ajusteRepository.findById(id).orElse(null);
    }

    @Transactional
    public void aprovarAjuste(int ajusteId) {
        Ajuste ajuste = ajusteRepository.findById(ajusteId)
                .orElseThrow(() -> new IllegalArgumentException("Ajuste não encontrado"));
        MarcacaoPonto marcacaoPonto = ajuste.getMarcacaoPonto();
        if (marcacaoPonto != null) {
            Long millisEntradaAntiga = marcacaoPonto.getHoraEntrada() != null ? marcacaoPonto.getHoraEntrada().getTime()
                    : 0;
            Long millisSaidaAntiga = marcacaoPonto.getHoraSaida() != null ? marcacaoPonto.getHoraSaida().getTime() : 0;
            Long diffAntigo = millisSaidaAntiga - millisEntradaAntiga;

            marcacaoPonto.setHoraEntrada(ajuste.getHoraEntrada());
            marcacaoPonto.setHoraSaida(ajuste.getHoraSaida());
            marcacaoPontoRepository.save(marcacaoPonto);

            Long millisEntradaNovo = ajuste.getHoraEntrada() != null ? ajuste.getHoraEntrada().getTime() : 0;
            Long millisSaidaNovo = ajuste.getHoraSaida() != null ? ajuste.getHoraSaida().getTime() : 0;
            Long diffNovo = millisSaidaNovo - millisEntradaNovo;

            BancoHoras bancoHoras = bancoHorasService.buscaBancosHorasPorUsuario(marcacaoPonto.getIdUsuario());
            bancoHoras.setSaldoAtual(bancoHoras.getSaldoAtual() - diffAntigo + diffNovo);
            bancoHorasRepository.save(bancoHoras);
        }

        ajuste.setStatus(true);
        ajusteRepository.save(ajuste);

        bancoHorasService.atualizarBancoHoras(marcacaoPonto.getIdUsuario());
    }

    @Transactional
    public void rejeitarAjuste(int ajusteId) {
        Ajuste ajuste = ajusteRepository.findById(ajusteId)
                .orElseThrow(() -> new IllegalArgumentException("Ajuste não encontrado"));
        ajuste.setStatus(false);
        ajusteRepository.save(ajuste);
    }

    public List<Ajuste> buscarPorMarcacaoPontoId(int marcacaoPontoId) {
        return ajusteRepository.findByMarcacaoPontoId(marcacaoPontoId);
    }

    public List<Ajuste> buscarTodosAjustes() {
        return ajusteRepository.findAll();
    }

    public List<AjusteDto> buscarTodosAjustesComDocente() {
        List<AjusteDto> ajusteDTOs = ajusteRepository.findAll().stream().map(ajuste -> {
            MarcacaoPonto marcacaoPonto = ajuste.getMarcacaoPonto();
            String nomeDocente = "Desconhecido";
            if (marcacaoPonto != null) {
                Usuario usuario = usuarioRepository.findById(marcacaoPonto.getIdUsuario()).orElse(null);
                nomeDocente = usuario != null ? usuario.getPessoa().getNomeCompleto() : "Desconhecido";
            }
            return new AjusteDto(ajuste, nomeDocente);
        }).collect(Collectors.toList());

        ajusteDTOs.forEach(dto -> {
            System.out.println("Ajuste ID: " + dto.getAjuste().getId() + ", Docente: " + dto.getNomeDocente()
                    + dto.getAjuste().getHoraEntrada());
        });

        return ajusteDTOs;
    }
}
