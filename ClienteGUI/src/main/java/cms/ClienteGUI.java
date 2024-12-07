package cms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class ClienteGUI {

    private BufferDeClientes bufferDeClientes;
    private JTable tabelaClientes;
    private DefaultTableModel modeloTabela;

    public ClienteGUI() {
        bufferDeClientes = new BufferDeClientes();
        criarInterface();
    }

    private void criarInterface() {
        JFrame frame = new JFrame("Gerenciador de Clientes");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Painel de controle
        JPanel painelControle = new JPanel();
        JButton btnCarregar = new JButton("Carregar Clientes");
        JButton btnPesquisar = new JButton("Pesquisar Cliente");
        JButton btnLimpar = new JButton("Limpar Busca");
        JButton btnInserir = new JButton("Inserir Cliente");
        JButton btnRemover = new JButton("Remover Cliente");

        painelControle.add(btnCarregar);
        painelControle.add(btnPesquisar);
        painelControle.add(btnLimpar);
        painelControle.add(btnInserir);
        painelControle.add(btnRemover);
        frame.add(painelControle, BorderLayout.NORTH);

        // Modelo da tabela
        modeloTabela = new DefaultTableModel(new Object[]{"#", "Nome", "Sobrenome", "Endereço", "Telefone", "CreditScore"}, 0);
        tabelaClientes = new JTable(modeloTabela) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Impede a edição das células
            }
        };

        // Ajusta a largura da primeira coluna
        tabelaClientes.getColumnModel().getColumn(0).setPreferredWidth(30);

        JScrollPane scrollPane = new JScrollPane(tabelaClientes);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Ações dos botões
        btnCarregar.addActionListener(e -> carregarClientes());
        btnPesquisar.addActionListener(e -> pesquisarCliente());
        btnLimpar.addActionListener(e -> {
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabela);
            tabelaClientes.setRowSorter(sorter);
            sorter.setRowFilter(null); // Remove qualquer filtro
            JOptionPane.showMessageDialog(null, "Filtro removido. Exibindo todos os clientes.");
        });
        btnInserir.addActionListener(e -> inserirCliente());
        btnRemover.addActionListener(e -> removerCliente());

        frame.setVisible(true);
    }

    private void carregarClientes() {
        String nomeArquivo = JOptionPane.showInputDialog(null, "Digite o nome do arquivo de clientes:");

        if (nomeArquivo != null && !nomeArquivo.trim().isEmpty()) {
            String nomeArquivoOrdenado = "clientes_ordenados.dat";

            // Ordena e carrega os clientes
            OrdenacaoClientes ordenacao = new OrdenacaoClientes();
            ordenacao.ordenarClientes(nomeArquivo, nomeArquivoOrdenado);

            bufferDeClientes.associaBuffer(new ArquivoCliente());
            bufferDeClientes.inicializaBuffer("leitura", nomeArquivoOrdenado);

            modeloTabela.setRowCount(0);

            Cliente cliente;
            int contador = 1;
            while ((cliente = bufferDeClientes.proximoCliente()) != null) {
                modeloTabela.addRow(new Object[]{contador++, cliente.getNome(), cliente.getSobrenome(), cliente.getEndereco(), cliente.getTelefone(), cliente.getCreditScore()});
            }

            bufferDeClientes.fechaBuffer();

            JOptionPane.showMessageDialog(null, "Clientes carregados e ordenados com sucesso!");
        } else {
            JOptionPane.showMessageDialog(null, "Nome do arquivo não pode ser vazio.");
        }
    }

    private void pesquisarCliente() {
        String termo = JOptionPane.showInputDialog(null, "Digite o nome ou sobrenome do cliente a ser pesquisado:");
        if (termo != null && !termo.trim().isEmpty()) {
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabela);
            tabelaClientes.setRowSorter(sorter);

            // RowFilter personalizado para filtrar apenas as colunas de Nome (1) e Sobrenome (2)
            RowFilter<DefaultTableModel, Integer> filtro = new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String nome = entry.getStringValue(1); // Coluna Nome
                    String sobrenome = entry.getStringValue(2); // Coluna Sobrenome
                    return nome.toLowerCase().contains(termo.toLowerCase()) || sobrenome.toLowerCase().contains(termo.toLowerCase());
                }
            };

            sorter.setRowFilter(filtro);

            JOptionPane.showMessageDialog(null, "Resultados filtrados com o termo: " + termo);
        } else {
            JOptionPane.showMessageDialog(null, "Termo de pesquisa não pode ser vazio.");
        }
    }

    private void inserirCliente() {
        String nome = JOptionPane.showInputDialog(null, "Digite o nome do cliente:");
        String sobrenome = JOptionPane.showInputDialog(null, "Digite o sobrenome do cliente:");
        String endereco = JOptionPane.showInputDialog(null, "Digite o endereço do cliente:");
        String telefone = JOptionPane.showInputDialog(null, "Digite o telefone do cliente:");
        String creditScoreStr = JOptionPane.showInputDialog(null, "Digite o Credit Score do cliente:");

        try {
            int creditScore = Integer.parseInt(creditScoreStr);

            Cliente novoCliente = new Cliente(nome, sobrenome, endereco, telefone, creditScore);

            modeloTabela.addRow(new Object[]{
                    modeloTabela.getRowCount() + 1,
                    novoCliente.getNome(),
                    novoCliente.getSobrenome(),
                    novoCliente.getEndereco(),
                    novoCliente.getTelefone(),
                    novoCliente.getCreditScore()
            });

            JOptionPane.showMessageDialog(null, "Cliente inserido com sucesso!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "O Credit Score deve ser um número válido.");
        }
    }

    private void removerCliente() {
        int linhaSelecionada = tabelaClientes.getSelectedRow();

        if (linhaSelecionada >= 0) {
            int modelRow = tabelaClientes.convertRowIndexToModel(linhaSelecionada); // Converte o índice visual para o modelo
            modeloTabela.removeRow(modelRow); // Remove a linha do modelo

            // Reajusta os índices da coluna "#"
            for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                modeloTabela.setValueAt(i + 1, i, 0);
            }

            JOptionPane.showMessageDialog(null, "Cliente removido com sucesso!");
        } else {
            JOptionPane.showMessageDialog(null, "Selecione um cliente para remover.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClienteGUI::new);
    }
}
