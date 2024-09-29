package com.dytni.bot;

import com.dytni.parser.ExcelParser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.*;
import java.net.URL;

public class FileBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "RoniEnergyExeclbot"; // Замените на имя вашего бота
    }

    @Override
    public String getBotToken() {
        return "7936818749:AAG_9lDrNkyPwtRHx4nOky_igNVrJMMenMU"; // Замените на токен вашего бота
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            // Проверяем, есть ли в сообщении документ
            if (message.hasDocument()) {
                // Проверяем, что это Excel-файл
                String fileName = message.getDocument().getFileName();
                if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                    handleFile(message.getDocument().getFileId(), message.getChatId(), fileName);
                } else {
                    sendTextMessage(message.getChatId(), "Пожалуйста, отправьте Excel-файл.");
                }
            } else {
                sendTextMessage(message.getChatId(), "Пожалуйста, отправьте файл.");
            }
        }
    }

    private void handleFile(String fileId, Long chatId, String fileName) {
        try {
            // Получаем файл с помощью API Telegram
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            File file = execute(getFile);

            // Скачиваем файл с сервера Telegram
            String filePath = file.getFilePath();
            URL fileUrl = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath);
            InputStream in = fileUrl.openStream();

            // Сохраняем файл локально
            java.io.File savedFile = saveFile(fileName, in);

            // Передаем файл в метод для обработки



            sendTextMessage(chatId, "Файл " + fileName + " успешно сохранён и передан на обработку.");

            processExcelFile(savedFile, chatId);


        } catch (Exception e) {
            sendTextMessage(chatId, "Ошибка при получении файла: " + e.getMessage());
        }
    }

    private java.io.File saveFile(String fileName, InputStream in) throws IOException {
        // Создаём папку, если её нет
        java.io.File dir = new java.io.File("files");
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Папка files успешно создана.");
            } else {
                throw new IOException("Не удалось создать папку для файлов.");
            }
        }

        java.io.File savedFile = new java.io.File(dir, fileName);
        try (in; FileOutputStream fos = new FileOutputStream(savedFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            System.out.println("Файл " + fileName + " успешно сохранён в " + savedFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении файла: " + e.getMessage());
            throw e;
        }
        return savedFile;
    }


    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void sendFile(Long chatId, java.io.File file) {
        try {
            // Создаём объект для отправки файла
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(new InputFile(file));

            // Отправляем файл
            execute(sendDocument);
            System.out.println("Файл " + file.getName() + " успешно отправлен пользователю.");
            deleteFile(file);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            sendTextMessage(chatId, "Ошибка при отправке файла: " + e.getMessage());
        }
    }

    // Метод для обработки Excel-файлов
    private void processExcelFile(java.io.File file, Long chatId) {
        // Здесь вы можете реализовать логику обработки Excel-файлов
        System.out.println("Обработка файла: " + file.getAbsolutePath());
        // Например, использовать Apache POI для парсинга файла
        java.io.File outFile = new java.io.File(ExcelParser.parseExcelFile(file));
        sendFile(chatId, outFile);
    }

    private void deleteFile(java.io.File file) {
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Файл " + file.getName() + " успешно удалён.");
            } else {
                System.err.println("Не удалось удалить файл " + file.getName() + ".");
            }
        } else {
            System.err.println("Файл " + file.getName() + " не найден для удаления.");
        }
    }


    public static void main(String[] args) {
        // Инициализация бота
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new FileBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
