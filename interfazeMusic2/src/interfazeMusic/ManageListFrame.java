package interfazeMusic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ManageListFrame extends JFrame {
    private JList<String> myPlaylists;
    private DefaultListModel<String> playlistModel;
    private Connection conn;
    private MainFrame parentFrame;

    public ManageListFrame(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setTitle("Mi Lista");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        if (parentFrame != null) {
            parentFrame.setState(JFrame.ICONIFIED);
        }

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/musica", "root", "123456");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        playlistModel = new DefaultListModel<>();
        myPlaylists = new JList<>(playlistModel);
        getContentPane().add(new JScrollPane(myPlaylists), BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new FlowLayout());
        JTextField txtNewPlaylist = new JTextField(15);
        JButton btnAdd = new JButton("Crear");
        JButton btnRemove = new JButton("Eliminar");
        JButton btnSelect = new JButton("Seleccionar");

        controlsPanel.add(txtNewPlaylist);
        controlsPanel.add(btnAdd);
        controlsPanel.add(btnRemove);
        controlsPanel.add(btnSelect);

        getContentPane().add(controlsPanel, BorderLayout.SOUTH);

        loadPlaylists();

        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newPlaylist = txtNewPlaylist.getText();
                if (!newPlaylist.isEmpty()) {
                    addPlaylistToDatabase(newPlaylist);
                    txtNewPlaylist.setText("");
                    loadPlaylists(); // Actualizar la lista de reproducción
                }
            }
        });

        btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = myPlaylists.getSelectedIndex();
                if (selectedIndex != -1) {
                    String playlistName = playlistModel.getElementAt(selectedIndex);
                    int confirm = JOptionPane.showConfirmDialog(
                            ManageListFrame.this, 
                            "¿Estás seguro de que quieres eliminar la lista de reproducción?", 
                            "Confirmar eliminación", 
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        removePlaylistFromDatabase(playlistName);
                        loadPlaylists(); // Actualizar la lista de reproducción
                    }
                }
            }
        });

        btnSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = myPlaylists.getSelectedIndex();
                if (selectedIndex != -1) {
                    String playlistName = playlistModel.getElementAt(selectedIndex);
                    int playlistId = getPlaylistId(playlistName); // Obtener el ID de la lista
                    if (playlistId != -1) {
                        ManagePlaylistSongsFrame managePlaylistSongsFrame = new ManagePlaylistSongsFrame(playlistName, playlistId);
                        managePlaylistSongsFrame.setVisible(true);
                    }
                }
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                    if (parentFrame != null) {
                        parentFrame.setState(JFrame.NORMAL);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        centerWindow();
    }

    private void loadPlaylists() {
        try {
            String sql = "SELECT Nombre_Lista FROM Listas_de_Reproduccion";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            playlistModel.clear();
            while (resultSet.next()) {
                String playlistName = resultSet.getString("Nombre_Lista");
                playlistModel.addElement(playlistName);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addPlaylistToDatabase(String playlistName) {
        try {
            String sql = "INSERT INTO Listas_de_Reproduccion (Nombre_Lista, Usuario_ID_Usuario) VALUES (?, 1)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, playlistName);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removePlaylistFromDatabase(String playlistName) {
        try {
            String sql = "DELETE FROM Listas_de_Reproduccion WHERE Nombre_Lista = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, playlistName);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getPlaylistId(String playlistName) {
        try {
            String sql = "SELECT ID_Lista FROM Listas_de_Reproduccion WHERE Nombre_Lista = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, playlistName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("ID_Lista");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Si no se encuentra el ID de la lista
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManageListFrame frame = new ManageListFrame(null);
            frame.setVisible(true);
        });
    }
}
