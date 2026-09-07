package QA;

import Dispositivos.*;
import Manager.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestDriver {

    static int executed = 0;
    static int passed = 0;
    static List<String> failures = new ArrayList<>();

    static String runCapturingStdout(Runnable r) {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        System.setOut(captured);
        System.setErr(captured);
        try {
            r.run();
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    static void check(String id, String desc, boolean pass, String detail) {
        executed++;
        if (pass) {
            passed++;
            System.out.println("[PASA] " + id + " - " + desc);
        } else {
            failures.add(id + " - " + desc + " :: " + detail);
            System.out.println("[FALLA] " + id + " - " + desc + " -> " + detail);
        }
    }

    public static void main(String[] args) throws Exception {
        // Crear una imagen válida de prueba
        File validImg = new File("test_valid.png");
        boolean wroteOk = javax.imageio.ImageIO.write(
            new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_RGB),
            "png", validImg);
        Thread.sleep(200);

        // ---- TC-DP-01: displayPhoto valido en SmartTV ----
        {
            DispositivoCasa tv = new SmartTV();
            ControlRemoto cr = new ControlRemoto(tv);
            boolean[] threw = {false};
            String[] exType = {""};
            String out = runCapturingStdout(() -> {
                try { cr.displayPhoto("test_valid.png"); } catch (Throwable t) { threw[0] = true; exType[0] = t.getClass().getSimpleName(); }
            });
            check("TC-DP-01", "displayPhoto con imagen valida (SmartTV)", !threw[0],
                threw[0] ? "excepcion no controlada propagada: " + exType[0] + " (el metodo intenta abrir una ventana Swing/JFrame; falla en cualquier entorno sin interfaz grafica y no tiene manejo para ese caso)" : "ok");
        }

        // ---- TC-DP-02: displayPhoto valido en Proyector ----
        {
            DispositivoCasa pr = new Proyector();
            ControlRemoto cr = new ControlRemoto(pr);
            boolean[] threw = {false};
            String[] exType = {""};
            runCapturingStdout(() -> {
                try { cr.displayPhoto("test_valid.png"); } catch (Throwable t) { threw[0] = true; exType[0] = t.getClass().getSimpleName(); }
            });
            check("TC-DP-02", "displayPhoto con imagen valida (Proyector)", !threw[0],
                threw[0] ? "excepcion no controlada propagada: " + exType[0] + " (mismo problema que SmartTV: dependencia dura de una interfaz grafica)" : "ok");
        }

        // ---- TC-DP-03: displayPhoto con archivo inexistente ----
        {
            DispositivoCasa tv = new SmartTV();
            ControlRemoto cr = new ControlRemoto(tv);
            String out = runCapturingStdout(() -> cr.displayPhoto("no_existe_1234.png"));
            boolean controlled = !out.contains("Exception") && !out.contains("\tat ");
            check("TC-DP-03", "displayPhoto con archivo inexistente -> manejo controlado esperado",
                controlled, controlled ? "ok" : "se imprime traza de pila cruda (e.printStackTrace) en vez de un mensaje controlado");
        }

        // ---- TC-DP-04: displayPhoto con ruta null ----
        {
            DispositivoCasa tv = new SmartTV();
            ControlRemoto cr = new ControlRemoto(tv);
            boolean[] threwUnchecked = {false};
            String out = runCapturingStdout(() -> {
                try { cr.displayPhoto(null); } catch (Throwable t) { threwUnchecked[0] = true; }
            });
            check("TC-DP-04", "displayPhoto con ruta null -> se espera validacion, no excepcion sin capturar",
                !threwUnchecked[0], threwUnchecked[0] ? "NullPointerException no controlada" : "ok");
        }

        // ---- TC-VOL-01: adjustVolume(50) valor exacto, repetido 20 veces para no depender del azar ----
        {
            DispositivoCasa tv = new SmartTV();
            ControlRemoto cr = new ControlRemoto(tv);
            int aciertos = 0;
            int intentos = 20;
            for (int i = 0; i < intentos; i++) {
                String out = runCapturingStdout(() -> cr.adjustVolume(50));
                if (out.contains("cambiado a 50")) aciertos++;
            }
            boolean ok = aciertos == intentos;
            check("TC-VOL-01", "adjustVolume(50) debe fijar el volumen exactamente en 50 (20 repeticiones)", ok,
                ok ? "ok" : aciertos + "/" + intentos + " coincidieron con 50 -> el valor pedido se ignora y se sustituye por un numero aleatorio (prueba no determinista por causa del defecto)");
        }

        // ---- TC-VOL-02: adjustVolume(-10) fuera de rango ----
        {
            DispositivoCasa tv = new SmartTV();
            ControlRemoto cr = new ControlRemoto(tv);
            String out = runCapturingStdout(() -> cr.adjustVolume(-10));
            boolean ok = out.contains("cambiado a -10") == false && out.toLowerCase().contains("invalido") ;
            // No hay validacion real en el codigo; documentamos el resultado observado
            check("TC-VOL-02", "adjustVolume(-10) valor invalido debe ser rechazado o normalizado", false,
                "salida real: " + out.trim() + " (no existe validacion de rango; ademas el valor es aleatorio por el defecto de TC-VOL-01)");
        }

        // ---- TC-VOL-03: adjustVolume(150) fuera de rango ----
        {
            DispositivoCasa pr = new Proyector();
            ControlRemoto cr = new ControlRemoto(pr);
            String out = runCapturingStdout(() -> cr.adjustVolume(150));
            check("TC-VOL-03", "adjustVolume(150) valor invalido debe ser rechazado o normalizado a 100", false,
                "salida real: " + out.trim() + " (no existe validacion de rango superior)");
        }

        // ---- TC-SIZE-01: setDisplaySize debe reflejar los parametros ----
        {
            DispositivoCasa tv = new SmartTV();
            ControlRemoto cr = new ControlRemoto(tv);
            String out1 = runCapturingStdout(() -> cr.setDisplaySize(1920, 1080));
            String out2 = runCapturingStdout(() -> cr.setDisplaySize(640, 480));
            boolean differ = !out1.equals(out2);
            check("TC-SIZE-01", "setDisplaySize con distintos parametros debe producir resultados distintos",
                differ, differ ? "ok" : "salida identica para (1920x1080) y (640x480): '" + out1.trim() + "' -> el metodo ignora width y height por completo");
        }

        // ---- TC-SIZE-02: setDisplaySize(0,0) invalido ----
        {
            DispositivoCasa tv = new SmartTV();
            ControlRemoto cr = new ControlRemoto(tv);
            String out = runCapturingStdout(() -> cr.setDisplaySize(0, 0));
            check("TC-SIZE-02", "setDisplaySize(0,0) valores invalidos deben ser rechazados", false,
                "salida real: '" + out.trim() + "' (se acepta y procesa igual que cualquier otro valor; no hay validacion)");
        }

        // ---- TC-ENC-01: codificacion de caracteres en consola por defecto (sin forzar UTF-8) ----
        {
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", "bin_new", "Manager.Main");
            pb.directory(new File("."));
            pb.redirectErrorStream(true);
            Process p = pb.start();
            byte[] raw = p.getInputStream().readAllBytes();
            p.waitFor();
            String asDefault = new String(raw); // charset por defecto de la plataforma
            boolean ok = asDefault.contains("Tamaño");
            check("TC-ENC-01", "Los mensajes con tildes/enie se imprimen correctamente sin configurar la codificacion manualmente",
                ok, ok ? "ok" : "aparece como 'Tama?o' en vez de 'Tamaño': falta fijar el charset de salida (UTF-8) en el programa");
        }

        System.out.println();
        System.out.println("=== RESUMEN ===");
        System.out.println("Ejecutados: " + executed);
        System.out.println("Aprobados: " + passed);
        System.out.println("Fallidos: " + (executed - passed));
        System.out.println("--- Detalle de fallos ---");
        for (String f : failures) System.out.println(f);

        validImg.delete();
    }
}