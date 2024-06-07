package interfazeMusic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MainFrame extends JFrame {
    private JTextField textField;
    private DefaultListModel<String> searchResults;
    private JList<String> resultsList;

    public MainFrame() {
        setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\victus\\eclipse-workspace\\interfazeMusic\\Imagenes\\minimalism-humor-music-player-bench-wallpaper-preview.jpg"));
        setAlwaysOnTop(true);
        setTitle("Music");
        setSize(851, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(30, 30, 30));

        JLabel logoLabel = new JLabel("Music");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(logoLabel);

        getContentPane().add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBackground(new Color(40, 40, 40));

        JLabel searchLabel = new JLabel("Busca tu música...");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("Arial", Font.BOLD, 18));
        centerPanel.add(searchLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(new Color(128, 128, 255));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchPanel.setLayout(new BorderLayout());

        textField = new JTextField();
        searchPanel.add(textField, BorderLayout.NORTH);

        searchResults = new DefaultListModel<>();
        resultsList = new JList<>(searchResults);
        searchPanel.add(new JScrollPane(resultsList), BorderLayout.CENTER);

        centerPanel.add(searchPanel, BorderLayout.CENTER);

        getContentPane().add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.setBackground(new Color(20, 20, 20));
        JButton btnSearch = new JButton("Buscar Música");
        JButton btnMyList = new JButton("Mi Lista");
        JButton btnHome = new JButton("Usuario");

        bottomPanel.add(btnHome);
        bottomPanel.add(btnSearch);
        bottomPanel.add(btnMyList);

        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        btnHome.setBackground(new Color(0, 0, 255));
        btnSearch.setForeground(new Color(0, 0, 0));
        btnMyList.setForeground(new Color(0, 0, 0));

        btnHome.addActionListener(e -> new LoginFrame(this).setVisible(true));
        btnSearch.addActionListener(e -> new SearchMusicFrame(this).setVisible(true));
        btnMyList.addActionListener(e -> new ManageListFrame(this).setVisible(true));

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchMusic(textField.getText());
            }
        });

        JButton btnPlay = new JButton("Reproducir");
        searchPanel.add(btnPlay, BorderLayout.SOUTH);

        btnPlay.addActionListener(e -> {
            String selectedSong = getSelectedSong();
            if (selectedSong != null) {
                JOptionPane.showMessageDialog(null, "Reproduciendo: " + selectedSong);
            } else {
                JOptionPane.showMessageDialog(null, "Por favor, selecciona una canción para reproducir.");
            }
        });
    }

    private void searchMusic(String nombreCancion) {
        searchResults.clear();
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/musica", "root", "123456");
            String sql = "SELECT c.Nombre_Cancion, a.Nombre_Album, ar.Nombre " +
                         "FROM Canciones c " +
                         "JOIN Album a ON c.Album_ID_Album = a.ID_Album " +
                         "JOIN Artista ar ON a.Artista_ID_Artista = ar.ID_Artista " +
                         "WHERE c.Nombre_Cancion LIKE ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, "%" + nombreCancion + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String nombre = resultSet.getString("Nombre_Cancion");
                String nombreAlbum = resultSet.getString("Nombre_Album");
                String nombreArtista = resultSet.getString("Nombre");
                searchResults.addElement(nombre + " - Álbum: " + nombreAlbum + ", Artista: " + nombreArtista);
            }
            resultSet.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getSelectedSong() {
        int selectedIndex = resultsList.getSelectedIndex();
        if (selectedIndex != -1) {
            return searchResults.getElementAt(selectedIndex).split(" - ")[0];
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

