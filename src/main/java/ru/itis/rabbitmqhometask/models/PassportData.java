package ru.itis.rabbitmqhometask.models;

import lombok.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PassportData {
        private String name;
        private String surname;
        private String passportId;
        private Integer age;
        private Date dateOfIssue;

        public static PassportData fromString(String s) {
                String[] params = s.split("PassportData")[1]
                                .split(", ");
                PassportData passportData = new PassportData();
                for(String keyValuePairString: params) {
                        String[] keyValuePair = keyValuePairString.split("[(=)]");
                        int index = 0;
                        if(keyValuePair[index].equals("")) {
                                index = 1;
                        }
                        try {
                                Field field = passportData.getClass().getDeclaredField(keyValuePair[index]);
                                field.setAccessible(true);

                                String value = keyValuePair[index + 1];

                                if(field.getType().equals(Integer.class)) {
                                        field.set(passportData, Integer.parseInt(value));
                                } else if(field.getType().equals(Date.class)) {
                                        String[] dateStrings = value.split("-");
                                        field.set(passportData, new Calendar.Builder()
                                                .setDate(Integer.parseInt(dateStrings[0]),
                                                        Integer.parseInt(dateStrings[1]),
                                                        Integer.parseInt(dateStrings[2]))
                                                .build()
                                                .getTime());
                                } else {
                                        field.set(passportData, value);
                                }

                        } catch (NoSuchFieldException | IllegalAccessException e) {
                                throw new IllegalArgumentException(e);
                        }
                }
                return passportData;
        }
}
