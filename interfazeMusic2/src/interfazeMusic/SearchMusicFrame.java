package interfazeMusic;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class SearchMusicFrame extends JFrame {
    private DefaultListModel<String> searchResults;
    private JList<String> resultsList;
    private Connection conn;
    private Player player;
    private byte[] currentAudioBytes;

    public SearchMusicFrame(JFrame parentFrame) {
        setTitle("Buscar Música");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // Minimize the parent frame
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

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);

        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(e -> searchMusic(searchField.getText()));

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        panel.add(searchPanel, BorderLayout.NORTH);

        searchResults = new DefaultListModel<>();
        resultsList = new JList<>(searchResults);
        panel.add(new JScrollPane(resultsList), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        JButton playButton = new JButton("Reproducir");
        JButton pauseButton = new JButton("Pausa");
        JButton resumeButton = new JButton("Continuar");
        JButton restartButton = new JButton("Reiniciar");

        playButton.addActionListener(e -> playMusic(resultsList.getSelectedValue()));
        pauseButton.addActionListener(e -> pauseMusic());
        resumeButton.addActionListener(e -> resumeMusic());
        restartButton.addActionListener(e -> restartMusic());

        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(resumeButton);
        controlPanel.add(restartButton);
        panel.add(controlPanel, BorderLayout.SOUTH);
    }

    private void searchMusic(String nombreCancion) {
        try {
            String sql = "SELECT c.ID_Cancion, c.Nombre_Cancion, c.Fecha_Lanzamiento, a.Nombre_Album, ar.Nombre " +
                         "FROM Canciones c " +
                         "JOIN Album a ON c.Album_ID_Album = a.ID_Album " +
                         "JOIN Artista ar ON a.Artista_ID_Artista = ar.ID_Artista " +
                         "WHERE c.Nombre_Cancion LIKE ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, "%" + nombreCancion + "%");
            ResultSet resultSet = statement.executeQuery();
            searchResults.clear();
            while (resultSet.next()) {
                String idCancion = resultSet.getString("ID_Cancion");
                String nombre = resultSet.getString("Nombre_Cancion");
                String nombreAlbum = resultSet.getString("Nombre_Album");
                String nombreArtista = resultSet.getString("Nombre");
                Date fechaLanzamiento = resultSet.getDate("Fecha_Lanzamiento");
                searchResults.addElement(idCancion + ": " + nombre + " - Álbum: " + nombreAlbum + ", Artista: " + nombreArtista + ", Fecha de Lanzamiento: " + fechaLanzamiento);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void playMusic(String selectedSong) {
        if (selectedSong != null) {
            String[] parts = selectedSong.split(":");
            int songId = Integer.parseInt(parts[0].trim());
            try {
                String sql = "SELECT Audio FROM Canciones WHERE ID_Cancion = ?";
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setInt(1, songId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    currentAudioBytes = resultSet.getBytes("Audio");
                    playAudio(currentAudioBytes);
                }
                resultSet.close();
                statement.close();
            } catch (SQLException | JavaLayerException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona una canción para reproducir.");
        }
    }

    private void playAudio(byte[] audioBytes) throws JavaLayerException {
        if (player != null) {
            player.close();
        }
        InputStream inputStream = new ByteArrayInputStream(audioBytes);
        player = new Player(inputStream);
        Thread playerThread = new Thread(() -> {
            try {
                player.play();
            } catch (JavaLayerException e) {
                e.printStackTrace();
            }
        });
        playerThread.start();
    }

    private void pauseMusic() {
        if (player != null) {
            player.close();
        }
    }

    private void resumeMusic() {
        try {
            playAudio(currentAudioBytes);
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
    }

    private void restartMusic() {
        if (player != null) {
            player.close();
        }
        try {
            playAudio(currentAudioBytes);
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SearchMusicFrame frame = new SearchMusicFrame(null);
            frame.setVisible(true);
        });
    }
}
