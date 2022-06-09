import java.net.*; 
import java.io.*;
import java.util.Scanner; 
public class ClientFullDuplex { 
	public static void main (String[] argumentos)throws IOException{ 
		Socket cliente = null; 
		PrintWriter escritor = null; 
		String DatosEnviados = null; 
		BufferedReader entrada=null;
		Scanner datos = new Scanner(System.in);

		
		String maquina; 
		int puerto; 
		BufferedReader DatosTeclado = new BufferedReader ( new InputStreamReader (System.in)); 

		if (argumentos.length != 2){ 
			maquina = "localhost";
			System.out.println("Ingrese puerto: ");
			puerto = datos.nextInt();
			System.out.println ("Establecidos valores por defecto:\nEQUIPO = localhost\nPORT ="+puerto); 
		} 
		else{ 
			maquina = argumentos[0]; 
			Integer pasarela = new Integer (argumentos[1]); 
			puerto = pasarela.parseInt(pasarela.toString()); 
			System.out.println ("Conectado a " + maquina + " en puerto: " + puerto); 
		} 
		try{ 
			cliente = new Socket (maquina,puerto); 
		}catch (Exception e){ 
			System.out.println ("Fallo : "+ e.toString()) ; 
			System.exit (0); 
		}
		try{ 
			escritor = new PrintWriter(cliente.getOutputStream(), true);
			entrada=new BufferedReader(new InputStreamReader(cliente.getInputStream()));
 
		}catch (Exception e){ 
			System.out.println ("Fallo : "+ e.toString()); 
			cliente.close(); 
			System.exit (0); 
		} 
		String line;
		
		System.out.println("Conectado con el Servidor. Listo para enviar datos...");
		
		do{ 
			DatosEnviados = DatosTeclado.readLine(); 
			escritor.println (DatosEnviados); 
			line = entrada.readLine();
			System.out.println(line);
		}while (!DatosEnviados.equals("FIN")); 
	
		System.out.println ("Finalizada conexion con el servidor"); 
		try{ 
			escritor.close(); 
		}catch (Exception e){}
	}
} 

