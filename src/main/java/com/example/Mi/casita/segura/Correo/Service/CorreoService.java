package com.example.Mi.casita.segura.Correo.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CorreoService {

    private final JavaMailSender mailSender;

    //Registrar usuario nuevo, mensaje con usuario y contraseña
    public void enviarBienvenida(String correoDestino, String nombre, String usuario, String contrasena, BufferedImage qrImage)
            throws MessagingException, IOException {

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setTo(correoDestino);
        helper.setSubject("¡Bienvenido a Mi Casita Segura! Aquí están tus credenciales y QR de acceso");

        String contenido = """
            <h3>Estimado/a %s,</h3>
            <p>¡Bienvenido/a a <strong>Mi Casita Segura</strong>! Tu cuenta ha sido creada exitosamente.</p>
            <p><strong>Usuario:</strong> %s<br/>
               <strong>Contraseña:</strong> %s</p>
            <p>Accede a la plataforma: <a href="https://app.mi-casita-segura.com/login">Ir a la aplicación</a></p>
            <p><strong>Tu Código QR para Ingreso al Residencial:</strong></p>
            <img src='cid:qr' />
            <p><strong style='color:red;'>IMPORTANTE:</strong> No compartas este mensaje ni tu QR con nadie.</p>
            <p>Gracias por ser parte de Mi Casita Segura.<br/>Administración</p>
            """.formatted(nombre, usuario, contrasena);

        helper.setText(contenido, true);

        // Adjuntar el QR
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);
        ByteArrayResource qrResource = new ByteArrayResource(baos.toByteArray());

        helper.addInline("qr", qrResource, "image/png");

        mailSender.send(mensaje);
    }

    /**
     * Envía el correo de confirmación al registrar un paquete.
     * No incluye archivos adjuntos, solo HTML en texto plano.
     */
    public void enviarRegistroPaquete(
            String correoDestino,
            String nombreResidente,
            Integer numeroCasa,
            String empresaDeEntrega,
            String numeroDeGuia,
            String tipoDePaquete,
            String observacion,
            java.time.LocalDateTime fechaRegistro,
            String codigoPaquete
    ) {
        MimeMessage mensaje = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, "UTF-8");
            helper.setTo(correoDestino);
            helper.setSubject("Paquete Registrado con Éxito – Código de Ingreso Generado");
            // helper.setFrom("no-reply@mi-casita-segura.com");

            String fechaFormateada = fechaRegistro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy – hh:mm a"));
            String contenido = new StringBuilder()
                    .append("<h3>Registro de Paquete Exitoso</h3>")
                    .append("<p>Estimado/a <strong>").append(nombreResidente).append("</strong>,</p>")
                    .append("<p>Tu paquete ha sido registrado con éxito. A continuación los detalles:</p>")
                    .append("<ul>")
                    .append("<li><strong>Casa:</strong> ").append(numeroCasa).append("</li>")
                    .append("<li><strong>Empresa de Entrega:</strong> ").append(empresaDeEntrega).append("</li>")
                    .append("<li><strong>Número de Guía / Tracking:</strong> ").append(numeroDeGuia).append("</li>")
                    .append("<li><strong>Tipo de Paquete:</strong> ").append(tipoDePaquete).append("</li>")
                    .append("<li><strong>Observaciones:</strong> ").append(observacion).append("</li>")
                    .append("<li><strong>Fecha y Hora del Registro:</strong> ").append(fechaFormateada).append("</li>")
                    .append("</ul>")
                    .append("<p><strong>Código de Ingreso del Paquete:</strong><br/>")
                    .append("<span style='font-size:1.2em;color:#2a6ebb;font-weight:bold;'>")
                    .append(codigoPaquete)
                    .append("</span></p>")
                    .append("<p>Este código deberá ser entregado al servicio de paquetería para que pueda entregar el paquete en la garita. ")
                    .append("Una vez recibido, serás notificado nuevamente para recogerlo presentando tu código de entrega.</p>")
                    .append("<p><strong>Importante:</strong><br/>")
                    .append("• No compartas este código con terceros no autorizados.<br/>")
                    .append("• El código tiene una validez de <strong>7 DÍAS</strong>.</p>")
                    .append("<p>¡Gracias por ser parte de Mi Casita Segura!<br/>")
                    .append("Atentamente,<br/>Administración – Mi Casita Segura</p>")
                    .append("<hr/>")
                    .append("<small>Nota: Este mensaje es personal y confidencial. No compartas tu información de acceso con terceros.</small>")
                    .toString();

            helper.setText(contenido, true);
            mailSender.send(mensaje);
        } catch (MessagingException e) {
            // Aquí podrías loguear un error si fallara el envío, pero no relanzar excepción
            // para que no rompa el flujo de registrar el paquete.
            e.printStackTrace();
        }
    }

    /* ========== NUEVO MÉTODO: notificación cuando el paquete llega a la garita ========== */
    public void enviarLlegadaAGarita(
            String correoDestino,
            String nombreResidente,
            Integer numeroCasa,
            String empresaDeEntrega,
            String numeroDeGuia,
            String tipoDePaquete,
            String observacion,
            LocalDateTime fechaRecepcion,
            String codigoEntrega
    ) {
        MimeMessage mensaje = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, "UTF-8");
            helper.setTo(correoDestino);
            helper.setSubject("Llegada del Paquete a la Garita – Listo para ser recogido");
            // helper.setFrom("no-reply@mi-casita-segura.com"); // opcional

            // Formateamos la hora de recepción como dd/MM/yyyy – hh:mm a
            String fechaFormateada = fechaRecepcion.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy – hh:mm a")
            );

            StringBuilder html = new StringBuilder();
            html.append("<h3>Llegada del Paquete a la Garita</h3>")
                    .append("<p>Tu paquete ha llegado a la garita – listo para ser recogido.</p>")
                    .append("<p><strong>Para:</strong> ").append(nombreResidente).append("<br/>")
                    .append("<strong>Casa:</strong> ").append(numeroCasa).append("</p>")
                    .append("<h4>Detalles del Paquete Recibido:</h4>")
                    .append("<ul>")
                    .append("<li><strong>Empresa de Entrega:</strong> ").append(empresaDeEntrega).append("</li>")
                    .append("<li><strong>Número de Guía / Tracking:</strong> ").append(numeroDeGuia).append("</li>")
                    .append("<li><strong>Tipo de Paquete:</strong> ").append(tipoDePaquete).append("</li>")
                    .append("<li><strong>Hora de Recepción en Garita:</strong> ").append(fechaFormateada).append("</li>")
                    .append("<li><strong>Observaciones:</strong> ").append(observacion).append("</li>")
                    .append("</ul>")
                    .append("<p><strong>Código de Entrega del Paquete:</strong><br/>")
                    .append("<span style='font-size:1.2em;color:#2a6ebb;font-weight:bold;'>")
                    .append(codigoEntrega)
                    .append("</span></p>")
                    .append("<p>Para recoger tu paquete, presenta este código al guardia de seguridad en la garita. ")
                    .append("Una vez validado, el paquete te será entregado.</p>")
                    .append("<p><strong>Importante:</strong><br/>")
                    .append("• Solo tú puedes recoger el paquete.<br/>")
                    .append("• No compartas este código con terceros no autorizados.</p>")
                    .append("<p>¡Gracias por ser parte de Mi Casita Segura!<br/>")
                    .append("Atentamente,<br/>Administración – Mi Casita Segura</p>")
                    .append("<hr/>")
                    .append("<small>Nota: Este mensaje es personal y confidencial. No compartas tu información de acceso con terceros.</small>");

            helper.setText(html.toString(), true);
            mailSender.send(mensaje);

        } catch (MessagingException ex) {
            // Loguea el error, pero no interrumpe el flujo principal
            ex.printStackTrace();
        }
    }

    /**
     * Envía un correo notificando que el paquete ya fue entregado al residente.
     */
    public void enviarEntregaAlResidente(
            String correoDestino,
            String nombreResidente,
            Integer numeroCasa,
            String empresaDeEntrega,
            String numeroDeGuia,
            String tipoDePaquete,
            LocalDateTime fechaEntrega,
            String nombreGuardia // o ID del guardia
    ) {
        MimeMessage mensaje = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, "UTF-8");
            helper.setTo(correoDestino);
            helper.setSubject("Paquete Entregado al Residente – Confirmación de Entrega");
            // helper.setFrom("no-reply@mi-casita-segura.com"); // opcional

            // Formateamos la hora de entrega como dd/MM/yyyy – hh:mm a
            String fechaFormateada = fechaEntrega.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy – hh:mm a")
            );

            StringBuilder html = new StringBuilder();
            html.append("<h3>Paquete Entregado al Residente</h3>")
                    .append("<p>Tu paquete ha sido entregado con éxito.</p>")
                    .append("<p><strong>Para:</strong> ").append(nombreResidente).append("<br/>")
                    .append("<strong>Casa / Apartamento:</strong> ").append(numeroCasa).append("</p>")
                    .append("<h4>Detalles del Paquete Entregado:</h4>")
                    .append("<ul>")
                    .append("<li><strong>Empresa de Entrega:</strong> ").append(empresaDeEntrega).append("</li>")
                    .append("<li><strong>Número de Guía / Tracking:</strong> ").append(numeroDeGuia).append("</li>")
                    .append("<li><strong>Tipo de Paquete:</strong> ").append(tipoDePaquete).append("</li>")
                    .append("<li><strong>Hora de Entrega:</strong> ").append(fechaFormateada).append("</li>")
                    .append("<li><strong>Guardia que Entregó:</strong> ").append(nombreGuardia).append("</li>")
                    .append("</ul>")
                    .append("<p><strong>Confirmación:</strong><br/>")
                    .append("El paquete fue entregado exitosamente tras validar el código de entrega proporcionado.</p>")
                    .append("<p>Este paquete ha sido marcado como <strong>“Entregado”</strong> y cerrado en el sistema.<br/>")
                    .append("Puedes ver el historial completo en tu perfil dentro de la aplicación.</p>")
                    .append("<p>¡Gracias por ser parte de Mi Casita Segura!<br/>")
                    .append("Atentamente,<br/>Administración – Mi Casita Segura</p>")
                    .append("<hr/>")
                    .append("<small>Nota: Este mensaje es personal y confidencial. No compartas tu información de acceso con terceros.</small>");

            helper.setText(html.toString(), true);
            mailSender.send(mensaje);

        } catch (MessagingException ex) {
            // Solo imprime en consola o registro de logs sin interrumpir el flujo
            ex.printStackTrace();
        }
    }


    /* ======================================== RESERVA NOTIFICACIÓN ======================================== */


    /**
     * Envía un correo al residente indicando que su reserva ha sido confirmada.
     */
    public void enviarConfirmacionReserva(
            String correoDestino,
            String nombreResidente,
            Integer numeroCasa,
            String areaComun,
            String fechaReserva,    // formato "dd/MM/yyyy"
            String horaInicio,      // formato "HH:mm"
            String horaFin          // formato "HH:mm"
    ) {
        MimeMessage mensaje = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, "UTF-8");
            helper.setTo(correoDestino);
            helper.setSubject("¡Reserva Confirmada! – Tu área común ha sido reservada con éxito");
            // helper.setFrom("no-reply@mi-casita-segura.com"); // opcional

            StringBuilder html = new StringBuilder();
            html.append("<h3>¡Reserva Confirmada!</h3>")
                    .append("<p>Tu área común ha sido reservada con éxito.</p>")
                    .append("<h4>Detalles de tu Reserva:</h4>")
                    .append("<ul>")
                    .append("<li><strong>Área Reservada:</strong> ").append(areaComun).append("</li>")
                    .append("<li><strong>Fecha de la Reserva:</strong> ").append(fechaReserva).append("</li>")
                    .append("<li><strong>Horario Reservado:</strong> ")
                    .append(horaInicio).append(" – ").append(horaFin)
                    .append("</li>")
                    .append("</ul>")
                    .append("<p>Tu reserva ha sido registrada correctamente y confirmada por el sistema.</p>")
                    .append("<p>Por favor, respeta el horario reservado y deja el área en buen estado al finalizar su uso.</p>")
                    .append("<hr/>")
                    .append("<p>¡Gracias por ser parte de Mi Casita Segura!<br/>")
                    .append("Atentamente,<br/>Administración – Mi Casita Segura</p>")
                    .append("<small>Nota: Este mensaje es personal y confidencial. No compartas tu información de acceso con terceros.</small>");

            helper.setText(html.toString(), true);
            mailSender.send(mensaje);

        } catch (MessagingException ex) {
            // Solo registramos el error en consola o logger
            ex.printStackTrace();
        }

    }

        /**
         * Envía un recordatorio de pago cuando el residente tiene al menos 2
         * pagos pendientes.
         *
         * Asunto: "Recordatorio de Pago – [Cuota o servicio]"
         *
         * Detalle del Pago Pendiente:
         * • Concepto: [Cuota o servicio a recordar]
         * • Periodo: [Mes y Año – Ej. Marzo 2025]
         * • Monto: Q [Cantidad exacta]
         * • Fecha límite de pago: [dd/MM/yyyy]
         * • Método de pago: [Tarjeta]
         *
         * @param correoDestino   El email del residente
         *      * @param nombreResidente El nombre completo del residente
         *      * @param numeroCasa      El número de casa o unidad del residente
         *      * @param concepto        El concepto del pago (por ejemplo: "Cuota mensual de mantenimiento")
         *      * @param mesAnio         El periodo formateado como "Mes Año" (por ejemplo: "Marzo 2025")
         *      * @param monto           El monto exacto en Q (por ejemplo: new BigDecimal("550.00"))
         *      * @param fechaLimite     Fecha límite de pago en formato "dd/MM/yyyy" (por ejemplo: "21/03/2025")
         *      * @param metodoPago      El método de pago sugerido (por ejemplo: "Tarjeta")
         *      */
        public void enviarRecordatorioPago(
                String correoDestino,
                String nombreResidente,
                Integer numeroCasa,
                String concepto,
                String mesAnio,
                BigDecimal monto,
                String fechaLimite,
                String metodoPago
    ) {
            try {
                MimeMessage mensaje = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

                helper.setTo(correoDestino);
                helper.setSubject("Recordatorio de Pago – " + concepto);

                String contenido = """
                <h3>Estimado/a %s,</h3>
                <p>Casa / Apartamento: <strong>%d</strong></p>
                <p><strong>Detalle del Pago Pendiente:</strong></p>
                <ul>
                  <li><strong>Concepto:</strong> %s</li>
                  <li><strong>Periodo:</strong> %s</li>
                  <li><strong>Monto:</strong> Q %.2f</li>
                  <li><strong>Fecha límite de pago:</strong> %s</li>
                  <li><strong>Método de pago:</strong> %s</li>
                </ul>
                <p><strong style='color:red;'>IMPORTANTE:</strong> Si no realizas el pago antes de la fecha límite, podrías ser sujeto a suspensión del servicio.</p>
                <p>¡Gracias por ser parte de Mi Casita Segura!<br/>Atentamente,<br/>Administración - Mi Casita Segura</p>
                """.formatted(
                        nombreResidente,
                        numeroCasa,
                        concepto,
                        mesAnio,
                        monto.doubleValue(),
                        fechaLimite,
                        metodoPago
                );

                helper.setText(contenido, true);
                mailSender.send(mensaje);

            } catch (Exception e) {
                System.err.println("Error enviando Recordatorio de Pago a " + correoDestino + ": " + e.getMessage());
            }
        }

    /**
     * Envía el correo notificando el registro de un visitante, con el QR incrustado.
     *
     * @param correoDestino   - Email del residente que registró al visitante
     * @param nombreVisitante - Nombre completo del visitante
     * @param numeroCasa      - Número de casa / unidad donde se espera al visitante
     * @param fechaVisita     - Fecha de la visita en formato "dd/MM/yyyy"
     * @param nota            - Nota opcional introducida al registrar
     * @param qrImage         - BufferedImage del QR (ya generado a partir del código del QR)
     */
    public void enviarRegistroVisitante(
            String    correoDestino,
            String    nombreVisitante,
            Integer   numeroCasa,
            String    fechaVisita,
            String    nota,
            BufferedImage qrImage
    ) {
        MimeMessage mensaje = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setTo(correoDestino);
            helper.setSubject("Tu visitante ha sido registrado – Aquí tienes el código QR de ingreso");

            // Armamos el HTML con la estructura solicitada
            StringBuilder html = new StringBuilder();
            html.append("<h3>Tu visitante ha sido registrado – Aquí tienes el código QR de ingreso</h3>")
                    .append("<p><strong>Detalles del Visitante Registrado:</strong></p>")
                    .append("<ul>")
                    .append("<li><strong>Nombre del Visitante:</strong> ").append(nombreVisitante).append("</li>")
                    .append("<li><strong>Número de casa:</strong> ").append(numeroCasa).append("</li>")
                    .append("<li><strong>Fecha de Visita:</strong> ").append(fechaVisita).append("</li>")
                    .append("<li><strong>Nota:</strong> ").append(nota == null ? "—" : nota).append("</li>")
                    .append("</ul>")
                    .append("<p><strong>Código QR de Ingreso del Visitante:</strong></p>")
                    // En la línea siguiente, colocamos el inline cid:"qrVisitor"
                    .append("<img src='cid:qrVisitor' alt='Código QR de Ingreso' />")
                    .append("<p>Este código debe ser presentado por el visitante en la entrada para validar su acceso al residencial.</p>")
                    .append("<hr/>")
                    .append("<p>¡Gracias por usar Mi Casita Segura!<br/>Administración – Mi Casita Segura</p>")
                    .append("<small>Nota: Este mensaje es personal y confidencial. No compartas tu información con terceros.</small>");

            helper.setText(html.toString(), true);

            // Convertir el BufferedImage del QR a bytes para adjuntarlo in-line
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);
            ByteArrayResource qrResource = new ByteArrayResource(baos.toByteArray());

            helper.addInline("qrVisitor", qrResource, "image/png");

            mailSender.send(mensaje);

        } catch (MessagingException | IOException ex) {
            // No lanzamos excepción para que no rompa el flujo principal
            ex.printStackTrace();
        }
    }

    }

