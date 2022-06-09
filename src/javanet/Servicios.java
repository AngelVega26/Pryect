package javanet;



import java.util.*;

public class Servicios {
    String nameOfService;

    int k = 1;
    Servicios s;
    String Sucursal,NoCuenta,CURP,Nombre,Apellidos;
    Integer Saldo;
    Integer Puerto;
    Hashtable <Integer, Servicios> hS = new Hashtable<Integer, Servicios>();

    public Servicios(String Sucursal,String NoCuenta, String CURP, String Nombre, String Apellidos, Integer Saldo){
        this.Sucursal=Sucursal;
        this.NoCuenta=NoCuenta;
        this.CURP=CURP;
        this.Nombre=Nombre;
        this.Apellidos=Apellidos;
        this.Saldo=Saldo;
    }
    public Servicios(Integer Puerto,String Sucursal){
        this.Puerto=Puerto;
        this.Sucursal=Sucursal;
    }

    public void showServices(){
        System.out.println("Servicios disponibles");
        for (int i = 1; i < k; i++) {

            System.out.println("Sucursal: "+ hS.get(i).getSucursal());
            System.out.println("Numero de cuenta: "+ hS.get(i).getNoCuenta());
            System.out.println("CURP: "+ hS.get(i).getCURP());
            System.out.println("Nombre: "+ hS.get(i).getNombres());
            System.out.println("Apellido: "+ hS.get(i).getApellidos());
            System.out.println("Saldo: "+ hS.get(i).getSaldo());
            System.out.println("");
        }
    }
    public boolean checkServices(String Sucursal){
        boolean flag=false;
        for (int i = 1; i < k; i++) {
            if(hS.get(i).getSucursal().equals(Sucursal)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public void addService(String Sucursal,String NoCuenta, String CURP, String Nombre, String Apellidos, Integer Saldo){
        s = new Servicios(Sucursal,NoCuenta,CURP,Nombre,Apellidos,Saldo);
        hS.put(k, s); k++;
    }


    public String getSucursal() {
        return Sucursal;
    }

    public void setSucursal(String Sucursal) {
        this.Sucursal = Sucursal;
    }

    public String getNoCuenta() {
        return NoCuenta;
    }

    public void setNoCuenta(String NoCuenta) {
        this.NoCuenta = NoCuenta;
    }

    public String getCURP() {
        return CURP;
    }

    public void setCURP(String CURP) {
        this.CURP = CURP;
    }

    public String getNombres() {
        return Nombre;
    }

    public void setNombres(String Nombre) {
        this.Nombre = Nombre;
    }

    public String getApellidos() {
        return Apellidos;
    }

    public void setApellidos(String Apellidos) {
        this.Apellidos = Apellidos;
    }

    public Integer getSaldo() {
        return Saldo;
    }

    public void setSaldo( Integer Saldo) {
        this.Saldo = Saldo;
    }
}
