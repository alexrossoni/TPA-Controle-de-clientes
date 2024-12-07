package cms;

import java.io.*;
import java.util.*;

public class OrdenacaoClientes {
    private static final int TAMANHO_LOTE = 1000; // Ajuste baseado na capacidade de memória disponível

    /**
     * Método principal para ordenar clientes em ordem alfabética usando Merge Sort Externo.
     *
     * @param arquivoEntrada  Nome do arquivo de entrada com os clientes desordenados.
     * @param arquivoSaida    Nome do arquivo de saída com os clientes ordenados.
     */
    public void ordenarClientes(String arquivoEntrada, String arquivoSaida) {
        try {
            // Passo 1: Divisão - Criação de arquivos temporários com lotes ordenados
            List<File> arquivosTemporarios = criarArquivosOrdenados(arquivoEntrada);

            // Passo 2: Intercalação - Mesclar os arquivos temporários em um único arquivo ordenado
            intercalarArquivosOrdenados(arquivosTemporarios, arquivoSaida);

            System.out.println("Ordenação concluída. Clientes ordenados estão em: " + arquivoSaida);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Divide o arquivo de entrada em lotes menores ordenados e grava em arquivos temporários.
     *
     * @param arquivoEntrada Nome do arquivo de entrada.
     * @return Lista de arquivos temporários contendo os lotes ordenados.
     */
    private List<File> criarArquivosOrdenados(String arquivoEntrada) throws IOException, ClassNotFoundException {
        List<File> arquivosTemporarios = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivoEntrada))) {
            boolean fimDoArquivo = false;
            while (!fimDoArquivo) {
                List<Cliente> lote = new ArrayList<>();
                for (int i = 0; i < TAMANHO_LOTE; i++) {
                    try {
                        Cliente cliente = (Cliente) ois.readObject();
                        lote.add(cliente);
                    } catch (EOFException e) {
                        fimDoArquivo = true;
                        break;
                    }
                }
                // Ordena o lote em memória
                lote.sort((c1, c2) -> {
                    int compareNome = c1.getNome().compareToIgnoreCase(c2.getNome());
                    if (compareNome != 0) {
                        return compareNome; // Primeiro critério: Nome
                    }
                    return c1.getSobrenome().compareToIgnoreCase(c2.getSobrenome()); // Segundo critério: Sobrenome
                });

                // Grava o lote ordenado em um arquivo temporário
                File arquivoTemporario = File.createTempFile("clientes_ordenados_", ".tmp");
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivoTemporario))) {
                    for (Cliente cliente : lote) {
                        oos.writeObject(cliente);
                    }
                }
                arquivosTemporarios.add(arquivoTemporario);
            }
        }
        return arquivosTemporarios;
    }

    /**
     * Mescla os arquivos temporários ordenados em um único arquivo ordenado.
     *
     * @param arquivosTemporarios Lista de arquivos temporários ordenados.
     * @param arquivoSaida        Nome do arquivo de saída ordenado.
     */
    private void intercalarArquivosOrdenados(List<File> arquivosTemporarios, String arquivoSaida) throws IOException, ClassNotFoundException {
        PriorityQueue<ParClienteArquivo> heap = new PriorityQueue<>((par1, par2) -> {
            int compareNome = par1.cliente.getNome().compareToIgnoreCase(par2.cliente.getNome());
            if (compareNome != 0) {
                return compareNome; // Primeiro critério: Nome
            }
            return par1.cliente.getSobrenome().compareToIgnoreCase(par2.cliente.getSobrenome()); // Segundo critério: Sobrenome
        });
        Map<ObjectInputStream, File> streams = new HashMap<>();

        // Abre os streams de cada arquivo temporário e insere o primeiro cliente de cada arquivo na heap
        for (File arquivo : arquivosTemporarios) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo));
            streams.put(ois, arquivo);
            try {
                Cliente cliente = (Cliente) ois.readObject();
                heap.add(new ParClienteArquivo(cliente, ois));
            } catch (EOFException e) {
                ois.close();
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivoSaida))) {
            while (!heap.isEmpty()) {
                // Remove o cliente com o menor nome da heap
                ParClienteArquivo menorPar = heap.poll();
                oos.writeObject(menorPar.cliente);

                // Lê o próximo cliente do mesmo arquivo, se disponível, e reinsere na heap
                try {
                    Cliente proximoCliente = (Cliente) menorPar.origem.readObject();
                    heap.add(new ParClienteArquivo(proximoCliente, menorPar.origem));
                } catch (EOFException e) {
                    menorPar.origem.close();
                    streams.remove(menorPar.origem);
                }
            }
        }

        // Fecha os streams restantes
        for (ObjectInputStream ois : streams.keySet()) {
            ois.close();
        }

        // Exclui os arquivos temporários
        for (File arquivo : arquivosTemporarios) {
            arquivo.delete();
        }
    }

    /**
     * Classe auxiliar para manter o cliente e o arquivo de origem na heap.
     */
    private static class ParClienteArquivo {
        Cliente cliente;
        ObjectInputStream origem;

        ParClienteArquivo(Cliente cliente, ObjectInputStream origem) {
            this.cliente = cliente;
            this.origem = origem;
        }
    }
}

