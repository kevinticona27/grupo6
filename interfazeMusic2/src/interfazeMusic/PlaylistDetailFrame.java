package interfazeMusic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class PlaylistDetailFrame extends JFrame {
    private DefaultListModel<String> songListModel;
    private JList<String> songList;
    private String playlistName;
    private Connection conn;

    public PlaylistDetailFrame(String playlistName, JFrame parentFrame) {
        this.playlistName = playlistName;

        setTitle("Gestionar Lista de Reproducción: " + playlistName);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        
        if (parentFrame != null) {
            parentFrame.setState(JFrame.ICONIFIED);
        }

        // Center this frame
        centerWindow();
        
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/musica", "root", "123456");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        songListModel = new DefaultListModel<>();
        songList = new JList<>(songListModel);
        getContentPane().add(new JScrollPane(songList), BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new FlowLayout());
        JTextField txtSongSearch = new JTextField(15);
        JButton btnAddSong = new JButton("Añadir Canción");
        JButton btnRemoveSong = new JButton("Eliminar Canción");

        controlsPanel.add(txtSongSearch);
        controlsPanel.add(btnAddSong);
        controlsPanel.add(btnRemoveSong);

        getContentPane().add(controlsPanel, BorderLayout.SOUTH);

        loadSongs();

        btnAddSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String songName = txtSongSearch.getText();
                if (!songName.isEmpty()) {
                    addSongToPlaylist(songName);
                    loadSongs(); // Reload songs after adding
                }
            }
        });

        btnRemoveSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = songList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String songName = songListModel.getElementAt(selectedIndex);
                    removeSongFromPlaylist(songName);
                    songListModel.remove(selectedIndex);
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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadSongs() {
        try {
            String sql = "SELECT c.Nombre_Cancion FROM Canciones c JOIN Listas_de_Reproduccion_Canciones lrc ON c.ID_Cancion = lrc.Cancion_ID_Cancion JOIN Listas_de_Reproduccion lr ON lrc.Lista_ID_Lista = lr.ID_Lista WHERE lr.Nombre_Lista = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, playlistName);
            ResultSet resultSet = statement.executeQuery();

            songListModel.clear();
            while (resultSet.next()) {
                String songName = resultSet.getString("Nombre_Cancion");
                songListModel.addElement(songName);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addSongToPlaylist(String songName) {
        try {
            String sql = "INSERT INTO Listas_de_Reproduccion_Canciones (Lista_ID_Lista, Cancion_ID_Cancion) VALUES ((SELECT ID_Lista FROM Listas_de_Reproduccion WHERE Nombre_Lista = ?), (SELECT ID_Cancion FROM Canciones WHERE Nombre_Cancion = ?))";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, playlistName);
            statement.setString(2, songName);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeSongFromPlaylist(String songName) {
        try {
            String sql = "DELETE FROM Listas_de_Reproduccion_Canciones WHERE Lista_ID_Lista = (SELECT ID_Lista FROM Listas_de_Reproduccion WHERE Nombre_Lista = ?) AND Cancion_ID_Cancion = (SELECT ID_Cancion FROM Canciones WHERE Nombre_Cancion = ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, playlistName);
            statement.setString(2, songName);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

}
