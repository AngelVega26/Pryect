import java.net.*;
import java.io.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
//import java.text.CompactNumberFormat;
import java.util.*;


public class MultiServerThread extends Thread {
   private Socket socket = null;

   String maquinaX="localhost";
   int puertoX=13777;

   String json;//DIRECCION JSON
   String sucursal; //Sucursal de servidor conectado

   PrintWriter escritorX = null;
   //String DatosEnviadosX = null;
   BufferedReader entradaX =null;
   Socket clienteX = null;
   boolean flag_s01=false,flag_s02=false,flag_s03=false;//Identificar si se conecta
   boolean flag_and =false, flag_or=false;  //identificar operacion
    int N_condiciones;
    int a;
    int b;
   //Servicios s= new Servicios("0","0",0,0);

    public MultiServerThread(Socket socket,String json,String sucursal) {
      super("MultiServerThread");
      this.socket = socket;
      this.json=json;
      this.sucursal=sucursal;
      Suc002.NoClients++;
      System.out.println("After Init");
   }

   public void run() {

      try {
         PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
         BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         String lineIn; //lineOut;


	     while((lineIn = entrada.readLine()) != null){
	         flag_and=false;
	         flag_or=false;
	         flag_s03=false;
	         flag_s02=false;
	         flag_s01=false;
	         N_condiciones=1;
             a=0;
             System.out.println("Recibido: "+lineIn);
            escritor.flush();
            if(lineIn.equals("FIN")){
                Suc002.NoClients--;
			          break;
            }
            else if(lineIn.toLowerCase().replace(" ","").startsWith("select")) { //DETECTA CONSULTA SELECT
                String Consulta_limpia = lineIn.toLowerCase().replace(" ", "");
                String Consulta_a_enviar = Consulta_limpia;                                        //CONSULTA QUE SE VA A ENVIAR A LOS OTROS SERVIDORES
                Consulta_limpia = Consulta_limpia.replace("select", "#+");
                Consulta_limpia = Consulta_limpia.replace("from", "#*");
                Consulta_limpia = Consulta_limpia.replace("where", "#-"); //OBTENER CONSULTA EN MINUSCULAS CON DATOS CAMBIADOS PARA IDENTIFICAR CADA PARTE
                //System.out.println(Consulta_limpia);
                //escritor.println("Echo... "+Consulta_limpia);
                //escritor.flush();

                //Obtener datos a buscar
                int ini_codigo = Consulta_limpia.indexOf('+') + 1;
                int fin_codigo = Consulta_limpia.indexOf('*') - 1;
                String Consulta_datos = Consulta_limpia.substring(ini_codigo, fin_codigo);// Nos da los datos a mostrar
                String[] Consulta_datos_spl = Consulta_datos.split(",", 6);//Separa los datos
                
                //Obtener datos para seleccion

                ini_codigo = Consulta_limpia.indexOf('-') + 1;
                String Consulta_sucursal = Consulta_limpia.substring(ini_codigo); //Obtiene las condiciones de la consulta
                Consulta_a_enviar = Consulta_a_enviar.replace(Consulta_sucursal, "");//Obtiene la consulta general sin las condiciones para que este sea enviado a los demas servers

                escritor.flush();

                flag_s01 = Consulta_sucursal.contains("sucursal=suc001"); //Identifica si estan pidiendo consultas especificas para otro servidor
                flag_s02 = Consulta_sucursal.contains("sucursal=suc002");
                flag_s03 = Consulta_sucursal.contains("sucursal=suc003");


                //IDENTIFICAR CUALES SUCURSALES PIDE Y ELIMINAR DE LA CONSULTA ORIGINAL
                if (!flag_s01 & !flag_s02 & !flag_s03) { //Pone en true todas las flags para realizar las consultas en los 3 servidores
                    flag_s01 = true;
                    flag_s02 = true;
                    flag_s03 = true;
                } else {
                    if (flag_s01) {//Eliminar datos de las consultas
                        if (flag_s02) {
                            if (flag_s03) {
                                if (Consulta_sucursal.contains("orsucursal=suc003and")) {
                                    Consulta_sucursal = Consulta_sucursal.replace("orsucursal=suc003and", "");
                                } else {
                                    Consulta_sucursal = Consulta_sucursal.replace("orsucursal=suc003", "");
                                }
                                Consulta_sucursal = Consulta_sucursal.replace("orsucursal=suc002", "");
                            } else {
                                if (Consulta_sucursal.contains("orsucursal=suc002and")) {
                                    Consulta_sucursal = Consulta_sucursal.replace("orsucursal=suc002and", "");
                                } else {
                                    Consulta_sucursal = Consulta_sucursal.replace("orsucursal=suc002", "");
                                }
                            }
                            Consulta_sucursal = Consulta_sucursal.replace("sucursal=suc001", "");
                        } else if (flag_s03) {
                            if (Consulta_sucursal.contains("orsucursal=suc003and")) {
                                Consulta_sucursal = Consulta_sucursal.replace("orsucursal=suc003and", "");
                            } else {
                                Consulta_sucursal = Consulta_sucursal.replace("orsucursal=suc003", "");
                            }
                            Consulta_sucursal = Consulta_sucursal.replace("sucursal=suc001", "");
                        } else {
                            if (Consulta_sucursal.contains("sucursal=suc001and")) {
                                Consulta_sucursal = Consulta_sucursal.replace("sucursal=suc001and", "");
                            } else {
                                Consulta_sucursal = Consulta_sucursal.replace("sucursal=suc001", "");
                            }
                        }

                    } else if (flag_s02) {
                        if (flag_s03) {
                            if (Consulta_sucursal.contains("orsucursal=suc003and")) {
                                Consulta_sucursal = Consulta_sucursal.replace("orsucursal=suc003and", "");
                            } else {
                                Consulta_sucursal = Consulta_sucursal.replace("orsucursal=suc003", "");
                            }
                            Consulta_sucursal = Consulta_sucursal.replace("sucursal=suc002", "");
                        } else {
                            if (Consulta_sucursal.contains("sucursal=suc002and")) {
                                Consulta_sucursal = Consulta_sucursal.replace("sucursal=suc002and", "");
                            } else {
                                Consulta_sucursal = Consulta_sucursal.replace("sucursal=suc002", "");
                            }

                        }
                    } else if (flag_s03) {
                        if (Consulta_sucursal.contains("sucursal=suc003and")) {
                            Consulta_sucursal = Consulta_sucursal.replace("sucursal=suc003and", "");
                        } else {
                            Consulta_sucursal = Consulta_sucursal.replace("sucursal=suc003", "");
                        }
                    }
                }

                System.out.println("Despues de limpieza: " + Consulta_sucursal);


                //IDENTIFICAR QUE DATOS SON LOS QUE CONDICIONAN LA CONSULTA
                if (Consulta_sucursal.contains("or")) {
                    flag_or = true;
                    N_condiciones++;
                } else if (Consulta_sucursal.contains("and")) {
                    flag_and = true;
                    N_condiciones++;
                }

                String[] condiciones = new String[N_condiciones];
                String[] datos_condiciones = new String[N_condiciones];
                String[] Operacion = new String[N_condiciones];

                //Si pide mas datos
                if (Consulta_sucursal.length() > 0) {

                    String operacion = "=";

                    String Consulta_condiciones = Consulta_sucursal; ///SOLAMENTE PARA OBTENER CONDICIONES DE FORMA INDIVIDUAL
                    int indx_fin = 0, indx_inicio = 0, indx_auxiliar = 0;
                    //OBTENER DATOS PRIMERA CONDICION
                    //escritor.println("Echo... "+Consulta_sucursal);
                    if (Consulta_sucursal.startsWith("saldo")) {
                        //System.out.println("encontro saldo");

                        indx_fin = Consulta_sucursal.indexOf("saldo");

                        if (flag_and) {
                            indx_auxiliar = Consulta_sucursal.indexOf("and");
                            if (indx_auxiliar < indx_fin) {
                                indx_fin = Consulta_sucursal.indexOf('='); //DETERMINA QUE SALDO ES LA SEGUNDA CONDICION
                            } else if (Consulta_sucursal.contains("<")) {
                                indx_fin = Consulta_sucursal.indexOf("<");
                                operacion = "<";
                            } else if (Consulta_sucursal.contains(">")) {
                                indx_fin = Consulta_sucursal.indexOf(">");
                                operacion = ">";
                            } else {
                                indx_fin = Consulta_sucursal.indexOf('=');
                            }

                        } else if (flag_or) {
                            indx_auxiliar = Consulta_sucursal.indexOf("or");
                            if (indx_auxiliar > indx_fin) {
                                if (Consulta_sucursal.startsWith("saldo>")) {
                                    indx_fin = Consulta_sucursal.indexOf('>');//DETERMINA QUE SALDO ES LA SEGUNDA CONDICION
                                    operacion = ">";
                                } else if (Consulta_sucursal.startsWith("saldo<")) {
                                    indx_fin = Consulta_sucursal.indexOf('<');//DETERMINA QUE SALDO ES LA SEGUNDA CONDICION
                                    operacion = "<";
                                } else if (Consulta_sucursal.startsWith("saldo=")) {
                                    indx_fin = Consulta_sucursal.indexOf('=');//DETERMINA QUE SALDO ES LA SEGUNDA CONDICION
                                    operacion = "=";
                                }

                            }
                        } else {
                            //System.out.println("No encontro saldo");

                            if (Consulta_sucursal.contains(">")) {
                                indx_fin = Consulta_sucursal.indexOf('>');//DETERMINA QUE SALDO ES LA SEGUNDA CONDICION
                                operacion = ">";
                            } else if (Consulta_sucursal.contains("<")) {
                                indx_fin = Consulta_sucursal.indexOf('<');//DETERMINA QUE SALDO ES LA SEGUNDA CONDICION
                                operacion = "<";
                            } else {
                                indx_fin = Consulta_sucursal.indexOf('=');//DETERMINA QUE SALDO ES LA SEGUNDA CONDICION
                            }


                        }
                    } else {
                        indx_fin = Consulta_sucursal.indexOf('=');
                    }


                    //System.out.println(Consulta_sucursal);
                    //System.out.println(indx_fin);
                    condiciones[0] = Consulta_sucursal.substring(0, indx_fin);
                    //System.out.println(condiciones[1]);

                    indx_inicio = indx_fin + 1;//OBTENER DONDE INICIA DATO DE LA CONDICIONAL

                    if (flag_or) {
                        indx_fin = Consulta_sucursal.indexOf("or");
                        datos_condiciones[0] = Consulta_sucursal.substring(indx_inicio, indx_fin);
                    } else if (flag_and) {
                        indx_fin = Consulta_sucursal.indexOf("and");
                        datos_condiciones[0] = Consulta_sucursal.substring(indx_inicio, indx_fin);
                    } else {
                        datos_condiciones[0] = Consulta_sucursal.substring(indx_inicio);
                    }

                    //System.out.println(indx_inicio);
                    //System.out.println(indx_fin);
                    //System.out.println(datos_condiciones[1]);
                    Operacion[0] = operacion;


                    //OBTENER DATOS SEGUNDA CONDICION

                    if (N_condiciones == 2) {
                        //ELIMINAR PRIMERA CONDICION
                        if (flag_or) {
                            Consulta_condiciones = Consulta_condiciones.replace(condiciones[0] + Operacion[0] + datos_condiciones[0] + "or", "");
                        } else if (flag_and) {
                            Consulta_condiciones = Consulta_condiciones.replace(condiciones[0] + Operacion[0] + datos_condiciones[0] + "and", "");
                        }
                        System.out.println(Consulta_condiciones);

                        indx_fin = 0;
                        indx_inicio = 0;
                        operacion = "=";
                        //OBTENER DATOS SEGUNDA CONDICION
                        //escritor.println("Echo... "+Consulta_condiciones);
                        if (Consulta_condiciones.contains("saldo")) {
                            //System.out.println("encontro saldo");

                            indx_fin = Consulta_condiciones.indexOf("saldo");

                            if (flag_and) {

                                if (Consulta_condiciones.contains("<")) {
                                    indx_fin = Consulta_condiciones.indexOf("<");
                                    operacion = "<";
                                } else if (Consulta_condiciones.contains(">")) {
                                    indx_fin = Consulta_condiciones.indexOf(">");
                                    operacion = ">";
                                } else {
                                    indx_fin = Consulta_condiciones.indexOf('=');
                                }

                            } else if (flag_or) {

                                if (Consulta_condiciones.contains("<")) {
                                    indx_fin = Consulta_condiciones.indexOf("<");
                                    operacion = "<";
                                } else if (Consulta_condiciones.contains(">")) {
                                    indx_fin = Consulta_condiciones.indexOf(">");
                                    operacion = ">";
                                } else {
                                    indx_fin = Consulta_condiciones.indexOf('=');
                                }
                            }
                        } else {
                            //System.out.println("No encontro saldo");
                            indx_fin = Consulta_condiciones.indexOf('=');
                        }

                        //System.out.println(Consulta_condiciones);
                        //System.out.println(indx_fin);
                        condiciones[1] = Consulta_condiciones.substring(0, indx_fin);
                        //System.out.println(condiciones[2]);

                        indx_inicio = indx_fin + 1;//OBTENER DONDE INICIA DATO DE LA CONDICIONAL
                        datos_condiciones[1] = Consulta_condiciones.substring(indx_inicio);

                        //System.out.println(indx_inicio);
                        //System.out.println(indx_fin);
                        //System.out.println(datos_condiciones[1]);
                        Operacion[1] = operacion;


                    }
                    for (int i = 0; i < N_condiciones; i++) {
                        System.out.println("Condicion" + i + ": " + condiciones[i] + " " + Operacion[i] + " " + datos_condiciones[i]);
                    }
                }


                if (sucursal == "suc001") {
                    flag_s01 = false;

                }
                if (sucursal == "suc002") {
                    flag_s02 = false;
                }
                if (sucursal == "suc003") {
                    flag_s03 = false;
                }

                String Consulta_a_enviar_2;

                String[] sucursal = new String[100];
                String[] nombres = new String[100];
                String[] no_cuenta = new String[100];


                //CONSULTA EN JSON SERVIDOR ORIGEN
                try (FileReader reader = new FileReader(json)) {

                    JsonObject jobj = new Gson().fromJson(reader, JsonObject.class); //Pasa a objeto Json el archivo
                    //System.out.println(jobj);
                    JsonObject getObject = jobj.getAsJsonObject("dictdist"); //Pasa tomar los datos del objeto dictdist
                    //System.out.println(getObject);
                    JsonArray getdatos = getObject.getAsJsonArray("services"); //Pasa tomar los datos del array services
                    //JsonArray getSucursal = getObject.getAsJsonArray("puertos");//Pasa tomar los datos del array puertos
                    //boolean a=s.checkServices(lineIn);
                    //System.out.println(getArray);

                    Servicios s = new Servicios("0", "0", "0", "0", "0", 0);

                    for (int i = 0; i < getdatos.size(); i++)//Obtenemos cantidad de datos del array services
                    {
                        JsonObject objects = (JsonObject) getdatos.get(i); //Asignamos del array services un objeto a la variable objects
                        Iterator key = objects.keySet().iterator(); //Obtenemos las keys o datos guardados por asi decirlo

                        if (N_condiciones == 2) {
                            if (flag_or) {
                                if (condiciones[0].equals("saldo") && condiciones[1].equals("saldo")) {
                                    if (Operacion[0].equals("=") && Operacion[1].equals("=")) {

                                        if (objects.get("Saldo").toString().equals(datos_condiciones[0]) || objects.get("Saldo").toString().equals(datos_condiciones[1])) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }

                                    } else if (Operacion[0].equals("=") && Operacion[1].equals(">")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[1]);
                                        if (objects.get("Saldo").toString().equals(datos_condiciones[0]) || Integer.parseInt(objects.get("Saldo").toString()) > valor_saldo) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }

                                    } else if (Operacion[0].equals("=") && Operacion[1].equals("<")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[1]);
                                        if (objects.get("Saldo").toString().equals(datos_condiciones[0]) || Integer.parseInt(objects.get("Saldo").toString()) < valor_saldo) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }

                                    } else if (Operacion[0].equals("<") && Operacion[1].equals("=")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[0]);
                                        if (Integer.parseInt(objects.get("Saldo").toString()) < valor_saldo || objects.get("Saldo").toString().equals(datos_condiciones[1])) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }

                                    } else if (Operacion[0].equals("<") && Operacion[1].equals(">")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[0]);
                                        int valor_saldo2 = Integer.parseInt(datos_condiciones[1]);
                                        if (Integer.parseInt(objects.get("Saldo").toString()) < valor_saldo || Integer.parseInt(objects.get("Saldo").toString()) > valor_saldo2) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }

                                    } else if (Operacion[0].equals(">") && Operacion[1].equals("=")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[0]);

