package ru.itis.rabbitmqhometask.consumers;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import ru.itis.rabbitmqhometask.models.PassportData;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class DriverLicensePDFMaker {

    private final static String EXCHANGE_NAME = "pdfs";
    private final static String EXCHANGE_TYPE = "fanout";
    private final static String PDF_DIST = "pdfs/driver_license.pdf";
    public static final String FONT = "assets/fonts/arial.ttf";

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(2);

            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
            // создаем временную очередь со случайным названием
            String queue = channel.queueDeclare().getQueue();

            // привязали очередь к EXCHANGE_NAME
            channel.queueBind(queue, EXCHANGE_NAME, "");

            DeliverCallback deliverCallback = (consumerTag, message) -> {
                String bodyString = new String(message.getBody());
                PassportData passportData = PassportData.fromString(bodyString);
                try {
                    PdfDocument pdfDocument = new PdfDocument(new PdfWriter(PDF_DIST));
                    Document document = new Document(pdfDocument);

                    //Font declaration for Cyrillic(Russian) symbols
                    PdfFont f1 = PdfFontFactory.createFont(FONT, "Identity-H", true);

                    document.add(new Paragraph("Заявление на права").setFont(f1));
                    document.add(new Paragraph("Я согласен на обработку моих данных, звявление об успешном завершении экзаменов прикреплю").setFont(f1));
                    document.add(new Paragraph("Мои данные:").setFont(f1));
                    document.add(new Paragraph(
                                    ""
                                            .concat("Имя: " + passportData.getName() + "\n")
                                            .concat("Фамилия: " + passportData.getSurname() + "\n")
                                            .concat("Номер и серия паспорта: " + passportData.getPassportId() + "\n")
                                            .concat("Возраст: " + passportData.getAge() + "\n")
                                            .concat("Дата выдачи паспорта: " +passportData.getDateOfIssue() + "\n")
                            ).setFont(f1)
                    );
                    document.close();
                    System.out.println("PDF for DriverLicense was created");
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                } catch (IOException e) {
                    System.out.println("Task was rejected with error: " + e.getLocalizedMessage());
                    channel.basicReject(message.getEnvelope().getDeliveryTag(), false);
                }
            };

            channel.basicConsume(queue, false, deliverCallback, consumerTag -> {});
        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
