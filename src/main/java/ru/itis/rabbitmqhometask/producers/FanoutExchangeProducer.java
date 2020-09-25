package ru.itis.rabbitmqhometask.producers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import ru.itis.rabbitmqhometask.models.PassportData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class FanoutExchangeProducer {

    private final static String EXCHANGE_NAME = "pdfs";
    private final static String EXCHANGE_TYPE = "fanout";

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите ваши данные: ");
        System.out.println("1) Имя");
        System.out.println("2) Фамилия ");
        System.out.println("3) Номер паспорта");
        System.out.println("4) Ваш возраст");
        System.out.println("5) Дата выдачи паспорта в формате yyyy-mm-dd");
        PassportData passportData = PassportData.builder()
                .name(scanner.nextLine())
                .surname(scanner.nextLine())
                .passportId(scanner.nextLine())
                .age(Integer.parseInt(scanner.nextLine()))
                .dateOfIssue(Date.valueOf(scanner.nextLine()))
                .build();
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            // создаем exchange
            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
            channel.basicPublish(EXCHANGE_NAME, "",null, passportData.toString().getBytes());
        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
