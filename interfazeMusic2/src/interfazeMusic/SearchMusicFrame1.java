package interfazeMusic;

import javax.swing.*;

import com.programacion.databases.Conexion;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class SearchMusicFrame1 extends JFrame {
    public SearchMusicFrame1() {
        setTitle("Buscar Música");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

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

        DefaultListModel<String> searchResults = new DefaultListModel<>();
        JList<String> resultsList = new JList<>(searchResults);
        panel.add(new JScrollPane(resultsList), BorderLayout.CENTER);
    }
    /**
     * @param nombreCancion
     */
    public  void searchMusic(String nombreCancion) {
    	
    	
    	 try {
    		 // Obtener la conexión a la base de datos
    		 Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/MySQL", "root", "123456");


    		    // Preparar la consulta SQL
    		    String sql = "SELECT Nombre_Cancion, Album_ID_Album, Album_Artista_ID_Artista, Fecha_Lanzamiento FROM canciones WHERE Nombre_Cancion LIKE ?";
    		    PreparedStatement statement = conn.prepareStatement(sql);
    		    statement.setString(1, "%" + nombreCancion + "%");
    		    // Ejecutar la consulta
    		    ResultSet resultSet = statement.executeQuery();

    		    // Actualizar la lista de resultados
    		    DefaultListModel<String> searchResults = new DefaultListModel<>();
    		    while (resultSet.next()) {
    		        String nombre = resultSet.getString("Nombre_Cancion");
    		        int idAlbum = resultSet.getInt("Album_ID_Album");
    		        int idArtista = resultSet.getInt("Album_Artista_ID_Artista");
    		        Date fechaLanzamiento = resultSet.getDate("Fecha_Lanzamiento");
    		        searchResults.addElement(nombre + " - Álbum ID: " + idAlbum + ", Artista ID: " + idArtista + ", Fecha de Lanzamiento: " + fechaLanzamiento);
    		   
    		    }
    		    // Actualizar la lista de resultados en la interfaz gráfica
    		    JList<String> resultsList = (JList<String>) getContentPane().getComponent(1);
    		    resultsList.setModel(searchResults);

    		    // Cerrar la conexión
    		    resultSet.close();
    		    statement.close();
    		    conn.close();
    		} catch (SQLException e) {
        	e.printStackTrace();
		}
    }
    }
