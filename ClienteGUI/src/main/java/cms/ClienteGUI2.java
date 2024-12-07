package cms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class ClienteGUI2 extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private BufferDeClientes bufferDeClientes;
    private final int TAMANHO_BUFFER = 10000;
    private int registrosCarregados = 0;
    private String arquivoSelecionado;
    private boolean arquivoCarregado = false;

    private JTextField txtNomePesquisa; // Campo de texto para pesquisa
    private JButton btnPesquisar; // Botão de pesquisa

    public ClienteGUI2() {
        setTitle("Gerenciamento de Clientes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        bufferDeClientes = new BufferDeClientes();
        criarInterface();
    }

    private void carregarArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        int retorno = fileChooser.showOpenDialog(this);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            arquivoSelecionado = fileChooser.getSelectedFile().getAbsolutePath();
            bufferDeClientes.associaBuffer(new ArquivoCliente());
            bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado);
            registrosCarregados = 0;
            tableModel.setRowCount(0);
            carregarMaisClientes();
            arquivoCarregado = true;
        }
    }

    private void criarInterface() {
        JPanel panel = new JPanel(new BorderLayout());

        // Adicionando painel para pesquisa
        JPanel pesquisaPanel = new JPanel(new FlowLayout());
        txtNomePesquisa = new JTextField(20); // Campo de texto com tamanho 20
        btnPesquisar = new JButton("Pesquisar Cliente");

        pesquisaPanel.add(new JLabel("Nome do Cliente:"));
        pesquisaPanel.add(txtNomePesquisa);
        pesquisaPanel.add(btnPesquisar);

        // Tabela
        tableModel = new DefaultTableModel(new String[]{"#", "Nome", "Sobrenome", "Telefone", "Endereço", "Credit Score"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!scrollPane.getVerticalScrollBar().getValueIsAdjusting()) {
                    if (arquivoCarregado &&
                            scrollPane.getVerticalScrollBar().getValue() +
                                    scrollPane.getVerticalScrollBar().getVisibleAmount() >=
                                    scrollPane.getVerticalScrollBar().getMaximum()) {
                        carregarMaisClientes();
                    }
                }
            }
        });

        btnPesquisar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pesquisarCliente();
            }
        });

        JButton btnCarregar = new JButton("Carregar Clientes");
        btnCarregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                carregarArquivo();
            }
        });

        panel.add(pesquisaPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(btnCarregar, BorderLayout.SOUTH);
        add(panel);
    }

    private void carregarMaisClientes() {
        Cliente[] clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER);
        if (clientes != null && clientes.length > 0) {
            for (Cliente cliente : clientes) {
                if (cliente != null) {
                    tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, cliente.getNome(), cliente.getSobrenome(), cliente.getTelefone(), cliente.getEndereco(), cliente.getCreditScore()});
                }
            }
            registrosCarregados += clientes.length;
        }
    }

    private void pesquisarCliente() {
        String nomePesquisa = txtNomePesquisa.getText().trim();
        if (nomePesquisa.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, insira um nome para pesquisar.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        bufferDeClientes.inicializaBuffer("leitura", arquivoSelecionado); // Reinicializa o buffer para leitura
        Cliente clienteEncontrado = null;
        Cliente[] clientes;

        // Percorrendo o arquivo em blocos
        while ((clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER)) != null && clientes.length > 0) {
            for (Cliente cliente : clientes) {
                if (cliente != null && cliente.getNome().equalsIgnoreCase(nomePesquisa)) {
                    clienteEncontrado = cliente;
                    break;
                }
            }
            if (clienteEncontrado != null) {
                break; // Se o cliente foi encontrado, sai do loop
            }
        }

        // Exibindo o resultado da pesquisa
        if (clienteEncontrado != null) {
            StringBuilder resultado = new StringBuilder("Cliente Encontrado:\n");
            resultado.append("Nome: ").append(clienteEncontrado.getNome()).append("\n");
            resultado.append("Sobrenome: ").append(clienteEncontrado.getSobrenome()).append("\n");
            resultado.append("Telefone: ").append(clienteEncontrado.getTelefone()).append("\n");
            resultado.append("Endereço: ").append(clienteEncontrado.getEndereco()).append("\n");
            resultado.append("Credit Score: ").append(clienteEncontrado.getCreditScore());
            JOptionPane.showMessageDialog(this, resultado.toString(), "Resultado da Pesquisa", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Cliente não encontrado.", "Resultado da Pesquisa", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteGUI2 gui = new ClienteGUI2();
            gui.setVisible(true);
        });
    }
}
