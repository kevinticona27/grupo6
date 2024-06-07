package interfazeMusic;

import javax.swing.*;
import java.awt.*;

public class PlayMusicFrame extends JFrame {
    public PlayMusicFrame() {
        setTitle("Reproducir Música");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        JLabel nowPlaying = new JLabel("Now Playing: ");
        nowPlaying.setHorizontalAlignment(SwingConstants.CENTER);
        getContentPane().add(nowPlaying, BorderLayout.NORTH);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new FlowLayout());
        JButton btnPlay = new JButton("Play");
        JButton btnPause = new JButton("Pause");
        JButton btnStop = new JButton("Stop");

        controlsPanel.add(btnPlay);
        controlsPanel.add(btnPause);
        controlsPanel.add(btnStop);

        getContentPane().add(controlsPanel, BorderLayout.SOUTH);

        // Aquí puedes agregar la lógica para controlar la reproducción de música
    }
}
