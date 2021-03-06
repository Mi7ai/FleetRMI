package cliente;
//package cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

//import tablero.Partida;
import javax.swing.*;

import comun.IntServidorJuegoRMI;
import comun.IntServidorPartidasRMI;

public class ClienteFlotaRMI {

	/**
	 * Implementa el juego 'Hundir la flota' mediante una interfaz gráfica (GUI)
	 */

	// parametros por defecto de la partida 
	public static final int NUMFILAS=8, NUMCOLUMNAS=8, NUMBARCOS=6;
	public static final int AGUA = -1, TOCADO = -2, HUNDIDO = -3;
	
	// opciones menu
	public static final String SALIR="Salir", NUEVAP="Nueva Partida", SOLUCION="Solucion";

	// opciones menu2
	public static final String PROPONER="Proponer partida", ACEPTAR="Aceptar partida", BORRAR="Borrar partida", LISTAR="Listar partidas";

	// definicion variables interfaz e implemetancion callback Cliente
	private GuiTablero guiTablero = null; 	
	private IntServidorPartidasRMI partida = null; 
	private IntServidorJuegoRMI servidorJuego = null;
	private ImplCallbackCliente callBack;
	
	/** Atributos de la partida guardados en el juego para simplificar su implementación */
	private int quedan = NUMBARCOS, disparos = 0;
	private String nombre;  //Nombre jugador
	private Scanner scn;
	/**
	 * Programa principal. Crea y lanza un nuevo juego
	 * @param args
	 */
	public static void main(String[] args) {
		ClienteFlotaRMI juego = new ClienteFlotaRMI();
		juego.ejecuta();
	} // end main

