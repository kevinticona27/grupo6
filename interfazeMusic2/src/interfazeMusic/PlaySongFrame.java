package interfazeMusic;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;

public class PlaySongFrame extends JFrame {
    private String song;
    private Player player;
    private Thread playerThread;
    private boolean isPaused;
    private InputStream audioStream;

    public PlaySongFrame(String song, JFrame parentFrame) {
        this.song = song;
        this.isPaused = false;

        setTitle("Reproduciendo: " + song);
        setSize(400, 200);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Minimize the parent frame
        if (parentFrame != null) {
            parentFrame.setState(JFrame.ICONIFIED);
        }

        // Center this frame
        centerWindow();

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        JButton btnPlay = new JButton("Play");
        JButton btnPause = new JButton("Pause");
        JButton btnRestart = new JButton("Restart");

        controlPanel.add(btnPlay);
        controlPanel.add(btnPause);
        controlPanel.add(btnRestart);

        add(controlPanel, BorderLayout.SOUTH);

        btnPlay.addActionListener(e -> playSong());
        btnPause.addActionListener(e -> pauseSong());
        btnRestart.addActionListener(e -> restartSong());

        // Load the audio data from the database
        loadAudioData();
    }

    private void playSong() {
        if (isPaused) {
            isPaused = false;
            synchronized (playerThread) {
                playerThread.notify();
            }
        } else {
            if (player != null) {
                player.close();
            }

            playerThread = new Thread(() -> {
                try {
                    player = new Player(audioStream);
                    player.play();
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                }
            });

            playerThread.start();
        }
    }

    private void pauseSong() {
        isPaused = true;
        if (player != null) {
            player.close();
        }
    }

    private void restartSong() {
        if (player != null) {
            player.close();
        }

        // Reload the audio data
        loadAudioData();

        playerThread = new Thread(() -> {
            try {
                player = new Player(audioStream);
                player.play();
            } catch (JavaLayerException e) {
                e.printStackTrace();
            }
        });

        playerThread.start();
    }

    private void loadAudioData() {
        try {
            byte[] audioData = getAudioDataFromDatabase(song);
            if (audioData != null) {
                audioStream = new ByteArrayInputStream(audioData);
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el audio para la canción: " + song);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private byte[] getAudioDataFromDatabase(String songName) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/musica", "root", "123456");
        String sql = "SELECT Audio FROM Canciones WHERE Nombre_Cancion = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, songName);
        ResultSet resultSet = statement.executeQuery();

        byte[] audioData = null;
        if (resultSet.next()) {
            audioData = resultSet.getBytes("Audio");
        }

        resultSet.close();
        statement.close();
        conn.close();

        return audioData;
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }
}
