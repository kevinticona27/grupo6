package interfazeMusic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class ManagePlaylistSongsFrame extends JFrame {
    private DefaultListModel<String> songListModel;
    private JList<String> songList;
    private String playlistName;
    private int playlistId;
    private Connection conn;
    private Player player;
    private InputStream songStream;
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private boolean isPaused = false;
    private long pauseLocation;
    private long totalSongLength;

    public ManagePlaylistSongsFrame(String playlistName, int playlistId) {
        this.playlistName = playlistName;
        this.playlistId = playlistId;
        setTitle("Gestionar Canciones de la Lista: " + playlistName);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

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
        JTextField searchField = new JTextField(15);
        JButton searchButton = new JButton("Buscar");
        JButton addButton = new JButton("Agregar");
        JButton removeButton = new JButton("Eliminar");
        playButton = new JButton("Reproducir");
        pauseButton = new JButton("Pausar");
        stopButton = new JButton("Reiniciar");

        controlsPanel.add(searchField);
        controlsPanel.add(searchButton);
        controlsPanel.add(addButton);
        controlsPanel.add(removeButton);
        controlsPanel.add(playButton);
        controlsPanel.add(pauseButton);
        controlsPanel.add(stopButton);

        getContentPane().add(controlsPanel, BorderLayout.SOUTH);

        loadPlaylistSongs();

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchMusic(searchField.getText());
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSong = songList.getSelectedValue();
                if (selectedSong != null) {
                    addSongToPlaylist(selectedSong);
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSong = songList.getSelectedValue();
                if (selectedSong != null) {
                    removeSongFromPlaylist(selectedSong);
                }
            }
        });

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSong = songList.getSelectedValue();
                if (selectedSong != null) {
                    playSong(selectedSong);
                }
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseSong();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopSong();
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

    private void loadPlaylistSongs() {
        try {
            String sql = "SELECT c.Nombre_Cancion " +
                         "FROM Canciones c " +
                         "JOIN Canciones_Lista_Reproduccion clr ON c.ID_Cancion = clr.Canciones_ID_Cancion " +
                         "WHERE clr.Listas_Reproduccion_ID = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, playlistId);
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

    private void searchMusic(String nombreCancion) {
        try {
            String sql = "SELECT Nombre_Cancion FROM Canciones WHERE Nombre_Cancion LIKE ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, "%" + nombreCancion + "%");
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
            String sql = "INSERT INTO Canciones_Lista_Reproduccion (Canciones_ID_Cancion, Listas_Reproduccion_ID) " +
                         "SELECT c.ID_Cancion, ? FROM Canciones c WHERE c.Nombre_Cancion = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, playlistId);
            statement.setString(2, songName);
            statement.executeUpdate();
            statement.close();
            JOptionPane.showMessageDialog(this, "Canción añadida a la lista de reproducción.");
            loadPlaylistSongs(); // Actualizar la lista de canciones después de agregar
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeSongFromPlaylist(String songName) {
        try {
            String sql = "DELETE clr FROM Canciones_Lista_Reproduccion clr " +
                         "JOIN Canciones c ON clr.Canciones_ID_Cancion = c.ID_Cancion " +
                         "WHERE clr.Listas_Reproduccion_ID = ? AND c.Nombre_Cancion = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, playlistId);
            statement.setString(2, songName);
            statement.executeUpdate();
            statement.close();
            JOptionPane.showMessageDialog(this, "Canción eliminada de la lista de reproducción.");
            loadPlaylistSongs(); // Actualizar la lista de canciones después de eliminar
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void playSong(String songName) {
        try {
            String sql = "SELECT Audio FROM Canciones WHERE Nombre_Cancion = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, songName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                byte[] audioBytes = resultSet.getBytes("Audio");
                songStream = new ByteArrayInputStream(audioBytes);
                totalSongLength = songStream.available();
                if (isPaused) {
                    songStream.skip(totalSongLength - pauseLocation);
                    isPaused = false;
                }
                player = new Player(songStream);
                new Thread(() -> {
                    try {
                        player.play();
                    } catch (JavaLayerException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pauseSong() {
        try {
            if (player != null) {
                pauseLocation = songStream.available();
                player.close();
                isPaused = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopSong() {
        try {
            if (player != null) {
                player.close();
                songStream.reset();
                isPaused = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManagePlaylistSongsFrame frame = new ManagePlaylistSongsFrame("Mi Lista", 1); // Cambiar por el nombre y ID reales
            frame.setVisible(true);
        });
    }
}