                                        if (Integer.parseInt(objects.get("Saldo").toString()) > valor_saldo || objects.get("Saldo").toString().equals(datos_condiciones[1])) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }

                                    } else if (Operacion[0].equals(">") && Operacion[1].equals("<")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[0]);
                                        int valor_saldo2 = Integer.parseInt(datos_condiciones[1]);
                                        if (Integer.parseInt(objects.get("Saldo").toString()) > valor_saldo || Integer.parseInt(objects.get("Saldo").toString()) < valor_saldo2) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }

                                    }

                                } else if (condiciones[0].equals("saldo") && condiciones[1].equals("nocuenta")) {

                                    if (objects.get("Saldo").toString().equals(datos_condiciones[0]) || ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("saldo") && condiciones[1].equals("curp")) {

                                    if (objects.get("Saldo").toString().equals(datos_condiciones[0]) || ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("CURP").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("saldo") && condiciones[1].equals("nombres")) {

                                    if (objects.get("Saldo").toString().equals(datos_condiciones[0]) || ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("Nombres").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("curp") && condiciones[1].equals("saldo")) {

                                    if (Operacion[1].equals("=")) {
                                        if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("CURP").toString()) || objects.get("Saldo").toString().equals(datos_condiciones[1])) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }
                                    } else if (Operacion[1].equals("<")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[1]);
                                        if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("CURP").toString()) || Integer.parseInt(objects.get("Saldo").toString()) < valor_saldo) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }
                                    } else if (Operacion[1].equals(">")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[1]);
                                        if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("CURP").toString()) || Integer.parseInt(objects.get("Saldo").toString()) > valor_saldo) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }
                                    }


                                } else if (condiciones[0].equals("nocuenta") && condiciones[1].equals("saldo")) {

                                    if (Operacion[1].equals("=")) {
                                        if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString()) || objects.get("Saldo").toString().equals(datos_condiciones[1])) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }
                                    } else if (Operacion[1].equals("<")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[1]);
                                        if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString()) || Integer.parseInt(objects.get("Saldo").toString()) < valor_saldo) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }
                                    } else if (Operacion[1].equals(">")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[1]);
                                        if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString()) || Integer.parseInt(objects.get("Saldo").toString()) > valor_saldo) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }
                                    }
                                } else if (condiciones[0].equals("nocuenta") && condiciones[1].equals("curp")) {

                                    if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString()) || ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("CURP").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("nocuenta") && condiciones[1].equals("nombres")) {

                                    if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString()) || ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("Nombres").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("nombres") && condiciones[1].equals("nocuenta")) {

                                    if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("Nombres").toString()) || ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("nombres") && condiciones[1].equals("curp")) {

                                    if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("Nombres").toString()) || ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("CURP").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("curp") && condiciones[1].equals("nocuenta")) {

                                    if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("CURP").toString()) || ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("curp") && condiciones[1].equals("nombres")) {

                                    if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("CURP").toString()) || ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("Nombres").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));

                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;

                                    }


                                }


                            } else if (flag_and) {

                                if (condiciones[0].equals("saldo") && condiciones[1].equals("nombres")) {
                                    if (objects.get("Saldo").toString().equals(datos_condiciones[0]) && ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("Nombres").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }
                                } else if (condiciones[0].equals("saldo") && condiciones[1].equals("curp")) {

                                    if (objects.get("Saldo").toString().equals(datos_condiciones[0]) && ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("CURP").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }
                                } else if (condiciones[0].equals("saldo") && condiciones[1].equals("nocuenta")) {
                                    if (objects.get("Saldo").toString().equals(datos_condiciones[0]) && ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }
                                } else if (condiciones[0].equals("curp") && condiciones[1].equals("saldo")) {

                                    if (Operacion[1].equals("=")) {
                                        if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("CURP").toString()) && objects.get("Saldo").toString().equals(datos_condiciones[1])) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }
                                    } else if (Operacion[1].equals("<")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[1]);
                                        if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("CURP").toString()) && Integer.parseInt(objects.get("Saldo").toString()) < valor_saldo) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }
                                    } else if (Operacion[1].equals(">")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[1]);
                                        if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("CURP").toString()) && Integer.parseInt(objects.get("Saldo").toString()) > valor_saldo) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }
                                    }


                                } else if (condiciones[0].equals("nocuenta") && condiciones[1].equals("saldo")) {

                                    if (Operacion[1].equals("=")) {
                                        if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString()) && objects.get("Saldo").toString().equals(datos_condiciones[1])) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }
                                    } else if (Operacion[1].equals("<")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[1]);
                                        if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString()) && Integer.parseInt(objects.get("Saldo").toString()) < valor_saldo) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }
                                    } else if (Operacion[1].equals(">")) {
                                        int valor_saldo = Integer.parseInt(datos_condiciones[1]);
                                        if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString()) && Integer.parseInt(objects.get("Saldo").toString()) > valor_saldo) {
                                            s.addService(objects.get("Sucursal").toString(),
                                                    objects.get("NoCuenta").toString(),
                                                    objects.get("CURP").toString(),
                                                    objects.get("Nombres").toString(),
                                                    objects.get("Apellidos").toString(),
                                                    Integer.parseInt(objects.get("Saldo").toString()));
                                            sucursal[a] = objects.get("Sucursal").toString();
                                            nombres[a] = objects.get("Nombres").toString();
                                            no_cuenta[a] = objects.get("NoCuenta").toString();
                                            a++;
                                        }
                                    }
                                } else if (condiciones[0].equals("nocuenta") && condiciones[1].equals("curp")) {

                                    if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString()) && ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("CURP").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("nocuenta") && condiciones[1].equals("nombres")) {

                                    if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString()) && ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("Nombres").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("nombres") && condiciones[1].equals("nocuenta")) {

                                    if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("Nombres").toString()) && ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));

                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("nombres") && condiciones[1].equals("curp")) {

                                    if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("Nombres").toString()) && ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("CURP").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("curp") && condiciones[1].equals("nocuenta")) {

                                    if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("CURP").toString()) && ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                } else if (condiciones[0].equals("curp") && condiciones[1].equals("nombres")) {

                                    if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("CURP").toString()) && ("\"" + datos_condiciones[1] + "\"").equalsIgnoreCase(objects.get("Nombres").toString())) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;
                                    }


                                }


                            }
                        } else if (condiciones[0]==null) {


                            s.addService(objects.get("Sucursal").toString(),
                                    objects.get("NoCuenta").toString(),
                                    objects.get("CURP").toString(),
                                    objects.get("Nombres").toString(),
                                    objects.get("Apellidos").toString(),
                                    Integer.parseInt(objects.get("Saldo").toString()));

                            sucursal[a] = objects.get("Sucursal").toString();
                            nombres[a] = objects.get("Nombres").toString();
                            no_cuenta[a] = objects.get("NoCuenta").toString();
                            a++;


                        }else if (condiciones[0]!=null) {
                            if (condiciones[0].equals("saldo")) {
                                if (Operacion[0].equals("=")) {
                                    if (objects.get("Saldo").toString().equals(datos_condiciones[0])) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;


                                    }
                                } else if (Operacion[0].equals(">")) {
                                    int valor_saldo = Integer.parseInt(datos_condiciones[0]);
                                    if (Integer.parseInt(objects.get("Saldo").toString()) > valor_saldo) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;

                                    }


                                } else if (Operacion[0].equals("<")) {
                                    int valor_saldo = Integer.parseInt(datos_condiciones[0]);
                                    if (Integer.parseInt(objects.get("Saldo").toString()) < valor_saldo) {
                                        s.addService(objects.get("Sucursal").toString(),
                                                objects.get("NoCuenta").toString(),
                                                objects.get("CURP").toString(),
                                                objects.get("Nombres").toString(),
                                                objects.get("Apellidos").toString(),
                                                Integer.parseInt(objects.get("Saldo").toString()));
                                        sucursal[a] = objects.get("Sucursal").toString();
                                        nombres[a] = objects.get("Nombres").toString();
                                        no_cuenta[a] = objects.get("NoCuenta").toString();
                                        a++;

                                    }

                                }

                            } else if (condiciones[0].equals("curp")) {

                                if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("CURP").toString())) {
                                    s.addService(objects.get("Sucursal").toString(),
                                            objects.get("NoCuenta").toString(),
                                            objects.get("CURP").toString(),
                                            objects.get("Nombres").toString(),
                                            objects.get("Apellidos").toString(),
                                            Integer.parseInt(objects.get("Saldo").toString()));
                                    sucursal[a] = objects.get("Sucursal").toString();
                                    nombres[a] = objects.get("Nombres").toString();
                                    no_cuenta[a] = objects.get("NoCuenta").toString();
                                    a++;
                                }
                            } else if (condiciones[0].equals("nocuenta")) {

                                if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("NoCuenta").toString())) {
                                    s.addService(objects.get("Sucursal").toString(),
                                            objects.get("NoCuenta").toString(),
                                            objects.get("CURP").toString(),
                                            objects.get("Nombres").toString(),
                                            objects.get("Apellidos").toString(),
                                            Integer.parseInt(objects.get("Saldo").toString()));
                                    sucursal[a] = objects.get("Sucursal").toString();
                                    nombres[a] = objects.get("Nombres").toString();
                                    no_cuenta[a] = objects.get("NoCuenta").toString();
                                    a++;
                                }
                            } else if (condiciones[0].equals("nombres")) {

                                if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("Nombres").toString())) {
                                    s.addService(objects.get("Sucursal").toString(),
                                            objects.get("NoCuenta").toString(),
                                            objects.get("CURP").toString(),
                                            objects.get("Nombres").toString(),
                                            objects.get("Apellidos").toString(),
                                            Integer.parseInt(objects.get("Saldo").toString()));
                                    sucursal[a] = objects.get("Sucursal").toString();
                                    nombres[a] = objects.get("Nombres").toString();
                                    no_cuenta[a] = objects.get("NoCuenta").toString();
                                    a++;
                                }
                            } else if (condiciones[0].equals("apellidos")) {

                                if (("\"" + datos_condiciones[0] + "\"").equalsIgnoreCase(objects.get("Apellidos").toString())) {
                                    s.addService(objects.get("Sucursal").toString(),
                                            objects.get("NoCuenta").toString(),
                                            objects.get("CURP").toString(),
                                            objects.get("Nombres").toString(),
                                            objects.get("Apellidos").toString(),
                                            Integer.parseInt(objects.get("Saldo").toString()));
                                    sucursal[a] = objects.get("Sucursal").toString();
                                    nombres[a] = objects.get("Nombres").toString();
                                    no_cuenta[a] = objects.get("NoCuenta").toString();
                                    a++;
                                }
                            }


                        }

                    }

                //}

                //System.out.println(); //Salto de linea pa que se vea mejor :v



                    //MOSTRAR TODOS LOS DATOS DE LA SUCURSAL
                    for(int b=0;b<a;b++){

                        escritor.printf("Sucursal: "+sucursal[b]+" "+"Nombres: "+nombres[b]+" "+"No_cuenta: "+no_cuenta[b]+"\r");


                    }
                    escritor.println("\nConectividad aceptada");
                    s.showServices();



                }






                if(flag_s01){

                    Consulta_a_enviar_2="";
                    if(Consulta_sucursal.length()>0){
                        Consulta_a_enviar_2=Consulta_a_enviar+"sucursal=suc001and"+Consulta_sucursal;
                    }else{
                        Consulta_a_enviar_2=Consulta_a_enviar+"sucursal=suc001";
                    }
                    System.out.println("consulta a enviar server 001:"+Consulta_a_enviar_2);
                    Integer puerto2= 13777;
                    //System.out.println(puerto2);


                    try {
                        clienteX = new Socket(maquinaX, puerto2);
                        System.out.println("Conectado a server"+puerto2);
                    } catch (Exception e) {
                        System.out.println("Fallo : " + e);
                        System.exit(0);
                    }

                    try {
                        escritorX = new PrintWriter(clienteX.getOutputStream(), true);
                        entradaX = new BufferedReader(new InputStreamReader(clienteX.getInputStream()));
                    } catch (Exception e) {
                        System.out.println("Fallo : " + e);
                        clienteX.close();
                        System.exit(0);
                    }

                    String [] lineX =new String[100];
                    String DatosEnviadosX;
                    System.out.println("Sending connecting to another server");
                    DatosEnviadosX = Consulta_a_enviar_2.toString();
                    escritorX.println(DatosEnviadosX);
                    for(int b=0;b<a;b++){

                        lineX [b]= entradaX.readLine();

                    }

                    System.out.println("Server001: " + lineX);
                    DatosEnviadosX = "FIN";
                    escritorX.println(DatosEnviadosX);
                    System.out.println("Closing another server");
                    clienteX.close();
                    escritorX.close();
                    entradaX.close();
                    escritor.println("Response from Server001...");
                    for(int b=0;b<a;b++){

                        escritor.println(lineX[b]);

                    }

                    escritor.flush();


                }
                if(flag_s02) {

                    Consulta_a_enviar_2="";
                    if(Consulta_sucursal.length()>0){
                        Consulta_a_enviar_2=Consulta_a_enviar+"sucursal=suc002and"+Consulta_sucursal;
                    }else{
                        Consulta_a_enviar_2=Consulta_a_enviar+"sucursal=suc002";
                    }
                    System.out.println("consulta a enviar server 002:"+Consulta_a_enviar_2);
                    Integer puerto2= 200;
                    //System.out.println(puerto2);
                    try {
                        clienteX = new Socket(maquinaX, puerto2);
                        System.out.println("Conectado a server"+puerto2);
                    } catch (Exception e) {
                        System.out.println("Fallo : " + e);
                        System.exit(0);

                    }

                    try {
                        escritorX = new PrintWriter(clienteX.getOutputStream(), true);
                        entradaX = new BufferedReader(new InputStreamReader(clienteX.getInputStream()));
                    } catch (Exception e) {
                        System.out.println("Fallo : " + e);
                        clienteX.close();
                        System.exit(0);
                    }

                    String [] lineX =new String[100];
                    String DatosEnviadosX;
                    System.out.println("Sending connecting to another server");
                    DatosEnviadosX = Consulta_a_enviar_2.toString();
                    escritorX.println(DatosEnviadosX);
                    for(int b=0;b<a;b++){

                        lineX [b]= entradaX.readLine();

                    }

                    System.out.println("Server002: " + lineX);
                    DatosEnviadosX = "FIN";
                    escritorX.println(DatosEnviadosX);
                    System.out.println("Cerrando");
                    clienteX.close();
                    escritorX.close();
                    entradaX.close();
                    escritor.println("Respuesta del Servidor 2...");
                    for(int b=0;b<a;b++){

                        escritor.println(lineX[b]);

                    }

                    escritor.flush();

                }
                if(flag_s03){

                    Consulta_a_enviar_2="";
                    if(Consulta_sucursal.length()>0){
                        Consulta_a_enviar_2=Consulta_a_enviar+"sucursal=suc003and"+Consulta_sucursal;
                    }else{
                        Consulta_a_enviar_2=Consulta_a_enviar+"sucursal=suc003";
                    }
                        System.out.println("consulta a enviar server 003:"+Consulta_a_enviar_2);
                    Integer puerto2= 300;
                   //System.out.println(puerto2);
                    try {
                        clienteX = new Socket(maquinaX, puerto2);
                        System.out.println("Conectado a server"+puerto2);
                    } catch (Exception e) {
                        System.out.println("Fallo : " + e);
                        System.exit(0);
                    }

                    try {
                        escritorX = new PrintWriter(clienteX.getOutputStream(), true);
                        entradaX = new BufferedReader(new InputStreamReader(clienteX.getInputStream()));
                    } catch (Exception e) {
                        System.out.println("Fallo : " + e);
                        clienteX.close();
                        System.exit(0);
                    }


                    String [] lineX =new String[100];
                    String DatosEnviadosX;
                    System.out.println("Solicitando otro cliente");
                    DatosEnviadosX = Consulta_a_enviar_2.toString();
                    escritorX.println(DatosEnviadosX);
                    for(int b=0;b<a;b++){

                        lineX [b]= entradaX.readLine();

                    }
                    System.out.println("Server003: " + lineX);
                    DatosEnviadosX = "FIN";
                    escritorX.println(DatosEnviadosX);
                    System.out.println("Cerrando");
                    clienteX.close();
                    escritorX.close();
                    entradaX.close();
                    escritor.println("Respuesta Servidor 3...");
                    for(int b=0;b<a;b++){

                        escritor.println(lineX[b]);

                    }

                    escritor.flush();
                }
                System.out.println("\n---------------------------------------------------------------------------------------------------------------------------\n\n");
            }
            else{

                escritor.println("Echo... "+lineIn);
                escritor.flush();

            }
        }
        try{
            entrada.close();
            escritor.close();
            socket.close();
         }catch(Exception e){
            System.out.println ("Error : " + e);
            socket.close();
            System.exit (0);
   	   }
      }catch (IOException e) {
        System.out.println("Error---");
         e.printStackTrace();
      }

      
//JSON


   }
}