	/**
	 * Lanza una nueva hebra que crea la primera partida y dibuja la interfaz grafica: tablero
	 */
	private void ejecuta() {
		// Instancia la primera partida
		try{	
//			if (System.getSecurityManager()==null)	//Generamos el SecurityManager de Java si no existe
//				System.setSecurityManager(new SecurityManager());
			
//			LocateRegistry.createRegistry(1099);
			// crear objeto remoto, partida, callback
			String registryURL = "rmi://localhost:1099/FleetRMI";
		
			servidorJuego = (IntServidorJuegoRMI)Naming.lookup(registryURL);
			
			partida = servidorJuego.nuevoServidorPartidas();
			
			partida.nuevaPartida(NUMFILAS, NUMCOLUMNAS, NUMBARCOS);
			
			callBack = new ImplCallbackCliente();
			
			scn = new Scanner(System.in);
			
			System.out.print("Introduce nombre jugador: ");
			
			nombre = scn.nextLine();
			
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					guiTablero = new GuiTablero(NUMFILAS, NUMCOLUMNAS);
 					guiTablero.dibujaTablero();
 				}
			});
 			
		}catch(Exception e){
			System.out.println("Fallo en ClienteFlotaRMI: " + e);
		}
	} // end ejecuta

	/******************************************************************************************/
	/*********************  CLASE INTERNA GuiTablero   ****************************************/
	/******************************************************************************************/
	private class GuiTablero {

		private int numFilas, numColumnas;

		private JFrame frame = null;        // Tablero de juego
		private JLabel estado = null;       // Texto en el panel de estado
		private JButton buttons[][] = null; // Botones asociados a las casillas de la partida

		/**
         * Constructor de una tablero dadas sus dimensiones
         */
		GuiTablero(int numFilas, int numColumnas) {
			this.numFilas = numFilas;
			this.numColumnas = numColumnas;
			frame = new JFrame();
			frame.addWindowListener(new CloseSocketListener());
		}// end GuiTablero

		/**
		 * Dibuja el tablero de juego y crea la partida inicial
		 */
		public void dibujaTablero() {
			anyadeMenu();
			anyadeGrid(numFilas, numColumnas);		
			anyadePanelEstado("Intentos: " + disparos + "    Barcos restantes: " + quedan);		
			frame.setSize(300, 300);
			frame.setVisible(true);	
		} // end dibujaTablero

		/**
		 * Anyade el menu de opciones del juego y le asocia un escuchador
		 */
		private void anyadeMenu() {
 			// create menu bar
			final JMenuBar menuBar = new JMenuBar();
			
			// crear menu
			JMenu opcionesMenu = new JMenu("Opciones");
			
			// crear elementos menu  
			JMenuItem nuevaPartida = new JMenuItem("Nueva Partida");
			 nuevaPartida.setActionCommand("Nueva Partida");
			JMenuItem mostrarSolucion = new JMenuItem("Mostrar Solucion");
			 mostrarSolucion.setActionCommand("Mostrar Solucion");
			JMenuItem salir = new JMenuItem("Salir");
			 salir.setActionCommand("Salir");
			
			// crear  elementos menu  listener y enlaces 
			MenuListener menuItemListener = new MenuListener();
			 nuevaPartida.addActionListener(menuItemListener);
 			 mostrarSolucion.addActionListener(menuItemListener);
 			 salir.addActionListener(menuItemListener);
			
 			 
 			// anadir elementos menu
 			 opcionesMenu.add(nuevaPartida);
 			 opcionesMenu.add(mostrarSolucion);
 			 opcionesMenu.add(salir);
			
 			// anadir elementos menu
 			 menuBar.add(opcionesMenu);
 			
 			 // crear segundo menu
 			JMenu menuMulti = new JMenu("Multijugador");
//			menuBar.add(menuMulti);

			// crear listener menu multijugador
			Menu2Listener listenerMulti = new Menu2Listener();
			
			// crear elementos menu
			JMenuItem proponer = new JMenuItem(PROPONER);  
			proponer.setActionCommand(PROPONER);
			proponer.addActionListener(listenerMulti);
			
			JMenuItem aceptar = new JMenuItem(ACEPTAR);	 
			aceptar.setActionCommand(ACEPTAR);
			aceptar.addActionListener(listenerMulti);
			
			JMenuItem borrar = new JMenuItem(BORRAR);	 
			borrar.setActionCommand(BORRAR);
			borrar.addActionListener(listenerMulti);
			
			JMenuItem listar = new JMenuItem(LISTAR); 
			listar.setActionCommand(LISTAR);
			listar.addActionListener(listenerMulti);
						

			// anadir elementos menu
			menuMulti.add(proponer);
			menuMulti.add(aceptar);
			menuMulti.add(borrar);
			menuMulti.add(listar);
			
			
			menuBar.add(menuMulti,BorderLayout.NORTH);
			
 			// anadir menubar al frame
 		      frame.setJMenuBar(menuBar);
 		      frame.setVisible(true);
			
 		} // end anyadeMenu

		/**
		 * Anyade el panel con las casillas del mar y sus etiquetas.
		 * Cada casilla sera un boton con su correspondiente escuchador
		 * @param nf	numero de filas
		 * @param nc	numero de columnas
		 */
		private void anyadeGrid(int nf, int nc) {
            // POR IMPLEMENTAR
			
			//create game table panel
			JPanel tablero = new JPanel(new GridLayout(nf, nc));
			JPanel panelNorth = new JPanel(new GridLayout(1, nc));
			JPanel panelEast = new JPanel(new GridLayout(nf, 1));
			JPanel panelWest = new JPanel(new GridLayout(nf, 1));
			buttons = new JButton[nf][nc];
			
			ButtonListener tableButtonListener = new ButtonListener();

			//North Panel
			panelNorth.add(new Label(" "));
			for (int i = 1; i <= nf; i++) {
				panelNorth.add(new Label(" "+ i));
			}
			//West Panel
			for (int i = 0; i < nc; i++) {
				panelWest.add(new Label(" "+ Character.toString((char) ('A' + i))));
			}
			//East Panel
			for (int i = 0; i < nc; i++) {
				panelEast.add(new Label(" "+ Character.toString((char) ('A' + i))));
			}
					
			//initialize buttons vector
			for (int i = 0; i < nf; i++) {
				for (int j = 0; j < nc; j++) {
					buttons[i][j]=new JButton();					
					buttons[i][j].putClientProperty("fila",i);
					buttons[i][j].putClientProperty("columna",j);					 
				}
			} 
			//add buttons to table 
			for (int i = 0; i < buttons.length; i++) {
				for (int j = 0; j < buttons.length; j++) {
					buttons[i][j].addActionListener(tableButtonListener);//a�adir evento a cada boton
					tablero.add(buttons[i][j]);//a�adir boton al tablero
				}
			}

			//add the table panel to main frame,centered
			frame.add(tablero,BorderLayout.CENTER, SwingConstants.CENTER);
			frame.add(panelNorth,BorderLayout.NORTH);
			frame.add(panelEast,BorderLayout.EAST);
			frame.add(panelWest,BorderLayout.WEST);

		} // end anyadeGrid


		/**
		 * Anyade el panel de estado al tablero
		 * @param cadena	cadena inicial del panel de estado
		 */
		private void anyadePanelEstado(String cadena) {	
			JPanel panelEstado = new JPanel();
			estado = new JLabel(cadena);
			panelEstado.add(estado);
			// El panel de estado queda en la posición SOUTH del frame
			frame.getContentPane().add(panelEstado, BorderLayout.SOUTH);
		} // end anyadePanel Estado

		/**
		 * Cambia la cadena mostrada en el panel de estado
		 * @param cadenaEstado	nuevo estado
		 */
		public void cambiaEstado(String cadenaEstado) {
			estado.setText(cadenaEstado);
		} // end cambiaEstado

		/**
		 * Muestra la solucion de la partida y marca la partida como finalizada
		 * @throws RemoteException 
		 */
		public void muestraSolucion() {
            for(int i = 0;i<NUMFILAS ; i++) {
            	for(int j=0; j<NUMCOLUMNAS ;j++) {
            		int color;
					try {
						color = partida.pruebaCasilla(i, j);
						
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}//devuelve color casilla        						 
            	}
            }
 
			guiTablero.cambiaEstado("LA SOLUCION ES : ");
			try {
				for(String cadenaBarco : partida.getSolucion()) {
					pintaBarcoHundido(cadenaBarco);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			quedan=0;
		} // end muestraSolucion


		/**
		 * Pinta un barco como hundido en el tablero
		 * @param cadenaBarco	cadena con los datos del barco codifificados como
		 *                      "filaInicial#columnaInicial#orientacion#tamanyo"
		 */
		public void pintaBarcoHundido(String cadenaBarco) {			
			String[] partes = cadenaBarco.split("#");
			int fila = Integer.parseInt(partes[0]);
			
 			int col = Integer.parseInt(partes[1]);
 			char ori = partes[2].charAt(0);
			
 			int tam = Integer.parseInt(partes[3]);
			
			switch (ori){
				case 'H':
					for (int i = col; i < col+tam; i++) {
						JButton btn =  buttons[fila][i];
						pintaBoton(btn, Color.red);
						
					}
					break;
				case 'V':
					for (int i = fila; i < fila+tam; i++) {
						JButton btn =  buttons[i][col];
						pintaBoton(btn, Color.red);
						
					}
					break;
			}
			JButton btn = null;
			for (int i = 0; i < tam; i++) {
				if (ori=='H') {
					btn = buttons[fila][col+i];
				}else{
					btn = buttons[fila+i][col];
				}
				pintaBoton(btn, Color.red);
			}	 
		quedan--;
		} // end pintaBarcoHundido

		/**
		 * Pinta un botón de un color dado
		 * @param b			boton a pintar
		 * @param color		color a usar
		 */
		public void pintaBoton(JButton b, Color color) {
			b.setBackground(color);
			// El siguiente código solo es necesario en Mac OS X
			b.setOpaque(true);
			b.setBorderPainted(false);
		} // end pintaBoton

		/**
		 * Limpia las casillas del tablero pintándolas del gris por defecto
		 */
		public void limpiaTablero() {
			for (int i = 0; i < numFilas; i++) {
				for (int j = 0; j < numColumnas; j++) {
					buttons[i][j].setBackground(null);
					buttons[i][j].setOpaque(true);
					buttons[i][j].setBorderPainted(true);
				}
			}
		} // end limpiaTablero

		/**
		 * 	Destruye y libera la memoria de todos los componentes del frame
		 */
		public void liberaRecursos() {
			frame.dispose();
		} // end liberaRecursos


	} // end class GuiTablero

	/******************************************************************************************/
	/*********************  CLASE INTERNA MenuListener ****************************************/
	/******************************************************************************************/

	/**
	 * Clase interna que escucha el menu de Opciones del tablero
	 * 
	 */
	private class MenuListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
            // POR IMPLEMENTAR
			switch (e.getActionCommand()) {
			
			case NUEVAP:
 				guiTablero.limpiaTablero();
//				partida= new Partida(NUMFILAS, NUMCOLUMNAS, NUMBARCOS);				 
				disparos=0;
 				quedan = NUMBARCOS;
				guiTablero.cambiaEstado("Intentos: "+ disparos + " Barcos restantes: "+ quedan);
				 try {
					partida.nuevaPartida(NUMFILAS, NUMCOLUMNAS, NUMBARCOS);
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					System.out.println("Fallo en MenuListener->NUEVAP: ");e1.printStackTrace();
				}
				break;
			case SOLUCION:
				guiTablero.muestraSolucion();
				break;
			case SALIR:
				guiTablero.liberaRecursos();
				break;	
			default:
				break;
			}
			 
			
			
		} // end actionPerformed

	} // end class MenuListener



	/******************************************************************************************/
	/*********************  CLASE INTERNA ButtonListener **************************************/
	/******************************************************************************************/
	/**
	 * Clase interna que escucha cada uno de los botones del tablero
	 * Para poder identificar el boton que ha generado el evento se pueden usar las propiedades
	 * de los componentes, apoyandose en los metodos putClientProperty y getClientProperty
	 */
	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (quedan > 0) {
				JButton btn = (JButton) e.getSource();//boton pulsado
				int fila =(int) btn.getClientProperty("fila");
				int col =(int) btn.getClientProperty("columna");
				int color;
				try {
					color = partida.pruebaCasilla(fila, col);
					switch (color) {
					case AGUA:
						guiTablero.pintaBoton(btn, Color.cyan);
						break;
					case TOCADO:

						guiTablero.pintaBoton(btn, Color.ORANGE);
						break;
					default://si devuelve la id del barco esta hundido
						if(color>=0) {
							guiTablero.pintaBarcoHundido(partida.getBarco(color));
						}
						break;
					}//end switch
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}//devuelve color casilla
				guiTablero.cambiaEstado("Intentos: "+ ++disparos + " Barcos restantes: "+ quedan);
			}else{
				guiTablero.cambiaEstado("Game over! Acabado en: "+disparos+" intentos");
			}
		} // end actionPerformed

	} // end class ButtonListener
	
	/******************************************************************************************/
	/*********************  CLASE INTERNA Menu2Listener  **************************************/
	/******************************************************************************************/
	/*
	 *  Clase escuchador menu multijugador.
	 * */
	private class Menu2Listener implements ActionListener{

		public void actionPerformed(ActionEvent e){
			try{
				switch(e.getActionCommand()){
				case PROPONER:
					// si es true, hay partida
					if (servidorJuego.proponerPartida(nombre, callBack))
						System.out.println(nombre+": ha propuesto una partida. ");
					else
						System.out.println("Partida propuesta existente.");
					break;
				case ACEPTAR:
					System.out.print("Introduce el nombre del jugaror cuya partida quieres aceptar: ");
					String oponente = scn.nextLine();
					System.out.println("Mi partida es: "+nombre);
					if (servidorJuego.aceptarPartida(nombre, oponente)) { 
						System.out.println("Partida "+oponente+" aceptada.");
						
					}else if (oponente.equals(nombre)) {
						System.out.println("Error: estas intentando acceptar tu propria partida y lo sabias -_- .");
						
					}else {
						System.out.println("Error al aceptar la partida de oponente: "+oponente+" .");
					}
					break;	
				case BORRAR:
					if (servidorJuego.borrarPartida(nombre))
						System.out.println("Confirmado: partida del jugador "+nombre+" borrada.");
					else
						System.out.println("No hay partidas existentes.");					
					break;
				case LISTAR:
					String[] listaPartidas = servidorJuego.mostrarPartidas();
//					if (listaPartidas.length == 0) {
//						System.out.println("Lista partidas vacia.");
//					}else {
						System.out.println("Lista partidas propuestas: ");
						for (String partida : listaPartidas){
							System.out.print(partida+" ");
						}
						System.out.println(" --- ");
//					}
					break;
				}
			}catch(Exception ex){
				System.out.println("Fallo en Menu2Listener.");
			}
		}
	}//end class Menu2Listeners

	/******************************************************************************************/
	/*********************  CLASE INTERNA CloseSocketListener *********************************/
	/******************************************************************************************/
	/*
	 * Clase para cerrar el socket si se cierra la venana.
	 * */
	private class CloseSocketListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			 
				try {
					servidorJuego.destruir();
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			 
		}

	} //end class CloseSocketListener
	
} // end class Juego
