package com.tcc.aplicacao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tcc.aplicacao.entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String nomeUsuario);

    @Query("select u from Usuario u where u.username = ?1")
    Usuario findByUsuarioJPQL(String nomeUsuario);

    @Query("select u from Usuario u where u.pessoa.id = ?1")
    Usuario findByIdPessoa(int id);

    @Query(value = "SELECT * FROM usuario WHERE username = ?1", nativeQuery = true)
    Usuario findByUsuarioNativo(String nomeUsuario);

    @Query(value = "SELECT * FROM usuario WHERE role != 'ADMIN' Order By username asc ", nativeQuery = true)
    List<Usuario> buscaTodosDocentes();

    Usuario findByPessoaId(int pessoaId);

}
